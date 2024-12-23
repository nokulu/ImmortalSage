package com.example.sagecraft;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod(SagecraftMod.MOD_ID)
@EventBusSubscriber(modid = SagecraftMod.MOD_ID, bus = Bus.MOD)
public class SagecraftMod {
    public static final String MOD_ID = "sagecraft";
    private static QiData qiData;

    public SagecraftMod() {
        // Register event listeners
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        // Common setup logic
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Client setup logic
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // Register the QiData instance
        qiData = event.getServer().getLevel().getDataStorage().computeIfAbsent(QiData::load, QiData::new, "qi_data");
    }

    public static QiData getQiData() {
        return qiData;
    }
}

/*
 * Documentation:
 * This class is the main entry point for the Sagecraft mod.
 * 
 * References:
 * - Mod Setup: https://docs.minecraftforge.net/en/latest/misc/overview/
 */
