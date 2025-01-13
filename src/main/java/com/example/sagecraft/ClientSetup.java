package com.example.sagecraft;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.common.MinecraftForge;

@OnlyIn(Dist.CLIENT)
public class ClientSetup {
    
    public static void init(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerKeyBindings();
            MinecraftForge.EVENT_BUS.register(ClientSetup.class);
        });
    }

    private static void registerKeyBindings() {
        KeyBindings.register(MinecraftForge.EVENT_BUS);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.screen == null && minecraft.player != null) {
            if (KeyBindings.guiKey.consumeClick()) {
                openCultivationScreen();
            }
        }
    }

    private static void openCultivationScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.getCapability(QiCapability.CAPABILITY_QI_MANAGER).ifPresent(qiStorage -> {
                QiManager qiManager = (QiManager) qiStorage; // Cast to QiManager
                PlayerPathManager playerPathManager = new PlayerPathManager(minecraft.player); // Pass the Player instance
                minecraft.setScreen(new CultivationScreen(
                    QiManager.getRealmName(qiManager.getRealmLevel()),
                    qiManager, // Pass the QiManager instance
                    playerPathManager // Pass the PlayerPathManager instance
                ));
            });
        }
    }
}
