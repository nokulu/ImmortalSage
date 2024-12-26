package com.example.sagecraft;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.example.sagecraft.PlayerPathManager; // Importing PlayerPathManager
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class GuiPathSelection extends Screen {
    private final PlayerPathManager pathManager;

    protected GuiPathSelection(PlayerPathManager pathManager) {
        super(Component.translatable("Select Your Path"));
        this.pathManager = pathManager;
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 20;

        createPathButton("Righteous", buttonWidth, buttonHeight, -30);
        createPathButton("Demonic", buttonWidth, buttonHeight, 0);
        createPathButton("Neutral", buttonWidth, buttonHeight, 30);
    }

    private void createPathButton(String pathName, int width, int height, int yOffset) {
        this.addRenderableWidget(Button.builder(Component.translatable(pathName), button -> {
            // Confirm selection
            this.confirmPathSelection(pathName);
        })
        .bounds(width / 2 - width / 2, height / 2 - height / 2 + yOffset, width, height)
        .build());
    }

    private void confirmPathSelection(String pathName) {
        // Logic for confirmation dialog or feedback
        // For example, display a message or change the path
        pathManager.setPath(pathName);
        // Display feedback to the user
        System.out.println("Path selected: " + pathName);
    }
} // Added closing brace for the class
