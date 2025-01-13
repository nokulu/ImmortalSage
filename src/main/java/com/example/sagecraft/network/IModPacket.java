package com.example.sagecraft.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent.Context;

public interface IModPacket {
    void encode(FriendlyByteBuf buf);
    void handle(Supplier<Context> ctx);
    
    // Optional helper method for common handling pattern
    default void safeHandle(Supplier<Context> ctx) {
        ctx.get().enqueueWork(() -> handle(ctx));
        ctx.get().setPacketHandled(true); 
    }
}