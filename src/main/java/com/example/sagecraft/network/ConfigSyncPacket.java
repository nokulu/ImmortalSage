package com.example.sagecraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.function.Supplier;

public class ConfigSyncPacket {
    private final String configData;

    public ConfigSyncPacket(String configData) {
        this.configData = configData;
    }

    public static void encode(ConfigSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.configData);
    }

    public static ConfigSyncPacket decode(FriendlyByteBuf buf) {
        return new ConfigSyncPacket(buf.readUtf(32767));
    }

    public static void handle(ConfigSyncPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            // Handle the received config data on the client side
            // Update the local state based on the received configData
        });
        context.setPacketHandled(true);
    }
}
