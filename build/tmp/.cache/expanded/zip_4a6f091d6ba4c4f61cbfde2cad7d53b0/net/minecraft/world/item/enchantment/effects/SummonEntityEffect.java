package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record SummonEntityEffect(HolderSet<EntityType<?>> entityTypes, boolean joinTeam) implements EnchantmentEntityEffect {
    public static final MapCodec<SummonEntityEffect> CODEC = RecordCodecBuilder.mapCodec(
        p_345460_ -> p_345460_.group(
                    RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entity").forGetter(SummonEntityEffect::entityTypes),
                    Codec.BOOL.optionalFieldOf("join_team", Boolean.valueOf(false)).forGetter(SummonEntityEffect::joinTeam)
                )
                .apply(p_345460_, SummonEntityEffect::new)
    );

    @Override
    public void apply(ServerLevel pLevel, int pEnchantmentLevel, EnchantedItemInUse pItem, Entity pEntity, Vec3 pOrigin) {
        BlockPos blockpos = BlockPos.containing(pOrigin);
        if (Level.isInSpawnableBounds(blockpos)) {
            Optional<Holder<EntityType<?>>> optional = this.entityTypes().getRandomElement(pLevel.getRandom());
            if (!optional.isEmpty()) {
                Entity entity = optional.get().value().spawn(pLevel, blockpos, MobSpawnType.TRIGGERED);
                if (entity != null) {
                    if (entity instanceof LightningBolt lightningbolt && pItem.owner() instanceof ServerPlayer serverplayer) {
                        lightningbolt.setCause(serverplayer);
                    }

                    if (this.joinTeam && pEntity.getTeam() != null) {
                        pLevel.getScoreboard().addPlayerToTeam(entity.getScoreboardName(), pEntity.getTeam());
                    }

                    entity.moveTo(pOrigin.x, pOrigin.y, pOrigin.z, entity.getYRot(), entity.getXRot());
                }
            }
        }
    }

    @Override
    public MapCodec<SummonEntityEffect> codec() {
        return CODEC;
    }
}