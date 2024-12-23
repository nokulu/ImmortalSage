package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

/**
 * A possibly unbounded range of integers based on {@link LootContext}. Minimum and maximum are given in the form of
 * {@link NumberProvider}s.
 * Minimum and maximum are both optional. If given, they are both inclusive.
 */
public class IntRange {
    private static final Codec<IntRange> RECORD_CODEC = RecordCodecBuilder.create(
        p_327547_ -> p_327547_.group(
                    NumberProviders.CODEC.optionalFieldOf("min").forGetter(p_296994_ -> Optional.ofNullable(p_296994_.min)),
                    NumberProviders.CODEC.optionalFieldOf("max").forGetter(p_296996_ -> Optional.ofNullable(p_296996_.max))
                )
                .apply(p_327547_, IntRange::new)
    );
    public static final Codec<IntRange> CODEC = Codec.either(Codec.INT, RECORD_CODEC)
        .xmap(p_296998_ -> p_296998_.map(IntRange::exact, Function.identity()), p_296997_ -> {
            OptionalInt optionalint = p_296997_.unpackExact();
            return optionalint.isPresent() ? Either.left(optionalint.getAsInt()) : Either.right(p_296997_);
        });
    @Nullable
    private final NumberProvider min;
    @Nullable
    private final NumberProvider max;
    private final IntRange.IntLimiter limiter;
    private final IntRange.IntChecker predicate;

    public Set<LootContextParam<?>> getReferencedContextParams() {
        Builder<LootContextParam<?>> builder = ImmutableSet.builder();
        if (this.min != null) {
            builder.addAll(this.min.getReferencedContextParams());
        }

        if (this.max != null) {
            builder.addAll(this.max.getReferencedContextParams());
        }

        return builder.build();
    }

    private IntRange(Optional<NumberProvider> p_300812_, Optional<NumberProvider> p_298905_) {
        this(p_300812_.orElse(null), p_298905_.orElse(null));
    }

    private IntRange(@Nullable NumberProvider pMin, @Nullable NumberProvider pMax) {
        this.min = pMin;
        this.max = pMax;
        if (pMin == null) {
            if (pMax == null) {
                this.limiter = (p_165050_, p_165051_) -> p_165051_;
                this.predicate = (p_165043_, p_165044_) -> true;
            } else {
                this.limiter = (p_165054_, p_165055_) -> Math.min(pMax.getInt(p_165054_), p_165055_);
                this.predicate = (p_165047_, p_165048_) -> p_165048_ <= pMax.getInt(p_165047_);
            }
        } else if (pMax == null) {
            this.limiter = (p_165033_, p_165034_) -> Math.max(pMin.getInt(p_165033_), p_165034_);
            this.predicate = (p_165019_, p_165020_) -> p_165020_ >= pMin.getInt(p_165019_);
        } else {
            this.limiter = (p_165038_, p_165039_) -> Mth.clamp(p_165039_, pMin.getInt(p_165038_), pMax.getInt(p_165038_));
            this.predicate = (p_165024_, p_165025_) -> p_165025_ >= pMin.getInt(p_165024_) && p_165025_ <= pMax.getInt(p_165024_);
        }
    }

    public static IntRange exact(int p_165010_) {
        ConstantValue constantvalue = ConstantValue.exactly((float)p_165010_);
        return new IntRange(Optional.of(constantvalue), Optional.of(constantvalue));
    }

    public static IntRange range(int pMin, int pMax) {
        return new IntRange(Optional.of(ConstantValue.exactly((float)pMin)), Optional.of(ConstantValue.exactly((float)pMax)));
    }

    public static IntRange lowerBound(int pMin) {
        return new IntRange(Optional.of(ConstantValue.exactly((float)pMin)), Optional.empty());
    }

    public static IntRange upperBound(int pMax) {
        return new IntRange(Optional.empty(), Optional.of(ConstantValue.exactly((float)pMax)));
    }

    public int clamp(LootContext pLootContext, int pValue) {
        return this.limiter.apply(pLootContext, pValue);
    }

    public boolean test(LootContext pLootContext, int pValue) {
        return this.predicate.test(pLootContext, pValue);
    }

    private OptionalInt unpackExact() {
        return Objects.equals(this.min, this.max)
                && this.min instanceof ConstantValue constantvalue
                && Math.floor((double)constantvalue.value()) == (double)constantvalue.value()
            ? OptionalInt.of((int)constantvalue.value())
            : OptionalInt.empty();
    }

    @FunctionalInterface
    interface IntChecker {
        boolean test(LootContext pLootContext, int pValue);
    }

    @FunctionalInterface
    interface IntLimiter {
        int apply(LootContext pLootContext, int pValue);
    }
}