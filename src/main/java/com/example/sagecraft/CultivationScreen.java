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
    private final int qiAmount;
    private final String currentPath;
    private final boolean isMeditating;

    public CultivationScreen(String realm, int qi, String path, boolean meditating) {
        super(Component.literal("Cultivation"));
        this.realm = realm;
        this.qiAmount = qi;
        this.currentPath = path;
        this.isMeditating = meditating;
    }

    @Override
    protected void init() {
        int leftPos = (width - GUI_WIDTH) / 2;
        int topPos = (height - GUI_HEIGHT) / 2;
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        
        int leftPos = (width - GUI_WIDTH) / 2;
        int topPos = (height - GUI_HEIGHT) / 2;
        
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        graphics.drawCenteredString(font, "Cultivation", 
            leftPos + GUI_WIDTH / 2, topPos + 15, 0xFFFFFF);

        // Draw cultivation info
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