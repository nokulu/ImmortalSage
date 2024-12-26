package com.example.sagecraft;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = SagecraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    public static KeyMapping cultivationKey;
    public static KeyMapping guiKey; // Declare guiKey here

    @SubscribeEvent
    public static void registerKeyMappings() {
        cultivationKey = new KeyMapping("key.sagecraft.cultivation", InputConstants.KEY_X, "key.categories.sagecraft"); // Using 'X' key for cultivation
        guiKey = new KeyMapping("key.sagecraft.open_gui", InputConstants.KEY_P, "key.categories.sagecraft"); // Using 'P' key to open GUI
        
        // Register the key mappings
        // Assuming the registration is done through the event bus or another method
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        registerKeyMappings();
    }
}

/*
 * Documentation:
 * This class implements key mappings for the Sagecraft mod.
 * 
 * References:
 * - Key Mappings: https://docs.minecraftforge.net/en/latest/misc/keymappings/
 * - Minecraft wiki for controls: https://minecraft.wiki/w/Options#Controls
 */
