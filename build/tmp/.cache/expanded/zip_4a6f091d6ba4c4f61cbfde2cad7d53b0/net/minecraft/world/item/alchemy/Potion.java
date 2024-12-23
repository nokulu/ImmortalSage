package net.minecraft.world.item.alchemy;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

/**
 * Defines a type of potion in the game. These are used to associate one or more effects with items such as the bottled
 * potion or the tipped arrows.
 */
public class Potion implements FeatureElement {
    public static final Codec<Holder<Potion>> CODEC = BuiltInRegistries.POTION.holderByNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Potion>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.POTION);
    @Nullable
    private final String name;
    private final List<MobEffectInstance> effects;
    private FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;

    public Potion(MobEffectInstance... pEffects) {
        this(null, pEffects);
    }

    public Potion(@Nullable String pName, MobEffectInstance... pEffects) {
        this.name = pName;
        this.effects = List.of(pEffects);
    }

    public Potion requiredFeatures(FeatureFlag... pRequiredFeatures) {
        this.requiredFeatures = FeatureFlags.REGISTRY.subset(pRequiredFeatures);
        return this;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    public static String getName(Optional<Holder<Potion>> pPotion, String pDescriptionId) {
        if (pPotion.isPresent()) {
            String s = pPotion.get().value().name;
            if (s != null) {
                return pDescriptionId + s;
            }
        }

        String s1 = pPotion.flatMap(Holder::unwrapKey).map(p_329074_ -> p_329074_.location().getPath()).orElse("empty");
        return pDescriptionId + s1;
    }

    public List<MobEffectInstance> getEffects() {
        return this.effects;
    }

    public boolean hasInstantEffects() {
        if (!this.effects.isEmpty()) {
            for (MobEffectInstance mobeffectinstance : this.effects) {
                if (mobeffectinstance.getEffect().value().isInstantenous()) {
                    return true;
                }
            }
        }

        return false;
    }
}