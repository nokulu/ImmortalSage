package com.example.sagecraft;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod class for Sagecraft.
 * Handles initialization and event registration.
 */
@Mod(SagecraftMod.MOD_ID)
public class SagecraftMod {
    public static final String MOD_ID = "sagecraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(SagecraftMod.class);

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
            Player currentPlayer = Minecraft.getInstance().player;
            if (currentPlayer != null) {
                LOGGER.debug("Current player: {}", currentPlayer);
                PlayerPathManager pathManager = new PlayerPathManager(currentPlayer);
                ScreenManager.register(GuiPathSelection.class, new GuiPathSelection(pathManager));
                
                currentPlayer.getCapability(QiCapability.CAPABILITY_QI_MANAGER).ifPresent(qiStorage -> {
                    QiManager qiManager = (QiManager) qiStorage;
                    ScreenManager.register(CultivationScreen.class, new CultivationScreen(
                        QiManager.getRealmName(qiManager.getRealmLevel()), 
                        qiManager, 
                        pathManager 
                    ));
                });
            } else {
                LOGGER.warn("Current player is null, cannot access Qi capabilities.");
            }
        });
    }

    private void onConfigReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() == ModConfig.Type.COMMON) {
            LOGGER.info("Reloading Sagecraft configuration");
            // TODO: Sync to clients
        }
    }
}
