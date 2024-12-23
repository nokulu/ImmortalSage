package net.minecraft.server.level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkDependencies;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class ChunkGenerationTask {
    private final GeneratingChunkMap chunkMap;
    private final ChunkPos pos;
    @Nullable
    private ChunkStatus scheduledStatus = null;
    public final ChunkStatus targetStatus;
    private volatile boolean markedForCancellation;
    private final List<CompletableFuture<ChunkResult<ChunkAccess>>> scheduledLayer = new ArrayList<>();
    private final StaticCache2D<GenerationChunkHolder> cache;
    private boolean needsGeneration;

    private ChunkGenerationTask(GeneratingChunkMap pChunkMap, ChunkStatus pTargetStatus, ChunkPos pPos, StaticCache2D<GenerationChunkHolder> pCache) {
        this.chunkMap = pChunkMap;
        this.targetStatus = pTargetStatus;
        this.pos = pPos;
        this.cache = pCache;
    }

    public static ChunkGenerationTask create(GeneratingChunkMap pChunkMap, ChunkStatus pTargetStatus, ChunkPos pPos) {
        int i = ChunkPyramid.GENERATION_PYRAMID.getStepTo(pTargetStatus).getAccumulatedRadiusOf(ChunkStatus.EMPTY);
        StaticCache2D<GenerationChunkHolder> staticcache2d = StaticCache2D.create(
            pPos.x, pPos.z, i, (p_342548_, p_344508_) -> pChunkMap.acquireGeneration(ChunkPos.asLong(p_342548_, p_344508_))
        );
        return new ChunkGenerationTask(pChunkMap, pTargetStatus, pPos, staticcache2d);
    }

    @Nullable
    public CompletableFuture<?> runUntilWait() {
        while (true) {
            CompletableFuture<?> completablefuture = this.waitForScheduledLayer();
            if (completablefuture != null) {
                return completablefuture;
            }

            if (this.markedForCancellation || this.scheduledStatus == this.targetStatus) {
                this.releaseClaim();
                return null;
            }

            this.scheduleNextLayer();
        }
    }

    private void scheduleNextLayer() {
        ChunkStatus chunkstatus;
        if (this.scheduledStatus == null) {
            chunkstatus = ChunkStatus.EMPTY;
        } else if (!this.needsGeneration && this.scheduledStatus == ChunkStatus.EMPTY && !this.canLoadWithoutGeneration()) {
            this.needsGeneration = true;
            chunkstatus = ChunkStatus.EMPTY;
        } else {
            chunkstatus = ChunkStatus.getStatusList().get(this.scheduledStatus.getIndex() + 1);
        }

        this.scheduleLayer(chunkstatus, this.needsGeneration);
        this.scheduledStatus = chunkstatus;
    }

    public void markForCancellation() {
        this.markedForCancellation = true;
    }

    private void releaseClaim() {
        GenerationChunkHolder generationchunkholder = this.cache.get(this.pos.x, this.pos.z);
        generationchunkholder.removeTask(this);
        this.cache.forEach(this.chunkMap::releaseGeneration);
    }

    private boolean canLoadWithoutGeneration() {
        if (this.targetStatus == ChunkStatus.EMPTY) {
            return true;
        } else {
            ChunkStatus chunkstatus = this.cache.get(this.pos.x, this.pos.z).getPersistedStatus();
            if (chunkstatus != null && !chunkstatus.isBefore(this.targetStatus)) {
                ChunkDependencies chunkdependencies = ChunkPyramid.LOADING_PYRAMID.getStepTo(this.targetStatus).accumulatedDependencies();
                int i = chunkdependencies.getRadius();

                for (int j = this.pos.x - i; j <= this.pos.x + i; j++) {
                    for (int k = this.pos.z - i; k <= this.pos.z + i; k++) {
                        int l = this.pos.getChessboardDistance(j, k);
                        ChunkStatus chunkstatus1 = chunkdependencies.get(l);
                        ChunkStatus chunkstatus2 = this.cache.get(j, k).getPersistedStatus();
                        if (chunkstatus2 == null || chunkstatus2.isBefore(chunkstatus1)) {
                            return false;
                        }
                    }
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public GenerationChunkHolder getCenter() {
        return this.cache.get(this.pos.x, this.pos.z);
    }

    private void scheduleLayer(ChunkStatus pStatus, boolean pNeedsGeneration) {
        int i = this.getRadiusForLayer(pStatus, pNeedsGeneration);

        for (int j = this.pos.x - i; j <= this.pos.x + i; j++) {
            for (int k = this.pos.z - i; k <= this.pos.z + i; k++) {
                GenerationChunkHolder generationchunkholder = this.cache.get(j, k);
                if (this.markedForCancellation || !this.scheduleChunkInLayer(pStatus, pNeedsGeneration, generationchunkholder)) {
                    return;
                }
            }
        }
    }

    private int getRadiusForLayer(ChunkStatus pStatus, boolean pNeedsGeneration) {
        ChunkPyramid chunkpyramid = pNeedsGeneration ? ChunkPyramid.GENERATION_PYRAMID : ChunkPyramid.LOADING_PYRAMID;
        return chunkpyramid.getStepTo(this.targetStatus).getAccumulatedRadiusOf(pStatus);
    }

    private boolean scheduleChunkInLayer(ChunkStatus pStatus, boolean pNeedsGeneration, GenerationChunkHolder pChunk) {
        ChunkStatus chunkstatus = pChunk.getPersistedStatus();
        boolean flag = chunkstatus != null && pStatus.isAfter(chunkstatus);
        ChunkPyramid chunkpyramid = flag ? ChunkPyramid.GENERATION_PYRAMID : ChunkPyramid.LOADING_PYRAMID;
        if (flag && !pNeedsGeneration) {
            throw new IllegalStateException("Can't load chunk, but didn't expect to need to generate");
        } else {
            CompletableFuture<ChunkResult<ChunkAccess>> completablefuture = pChunk.applyStep(
                chunkpyramid.getStepTo(pStatus), this.chunkMap, this.cache
            );
            ChunkResult<ChunkAccess> chunkresult = completablefuture.getNow(null);
            if (chunkresult == null) {
                this.scheduledLayer.add(completablefuture);
                return true;
            } else if (chunkresult.isSuccess()) {
                return true;
            } else {
                this.markForCancellation();
                return false;
            }
        }
    }

    @Nullable
    private CompletableFuture<?> waitForScheduledLayer() {
        while (!this.scheduledLayer.isEmpty()) {
            CompletableFuture<ChunkResult<ChunkAccess>> completablefuture = this.scheduledLayer.getLast();
            ChunkResult<ChunkAccess> chunkresult = completablefuture.getNow(null);
            if (chunkresult == null) {
                return completablefuture;
            }

            this.scheduledLayer.removeLast();
            if (!chunkresult.isSuccess()) {
                this.markForCancellation();
            }
        }

        return null;
    }
}