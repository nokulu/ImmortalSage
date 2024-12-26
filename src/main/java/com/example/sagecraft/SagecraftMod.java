package com.example.sagecraft;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.*; // THE USED ONE IS CORRECT THE COMMENTED ONE DOES! NOT! EXIST! ->  net.minecraftforge.eventbus.api.EventBusSubscriber;
import net.minecraft.world.level.Level; // Import for Level
import net.minecraft.nbt.CompoundTag; // Import for CompoundTag
//the following ones are idk
import net.jodah.typetools.TypeResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraftforge.eventbus.*;
import net.minecraftforge.*;

import net.minecraftforge.fml.event.*;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;


@Mod(SagecraftMod.MOD_ID)
@EventBusSubscriber(modid = SagecraftMod.MOD_ID, bus = Bus.MOD) // Ensure this is correctly defined
public class SagecraftMod {
    public static final String MOD_ID = "sagecraft";
    private static QiData qiData = new QiData(); // Initialize qiData

    public SagecraftMod() {
        // Register event listeners
        qiData = new QiData(); // Ensure qiData is initialized properly
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // Register the QiData instance
        qiData = event.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(QiData.FACTORY, "qi_data"); // Ensure this line is correct
    }

    /**
     * This method is called during the common setup phase of the mod.
     * It is used to register packet handlers and perform any necessary
     * initialization that is common to both client and server.
     *
     * @param event The FMLCommonSetupEvent that contains information about the setup phase.
     */
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        // Common setup logic
        PacketHandler.sendToServer(new Object()); // Register the packet handler with a dummy object
    }

    /**
     * This method is called during the client setup phase of the mod.
     * It is used to perform any client-specific initialization.
     *
     * @param event The FMLClientSetupEvent that contains information about the client setup phase.
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Client setup logic
    }

    /**
     * Retrieves the current QiData instance.
     *
     * @return The QiData instance containing player-specific Qi information.
     */
    public static QiData getQiData() {
        return qiData;
    }
}

/*
 * Documentation:
 * This class is the main entry point for the Sagecraft mod.
 * It handles the mod's initialization and event registration.
 * 
 * Key functionalities:
 * - Registers event listeners for common setup, client setup, and server starting events.
 * - Manages the QiData instance, which is essential for the mod's functionality.
 * 
 * References:
 * - Mod Setup: https://docs.minecraftforge.net/en/latest/misc/overview/
 */
