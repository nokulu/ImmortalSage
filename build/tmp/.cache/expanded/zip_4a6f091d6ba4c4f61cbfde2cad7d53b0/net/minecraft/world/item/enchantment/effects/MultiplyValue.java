package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record MultiplyValue(LevelBasedValue factor) implements EnchantmentValueEffect {
    public static final MapCodec<MultiplyValue> CODEC = RecordCodecBuilder.mapCodec(
        p_342244_ -> p_342244_.group(LevelBasedValue.CODEC.fieldOf("factor").forGetter(MultiplyValue::factor)).apply(p_342244_, MultiplyValue::new)
    );

    @Override
    public float process(int pEnchantmentLevel, RandomSource pRandom, float pValue) {
        return pValue * this.factor.calculate(pEnchantmentLevel);
    }

    @Override
    public MapCodec<MultiplyValue> codec() {
        return CODEC;
    }
}