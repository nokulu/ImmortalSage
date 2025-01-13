package com.example.sagecraft;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nonnull;

public class CultivationScreen extends Screen {
    private static final ResourceLocation TEXTURE = 
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/book");
    
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    private final String realm;    
    private final QiManager qiManager; // Reference to QiManager
    private final PlayerPathManager pathManager; // Reference to PlayerPathManager
    private int qiAmount; // Define as a field
    private String currentPath; // Define as a field
    private boolean isMeditating; // Define as a field

    public CultivationScreen(String realm, QiManager qiManager, PlayerPathManager pathManager) {
        super(Component.literal("Cultivation"));
        this.realm = realm;
        this.qiManager = qiManager; // Initialize QiManager
        this.pathManager = pathManager; // Initialize PlayerPathManager
    }

    @Override
    protected void init() {
        // No need to define leftPos and topPos here if not used
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        
        int leftPos = (width - GUI_WIDTH) / 2;
        int topPos = (height - GUI_HEIGHT) / 2;
        
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        graphics.drawCenteredString(font, "Cultivation", 
            leftPos + GUI_WIDTH / 2, topPos + 15, 0xFFFFFF);

        // Update cultivation info dynamically
        this.qiAmount = qiManager.getQi(); // Update Qi amount
        this.currentPath = pathManager.getCurrentPath(); // Update current path
        this.isMeditating = qiManager.isMeditating(); // Update meditation state

        graphics.drawString(font, "Realm: " + realm, leftPos + 10, topPos + 35, 0xFFFFFF);
        graphics.drawString(font, "Qi: " + qiAmount, leftPos + 10, topPos + 45, 0xFFFFFF);
        graphics.drawString(font, "Path: " + currentPath, leftPos + 10, topPos + 55, 0xFFFFFF);
        graphics.drawString(font, "Meditating: " + isMeditating, leftPos + 10, topPos + 65, 0xFFFFFF);
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}