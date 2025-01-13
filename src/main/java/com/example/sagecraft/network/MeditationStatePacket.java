package com.example.sagecraft.network;

import java.util.function.Supplier;

import com.example.sagecraft.QiCapability;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent.Context;

/**
 * Represents a packet for updating the meditation state on the server.
 * Encodes/decodes a boolean and updates the capability when received.
 */
public class MeditationStatePacket implements IModPacket {

    private final boolean isMeditating;

    /**
     * Constructs a MeditationStatePacket with the given meditation state.
     * @param isMeditating whether the player is meditating
     */
    public MeditationStatePacket(boolean isMeditating) {
        this.isMeditating = isMeditating;
    }

    /**
     * Encodes the meditation state into the given buffer.
     * @param buf the FriendlyByteBuf to write to
     */
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(isMeditating);
    }

    /**
     * Decodes a new MeditationStatePacket from the given buffer.
     * @param buf the FriendlyByteBuf to read from
     * @return a new MeditationStatePacket instance
     */
    public static MeditationStatePacket decode(FriendlyByteBuf buf) {
        return new MeditationStatePacket(buf.readBoolean());
    }

    /**
     * Handles the packet on receipt. Schedules work to update the player's Qi capability.
     * @param contextSupplier supplies the CustomPayloadEvent.Context, which holds player connection info
     */
    public void handle(Supplier<Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                player.getCapability(QiCapability.CAPABILITY_QI_MANAGER).ifPresent(qi -> {
                    qi.setMeditating(isMeditating);
                });
            }
        });
        context.get().setPacketHandled(true);
    }
}