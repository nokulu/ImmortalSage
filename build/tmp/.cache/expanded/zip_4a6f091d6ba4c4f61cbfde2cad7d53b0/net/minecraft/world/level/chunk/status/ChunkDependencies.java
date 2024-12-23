package net.minecraft.world.level.chunk.status;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.Locale;

public final class ChunkDependencies {
    private final ImmutableList<ChunkStatus> dependencyByRadius;
    private final int[] radiusByDependency;

    public ChunkDependencies(ImmutableList<ChunkStatus> pDependencyByRadius) {
        this.dependencyByRadius = pDependencyByRadius;
        int i = pDependencyByRadius.isEmpty() ? 0 : pDependencyByRadius.getFirst().getIndex() + 1;
        this.radiusByDependency = new int[i];

        for (int j = 0; j < pDependencyByRadius.size(); j++) {
            ChunkStatus chunkstatus = pDependencyByRadius.get(j);
            int k = chunkstatus.getIndex();

            for (int l = 0; l <= k; l++) {
                this.radiusByDependency[l] = j;
            }
        }
    }

    @VisibleForTesting
    public ImmutableList<ChunkStatus> asList() {
        return this.dependencyByRadius;
    }

    public int size() {
        return this.dependencyByRadius.size();
    }

    public int getRadiusOf(ChunkStatus pStatus) {
        int i = pStatus.getIndex();
        if (i >= this.radiusByDependency.length) {
            throw new IllegalArgumentException(
                String.format(Locale.ROOT, "Requesting a ChunkStatus(%s) outside of dependency range(%s)", pStatus, this.dependencyByRadius)
            );
        } else {
            return this.radiusByDependency[i];
        }
    }

    public int getRadius() {
        return Math.max(0, this.dependencyByRadius.size() - 1);
    }

    public ChunkStatus get(int pRadius) {
        return this.dependencyByRadius.get(pRadius);
    }

    @Override
    public String toString() {
        return this.dependencyByRadius.toString();
    }
}