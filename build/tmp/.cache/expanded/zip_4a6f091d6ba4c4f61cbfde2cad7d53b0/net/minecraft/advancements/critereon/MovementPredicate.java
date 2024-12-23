package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.util.Mth;

public record MovementPredicate(
    MinMaxBounds.Doubles x,
    MinMaxBounds.Doubles y,
    MinMaxBounds.Doubles z,
    MinMaxBounds.Doubles speed,
    MinMaxBounds.Doubles horizontalSpeed,
    MinMaxBounds.Doubles verticalSpeed,
    MinMaxBounds.Doubles fallDistance
) {
    public static final Codec<MovementPredicate> CODEC = RecordCodecBuilder.create(
        p_345285_ -> p_345285_.group(
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::x),
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::y),
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::z),
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("speed", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::speed),
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("horizontal_speed", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::horizontalSpeed),
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("vertical_speed", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::verticalSpeed),
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("fall_distance", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::fallDistance)
                )
                .apply(p_345285_, MovementPredicate::new)
    );

    public static MovementPredicate speed(MinMaxBounds.Doubles pSpeed) {
        return new MovementPredicate(
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            pSpeed,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY
        );
    }

    public static MovementPredicate horizontalSpeed(MinMaxBounds.Doubles pHorizontalSpeed) {
        return new MovementPredicate(
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            pHorizontalSpeed,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY
        );
    }

    public static MovementPredicate verticalSpeed(MinMaxBounds.Doubles pVerticalSpeed) {
        return new MovementPredicate(
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            pVerticalSpeed,
            MinMaxBounds.Doubles.ANY
        );
    }

    public static MovementPredicate fallDistance(MinMaxBounds.Doubles pFallDistance) {
        return new MovementPredicate(
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            pFallDistance
        );
    }

    public boolean matches(double pX, double pY, double pZ, double pFallDistance) {
        if (this.x.matches(pX) && this.y.matches(pY) && this.z.matches(pZ)) {
            double d0 = Mth.lengthSquared(pX, pY, pZ);
            if (!this.speed.matchesSqr(d0)) {
                return false;
            } else {
                double d1 = Mth.lengthSquared(pX, pZ);
                if (!this.horizontalSpeed.matchesSqr(d1)) {
                    return false;
                } else {
                    double d2 = Math.abs(pY);
                    return !this.verticalSpeed.matches(d2) ? false : this.fallDistance.matches(pFallDistance);
                }
            }
        } else {
            return false;
        }
    }
}