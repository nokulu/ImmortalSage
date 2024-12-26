package com.example.sagecraft;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Documentation:
 * This class creates a custom screen to display the cultivation realm name and Qi amount.
 * It extends the Screen class from Minecraft's GUI framework.
 * 
 * Key functionalities:
 * - Displays the name of the cultivation realm.
 * - Displays the current amount of Qi.
 * 
 * References:
 * - GUI Screens: https://docs.minecraftforge.net/en/latest/gui/screens/
 */
public class CultivationScreen extends Screen {
    private final String realmName; // The name of the cultivation realm
    private final int qiAmount; // The amount of Qi

    /**
     * Constructs a new CultivationScreen.
     *
     * @param realmName The name of the cultivation realm to display.
     * @param qiAmount The amount of Qi to display.
     */
    public CultivationScreen(String realmName, int qiAmount) {
        super(Component.translatable("screen.sagecraft.cultivation"));
        this.realmName = realmName;
        this.qiAmount = qiAmount;
    }

    /**
     * Renders the screen.
     *
     * @param guiGraphics The graphics context for rendering.
     * @param mouseX The X coordinate of the mouse.
     * @param mouseY The Y coordinate of the mouse.
     * @param partialTicks The partial ticks for rendering.
     */
    @Override
public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawString(this.font, "Cultivation Realm: " + realmName, this.width / 2 - this.font.width("Cultivation Realm: " + realmName) / 2, this.height / 2 - 10, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Qi Amount: " + qiAmount, this.width / 2 - this.font.width("Qi Amount: " + qiAmount) / 2, this.height / 2 + 10, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /**
     * Renders the background for the screen.
     *
     * @param guiGraphics The graphics context for rendering.
     */
    private void renderBackground(GuiGraphics guiGraphics) {
        // Background rendering logic can be added here
    }
}

/*
 * End of Documentation:
 * This file contains the CultivationScreen class, which is responsible for displaying
 * the cultivation realm and Qi amount in the Sagecraft mod.
 */
