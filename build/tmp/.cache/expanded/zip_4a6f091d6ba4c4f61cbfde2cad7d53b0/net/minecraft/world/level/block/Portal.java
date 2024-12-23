package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;

public interface Portal {
    default int getPortalTransitionTime(ServerLevel pLevel, Entity pEntity) {
        return 0;
    }

    @Nullable
    DimensionTransition getPortalDestination(ServerLevel pLevel, Entity pEntity, BlockPos pPos);

    default Portal.Transition getLocalTransition() {
        return Portal.Transition.NONE;
    }

    public static enum Transition {
        CONFUSION,
        NONE;
    }
}