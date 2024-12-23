package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record ApplyMobEffect(
    HolderSet<MobEffect> toApply, LevelBasedValue minDuration, LevelBasedValue maxDuration, LevelBasedValue minAmplifier, LevelBasedValue maxAmplifier
) implements EnchantmentEntityEffect {
    public static final MapCodec<ApplyMobEffect> CODEC = RecordCodecBuilder.mapCodec(
        p_344225_ -> p_344225_.group(
                    RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("to_apply").forGetter(ApplyMobEffect::toApply),
                    LevelBasedValue.CODEC.fieldOf("min_duration").forGetter(ApplyMobEffect::minDuration),
                    LevelBasedValue.CODEC.fieldOf("max_duration").forGetter(ApplyMobEffect::maxDuration),
                    LevelBasedValue.CODEC.fieldOf("min_amplifier").forGetter(ApplyMobEffect::minAmplifier),
                    LevelBasedValue.CODEC.fieldOf("max_amplifier").forGetter(ApplyMobEffect::maxAmplifier)
                )
                .apply(p_344225_, ApplyMobEffect::new)
    );

    @Override
    public void apply(ServerLevel pLevel, int pEnchantmentLevel, EnchantedItemInUse pItem, Entity pEntity, Vec3 pOrigin) {
        if (pEntity instanceof LivingEntity livingentity) {
            RandomSource randomsource = livingentity.getRandom();
            Optional<Holder<MobEffect>> optional = this.toApply.getRandomElement(randomsource);
            if (optional.isPresent()) {
                int i = Math.round(Mth.randomBetween(randomsource, this.minDuration.calculate(pEnchantmentLevel), this.maxDuration.calculate(pEnchantmentLevel)) * 20.0F);
                int j = Math.max(0, Math.round(Mth.randomBetween(randomsource, this.minAmplifier.calculate(pEnchantmentLevel), this.maxAmplifier.calculate(pEnchantmentLevel))));
                livingentity.addEffect(new MobEffectInstance(optional.get(), i, j));
            }
        }
    }

    @Override
    public MapCodec<ApplyMobEffect> codec() {
        return CODEC;
    }
}