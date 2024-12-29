package com.example.sagecraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import java.util.concurrent.atomic.AtomicInteger;

public class QiStorageImpl implements IQiStorage, ICapabilityProvider, INBTSerializable<CompoundTag> {
    private final AtomicInteger qiAmount = new AtomicInteger(0);
    private volatile String currentPath = "Neutral";
    private final AtomicInteger realmLevel = new AtomicInteger(0);
    private boolean isMeditating = false;
    private int meditationTicks = 0;
    private static final int MEDITATION_TICK_RATE = 20; // 1 second

    @Override
    public int getQiAmount() {
        return qiAmount.get();
    }

    @Override
    public void setQiAmount(int amount) {
        qiAmount.set(amount);
    }

    @Override
    public String getCurrentPath() {
        return currentPath;
    }

    @Override
    public void setCurrentPath(String path) {
        this.currentPath = path;
    }

    @Override
    public int getRealmLevel() {
        return realmLevel.get();
    }

    @Override
    public void setRealmLevel(int level) {
        realmLevel.set(level);
    }

    @Override
    public boolean isMeditating() {
        return isMeditating;
    }

    @Override
    public void setMeditating(boolean meditating) {
        this.isMeditating = meditating;
        if (!meditating) {
            meditationTicks = 0;
        }
    }

    @Override
    public void tickCultivation() {
        if (isMeditating) {
            meditationTicks++;
            if (meditationTicks >= MEDITATION_TICK_RATE) {
                gainQi(calculateQiGain());
                meditationTicks = 0;
            }
        }
    }

    @Override
    public void gainQi(int amount) {
        qiAmount.addAndGet(amount);
        checkRealmAdvancement();
    }

    private void checkRealmAdvancement() {
        int nextRealmCost = Config.getRealmCost(realmLevel.get());
        while (qiAmount.get() >= nextRealmCost) {
            qiAmount.addAndGet(-nextRealmCost);
            realmLevel.incrementAndGet();
            nextRealmCost = Config.getRealmCost(realmLevel.get());
        }
    }

    private int calculateQiGain() {
        double baseGain = Config.baseQiGain.get();
        double pathMultiplier = switch (currentPath) {
            case "Righteous" -> 1.0;
            case "Demonic" -> 5.0;
            case "Beast" -> 3.0;
            default -> 1.0;
        };
        return (int)(baseGain * pathMultiplier * (isMeditating ? Config.meditationMultiplier.get() : 1.0));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return QiCapability.CAPABILITY_QI_MANAGER.orEmpty(cap, LazyOptional.of(() -> this));
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag compound = new CompoundTag();
        compound.putInt("QiAmount", qiAmount.get());
        compound.putString("CurrentPath", currentPath);
        compound.putInt("RealmLevel", realmLevel.get());
        return compound;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        if (nbt.contains("QiAmount")) {
            setQiAmount(nbt.getInt("QiAmount"));
        }
        if (nbt.contains("CurrentPath")) {
            setCurrentPath(nbt.getString("CurrentPath"));
        }
        if (nbt.contains("RealmLevel")) {
            setRealmLevel(nbt.getInt("RealmLevel"));
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return serialize();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        deserialize(nbt);
    }


    public void invalidateCapability() {
        // Cleanup when capability is invalidated
    }
}