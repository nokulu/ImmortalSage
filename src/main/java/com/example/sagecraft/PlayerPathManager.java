package com.example.sagecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatType; // Import for Component
import net.minecraft.network.chat.OutgoingChatMessage; // Import for OutgoingChatMessage
import net.minecraft.network.chat.PlayerChatMessage; // Import for ChatType
import net.minecraft.world.entity.player.Player; // Existing import
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod; // Import for Minecraft instance

@Mod.EventBusSubscriber
public class PlayerPathManager {
    private String currentPath; // Variable to store the current path

    public PlayerPathManager() {
        // Initialize any necessary variables here
    }

    public void setPath(String path) {
        this.currentPath = path; // Set the current path
        // Additional logic can be added here if needed
    }

    public String getCurrentPath() {
        return currentPath; // Return the current path
    }

    public void onKeyPress(Player player) {
        // Logic for key press related to path management
    }

    public void onKeyRelease() {
        // Logic for key release related to path management
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = Minecraft.getInstance().player; // Retrieve the player instance from the event
        if (player != null) {
            player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(PlayerChatMessage.unsigned(player.getUUID(), "You are on the path: " + currentPath)), false, ChatType.bind(ChatType.CHAT, player));
        }
    }
}
