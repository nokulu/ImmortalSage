package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.phys.Vec3;

public record ReplaceBlock(Vec3i offset, Optional<BlockPredicate> predicate, BlockStateProvider blockState, Optional<Holder<GameEvent>> triggerGameEvent)
    implements EnchantmentEntityEffect {
    public static final MapCodec<ReplaceBlock> CODEC = RecordCodecBuilder.mapCodec(
        p_343011_ -> p_343011_.group(
                    Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(ReplaceBlock::offset),
                    BlockPredicate.CODEC.optionalFieldOf("predicate").forGetter(ReplaceBlock::predicate),
                    BlockStateProvider.CODEC.fieldOf("block_state").forGetter(ReplaceBlock::blockState),
                    GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(ReplaceBlock::triggerGameEvent)
                )
                .apply(p_343011_, ReplaceBlock::new)
    );

    @Override
    public void apply(ServerLevel pLevel, int pEnchantmentLevel, EnchantedItemInUse pItem, Entity pEntity, Vec3 pOrigin) {
        BlockPos blockpos = BlockPos.containing(pOrigin).offset(this.offset);
        if (this.predicate.map(p_342514_ -> p_342514_.test(pLevel, blockpos)).orElse(true)
            && pLevel.setBlockAndUpdate(blockpos, this.blockState.getState(pEntity.getRandom(), blockpos))) {
            this.triggerGameEvent.ifPresent(p_345261_ -> pLevel.gameEvent(pEntity, (Holder<GameEvent>)p_345261_, blockpos));
        }
    }

    @Override
    public MapCodec<ReplaceBlock> codec() {
        return CODEC;
    }
}