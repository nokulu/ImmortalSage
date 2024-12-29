package com.example.sagecraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import com.example.sagecraft.QiCapability;

public class MeditationStatePacket {
    private final boolean isMeditating;

    public MeditationStatePacket(boolean isMeditating) {
        this.isMeditating = isMeditating;
    }

    public static void encode(MeditationStatePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.isMeditating);
    }

    public static MeditationStatePacket decode(FriendlyByteBuf buf) {
        return new MeditationStatePacket(buf.readBoolean());
    }

    public static void handle(MeditationStatePacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(QiCapability.CAPABILITY_QI_MANAGER).ifPresent(qi -> {
                    qi.setMeditating(packet.isMeditating);
                });
            }
        });
        context.setPacketHandled(true);
    }
}