package com.example.sagecraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import com.example.sagecraft.QiCapability;

public class RealmLevelPacket {
    private final int realmLevel;

    public RealmLevelPacket(int realmLevel) {
        this.realmLevel = realmLevel;
    }

    public static void encode(RealmLevelPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.realmLevel);
    }

    public static RealmLevelPacket decode(FriendlyByteBuf buf) {
        return new RealmLevelPacket(buf.readInt());
    }

    public static void handle(RealmLevelPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(QiCapability.CAPABILITY_QI_MANAGER).ifPresent(qi -> {
                    qi.setRealmLevel(packet.realmLevel);
                });
            }
        });
        context.setPacketHandled(true);
    }
}