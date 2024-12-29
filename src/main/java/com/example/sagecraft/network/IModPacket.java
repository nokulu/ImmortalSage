package com.example.sagecraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent.Context;
import java.util.function.Supplier;

public interface IModPacket {
    void encode(FriendlyByteBuf buf);
    void handle(Supplier<Context> ctx);
    
    // Optional helper method for common handling pattern
    default void safeHandle(Supplier<Context> ctx) {
        ctx.get().enqueueWork(() -> handle(ctx));
        ctx.get().setPacketHandled(true);
    }
}