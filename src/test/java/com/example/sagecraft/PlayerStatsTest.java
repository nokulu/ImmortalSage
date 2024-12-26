package com.example.sagecraft;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerStatsTest {

    @Test
    public void testPlayerStatsPersistence() {
        // Create instances of QiData and PlayerPathManager
        QiData qiData = new QiData();
        PlayerPathManager playerPathManager = new PlayerPathManager();

        // Set initial values
        qiData.setQiAmount(500);
        playerPathManager.setPath("Qi Sensing");
        qiData.setRealmLevel(1);

        // Serialize the data
        CompoundTag serializedData = qiData.save(new CompoundTag());
        CompoundTag pathData = playerPathManager.serializeNBT();

        // Create new instances to simulate loading
        QiData loadedQiData = QiData.load(serializedData);
        PlayerPathManager loadedPathManager = new PlayerPathManager();
        loadedPathManager.deserializeNBT(pathData);

        // Assert that the loaded values match the original values
        assertEquals(500, loadedQiData.getQiAmount());
        assertEquals("Qi Sensing", loadedPathManager.getPath());
        assertEquals(1, loadedQiData.getRealmLevel());
    }
}
