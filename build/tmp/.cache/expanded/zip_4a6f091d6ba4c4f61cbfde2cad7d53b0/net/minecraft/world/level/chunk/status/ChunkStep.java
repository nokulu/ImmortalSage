package net.minecraft.world.level.chunk.status;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;

public record ChunkStep(ChunkStatus targetStatus, ChunkDependencies directDependencies, ChunkDependencies accumulatedDependencies, int blockStateWriteRadius, ChunkStatusTask task) {
    public int getAccumulatedRadiusOf(ChunkStatus pStatus) {
        return pStatus == this.targetStatus ? 0 : this.accumulatedDependencies.getRadiusOf(pStatus);
    }

    public CompletableFuture<ChunkAccess> apply(WorldGenContext pWorldGenContext, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk) {
        if (pChunk.getPersistedStatus().isBefore(this.targetStatus)) {
            ProfiledDuration profiledduration = JvmProfiler.INSTANCE
                .onChunkGenerate(pChunk.getPos(), pWorldGenContext.level().dimension(), this.targetStatus.getName());
            return this.task.doWork(pWorldGenContext, this, pCache, pChunk).thenApply(p_345132_ -> this.completeChunkGeneration(p_345132_, profiledduration));
        } else {
            return this.task.doWork(pWorldGenContext, this, pCache, pChunk);
        }
    }

    private ChunkAccess completeChunkGeneration(ChunkAccess pChunk, @Nullable ProfiledDuration pDuration) {
        if (pChunk instanceof ProtoChunk protochunk && protochunk.getPersistedStatus().isBefore(this.targetStatus)) {
            protochunk.setPersistedStatus(this.targetStatus);
        }

        if (pDuration != null) {
            pDuration.finish();
        }

        return pChunk;
    }

    public static class Builder {
        private final ChunkStatus status;
        @Nullable
        private final ChunkStep parent;
        private ChunkStatus[] directDependenciesByRadius;
        private int blockStateWriteRadius = -1;
        private ChunkStatusTask task = ChunkStatusTasks::passThrough;

        protected Builder(ChunkStatus pStatus) {
            if (pStatus.getParent() != pStatus) {
                throw new IllegalArgumentException("Not starting with the first status: " + pStatus);
            } else {
                this.status = pStatus;
                this.parent = null;
                this.directDependenciesByRadius = new ChunkStatus[0];
            }
        }

        protected Builder(ChunkStatus pStatus, ChunkStep pParent) {
            if (pParent.targetStatus.getIndex() != pStatus.getIndex() - 1) {
                throw new IllegalArgumentException("Out of order status: " + pStatus);
            } else {
                this.status = pStatus;
                this.parent = pParent;
                this.directDependenciesByRadius = new ChunkStatus[]{pParent.targetStatus};
            }
        }

        public ChunkStep.Builder addRequirement(ChunkStatus pStatus, int pRadius) {
            if (pStatus.isOrAfter(this.status)) {
                throw new IllegalArgumentException("Status " + pStatus + " can not be required by " + this.status);
            } else {
                ChunkStatus[] achunkstatus = this.directDependenciesByRadius;
                int i = pRadius + 1;
                if (i > achunkstatus.length) {
                    this.directDependenciesByRadius = new ChunkStatus[i];
                    Arrays.fill(this.directDependenciesByRadius, pStatus);
                }

                for (int j = 0; j < Math.min(i, achunkstatus.length); j++) {
                    this.directDependenciesByRadius[j] = ChunkStatus.max(achunkstatus[j], pStatus);
                }

                return this;
            }
        }

        public ChunkStep.Builder blockStateWriteRadius(int pBlockStateWriteRadius) {
            this.blockStateWriteRadius = pBlockStateWriteRadius;
            return this;
        }

        public ChunkStep.Builder setTask(ChunkStatusTask pTask) {
            this.task = pTask;
            return this;
        }

        public ChunkStep build() {
            return new ChunkStep(
                this.status,
                new ChunkDependencies(ImmutableList.copyOf(this.directDependenciesByRadius)),
                new ChunkDependencies(ImmutableList.copyOf(this.buildAccumulatedDependencies())),
                this.blockStateWriteRadius,
                this.task
            );
        }

        private ChunkStatus[] buildAccumulatedDependencies() {
            if (this.parent == null) {
                return this.directDependenciesByRadius;
            } else {
                int i = this.getRadiusOfParent(this.parent.targetStatus);
                ChunkDependencies chunkdependencies = this.parent.accumulatedDependencies;
                ChunkStatus[] achunkstatus = new ChunkStatus[Math.max(i + chunkdependencies.size(), this.directDependenciesByRadius.length)];

                for (int j = 0; j < achunkstatus.length; j++) {
                    int k = j - i;
                    if (k < 0 || k >= chunkdependencies.size()) {
                        achunkstatus[j] = this.directDependenciesByRadius[j];
                    } else if (j >= this.directDependenciesByRadius.length) {
                        achunkstatus[j] = chunkdependencies.get(k);
                    } else {
                        achunkstatus[j] = ChunkStatus.max(this.directDependenciesByRadius[j], chunkdependencies.get(k));
                    }
                }

                return achunkstatus;
            }
        }

        private int getRadiusOfParent(ChunkStatus pStatus) {
            for (int i = this.directDependenciesByRadius.length - 1; i >= 0; i--) {
                if (this.directDependenciesByRadius[i].isOrAfter(pStatus)) {
                    return i;
                }
            }

            return 0;
        }
    }
}