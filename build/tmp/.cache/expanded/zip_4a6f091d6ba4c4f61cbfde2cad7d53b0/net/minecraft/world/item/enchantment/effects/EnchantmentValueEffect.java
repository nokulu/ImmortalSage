package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;

public interface EnchantmentValueEffect {
    Codec<EnchantmentValueEffect> CODEC = BuiltInRegistries.ENCHANTMENT_VALUE_EFFECT_TYPE.byNameCodec().dispatch(EnchantmentValueEffect::codec, Function.identity());

    static MapCodec<? extends EnchantmentValueEffect> bootstrap(Registry<MapCodec<? extends EnchantmentValueEffect>> pRegistry) {
        Registry.register(pRegistry, "add", AddValue.CODEC);
        Registry.register(pRegistry, "all_of", AllOf.ValueEffects.CODEC);
        Registry.register(pRegistry, "multiply", MultiplyValue.CODEC);
        Registry.register(pRegistry, "remove_binomial", RemoveBinomial.CODEC);
        return Registry.register(pRegistry, "set", SetValue.CODEC);
    }

    float process(int pEnchantmentLevel, RandomSource pRandom, float pValue);

    MapCodec<? extends EnchantmentValueEffect> codec();
}