package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record RemoveBinomial(LevelBasedValue chance) implements EnchantmentValueEffect {
    public static final MapCodec<RemoveBinomial> CODEC = RecordCodecBuilder.mapCodec(
        p_345282_ -> p_345282_.group(LevelBasedValue.CODEC.fieldOf("chance").forGetter(RemoveBinomial::chance)).apply(p_345282_, RemoveBinomial::new)
    );

    @Override
    public float process(int pEnchantmentLevel, RandomSource pRandom, float pValue) {
        float f = this.chance.calculate(pEnchantmentLevel);
        int i = 0;

        for (int j = 0; (float)j < pValue; j++) {
            if (pRandom.nextFloat() < f) {
                i++;
            }
        }

        return pValue - (float)i;
    }

    @Override
    public MapCodec<RemoveBinomial> codec() {
        return CODEC;
    }
}