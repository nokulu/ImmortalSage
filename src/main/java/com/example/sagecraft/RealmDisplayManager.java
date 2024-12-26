package com.example.sagecraft;

import com.example.sagecraft.PlayerPathManager; // Import for PlayerPathManager
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.gui.GuiGraphics; // Import for GuiGraphics
import net.minecraftforge.common.capabilities.ForgeCapabilities; // Import for capabilities
import net.minecraftforge.common.util.LazyOptional; // Import for LazyOptional
import net.minecraft.client.Minecraft; // Import for accessing Minecraft instance
import net.minecraft.client.gui.Font; // Import for Font
import net.minecraft.network.chat.Style; // Import for Style
import net.minecraft.network.chat.TextColor; // Import for TextColor
import net.minecraft.ChatFormatting; // Import for ChatFormatting

/* 
* Documentation:
* This class manages the display of realm information and Qi amount for players in the Sagecraft mod.
* It provides methods to update player name tags and render GUI elements related to the player's realm.
* * Websites to use: https://minecraft.wiki/w/Font
* - https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/minecraft/client/gui/font/FontSet.html
* - https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/minecraft/client/gui/font/providers/package-summary.html
* - https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/minecraft/client/gui/font/glyphs/package-summary.html
* - https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/minecraft/client/gui/font/providers/FontProvider.html
* - https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/minecraft/client/gui/font/providers/FontProviderBuilder.html
* - https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/minecraft/client/gui/font/providers/FontProviderRegistry.html
* - https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/minecraft/client/gui/font/providers/FontProviderRegistryImpl.html
*  - https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21
*/

public class RealmDisplayManager {

    public void updatePlayerNameTag(Player player, String path, String realm) {
        String nameTag = "Realm: " + realm; // Only realm name shown to others
        int color = determineColorBasedOnPath(path); // Determine color based on current path
        player.setCustomName(Component.literal(nameTag).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)))); // Set name with color
    }

    public void renderGui(Player player, GuiGraphics guiGraphics, String path, String realm) {
        String realmName = "Realm: " + realm; // Use the realm parameter directly
        LazyOptional<QiManager> qiManagerOpt = player.getCapability(QiCapability.CAPABILITY_QI_MANAGER);
        QiManager qiManager = qiManagerOpt.orElse(null); // Adjusted access
        int qiAmount = (qiManager != null) ? qiManager.getQi() : 0; // Default to 0 if QiManager is not available

        // Determine color based on player's path
        int color = determineColorBasedOnPath(path); // Use the actual path variable

        // Obtain the font from Minecraft instance
        Font font = Minecraft.getInstance().font; // Access the default Minecraft font

        // Render the realm name and qi amount for the player
        guiGraphics.drawString(font, Component.literal(realmName), (int) player.getX() - 100, (int) player.getY() + 50, color);
        guiGraphics.drawString(font, Component.literal("Qi: " + qiAmount), (int) player.getX() - 100, (int) player.getY() + 70, 0xFFFFFF); // Qi amount in white
    }

    // Helper method to determine color based on path
    private int determineColorBasedOnPath(String pathType) {
        switch (pathType) {
            case "Demonic":
                return 0xFF0000; // Red
            case "Righteous":
                return 0xFFD700; // Golden/Yellow
            default:
                return 0xFFFFFF; // White for Neutral
        }
    }
}
