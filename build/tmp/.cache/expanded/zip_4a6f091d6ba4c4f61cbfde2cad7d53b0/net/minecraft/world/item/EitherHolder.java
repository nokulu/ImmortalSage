package net.minecraft.world.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record EitherHolder<T>(Optional<Holder<T>> holder, ResourceKey<T> key) {
    public EitherHolder(Holder<T> p_344455_) {
        this(Optional.of(p_344455_), p_344455_.unwrapKey().orElseThrow());
    }

    public EitherHolder(ResourceKey<T> p_343550_) {
        this(Optional.empty(), p_343550_);
    }

    public static <T> Codec<EitherHolder<T>> codec(ResourceKey<Registry<T>> pRegistryKey, Codec<Holder<T>> pCodec) {
        return Codec.either(
                pCodec,
                ResourceKey.codec(pRegistryKey).comapFlatMap(p_343571_ -> DataResult.error(() -> "Cannot parse as key without registry"), Function.identity())
            )
            .xmap(EitherHolder::fromEither, EitherHolder::asEither);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, EitherHolder<T>> streamCodec(
        ResourceKey<Registry<T>> pRegistryKey, StreamCodec<RegistryFriendlyByteBuf, Holder<T>> pStreamCodec
    ) {
        return StreamCodec.composite(ByteBufCodecs.either(pStreamCodec, ResourceKey.streamCodec(pRegistryKey)), EitherHolder::asEither, EitherHolder::fromEither);
    }

    public Either<Holder<T>, ResourceKey<T>> asEither() {
        return (Either)this.holder.map(Either::left).orElseGet(() -> Either.right(this.key));
    }

    public static <T> EitherHolder<T> fromEither(Either<Holder<T>, ResourceKey<T>> p_343721_) {
        return p_343721_.map(EitherHolder::new, EitherHolder::new);
    }

    public Optional<T> unwrap(Registry<T> pRegistry) {
        return this.holder.map(Holder::value).or(() -> pRegistry.getOptional(this.key));
    }

    public Optional<Holder<T>> unwrap(HolderLookup.Provider pRegistries) {
        return this.holder.or(() -> pRegistries.lookupOrThrow(this.key.registryKey()).get(this.key));
    }
}