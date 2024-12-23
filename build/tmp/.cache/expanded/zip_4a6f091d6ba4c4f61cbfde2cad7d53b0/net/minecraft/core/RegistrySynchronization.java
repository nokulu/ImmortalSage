package net.minecraft.core;

import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.repository.KnownPack;

public class RegistrySynchronization {
    public static final Set<ResourceKey<? extends Registry<?>>> NETWORKABLE_REGISTRIES = net.minecraftforge.registries.DataPackRegistriesHooks.grabNetworkableRegistries(() -> RegistryDataLoader.SYNCHRONIZED_REGISTRIES
        .stream()
        .map(RegistryDataLoader.RegistryData::key)
        .collect(Collectors.toUnmodifiableSet())
    );

    public static void packRegistries(
        DynamicOps<Tag> pOps,
        RegistryAccess pRegistryAccess,
        Set<KnownPack> pPacks,
        BiConsumer<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> pPacketSender
    ) {
        RegistryDataLoader.SYNCHRONIZED_REGISTRIES.forEach(p_325710_ -> packRegistry(pOps, (RegistryDataLoader.RegistryData<?>)p_325710_, pRegistryAccess, pPacks, pPacketSender));
    }

    private static <T> void packRegistry(
        DynamicOps<Tag> pOps,
        RegistryDataLoader.RegistryData<T> pRegistryData,
        RegistryAccess pRegistryAccess,
        Set<KnownPack> pPacks,
        BiConsumer<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> pPacketSender
    ) {
        pRegistryAccess.registry(pRegistryData.key())
            .ifPresent(
                p_340985_ -> {
                    List<RegistrySynchronization.PackedRegistryEntry> list = new ArrayList<>(p_340985_.size());
                    p_340985_.holders()
                        .forEach(
                            p_325717_ -> {
                                boolean flag = p_340985_.registrationInfo(p_325717_.key())
                                    .flatMap(RegistrationInfo::knownPackInfo)
                                    .filter(pPacks::contains)
                                    .isPresent();
                                Optional<Tag> optional;
                                if (flag) {
                                    optional = Optional.empty();
                                } else {
                                    Tag tag = pRegistryData.elementCodec()
                                        .encodeStart(pOps, p_325717_.value())
                                        .getOrThrow(
                                            p_325700_ -> new IllegalArgumentException("Failed to serialize " + p_325717_.key() + ": " + p_325700_)
                                        );
                                    optional = Optional.of(tag);
                                }

                                list.add(new RegistrySynchronization.PackedRegistryEntry(p_325717_.key().location(), optional));
                            }
                        );
                    pPacketSender.accept(p_340985_.key(), list);
                }
            );
    }

    private static Stream<RegistryAccess.RegistryEntry<?>> ownedNetworkableRegistries(RegistryAccess pRegistryAccess) {
        return pRegistryAccess.registries().filter(p_325711_ -> NETWORKABLE_REGISTRIES.contains(p_325711_.key()));
    }

    public static Stream<RegistryAccess.RegistryEntry<?>> networkedRegistries(LayeredRegistryAccess<RegistryLayer> pRegistryAccess) {
        return ownedNetworkableRegistries(pRegistryAccess.getAccessFrom(RegistryLayer.WORLDGEN));
    }

    public static Stream<RegistryAccess.RegistryEntry<?>> networkSafeRegistries(LayeredRegistryAccess<RegistryLayer> pRegistryAccess) {
        Stream<RegistryAccess.RegistryEntry<?>> stream = pRegistryAccess.getLayer(RegistryLayer.STATIC).registries();
        Stream<RegistryAccess.RegistryEntry<?>> stream1 = networkedRegistries(pRegistryAccess);
        return Stream.concat(stream1, stream);
    }

    public static record PackedRegistryEntry(ResourceLocation id, Optional<Tag> data) {
        public static final StreamCodec<ByteBuf, RegistrySynchronization.PackedRegistryEntry> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            RegistrySynchronization.PackedRegistryEntry::id,
            ByteBufCodecs.TAG.apply(ByteBufCodecs::optional),
            RegistrySynchronization.PackedRegistryEntry::data,
            RegistrySynchronization.PackedRegistryEntry::new
        );
    }
}
