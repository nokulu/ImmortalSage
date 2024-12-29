package com.example.sagecraft;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent; // 1.21+ location
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.network.chat.Component;

@OnlyIn(Dist.CLIENT)
public class HudOverlay {

    private static final int COLOR_NEUTRAL = 0xFFFFFF;
    private static final int COLOR_DEMONIC = 0xFF0000;
    private static final int COLOR_RIGHTEOUS = 0xFFFF00;
    private static final int COLOR_BEAST = 0x0000FF;

    @SubscribeEvent
    public static void onRenderOverlay(CustomizeGuiOverlayEvent event) {
        if (event.getWindow() == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.getCapability(QiCapability.CAPABILITY_QI_MANAGER).ifPresent(cap -> {
                int qi = cap.getQiAmount();
                int realmLevel = cap.getRealmLevel();
                String realm = QiManager.getRealmName(realmLevel);
                String path = cap.getCurrentPath();
                boolean isMeditating = cap.isMeditating(); // Assuming this method exists

                int color = switch (path) {
                    case "Demonic" -> COLOR_DEMONIC;
                    case "Righteous" -> COLOR_RIGHTEOUS;
                    case "Beast" -> COLOR_BEAST;
                    default -> COLOR_NEUTRAL;
                };

                // Debug logging
                System.out.println("Rendering HUD: Realm: " + realm + ", Qi: " + qi + ", Path: " + path);
                
                // Display the CultivationScreen
                CultivationScreen cultivationScreen = new CultivationScreen(realm, qi, path, isMeditating);
                minecraft.setScreen(cultivationScreen);
            });
        }
    }
}
