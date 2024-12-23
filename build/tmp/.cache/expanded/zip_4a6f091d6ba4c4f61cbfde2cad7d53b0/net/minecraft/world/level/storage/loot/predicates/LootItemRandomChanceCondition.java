package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record LootItemRandomChanceCondition(NumberProvider chance) implements LootItemCondition {
    public static final MapCodec<LootItemRandomChanceCondition> CODEC = RecordCodecBuilder.mapCodec(
        p_342030_ -> p_342030_.group(NumberProviders.CODEC.fieldOf("chance").forGetter(LootItemRandomChanceCondition::chance))
                .apply(p_342030_, LootItemRandomChanceCondition::new)
    );

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.RANDOM_CHANCE;
    }

    public boolean test(LootContext pContext) {
        float f = this.chance.getFloat(pContext);
        return pContext.getRandom().nextFloat() < f;
    }

    public static LootItemCondition.Builder randomChance(float pChance) {
        return () -> new LootItemRandomChanceCondition(ConstantValue.exactly(pChance));
    }

    public static LootItemCondition.Builder randomChance(NumberProvider pChance) {
        return () -> new LootItemRandomChanceCondition(pChance);
    }
}