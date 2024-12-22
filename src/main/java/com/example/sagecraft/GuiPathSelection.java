package com.example.sagecraft;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

        addButton(new Button(width / 2 - buttonWidth / 2, height / 2 - buttonHeight / 2 - 30, buttonWidth, buttonHeight, Component.translatable("Righteous"), button -> pathManager.setPath("Righteous")));
        addButton(new Button(width / 2 - buttonWidth / 2, height / 2 - buttonHeight / 2, buttonWidth, buttonHeight, Component.translatable("Demonic"), button -> pathManager.setPath("Demonic")));
        addButton(new Button(width / 2 - buttonWidth / 2, height / 2 - buttonHeight / 2 + 30, buttonWidth, buttonHeight, Component.translatable("Neutral"), button -> pathManager.setPath("Neutral")));
    }

    @SubscribeEvent
    public static void onCreativeTabBuild(CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.getEntries().add(new ItemStack(Items.DIAMOND));
        }
    }
}
