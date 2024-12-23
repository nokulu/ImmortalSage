package com.example.sagecraft;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class CultivationScreen extends Screen {
    private final String realmName;
    private final int qiAmount;

    protected CultivationScreen(String realmName, int qiAmount) {
        super(Component.translatable("screen.sagecraft.cultivation"));
        this.realmName = realmName;
        this.qiAmount = qiAmount;
    }

    @Override
    protected void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawString(this.font, "Cultivation Realm: " + realmName, this.width / 2 - this.font.width("Cultivation Realm: " + realmName) / 2, this.height / 2 - 10, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Qi Amount: " + qiAmount, this.width / 2 - this.font.width("Qi Amount: " + qiAmount) / 2, this.height / 2 + 10, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}

/*
 * Documentation:
 * This class creates a custom screen to display the cultivation realm name and qi amount.
 * 
 * References:
 * - GUI Screens: https://docs.minecraftforge.net/en/latest/gui/screens/
 */
