package com.example.sagecraft;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.gui.GuiGraphics; // Import for GuiGraphics
import net.minecraftforge.common.capabilities.ForgeCapabilities; // Import for capabilities

public class RealmDisplayManager {
    public void updatePlayerNameTag(Player player, String realm, String path) {
        String nameTag = "Realm: " + realm; // Only realm name shown to others
        player.setCustomName(Component.literal(nameTag));
    }

    public void renderGui(Player player, GuiGraphics guiGraphics) {
        String realmName = "Realm: " + (player.getCustomName() != null ? player.getCustomName().getString() : "Unknown");
        int qiAmount = ((QiManager) player.getCapability(ForgeCapabilities.CAPABILITY_QI_MANAGER).orElse(null)).getQi(); // Get the qi amount

        // Determine color based on player's path
        int color;
        String pathType = "Neutral"; // Replace with actual path type logic
        switch (pathType) {
            case "Demonic":
                color = 0xFF0000; // Red
                break;
            case "Righteous":
                color = 0xFFD700; // Golden/Yellow
                break;
            default:
                color = 0xFFFFFF; // White for Neutral
                break;
        }

        // Render the realm name and qi amount for the player
        guiGraphics.drawString(guiGraphics.getFont(), realmName, player.getX() - 100, player.getY() + 50, color);
        guiGraphics.drawString(guiGraphics.getFont(), "Qi: " + qiAmount, player.getX() - 100, player.getY() + 70, 0xFFFFFF); // Qi amount in white

        // Render the realm name above the player's head for others to see
        // This part may require additional logic to ensure it is only shown to other players
        System.out.println("Displaying above player's head: " + realmName);
    }
}
