package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record DiscardedPayload(ResourceLocation id) implements CustomPacketPayload {
    public static <T extends FriendlyByteBuf> StreamCodec<T, DiscardedPayload> codec(ResourceLocation pId, int pMaxSize) {
        return CustomPacketPayload.codec((p_330619_, p_329210_) -> {
        }, p_333509_ -> {
            int i = p_333509_.readableBytes();
            if (i >= 0 && i <= pMaxSize) {
                p_333509_.skipBytes(i);
                return new DiscardedPayload(pId);
            } else {
                throw new IllegalArgumentException("Payload may not be larger than " + pMaxSize + " bytes");
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<DiscardedPayload> type() {
        return new CustomPacketPayload.Type<>(this.id);
    }
}