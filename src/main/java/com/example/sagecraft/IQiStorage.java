package com.example.sagecraft;

import net.minecraft.nbt.CompoundTag;

/**
 * Interface for managing Qi storage and related functionality
 */
public interface IQiStorage {
    // Qi Management
    int getQiAmount();
    void setQiAmount(int amount);
    void gainQi(int amount);
    
    // meditation Management
    void tickCultivation();

    // Path Management
    String getCurrentPath();
    void setCurrentPath(String path);
    
    // Realm Management
    int getRealmLevel();
    void setRealmLevel(int level);
    
    // State Management
    boolean isMeditating();
    void setMeditating(boolean meditating);
    
    // Data Serialization
    CompoundTag serialize();
    void deserialize(CompoundTag nbt);
}
