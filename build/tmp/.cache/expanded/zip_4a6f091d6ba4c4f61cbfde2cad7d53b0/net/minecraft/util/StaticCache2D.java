package net.minecraft.util;

import java.util.Locale;
import java.util.function.Consumer;

public class StaticCache2D<T> {
    private final int minX;
    private final int minZ;
    private final int sizeX;
    private final int sizeZ;
    private final Object[] cache;

    public static <T> StaticCache2D<T> create(int pCenterX, int pCenterZ, int pSize, StaticCache2D.Initializer<T> pInitializer) {
        int i = pCenterX - pSize;
        int j = pCenterZ - pSize;
        int k = 2 * pSize + 1;
        return new StaticCache2D<>(i, j, k, k, pInitializer);
    }

    private StaticCache2D(int pMinX, int pMinZ, int pSizeX, int pSizeZ, StaticCache2D.Initializer<T> pInitializer) {
        this.minX = pMinX;
        this.minZ = pMinZ;
        this.sizeX = pSizeX;
        this.sizeZ = pSizeZ;
        this.cache = new Object[this.sizeX * this.sizeZ];

        for (int i = pMinX; i < pMinX + pSizeX; i++) {
            for (int j = pMinZ; j < pMinZ + pSizeZ; j++) {
                this.cache[this.getIndex(i, j)] = pInitializer.get(i, j);
            }
        }
    }

    public void forEach(Consumer<T> pAction) {
        for (Object object : this.cache) {
            pAction.accept((T)object);
        }
    }

    public T get(int pX, int pZ) {
        if (!this.contains(pX, pZ)) {
            throw new IllegalArgumentException("Requested out of range value (" + pX + "," + pZ + ") from " + this);
        } else {
            return (T)this.cache[this.getIndex(pX, pZ)];
        }
    }

    public boolean contains(int pX, int pZ) {
        int i = pX - this.minX;
        int j = pZ - this.minZ;
        return i >= 0 && i < this.sizeX && j >= 0 && j < this.sizeZ;
    }

    @Override
    public String toString() {
        return String.format(
            Locale.ROOT, "StaticCache2D[%d, %d, %d, %d]", this.minX, this.minZ, this.minX + this.sizeX, this.minZ + this.sizeZ
        );
    }

    private int getIndex(int pX, int pZ) {
        int i = pX - this.minX;
        int j = pZ - this.minZ;
        return i * this.sizeZ + j;
    }

    @FunctionalInterface
    public interface Initializer<T> {
        T get(int pX, int pZ);
    }
}