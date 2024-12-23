package net.minecraft.server.network.config;

import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.tags.TagNetworkSerialization;

public class SynchronizeRegistriesTask implements ConfigurationTask {
    public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type("synchronize_registries");
    private final List<KnownPack> requestedPacks;
    private final LayeredRegistryAccess<RegistryLayer> registries;

    public SynchronizeRegistriesTask(List<KnownPack> pRequestedPacks, LayeredRegistryAccess<RegistryLayer> pRegistries) {
        this.requestedPacks = pRequestedPacks;
        this.registries = pRegistries;
    }

    @Override
    public void start(Consumer<Packet<?>> pTask) {
        pTask.accept(new ClientboundSelectKnownPacks(this.requestedPacks));
    }

    private void sendRegistries(Consumer<Packet<?>> pPacketSender, Set<KnownPack> pPacks) {
        DynamicOps<Tag> dynamicops = this.registries.compositeAccess().createSerializationContext(NbtOps.INSTANCE);
        RegistrySynchronization.packRegistries(
            dynamicops,
            this.registries.getAccessFrom(RegistryLayer.WORLDGEN),
            pPacks,
            (p_334638_, p_328189_) -> pPacketSender.accept(new ClientboundRegistryDataPacket(p_334638_, p_328189_))
        );
        pPacketSender.accept(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.registries)));
    }

    public void handleResponse(List<KnownPack> pPacks, Consumer<Packet<?>> pPacketSender) {
        if (pPacks.equals(this.requestedPacks)) {
            this.sendRegistries(pPacketSender, Set.copyOf(this.requestedPacks));
        } else {
            this.sendRegistries(pPacketSender, Set.of());
        }
    }

    @Override
    public ConfigurationTask.Type type() {
        return TYPE;
    }
}