package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record SetValue(LevelBasedValue value) implements EnchantmentValueEffect {
    public static final MapCodec<SetValue> CODEC = RecordCodecBuilder.mapCodec(
        p_344143_ -> p_344143_.group(LevelBasedValue.CODEC.fieldOf("value").forGetter(SetValue::value)).apply(p_344143_, SetValue::new)
    );

    @Override
    public float process(int pEnchantmentLevel, RandomSource pRandom, float pValue) {
        return this.value.calculate(pEnchantmentLevel);
    }

    @Override
    public MapCodec<SetValue> codec() {
        return CODEC;
    }
}