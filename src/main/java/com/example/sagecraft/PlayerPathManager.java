package com.example.sagecraft;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.sagecraft.network.PathUpdatePacket;
import javax.annotation.Nullable;

public class PlayerPathManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerPathManager.class);
    private static final String[] VALID_PATHS = {"Neutral", "Righteous", "Demonic", "Beast"};
    
    private final Player player;
    private String currentPath;
    private double pathBonus;
    private boolean isRegistered;

    public PlayerPathManager(Player player) {
        this.player = player;
        this.currentPath = "Neutral";
        this.pathBonus = 1.0;
        this.isRegistered = false;
        registerEvents();
    }

    private void registerEvents() {
        if (!isRegistered) {
            MinecraftForge.EVENT_BUS.register(this);
            isRegistered = true;
            LOGGER.debug("Registered events for player: {}", player.getName().getString());
        }
    }

    public boolean setPath(String path) {
        if (isValidPath(path) && !currentPath.equals(path)) {
            String oldPath = currentPath;
            currentPath = path;
            updatePathBonus();
            syncPath();
            LOGGER.info("Player {} changed path from {} to {}", 
                player.getName().getString(), oldPath, path);
            return true;
        }
        return false;
    }

    private boolean isValidPath(String path) {
        for (String validPath : VALID_PATHS) {
            if (validPath.equals(path)) return true;
        }
        LOGGER.warn("Invalid path attempted: {}", path);
        return false;
    }

    private void updatePathBonus() {
        pathBonus = switch (currentPath) {
            case "Righteous" -> Config.pathBonusMultiplier.get() * 1.0;
            case "Demonic" -> Config.pathBonusMultiplier.get() * 5.0;
            case "Beast" -> Config.pathBonusMultiplier.get() * 3.0;
            default -> 1.0;
        };
    }

    private void syncPath() {
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.server != null) {
            PathUpdatePacket packet = new PathUpdatePacket(currentPath);
            PacketHandler.sendToAll(packet, serverPlayer.server.getPlayerList());
            LOGGER.debug("Synced path data for player: {}", player.getName().getString());
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && event.getEntity() == player) {
            serverPlayer.sendSystemMessage(
                Component.literal("Your cultivation path: " + currentPath)
            );
            PacketHandler.sendToPlayer(
                new PathUpdatePacket(currentPath), 
                serverPlayer
            );
            LOGGER.debug("Sent path data to joining player: {}", serverPlayer.getName().getString());
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() == player) {
            syncPath();
        }
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public double getPathBonus() {
        return pathBonus;
    }
}