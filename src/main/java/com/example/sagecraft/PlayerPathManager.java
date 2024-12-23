package com.example.sagecraft;

import net.minecraft.client.Minecraft; // Import for Minecraft instance
import net.minecraft.network.chat.ChatType; // Import for ChatType
import net.minecraft.network.chat.PlayerChatMessage; // Import for PlayerChatMessage
import net.minecraft.network.chat.OutgoingChatMessage; // Import for OutgoingChatMessage
import net.minecraft.world.entity.player.Player;
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
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            PlayerChatMessage chatMessage = PlayerChatMessage.unsigned(player.getUUID(), "You are on the " + currentPath + " path.");
            player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(chatMessage), false, ChatType.bind(ChatType.CHAT, player));
        }
    }
}
