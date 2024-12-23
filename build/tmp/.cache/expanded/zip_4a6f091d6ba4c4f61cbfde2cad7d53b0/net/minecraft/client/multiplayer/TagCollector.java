package net.minecraft.client.multiplayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TagCollector {
    private final Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags = new HashMap<>();

    public void append(ResourceKey<? extends Registry<?>> pRegistryKey, TagNetworkSerialization.NetworkPayload pNetworkPayload) {
        this.tags.put(pRegistryKey, pNetworkPayload);
    }

    private static void refreshBuiltInTagDependentData() {
        AbstractFurnaceBlockEntity.invalidateCache();
        Blocks.rebuildCache();
    }

    private void applyTags(RegistryAccess pRegistryAccess, Predicate<ResourceKey<? extends Registry<?>>> pFilter) {
        this.tags.forEach((p_335891_, p_332296_) -> {
            if (pFilter.test((ResourceKey<? extends Registry<?>>)p_335891_)) {
                p_332296_.applyToRegistry(pRegistryAccess.registryOrThrow((ResourceKey<? extends Registry<?>>)p_335891_));
            }
        });
    }

    public void updateTags(RegistryAccess pRegistryAccess, boolean pIsMemoryConnection) {
        if (pIsMemoryConnection) {
            this.applyTags(pRegistryAccess, RegistrySynchronization.NETWORKABLE_REGISTRIES::contains);
        } else {
            pRegistryAccess.registries()
                .filter(p_331412_ -> !RegistrySynchronization.NETWORKABLE_REGISTRIES.contains(p_331412_.key()))
                .forEach(p_328076_ -> p_328076_.value().resetTags());
            this.applyTags(pRegistryAccess, p_328746_ -> true);
            refreshBuiltInTagDependentData();
        }
    }
}