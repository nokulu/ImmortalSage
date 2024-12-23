package net.minecraft.resources;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;

/**
 * An immutable key for a resource, in terms of the name of its parent registry and its location in that registry.
 * <p>
 * {@link net.minecraft.core.Registry} uses this to return resource keys for registry objects via {@link
 * net.minecraft.core.Registry#getResourceKey(Object)}. It also uses this class to store its name, with the parent
 * registry name set to {@code minecraft:root}. When used in this way it is usually referred to as a "registry key".</p>
 * <p>
 * @param <T> The type of the resource represented by this {@code ResourceKey}, or the type of the registry if it is a
 * registry key.
 * @see net.minecraft.resources.ResourceLocation
 */
public class ResourceKey<T> implements Comparable<ResourceKey<?>>, net.minecraftforge.common.extensions.IForgeResourceKey<T> {
    private static final ConcurrentMap<ResourceKey.InternKey, ResourceKey<?>> VALUES = new MapMaker().weakValues().makeMap();
    private final ResourceLocation registryName;
    private final ResourceLocation location;

    public static <T> Codec<ResourceKey<T>> codec(ResourceKey<? extends Registry<T>> pRegistryKey) {
        return ResourceLocation.CODEC.xmap(p_195979_ -> create(pRegistryKey, p_195979_), ResourceKey::location);
    }

    public static <T> StreamCodec<ByteBuf, ResourceKey<T>> streamCodec(ResourceKey<? extends Registry<T>> pRegistryKey) {
        return ResourceLocation.STREAM_CODEC.map(p_326178_ -> create(pRegistryKey, p_326178_), ResourceKey::location);
    }

    public static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> pRegistryKey, ResourceLocation pLocation) {
        return create(pRegistryKey.location, pLocation);
    }

    public static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation pLocation) {
        return create(Registries.ROOT_REGISTRY_NAME, pLocation);
    }

    private static <T> ResourceKey<T> create(ResourceLocation pRegistryName, ResourceLocation pLocation) {
        return (ResourceKey<T>)VALUES.computeIfAbsent(
            new ResourceKey.InternKey(pRegistryName, pLocation), p_258225_ -> new ResourceKey(p_258225_.registry, p_258225_.location)
        );
    }

    private ResourceKey(ResourceLocation pRegistryName, ResourceLocation pLocation) {
        this.registryName = pRegistryName;
        this.location = pLocation;
    }

    @Override
    public String toString() {
        return "ResourceKey[" + this.registryName + " / " + this.location + "]";
    }

    public boolean isFor(ResourceKey<? extends Registry<?>> pRegistryKey) {
        return this.registryName.equals(pRegistryKey.location());
    }

    public <E> Optional<ResourceKey<E>> cast(ResourceKey<? extends Registry<E>> pRegistryKey) {
        return this.isFor(pRegistryKey) ? Optional.of((ResourceKey<E>)this) : Optional.empty();
    }

    public ResourceLocation location() {
        return this.location;
    }

    public ResourceLocation registry() {
        return this.registryName;
    }

    public ResourceKey<Registry<T>> registryKey() {
        return createRegistryKey(this.registryName);
    }

    static record InternKey(ResourceLocation registry, ResourceLocation location) {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ResourceKey<?> resourceKey)) return false;
        return location.equals(resourceKey.location) && registryName.equals(resourceKey.registryName);
    }

    @Override
    public int compareTo(ResourceKey<?> resourceKey) {
        int ret = this.registryName.compareTo(resourceKey.registryName);
        return ret == 0 ? this.location.compareTo(resourceKey.location) : ret;
    }
}
