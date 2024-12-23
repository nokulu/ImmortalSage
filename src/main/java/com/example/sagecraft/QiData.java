package com.example.sagecraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData; // Updated import

public class QiData extends SavedData {
    private int qiAmount;

    public QiData() {
        this.qiAmount = 0; // Default qi amount
    }

    public int getQiAmount() {
        return qiAmount;
    }

    public void setQiAmount(int qiAmount) {
        this.qiAmount = qiAmount;
        this.setDirty(); // Mark data as dirty to save
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        compound.putInt("QiAmount", qiAmount);
        return compound;
    }

    public static QiData load(CompoundTag compound) {
        QiData data = new QiData();
        if (compound.contains("QiAmount")) {
            data.setQiAmount(compound.getInt("QiAmount"));
        }
        return data;
    }
}

/*
 * Documentation:
 * This class manages the qi amount for the Sagecraft mod.
 * 
 * References:
 * - Data Storage: https://docs.minecraftforge.net/en/latest/datastorage/saveddata/
 */
