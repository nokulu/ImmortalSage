package com.example.sagecraft;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QiStorageImplTest {

    @Test
    public void testQiStorage() {
        IQiStorage storage = new QiStorageImpl();
        
        // Test initial values
        assertEquals(0, storage.getQiAmount());
        assertEquals("Neutral", storage.getCurrentPath());
        assertEquals(0, storage.getRealmLevel());

        // Test setting values
        storage.setQiAmount(100);
        assertEquals(100, storage.getQiAmount());

        storage.setCurrentPath("Qi Sensing");
        assertEquals("Qi Sensing", storage.getCurrentPath());

        storage.setRealmLevel(1);
        assertEquals(1, storage.getRealmLevel());

        // Test serialization
        CompoundTag nbt = storage.serialize();
        assertEquals(100, nbt.getInt("QiAmount"));
        assertEquals("Qi Sensing", nbt.getString("CurrentPath"));
        assertEquals(1, nbt.getInt("RealmLevel"));

        // Test deserialization
        CompoundTag newNbt = new CompoundTag();
        newNbt.putInt("QiAmount", 200);
        newNbt.putString("CurrentPath", "Foundation Building");
        newNbt.putInt("RealmLevel", 2);
        storage.deserialize(newNbt);

        assertEquals(200, storage.getQiAmount());
        assertEquals("Foundation Building", storage.getCurrentPath());
        assertEquals(2, storage.getRealmLevel());
    }
}
