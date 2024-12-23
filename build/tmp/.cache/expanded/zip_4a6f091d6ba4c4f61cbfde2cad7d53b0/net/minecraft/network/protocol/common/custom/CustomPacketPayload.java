package net.minecraft.network.protocol.common.custom;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamMemberEncoder;
import net.minecraft.resources.ResourceLocation;

public interface CustomPacketPayload {
    CustomPacketPayload.Type<? extends CustomPacketPayload> type();

    static <B extends ByteBuf, T extends CustomPacketPayload> StreamCodec<B, T> codec(StreamMemberEncoder<B, T> pEncoder, StreamDecoder<B, T> pDecoder) {
        return StreamCodec.ofMember(pEncoder, pDecoder);
    }

    static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String pId) {
        return new CustomPacketPayload.Type<>(ResourceLocation.parse(pId));
    }

    static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String namespace, String path) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(ResourceLocation name) {
        return new CustomPacketPayload.Type<>(name);
    }

    static <B extends FriendlyByteBuf> StreamCodec<B, CustomPacketPayload> codec(
        final CustomPacketPayload.FallbackProvider<B> pFallbackProvider, List<CustomPacketPayload.TypeAndCodec<? super B, ?>> pTypeAndCodecs
    ) {
        final Map<ResourceLocation, StreamCodec<? super B, ? extends CustomPacketPayload>> map = pTypeAndCodecs.stream()
            .collect(Collectors.toUnmodifiableMap(p_332174_ -> p_332174_.type().id(), CustomPacketPayload.TypeAndCodec::codec));
        return new StreamCodec<B, CustomPacketPayload>() {
            private StreamCodec<? super B, ? extends CustomPacketPayload> findCodec(ResourceLocation p_335824_) {
                StreamCodec<? super B, ? extends CustomPacketPayload> streamcodec = map.get(p_335824_);
                return streamcodec != null ? streamcodec : pFallbackProvider.create(p_335824_);
            }

            private <T extends CustomPacketPayload> void writeCap(B p_332252_, CustomPacketPayload.Type<T> p_334465_, CustomPacketPayload p_334290_) {
                p_332252_.writeResourceLocation(p_334465_.id());
                StreamCodec<B, T> streamcodec = (StreamCodec)this.findCodec(p_334465_.id);
                streamcodec.encode(p_332252_, (T)p_334290_);
            }

            public void encode(B p_334992_, CustomPacketPayload p_329854_) {
                this.writeCap(p_334992_, p_329854_.type(), p_329854_);
            }

            public CustomPacketPayload decode(B p_334320_) {
                ResourceLocation resourcelocation = p_334320_.readResourceLocation();
                return (CustomPacketPayload)this.findCodec(resourcelocation).decode(p_334320_);
            }
        };
    }

    public interface FallbackProvider<B extends FriendlyByteBuf> {
        StreamCodec<B, ? extends CustomPacketPayload> create(ResourceLocation pId);
    }

    public static record Type<T extends CustomPacketPayload>(ResourceLocation id) {
    }

    public static record TypeAndCodec<B extends FriendlyByteBuf, T extends CustomPacketPayload>(
        CustomPacketPayload.Type<T> type, StreamCodec<B, T> codec
    ) {
    }
}
