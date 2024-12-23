package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;

public abstract class GenerationChunkHolder {
    private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
    private static final ChunkResult<ChunkAccess> NOT_DONE_YET = ChunkResult.error("Not done yet");
    public static final ChunkResult<ChunkAccess> UNLOADED_CHUNK = ChunkResult.error("Unloaded chunk");
    public static final CompletableFuture<ChunkResult<ChunkAccess>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_CHUNK);
    protected final ChunkPos pos;
    @Nullable
    private volatile ChunkStatus highestAllowedStatus;
    private final AtomicReference<ChunkStatus> startedWork = new AtomicReference<>();
    private final AtomicReferenceArray<CompletableFuture<ChunkResult<ChunkAccess>>> futures = new AtomicReferenceArray<>(CHUNK_STATUSES.size());
    private final AtomicReference<ChunkGenerationTask> task = new AtomicReference<>();
    private final AtomicInteger generationRefCount = new AtomicInteger();
    /** Forge: Used to bypass the future chain when loading chunks, because modders can do weird things that cause deadlocks, modders should never touch this **/
    @org.jetbrains.annotations.ApiStatus.Internal
    public net.minecraft.world.level.chunk.LevelChunk currentlyLoading;

    public GenerationChunkHolder(ChunkPos pPos) {
        this.pos = pPos;
    }

    public CompletableFuture<ChunkResult<ChunkAccess>> scheduleChunkGenerationTask(ChunkStatus pTargetStatus, ChunkMap pChunkMap) {
        if (this.isStatusDisallowed(pTargetStatus)) {
            return UNLOADED_CHUNK_FUTURE;
        } else {
            CompletableFuture<ChunkResult<ChunkAccess>> completablefuture = this.getOrCreateFuture(pTargetStatus);
            if (completablefuture.isDone()) {
                return completablefuture;
            } else {
                ChunkGenerationTask chunkgenerationtask = this.task.get();
                if (chunkgenerationtask == null || pTargetStatus.isAfter(chunkgenerationtask.targetStatus)) {
                    this.rescheduleChunkTask(pChunkMap, pTargetStatus);
                }

                return completablefuture;
            }
        }
    }

    CompletableFuture<ChunkResult<ChunkAccess>> applyStep(ChunkStep pStep, GeneratingChunkMap pChunkMap, StaticCache2D<GenerationChunkHolder> pCache) {
        if (this.isStatusDisallowed(pStep.targetStatus())) {
            return UNLOADED_CHUNK_FUTURE;
        } else {
            return this.acquireStatusBump(pStep.targetStatus()) ? pChunkMap.applyStep(this, pStep, pCache).handle((p_343850_, p_344393_) -> {
                if (p_344393_ != null) {
                    CrashReport crashreport = CrashReport.forThrowable(p_344393_, "Exception chunk generation/loading");
                    MinecraftServer.setFatalException(new ReportedException(crashreport));
                } else {
                    this.completeFuture(pStep.targetStatus(), p_343850_);
                }

                return ChunkResult.of(p_343850_);
            }) : this.getOrCreateFuture(pStep.targetStatus());
        }
    }

    protected void updateHighestAllowedStatus(ChunkMap pChunkMap) {
        ChunkStatus chunkstatus = this.highestAllowedStatus;
        ChunkStatus chunkstatus1 = ChunkLevel.generationStatus(this.getTicketLevel());
        this.highestAllowedStatus = chunkstatus1;
        boolean flag = chunkstatus != null && (chunkstatus1 == null || chunkstatus1.isBefore(chunkstatus));
        if (flag) {
            this.failAndClearPendingFuturesBetween(chunkstatus1, chunkstatus);
            if (this.task.get() != null) {
                this.rescheduleChunkTask(pChunkMap, this.findHighestStatusWithPendingFuture(chunkstatus1));
            }
        }
    }

    public void replaceProtoChunk(ImposterProtoChunk pChunk) {
        CompletableFuture<ChunkResult<ChunkAccess>> completablefuture = CompletableFuture.completedFuture(ChunkResult.of(pChunk));

        for (int i = 0; i < this.futures.length() - 1; i++) {
            CompletableFuture<ChunkResult<ChunkAccess>> completablefuture1 = this.futures.get(i);
            Objects.requireNonNull(completablefuture1);
            ChunkAccess chunkaccess = completablefuture1.getNow(NOT_DONE_YET).orElse(null);
            if (!(chunkaccess instanceof ProtoChunk)) {
                throw new IllegalStateException("Trying to replace a ProtoChunk, but found " + chunkaccess);
            }

            if (!this.futures.compareAndSet(i, completablefuture1, completablefuture)) {
                throw new IllegalStateException("Future changed by other thread while trying to replace it");
            }
        }
    }

    void removeTask(ChunkGenerationTask pTask) {
        this.task.compareAndSet(pTask, null);
    }

    private void rescheduleChunkTask(ChunkMap pChunkMap, @Nullable ChunkStatus pTargetStatus) {
        ChunkGenerationTask chunkgenerationtask;
        if (pTargetStatus != null) {
            chunkgenerationtask = pChunkMap.scheduleGenerationTask(pTargetStatus, this.getPos());
        } else {
            chunkgenerationtask = null;
        }

        ChunkGenerationTask chunkgenerationtask1 = this.task.getAndSet(chunkgenerationtask);
        if (chunkgenerationtask1 != null) {
            chunkgenerationtask1.markForCancellation();
        }
    }

    private CompletableFuture<ChunkResult<ChunkAccess>> getOrCreateFuture(ChunkStatus pTargetStatus) {
        if (this.isStatusDisallowed(pTargetStatus)) {
            return UNLOADED_CHUNK_FUTURE;
        } else {
            int i = pTargetStatus.getIndex();
            CompletableFuture<ChunkResult<ChunkAccess>> completablefuture = this.futures.get(i);

            while (completablefuture == null) {
                CompletableFuture<ChunkResult<ChunkAccess>> completablefuture1 = new CompletableFuture<>();
                completablefuture = this.futures.compareAndExchange(i, null, completablefuture1);
                if (completablefuture == null) {
                    if (this.isStatusDisallowed(pTargetStatus)) {
                        this.failAndClearPendingFuture(i, completablefuture1);
                        return UNLOADED_CHUNK_FUTURE;
                    }

                    return completablefuture1;
                }
            }

            return completablefuture;
        }
    }

    private void failAndClearPendingFuturesBetween(@Nullable ChunkStatus pHighestAllowableStatus, ChunkStatus pCurrentStatus) {
        int i = pHighestAllowableStatus == null ? 0 : pHighestAllowableStatus.getIndex() + 1;
        int j = pCurrentStatus.getIndex();

        for (int k = i; k <= j; k++) {
            CompletableFuture<ChunkResult<ChunkAccess>> completablefuture = this.futures.get(k);
            if (completablefuture != null) {
                this.failAndClearPendingFuture(k, completablefuture);
            }
        }
    }

    private void failAndClearPendingFuture(int pStatus, CompletableFuture<ChunkResult<ChunkAccess>> pFuture) {
        if (pFuture.complete(UNLOADED_CHUNK) && !this.futures.compareAndSet(pStatus, pFuture, null)) {
            throw new IllegalStateException("Nothing else should replace the future here");
        }
    }

    private void completeFuture(ChunkStatus pTargetStatus, ChunkAccess pChunkAccess) {
        ChunkResult<ChunkAccess> chunkresult = ChunkResult.of(pChunkAccess);
        int i = pTargetStatus.getIndex();

        while (true) {
            CompletableFuture<ChunkResult<ChunkAccess>> completablefuture = this.futures.get(i);
            if (completablefuture == null) {
                if (this.futures.compareAndSet(i, null, CompletableFuture.completedFuture(chunkresult))) {
                    return;
                }
            } else {
                if (completablefuture.complete(chunkresult)) {
                    return;
                }

                if (completablefuture.getNow(NOT_DONE_YET).isSuccess()) {
                    throw new IllegalStateException("Trying to complete a future but found it to be completed successfully already");
                }

                Thread.yield();
            }
        }
    }

    @Nullable
    private ChunkStatus findHighestStatusWithPendingFuture(@Nullable ChunkStatus pGenerationStatus) {
        if (pGenerationStatus == null) {
            return null;
        } else {
            ChunkStatus chunkstatus = pGenerationStatus;

            for (ChunkStatus chunkstatus1 = this.startedWork.get();
                chunkstatus1 == null || chunkstatus.isAfter(chunkstatus1);
                chunkstatus = chunkstatus.getParent()
            ) {
                if (this.futures.get(chunkstatus.getIndex()) != null) {
                    return chunkstatus;
                }

                if (chunkstatus == ChunkStatus.EMPTY) {
                    break;
                }
            }

            return null;
        }
    }

    private boolean acquireStatusBump(ChunkStatus pStatus) {
        ChunkStatus chunkstatus = pStatus == ChunkStatus.EMPTY ? null : pStatus.getParent();
        ChunkStatus chunkstatus1 = this.startedWork.compareAndExchange(chunkstatus, pStatus);
        if (chunkstatus1 == chunkstatus) {
            return true;
        } else if (chunkstatus1 != null && !pStatus.isAfter(chunkstatus1)) {
            return false;
        } else {
            throw new IllegalStateException("Unexpected last startedWork status: " + chunkstatus1 + " while trying to start: " + pStatus);
        }
    }

    private boolean isStatusDisallowed(ChunkStatus pStatus) {
        ChunkStatus chunkstatus = this.highestAllowedStatus;
        return chunkstatus == null || pStatus.isAfter(chunkstatus);
    }

    public void increaseGenerationRefCount() {
        this.generationRefCount.incrementAndGet();
    }

    public void decreaseGenerationRefCount() {
        int i = this.generationRefCount.decrementAndGet();
        if (i < 0) {
            throw new IllegalStateException("More releases than claims. Count: " + i);
        }
    }

    public int getGenerationRefCount() {
        return this.generationRefCount.get();
    }

    @Nullable
    public ChunkAccess getChunkIfPresentUnchecked(ChunkStatus pStatus) {
        CompletableFuture<ChunkResult<ChunkAccess>> completablefuture = this.futures.get(pStatus.getIndex());
        return completablefuture == null ? null : completablefuture.getNow(NOT_DONE_YET).orElse(null);
    }

    @Nullable
    public ChunkAccess getChunkIfPresent(ChunkStatus pStatus) {
        return this.isStatusDisallowed(pStatus) ? null : this.getChunkIfPresentUnchecked(pStatus);
    }

    @Nullable
    public ChunkAccess getLatestChunk() {
        ChunkStatus chunkstatus = this.startedWork.get();
        if (chunkstatus == null) {
            return null;
        } else {
            ChunkAccess chunkaccess = this.getChunkIfPresentUnchecked(chunkstatus);
            return chunkaccess != null ? chunkaccess : this.getChunkIfPresentUnchecked(chunkstatus.getParent());
        }
    }

    @Nullable
    public ChunkStatus getPersistedStatus() {
        CompletableFuture<ChunkResult<ChunkAccess>> completablefuture = this.futures.get(ChunkStatus.EMPTY.getIndex());
        ChunkAccess chunkaccess = completablefuture == null ? null : completablefuture.getNow(NOT_DONE_YET).orElse(null);
        return chunkaccess == null ? null : chunkaccess.getPersistedStatus();
    }

    public ChunkPos getPos() {
        return this.pos;
    }

    public FullChunkStatus getFullStatus() {
        return ChunkLevel.fullStatus(this.getTicketLevel());
    }

    public abstract int getTicketLevel();

    public abstract int getQueueLevel();

    @VisibleForDebug
    public List<Pair<ChunkStatus, CompletableFuture<ChunkResult<ChunkAccess>>>> getAllFutures() {
        List<Pair<ChunkStatus, CompletableFuture<ChunkResult<ChunkAccess>>>> list = new ArrayList<>();

        for (int i = 0; i < CHUNK_STATUSES.size(); i++) {
            list.add(Pair.of(CHUNK_STATUSES.get(i), this.futures.get(i)));
        }

        return list;
    }

    @Nullable
    @VisibleForDebug
    public ChunkStatus getLatestStatus() {
        for (int i = CHUNK_STATUSES.size() - 1; i >= 0; i--) {
            ChunkStatus chunkstatus = CHUNK_STATUSES.get(i);
            ChunkAccess chunkaccess = this.getChunkIfPresentUnchecked(chunkstatus);
            if (chunkaccess != null) {
                return chunkstatus;
            }
        }

        return null;
    }
}
