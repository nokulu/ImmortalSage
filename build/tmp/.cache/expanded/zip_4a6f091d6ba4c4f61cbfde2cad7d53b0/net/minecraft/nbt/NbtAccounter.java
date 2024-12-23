package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;

public class NbtAccounter {
    private static final int MAX_STACK_DEPTH = 512;
    private final long quota;
    private long usage;
    private final int maxDepth;
    private int depth;

    public NbtAccounter(long pQuota, int pMaxDepth) {
        this.quota = pQuota;
        this.maxDepth = pMaxDepth;
    }

    public static NbtAccounter create(long pQuota) {
        return new NbtAccounter(pQuota, 512);
    }

    public static NbtAccounter unlimitedHeap() {
        return new NbtAccounter(Long.MAX_VALUE, 512);
    }

    public void accountBytes(long pBytesPerItem, long pItems) {
        this.accountBytes(pBytesPerItem * pItems);
    }

    public void accountBytes(long pBytes) {
        if (this.usage + pBytes > this.quota) {
            throw new NbtAccounterException(
                "Tried to read NBT tag that was too big; tried to allocate: "
                    + this.usage
                    + " + "
                    + pBytes
                    + " bytes where max allowed: "
                    + this.quota
            );
        } else {
            this.usage += pBytes;
        }
    }

    public void pushDepth() {
        if (this.depth >= this.maxDepth) {
            throw new NbtAccounterException("Tried to read NBT tag with too high complexity, depth > " + this.maxDepth);
        } else {
            this.depth++;
        }
    }

    public void popDepth() {
        if (this.depth <= 0) {
            throw new NbtAccounterException("NBT-Accounter tried to pop stack-depth at top-level");
        } else {
            this.depth--;
        }
    }

    @VisibleForTesting
    public long getUsage() {
        return this.usage;
    }

    @VisibleForTesting
    public int getDepth() {
        return this.depth;
    }
}