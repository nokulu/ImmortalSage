package com.example.sagecraft;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
// Removed unused import
// import net.minecraftforge.client.gui.GuiOverlayManager; // Import for GuiOverlayManager
import net.minecraft.world.entity.player.Player; // Import for Player
import net.minecraft.client.Minecraft; // Import for Minecraft
import org.slf4j.Logger;
import net.minecraftforge.event.entity.player.PlayerEvent; // Import for PlayerEvent
import org.slf4j.LoggerFactory;

/**
 * Main mod class for Sagecraft.
 * Handles initialization and event registration.
 */
@Mod(SagecraftMod.MOD_ID)
public class SagecraftMod {
    public static final String MOD_ID = "sagecraft";
    private static final Logger LOGGER = LoggerFactory.getLogger(SagecraftMod.class);

    public SagecraftMod() {
        LOGGER.info("Initializing Sagecraft Mod");
        
        // Get mod event bus
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Register mod setup events
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        
        // Register config
        Config.register();

        // Register capabilities with eventbus
        QiCapability.register(modEventBus);
        
        // Register network packets
        PacketHandler.register();
        
        // Register key bindings
        KeyBindings.register(modEventBus);
        
        // Register forge event bus
        MinecraftForge.EVENT_BUS.register(this);
        
        modEventBus.addListener(this::onConfigReload);
   
        LOGGER.info("Sagecraft Mod Initialized and ready for usage!");
        LOGGER.info("Happy Cultivation!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common Setup");
        event.enqueueWork(() -> {
            // Setup work that needs to be thread-safe
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Client Setup");
        MinecraftForge.EVENT_BUS.register(HudOverlay.class);
        
        // Register GUI classes
        event.enqueueWork(() -> {
            // Obtain the current player instance
            Player currentPlayer = Minecraft.getInstance().player; // This is a common way to get the player instance
            PlayerPathManager pathManager = null; // Declare pathManager here
            if (currentPlayer != null) {
                pathManager = new PlayerPathManager(currentPlayer); // Instantiate pathManager
                ScreenManager.register(GuiPathSelection.class, new GuiPathSelection(pathManager));
            } else {
                LOGGER.warn("Current player is null, cannot initialize PlayerPathManager.");
            }
            ScreenManager.register(CultivationScreen.class, new CultivationScreen("defaultRealm", 0, "defaultPath", false));
        });
    }

    private void onConfigReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() == ModConfig.Type.COMMON) {
            LOGGER.info("Reloading Sagecraft configuration");
            // Future: Sync to clients
        }
    }
}
