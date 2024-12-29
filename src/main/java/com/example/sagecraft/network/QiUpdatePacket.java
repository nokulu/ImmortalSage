package com.example.sagecraft.network;


import com.example.sagecraft.QiCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent.Context;
import java.util.function.Supplier;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Network packet for synchronizing Qi values between server and client.
 * Handles updating player's qi amount on the client side.
 */
public class QiUpdatePacket implements IModPacket {
    private final int qi;
    private final UUID playerId;

    public QiUpdatePacket(int qi, UUID playerId) {
        this.qi = qi;
        this.playerId = playerId;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(qi);
        buf.writeUUID(playerId);
    }

    public static QiUpdatePacket decode(FriendlyByteBuf buf) {
        return new QiUpdatePacket(buf.readInt(), buf.readUUID());
    }

    public static void handle(QiUpdatePacket msg, Context context) {
        context.enqueueWork(() -> {
            Player player = Minecraft.getInstance().level.getPlayerByUUID(msg.playerId);
            if (player != null) {
                player.getCapability(QiCapability.CAPABILITY_QI_MANAGER).ifPresent(cap -> {
                    cap.setQiAmount(msg.qi);
                });
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public void handle(Supplier<Context> ctx) {
        handle(this, ctx.get());
    }
}