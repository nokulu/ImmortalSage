package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record ConditionalEffect<T>(T effect, Optional<LootItemCondition> requirements) {
    public static Codec<LootItemCondition> conditionCodec(LootContextParamSet pParams) {
        return LootItemCondition.DIRECT_CODEC
            .validate(
                p_342331_ -> {
                    ProblemReporter.Collector problemreporter$collector = new ProblemReporter.Collector();
                    ValidationContext validationcontext = new ValidationContext(problemreporter$collector, pParams);
                    p_342331_.validate(validationcontext);
                    return (DataResult)problemreporter$collector.getReport()
                        .map(p_343789_ -> DataResult.error(() -> "Validation error in enchantment effect condition: " + p_343789_))
                        .orElseGet(() -> DataResult.success(p_342331_));
                }
            );
    }

    public static <T> Codec<ConditionalEffect<T>> codec(Codec<T> pCodec, LootContextParamSet pParams) {
        return RecordCodecBuilder.create(
            p_344871_ -> p_344871_.group(
                        pCodec.fieldOf("effect").forGetter(ConditionalEffect::effect),
                        conditionCodec(pParams).optionalFieldOf("requirements").forGetter(ConditionalEffect::requirements)
                    )
                    .apply(p_344871_, ConditionalEffect::new)
        );
    }

    public boolean matches(LootContext pContext) {
        return this.requirements.isEmpty() ? true : this.requirements.get().test(pContext);
    }
}