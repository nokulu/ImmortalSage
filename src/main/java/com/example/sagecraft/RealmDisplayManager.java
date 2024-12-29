package com.example.sagecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RealmDisplayManager handles the visual representation of cultivation realms and Qi amounts.
 * This includes both player nametags and GUI elements.
 * 
 * Features:
 * - Dynamic color coding based on cultivation path
 * - Real-time Qi amount display
 * - Realm level visualization
 * - GUI positioning and rendering
 * 
 * @version 1.0
 * @since 1.21.1
 */
public class RealmDisplayManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RealmDisplayManager.class);
    private static final int GUI_OFFSET_X = 100;
    private static final int GUI_OFFSET_Y_REALM = 50;
    private static final int GUI_OFFSET_Y_QI = 70;

    /**
     * Updates the player's nametag with their current realm information.
     * 
     * @param player The player whose nametag should be updated
     * @param path The player's cultivation path
     * @param realm The player's current realm
     */
    public void updatePlayerNameTag(Player player, String path, String realm) {
        String nameTag = "Realm: " + realm;
        int color = determineColorBasedOnPath(path);
        player.setCustomName(Component.literal(nameTag)
            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
        LOGGER.debug("Updated nametag for player {} with realm {}", player.getName(), realm);
    }

    /**
     * Renders the GUI elements showing realm and Qi information.
     * 
     * @param player The player whose information should be displayed
     * @param guiGraphics The graphics context for rendering
     * @param path The player's cultivation path
     * @param realm The player's current realm
     */
    public void renderGui(Player player, GuiGraphics guiGraphics, String path, String realm) {
        if (player == null || guiGraphics == null) {
            LOGGER.warn("Attempted to render GUI with null player or graphics context");
            return;
        }

        String realmName = "Realm: " + realm;
        LazyOptional<IQiStorage> qiOptional = player.getCapability(QiCapability.CAPABILITY_QI_MANAGER);
        
        qiOptional.ifPresent(qiStorage -> {
            int qiAmount = qiStorage.getQiAmount();
            int color = determineColorBasedOnPath(path);
            Font font = Minecraft.getInstance().font;

            // Render realm name
            guiGraphics.drawString(
                font, 
                Component.literal(realmName), 
                (int) player.getX() - GUI_OFFSET_X, 
                (int) player.getY() + GUI_OFFSET_Y_REALM, 
                color
            );

            // Render Qi amount
            guiGraphics.drawString(
                font, 
                Component.literal("Qi: " + qiAmount), 
                (int) player.getX() - GUI_OFFSET_X, 
                (int) player.getY() + GUI_OFFSET_Y_QI, 
                0xFFFFFF
            );
        });
    }

    /**
     * Determines the color to use based on the cultivation path.
     * 
     * @param pathType The cultivation path
     * @return The RGB color value for the path
     */
    private int determineColorBasedOnPath(String pathType) {
        return switch (pathType) {
            case "Demonic" -> 0xFF0000;    // Red - represents demonic cultivation
            case "Righteous" -> 0xFFD700;   // Gold - represents righteous path
            case "Neutral" -> 0xFFFFFF;     // White - represents neutral cultivation
            case "Beast" -> 0x8B4513;       // Brown - represents beast cultivation
            default -> {
                LOGGER.warn("Unknown path type: {}, defaulting to white", pathType);
                yield 0xFFFFFF;
            }
        };
    }
}