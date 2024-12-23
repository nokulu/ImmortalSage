package net.minecraft.world.level.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record DimensionTransition(
    ServerLevel newLevel,
    Vec3 pos,
    Vec3 speed,
    float yRot,
    float xRot,
    boolean missingRespawnBlock,
    DimensionTransition.PostDimensionTransition postDimensionTransition
) {
    public static final DimensionTransition.PostDimensionTransition DO_NOTHING = p_343587_ -> {
    };
    public static final DimensionTransition.PostDimensionTransition PLAY_PORTAL_SOUND = DimensionTransition::playPortalSound;
    public static final DimensionTransition.PostDimensionTransition PLACE_PORTAL_TICKET = DimensionTransition::placePortalTicket;

    public DimensionTransition(
        ServerLevel pNewLevel, Vec3 pPos, Vec3 pSpeed, float pYRot, float pXRot, DimensionTransition.PostDimensionTransition pPostDimensionTransition
    ) {
        this(pNewLevel, pPos, pSpeed, pYRot, pXRot, false, pPostDimensionTransition);
    }

    public DimensionTransition(ServerLevel pNewLevel, Entity pEntity, DimensionTransition.PostDimensionTransition pPostDimensionTransition) {
        this(pNewLevel, findAdjustedSharedSpawnPos(pNewLevel, pEntity), Vec3.ZERO, 0.0F, 0.0F, false, pPostDimensionTransition);
    }

    private static void playPortalSound(Entity p_342599_) {
        if (p_342599_ instanceof ServerPlayer serverplayer) {
            serverplayer.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
        }
    }

    private static void placePortalTicket(Entity p_344820_) {
        p_344820_.placePortalTicket(BlockPos.containing(p_344820_.position()));
    }

    public static DimensionTransition missingRespawnBlock(ServerLevel pLevel, Entity pEntity, DimensionTransition.PostDimensionTransition pPostDimensionTransition) {
        return new DimensionTransition(pLevel, findAdjustedSharedSpawnPos(pLevel, pEntity), Vec3.ZERO, 0.0F, 0.0F, true, pPostDimensionTransition);
    }

    private static Vec3 findAdjustedSharedSpawnPos(ServerLevel pNewLevel, Entity pEntity) {
        return pEntity.adjustSpawnLocation(pNewLevel, pNewLevel.getSharedSpawnPos()).getBottomCenter();
    }

    @FunctionalInterface
    public interface PostDimensionTransition {
        void onTransition(Entity pEntity);

        default DimensionTransition.PostDimensionTransition then(DimensionTransition.PostDimensionTransition pTransition) {
            return p_344375_ -> {
                this.onTransition(p_344375_);
                pTransition.onTransition(p_344375_);
            };
        }
    }
}