package com.example.sagecraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import com.example.sagecraft.QiCapability;
import com.example.sagecraft.QiDataChangeEvent; // Import statement added

/**
 * Network packet for updating player cultivation path.
 * Handles synchronization of cultivation path changes between client and server.
 */
public class PathUpdatePacket {
    /** Current cultivation path */
    private final String path;
    private final QiDataChangeEvent.ChangeType changeType;

    /**
     * Creates new path update packet with empty path.
     * Required for packet registration.
     */
    public PathUpdatePacket() {
        this.path = "";
        this.changeType = QiDataChangeEvent.ChangeType.QI_AMOUNT; // Default value
    }

    /**
     * Creates new path update packet with specified path.
     * @param path New cultivation path to set
     */
    public PathUpdatePacket(String path) {
        this.path = path;
        this.changeType = QiDataChangeEvent.ChangeType.PATH; // Default value
    }

    /**
     * Creates new path update packet with specified change type and path.
     * @param changeType The type of change
     * @param path The new cultivation path
     */
    public PathUpdatePacket(QiDataChangeEvent.ChangeType changeType, String path) {
        this.changeType = changeType;
        this.path = path;
    }

    /**
     * Creates new path update packet with specified change type and level.
     * @param changeType The type of change
     * @param level The new level
     */
    public PathUpdatePacket(QiDataChangeEvent.ChangeType changeType, int level) {
        this.changeType = changeType;
        this.path = String.valueOf(level); // Convert level to String
    }

    /**
     * Encodes packet data to network buffer.
     * @param buf Network buffer to write to
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(path);
    }

    /**
     * Decodes packet data from network buffer.
     * @param buf Network buffer to read from
     * @return New packet instance with decoded data
     */
    public static PathUpdatePacket decode(FriendlyByteBuf buf) {
        return new PathUpdatePacket(buf.readUtf());
    }

    /**
     * Handles packet on receiving side.
     * Updates player's cultivation path capability.
     * @param message Packet to handle
     * @param context Network context
     */
    public static void handle(PathUpdatePacket message, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(QiCapability.CAPABILITY_QI_MANAGER).ifPresent(qi -> {
                    qi.setCurrentPath(message.getPath());
                });
            }
        });
        context.setPacketHandled(true);
    }

    /**
     * Gets current path value.
     * @return Current cultivation path
     */
    public String getPath() {
        return path;
    }
}
