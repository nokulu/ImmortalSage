package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.phys.Vec3;

public record ReplaceDisk(
    LevelBasedValue radius,
    LevelBasedValue height,
    Vec3i offset,
    Optional<BlockPredicate> predicate,
    BlockStateProvider blockState,
    Optional<Holder<GameEvent>> triggerGameEvent
) implements EnchantmentEntityEffect {
    public static final MapCodec<ReplaceDisk> CODEC = RecordCodecBuilder.mapCodec(
        p_345029_ -> p_345029_.group(
                    LevelBasedValue.CODEC.fieldOf("radius").forGetter(ReplaceDisk::radius),
                    LevelBasedValue.CODEC.fieldOf("height").forGetter(ReplaceDisk::height),
                    Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(ReplaceDisk::offset),
                    BlockPredicate.CODEC.optionalFieldOf("predicate").forGetter(ReplaceDisk::predicate),
                    BlockStateProvider.CODEC.fieldOf("block_state").forGetter(ReplaceDisk::blockState),
                    GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(ReplaceDisk::triggerGameEvent)
                )
                .apply(p_345029_, ReplaceDisk::new)
    );

    @Override
    public void apply(ServerLevel pLevel, int pEnchantmentLevel, EnchantedItemInUse pItem, Entity pEntity, Vec3 pOrigin) {
        BlockPos blockpos = BlockPos.containing(pOrigin).offset(this.offset);
        RandomSource randomsource = pEntity.getRandom();
        int i = (int)this.radius.calculate(pEnchantmentLevel);
        int j = (int)this.height.calculate(pEnchantmentLevel);

        for (BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-i, 0, -i), blockpos.offset(i, Math.min(j - 1, 0), i))) {
            if (blockpos1.distToCenterSqr(pOrigin.x(), (double)blockpos1.getY() + 0.5, pOrigin.z()) < (double)Mth.square(i)
                && this.predicate.map(p_343365_ -> p_343365_.test(pLevel, blockpos1)).orElse(true)
                && !net.minecraftforge.event.ForgeEventFactory.onBlockPlace(pEntity, net.minecraftforge.common.util.BlockSnapshot.create(pLevel.dimension(), pLevel, blockpos), net.minecraft.core.Direction.UP)
                && pLevel.setBlockAndUpdate(blockpos1, this.blockState.getState(randomsource, blockpos1))) {
                this.triggerGameEvent.ifPresent(p_344749_ -> pLevel.gameEvent(pEntity, (Holder<GameEvent>)p_344749_, blockpos1));
            }
        }
    }

    @Override
    public MapCodec<ReplaceDisk> codec() {
        return CODEC;
    }
}
