package com.example.sagecraft;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;


/**
 * Documentation:
 * This class manages the Qi amount, current path, and realm level for the Sagecraft mod.
 * It extends the SavedData class to allow for saving and loading of data.
 * 
 * Key functionalities:
 * - Tracks the current amount of Qi.
 * - Tracks the player's current path.
 * - Tracks the realm level.
 * 
 * References:
 * - Data Storage: https://docs.minecraftforge.net/en/latest/datastorage/saveddata/
 * - How to fix capability from 1.20.x -> 1.21.1: https://forums.minecraftforge.net/topic/149807-forge-121-5101-configjava-uses-resourcelocation-but-it-is-set-to-private/
 * - idk: https://forge.gemwire.uk/wiki/Capabilities
 */
public class QiData extends SavedData {
    public static final Factory<QiData> FACTORY = new Factory<>(
        () -> new QiData(),                                    // Supplier<QiData>
        (tag, provider) -> QiData.load(tag, provider),        // BiFunction<CompoundTag, Provider, QiData>
        DataFixTypes.LEVEL                                    // DataFixTypes parameter
    );
    private int qiAmount; // The current amount of Qi
    private String currentPath; // The player's current path
    private int realmLevel; // The level of the realm

    /** 
     * Constructs a new QiData instance with default values.
     */
    public QiData() {
        this.qiAmount = 0; // Default qi amount
        this.currentPath = "Neutral"; // Default path
        this.realmLevel = 0; // Default realm level
    }

    /**
     * Gets the current amount of Qi.
     *
     * @return The current Qi amount.
     */
    public int getQiAmount() {
        return qiAmount;
    }

    /**
     * Sets the current amount of Qi.
     *
     * @param qiAmount The new Qi amount.
     */
    public void setQiAmount(int qiAmount) {
        this.qiAmount = qiAmount;
        this.setDirty(); // Mark data as dirty to save
    }

    /**
     * Gets the current path of the player.
     *
     * @return The current path.
     */
    public String getCurrentPath() {
        return currentPath;
    }

    /**
     * Sets the current path of the player.
     *
     * @param currentPath The new current path.
     */
    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
        this.setDirty(); // Mark data as dirty to save
    }

    /**
     * Gets the current realm level.
     *
     * @return The current realm level.
     */
    public int getRealmLevel() {
        return realmLevel;
    }

    /**
     * Sets the current realm level.
     *
     * @param realmLevel The new realm level.
     */
    public void setRealmLevel(int realmLevel) {
        this.realmLevel = realmLevel;
        this.setDirty(); // Mark data as dirty to save
    }

/**
 * Saves the current state to a CompoundTag.
 *
 * @param compound The CompoundTag to save data to.
 * @param provider The Provider instance for registry lookups
 * @return The updated CompoundTag.
 */
@Override
public CompoundTag save(CompoundTag compound, Provider provider) {
    compound.putInt("QiAmount", qiAmount);
    compound.putString("CurrentPath", currentPath);
    compound.putInt("RealmLevel", realmLevel);
    return compound;
}

    /**
     * Loads the state from a CompoundTag.
     *
     * @param compound The CompoundTag to load data from.
     * @return A new QiData instance with loaded data.
     */
    // Update load method signature to match BiFunction
    public static QiData load(CompoundTag tag, Provider provider) {
        QiData data = new QiData();
        if (tag.contains("QiAmount")) {
            data.setQiAmount(tag.getInt("QiAmount"));
        }
        if (tag.contains("CurrentPath")) {
            data.setCurrentPath(tag.getString("CurrentPath"));
        }
        if (tag.contains("RealmLevel")) {
            data.setRealmLevel(tag.getInt("RealmLevel"));
        }
        return data;
    }
}
