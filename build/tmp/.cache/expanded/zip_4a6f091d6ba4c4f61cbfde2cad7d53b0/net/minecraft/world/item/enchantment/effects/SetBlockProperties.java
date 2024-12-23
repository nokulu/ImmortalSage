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
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public record SetBlockProperties(BlockItemStateProperties properties, Vec3i offset, Optional<Holder<GameEvent>> triggerGameEvent) implements EnchantmentEntityEffect {
    public static final MapCodec<SetBlockProperties> CODEC = RecordCodecBuilder.mapCodec(
        p_344188_ -> p_344188_.group(
                    BlockItemStateProperties.CODEC.fieldOf("properties").forGetter(SetBlockProperties::properties),
                    Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(SetBlockProperties::offset),
                    GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(SetBlockProperties::triggerGameEvent)
                )
                .apply(p_344188_, SetBlockProperties::new)
    );

    public SetBlockProperties(BlockItemStateProperties pProperties) {
        this(pProperties, Vec3i.ZERO, Optional.of(GameEvent.BLOCK_CHANGE));
    }

    @Override
    public void apply(ServerLevel pLevel, int pEnchantmentLevel, EnchantedItemInUse pItem, Entity pEntity, Vec3 pOrigin) {
        BlockPos blockpos = BlockPos.containing(pOrigin).offset(this.offset);
        BlockState blockstate = pEntity.level().getBlockState(blockpos);
        BlockState blockstate1 = this.properties.apply(blockstate);
        if (!blockstate.equals(blockstate1) && pEntity.level().setBlock(blockpos, blockstate1, 3)) {
            this.triggerGameEvent.ifPresent(p_344693_ -> pLevel.gameEvent(pEntity, (Holder<GameEvent>)p_344693_, blockpos));
        }
    }

    @Override
    public MapCodec<SetBlockProperties> codec() {
        return CODEC;
    }
}