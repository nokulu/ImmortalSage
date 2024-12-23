package com.example.sagecraft;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = SagecraftMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    public static KeyMapping cultivationKey;

    public static void registerKeyBindings() {
        cultivationKey = new KeyMapping("key.sagecraft.cultivation", 0, "key.categories.sagecraft");
        // No need to register the key mapping explicitly
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        registerKeyBindings();
    }
}

/*
 * Documentation:
 * This class implements key mappings for the Sagecraft mod.
 * 
 * References:
 * - Key Mappings: https://docs.minecraftforge.net/en/latest/misc/keymappings/
 */
