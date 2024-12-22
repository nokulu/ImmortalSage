package com.example.sagecraft;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlayerPathManager {
    private String currentPath;

    public PlayerPathManager() {
        this.currentPath = "Neutral"; // Default path
    }

    public void setPath(String path) {
        this.currentPath = path;
    }

    public String getPath() {
        return currentPath;
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        // Example: Notify player of their current path
        event.getPlayer().sendMessage(new StringTextComponent("You are on the " + currentPath + " path."));
    }
}
