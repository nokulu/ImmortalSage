package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.portal.DimensionTransition;

public class PortalProcessor {
    private Portal portal;
    private BlockPos entryPosition;
    private int portalTime;
    private boolean insidePortalThisTick;

    public PortalProcessor(Portal pPortal, BlockPos pEntryPosition) {
        this.portal = pPortal;
        this.entryPosition = pEntryPosition;
        this.insidePortalThisTick = true;
    }

    public boolean processPortalTeleportation(ServerLevel pLevel, Entity pEntity, boolean pCanChangeDimensions) {
        if (!this.insidePortalThisTick) {
            this.decayTick();
            return false;
        } else {
            this.insidePortalThisTick = false;
            return pCanChangeDimensions && this.portalTime++ >= this.portal.getPortalTransitionTime(pLevel, pEntity);
        }
    }

    @Nullable
    public DimensionTransition getPortalDestination(ServerLevel pLevel, Entity pEntity) {
        return this.portal.getPortalDestination(pLevel, pEntity, this.entryPosition);
    }

    public Portal.Transition getPortalLocalTransition() {
        return this.portal.getLocalTransition();
    }

    private void decayTick() {
        this.portalTime = Math.max(this.portalTime - 4, 0);
    }

    public boolean hasExpired() {
        return this.portalTime <= 0;
    }

    public BlockPos getEntryPosition() {
        return this.entryPosition;
    }

    public void updateEntryPosition(BlockPos pEntryPosition) {
        this.entryPosition = pEntryPosition;
    }

    public int getPortalTime() {
        return this.portalTime;
    }

    public boolean isInsidePortalThisTick() {
        return this.insidePortalThisTick;
    }

    public void setAsInsidePortalThisTick(boolean pInsidePortalThisTick) {
        this.insidePortalThisTick = pInsidePortalThisTick;
    }

    public boolean isSamePortal(Portal pPortal) {
        return this.portal == pPortal;
    }
}