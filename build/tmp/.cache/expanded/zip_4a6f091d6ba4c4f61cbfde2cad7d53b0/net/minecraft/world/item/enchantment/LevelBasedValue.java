package net.minecraft.world.item.enchantment;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;

public interface LevelBasedValue {
    Codec<LevelBasedValue> DISPATCH_CODEC = BuiltInRegistries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE.byNameCodec().dispatch(LevelBasedValue::codec, p_344739_ -> p_344739_);
    Codec<LevelBasedValue> CODEC = Codec.either(LevelBasedValue.Constant.CODEC, DISPATCH_CODEC)
        .xmap(
            p_342800_ -> p_342800_.map(p_345300_ -> (LevelBasedValue)p_345300_, p_342996_ -> (LevelBasedValue)p_342996_),
            p_343036_ -> p_343036_ instanceof LevelBasedValue.Constant levelbasedvalue$constant
                    ? Either.left(levelbasedvalue$constant)
                    : Either.right(p_343036_)
        );

    static MapCodec<? extends LevelBasedValue> bootstrap(Registry<MapCodec<? extends LevelBasedValue>> pRegistry) {
        Registry.register(pRegistry, "clamped", LevelBasedValue.Clamped.CODEC);
        Registry.register(pRegistry, "fraction", LevelBasedValue.Fraction.CODEC);
        Registry.register(pRegistry, "levels_squared", LevelBasedValue.LevelsSquared.CODEC);
        Registry.register(pRegistry, "linear", LevelBasedValue.Linear.CODEC);
        return Registry.register(pRegistry, "lookup", LevelBasedValue.Lookup.CODEC);
    }

    static LevelBasedValue.Constant constant(float pValue) {
        return new LevelBasedValue.Constant(pValue);
    }

    static LevelBasedValue.Linear perLevel(float pBase, float pPerLevelAfterFirst) {
        return new LevelBasedValue.Linear(pBase, pPerLevelAfterFirst);
    }

    static LevelBasedValue.Linear perLevel(float pPerLevel) {
        return perLevel(pPerLevel, pPerLevel);
    }

    static LevelBasedValue.Lookup lookup(List<Float> pValues, LevelBasedValue pFallback) {
        return new LevelBasedValue.Lookup(pValues, pFallback);
    }

    float calculate(int pLevel);

    MapCodec<? extends LevelBasedValue> codec();

    public static record Clamped(LevelBasedValue value, float min, float max) implements LevelBasedValue {
        public static final MapCodec<LevelBasedValue.Clamped> CODEC = RecordCodecBuilder.<LevelBasedValue.Clamped>mapCodec(
                p_343138_ -> p_343138_.group(
                            LevelBasedValue.CODEC.fieldOf("value").forGetter(LevelBasedValue.Clamped::value),
                            Codec.FLOAT.fieldOf("min").forGetter(LevelBasedValue.Clamped::min),
                            Codec.FLOAT.fieldOf("max").forGetter(LevelBasedValue.Clamped::max)
                        )
                        .apply(p_343138_, LevelBasedValue.Clamped::new)
            )
            .validate(
                p_345252_ -> p_345252_.max <= p_345252_.min
                        ? DataResult.error(() -> "Max must be larger than min, min: " + p_345252_.min + ", max: " + p_345252_.max)
                        : DataResult.success(p_345252_)
            );

        @Override
        public float calculate(int pLevel) {
            return Mth.clamp(this.value.calculate(pLevel), this.min, this.max);
        }

        @Override
        public MapCodec<LevelBasedValue.Clamped> codec() {
            return CODEC;
        }
    }

    public static record Constant(float value) implements LevelBasedValue {
        public static final Codec<LevelBasedValue.Constant> CODEC = Codec.FLOAT.xmap(LevelBasedValue.Constant::new, LevelBasedValue.Constant::value);
        public static final MapCodec<LevelBasedValue.Constant> TYPED_CODEC = RecordCodecBuilder.mapCodec(
            p_345310_ -> p_345310_.group(Codec.FLOAT.fieldOf("value").forGetter(LevelBasedValue.Constant::value))
                    .apply(p_345310_, LevelBasedValue.Constant::new)
        );

        @Override
        public float calculate(int pLevel) {
            return this.value;
        }

        @Override
        public MapCodec<LevelBasedValue.Constant> codec() {
            return TYPED_CODEC;
        }
    }

    public static record Fraction(LevelBasedValue numerator, LevelBasedValue denominator) implements LevelBasedValue {
        public static final MapCodec<LevelBasedValue.Fraction> CODEC = RecordCodecBuilder.mapCodec(
            p_342531_ -> p_342531_.group(
                        LevelBasedValue.CODEC.fieldOf("numerator").forGetter(LevelBasedValue.Fraction::numerator),
                        LevelBasedValue.CODEC.fieldOf("denominator").forGetter(LevelBasedValue.Fraction::denominator)
                    )
                    .apply(p_342531_, LevelBasedValue.Fraction::new)
        );

        @Override
        public float calculate(int pLevel) {
            float f = this.denominator.calculate(pLevel);
            return f == 0.0F ? 0.0F : this.numerator.calculate(pLevel) / f;
        }

        @Override
        public MapCodec<LevelBasedValue.Fraction> codec() {
            return CODEC;
        }
    }

    public static record LevelsSquared(float added) implements LevelBasedValue {
        public static final MapCodec<LevelBasedValue.LevelsSquared> CODEC = RecordCodecBuilder.mapCodec(
            p_342272_ -> p_342272_.group(Codec.FLOAT.fieldOf("added").forGetter(LevelBasedValue.LevelsSquared::added))
                    .apply(p_342272_, LevelBasedValue.LevelsSquared::new)
        );

        @Override
        public float calculate(int pLevel) {
            return (float)Mth.square(pLevel) + this.added;
        }

        @Override
        public MapCodec<LevelBasedValue.LevelsSquared> codec() {
            return CODEC;
        }
    }

    public static record Linear(float base, float perLevelAboveFirst) implements LevelBasedValue {
        public static final MapCodec<LevelBasedValue.Linear> CODEC = RecordCodecBuilder.mapCodec(
            p_345355_ -> p_345355_.group(
                        Codec.FLOAT.fieldOf("base").forGetter(LevelBasedValue.Linear::base),
                        Codec.FLOAT.fieldOf("per_level_above_first").forGetter(LevelBasedValue.Linear::perLevelAboveFirst)
                    )
                    .apply(p_345355_, LevelBasedValue.Linear::new)
        );

        @Override
        public float calculate(int pLevel) {
            return this.base + this.perLevelAboveFirst * (float)(pLevel - 1);
        }

        @Override
        public MapCodec<LevelBasedValue.Linear> codec() {
            return CODEC;
        }
    }

    public static record Lookup(List<Float> values, LevelBasedValue fallback) implements LevelBasedValue {
        public static final MapCodec<LevelBasedValue.Lookup> CODEC = RecordCodecBuilder.mapCodec(
            p_342915_ -> p_342915_.group(
                        Codec.FLOAT.listOf().fieldOf("values").forGetter(LevelBasedValue.Lookup::values),
                        LevelBasedValue.CODEC.fieldOf("fallback").forGetter(LevelBasedValue.Lookup::fallback)
                    )
                    .apply(p_342915_, LevelBasedValue.Lookup::new)
        );

        @Override
        public float calculate(int pLevel) {
            return pLevel <= this.values.size() ? this.values.get(pLevel - 1) : this.fallback.calculate(pLevel);
        }

        @Override
        public MapCodec<LevelBasedValue.Lookup> codec() {
            return CODEC;
        }
    }
}