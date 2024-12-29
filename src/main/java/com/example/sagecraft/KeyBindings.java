package com.example.sagecraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // Add this import

import com.mojang.blaze3d.platform.InputConstants; // Add this import

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.InputEvent; 
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus; // Import Logger
import net.minecraftforge.eventbus.api.SubscribeEvent; // Import LoggerFactory

public class KeyBindings {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyBindings.class); // Declare LOGGER
    public static KeyMapping cultivationKey;
    public static KeyMapping guiKey;
    
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(KeyBindings::registerKeyMappings);
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        cultivationKey = new KeyMapping(
            "key.sagecraft.cultivation",
            InputConstants.KEY_X,
            "key.categories.sagecraft"
        );
        
        guiKey = new KeyMapping(
            "key.sagecraft.open_gui",
            InputConstants.KEY_P,
            "key.categories.sagecraft"
        );

        event.register(cultivationKey);
        event.register(guiKey);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (guiKey.consumeClick()) {
            // Open the path selection GUI
            Player player = Minecraft.getInstance().player; // Get the player instance
            if (player != null) {
                Minecraft.getInstance().setScreen(new GuiPathSelection(new PlayerPathManager(player))); // Pass the player instance
            } else {
                // Handle the case where the player is null (optional)
                LOGGER.warn("Player instance is null, cannot open GUI.");
            }
        } else if (cultivationKey.isDown()) {
            // Handle meditation (hold)
            startMeditation();
        } else if (cultivationKey.consumeClick()) {
            // Handle single click
            gainQiFromClick();
        }
    }

    private static void startMeditation() {
        // TODO: Implement meditation start
    }

    private static void gainQiFromClick() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player != null) {
            player.getCapability(QiCapability.CAPABILITY_QI_MANAGER).ifPresent(qi -> {
                String currentPath = qi.getCurrentPath();
                int qiGainAmount = 10; // Define the amount of Qi to gain per click
                qi.gainQi(qiGainAmount);
                LOGGER.info("Gained {} Qi from click. Current Qi: {}", qiGainAmount, qi.getQiAmount());
            });
        } else {
            LOGGER.warn("Player instance is null, cannot gain Qi.");
        }
    }
}
