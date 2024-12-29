package com.example.sagecraft;

import com.example.sagecraft.network.PathUpdatePacket;
import com.example.sagecraft.network.QiUpdatePacket;
import com.example.sagecraft.network.MeditationStatePacket;
import com.example.sagecraft.network.RealmLevelPacket;
import com.example.sagecraft.network.IModPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;
import com.example.sagecraft.network.*;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.*;

/**
 * Handles network communication for Sagecraft mod.
 * Manages packet registration and distribution.
 */
public class PacketHandler {
    private static final int PROTOCOL_VERSION = 1;
    
    private static final SimpleChannel INSTANCE = ChannelBuilder.named(
            ResourceLocation.fromNamespaceAndPath(SagecraftMod.MOD_ID, "main"))
        .serverAcceptedVersions((status, version) -> true)
        .clientAcceptedVersions((status, version) -> true)
        .networkProtocolVersion(PROTOCOL_VERSION)
        .simpleChannel();



    /**
     * Registers all network packets.
     * Called during mod initialization.
     */
    public static void register() {
        int id = 0;
        
        // Register QiUpdate packet
        INSTANCE.messageBuilder(QiUpdatePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(QiUpdatePacket::encode)
            .decoder(QiUpdatePacket::decode)
            .consumerMainThread(QiUpdatePacket::handle)
            .add();

        // Register MeditationState packet
        INSTANCE.messageBuilder(MeditationStatePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(MeditationStatePacket::encode)
            .decoder(MeditationStatePacket::decode)
            .consumerMainThread(MeditationStatePacket::handle)
            .add();

        // Register RealmLevel packet
        INSTANCE.messageBuilder(RealmLevelPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(RealmLevelPacket::encode)
            .decoder(RealmLevelPacket::decode)
            .consumerMainThread(RealmLevelPacket::handle)
            .add();

        // Register PathUpdate packet
        INSTANCE.messageBuilder(PathUpdatePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(PathUpdatePacket::encode)
            .decoder(PathUpdatePacket::decode)
            .consumerMainThread(PathUpdatePacket::handle)
            .add();
    
    }
    
    /**
     * Sends packet to specific player
     * @param packet Packet to send
     * @param player Target player
     */
    public static void sendToPlayer(Object packet, ServerPlayer player) {
        INSTANCE.send(packet, player.connection.getConnection());
    }

    /**
     * Sends packet to all players
     */
    /**
     * Sends packet to all players
     */
    public static void sendToAll(Object packet, PlayerList playerList) {
        for (ServerPlayer player : playerList.getPlayers()) {
            sendToPlayer(packet, player);
        }
    }
    
    /**
     * Sends packet to server
     */
    public static void sendToServer(IModPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null) {
            INSTANCE.send(packet, minecraft.getConnection().getConnection());
        }
    }
    /**
     * Gets network channel instance
     */
    public static SimpleChannel getInstance() {
        return INSTANCE;
    }
}