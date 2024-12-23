package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownEnderpearl extends ThrowableItemProjectile {
    public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ThrownEnderpearl(Level pLevel, LivingEntity pShooter) {
        super(EntityType.ENDER_PEARL, pShooter, pLevel);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        pResult.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);

        for (int i = 0; i < 32; i++) {
            this.level()
                .addParticle(
                    ParticleTypes.PORTAL,
                    this.getX(),
                    this.getY() + this.random.nextDouble() * 2.0,
                    this.getZ(),
                    this.random.nextGaussian(),
                    0.0,
                    this.random.nextGaussian()
                );
        }

        if (this.level() instanceof ServerLevel serverlevel && !this.isRemoved()) {
            Entity entity = this.getOwner();
            if (entity != null && isAllowedToTeleportOwner(entity, serverlevel)) {
                if (entity.isPassenger()) {
                    entity.unRide();
                }

                if (entity instanceof ServerPlayer serverplayer) {
                    if (serverplayer.connection.isAcceptingMessages()) {
                        var event = net.minecraftforge.event.ForgeEventFactory.onEnderPearlLand(serverplayer, this.getX(), this.getY(), this.getZ(), this, 5.0F, pResult);
                        if (event.isCanceled()) {
                            this.discard();
                            return;
                        }

                        if (this.random.nextFloat() < 0.05F && serverlevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                            Endermite endermite = EntityType.ENDERMITE.create(serverlevel);
                            if (endermite != null) {
                                endermite.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                                serverlevel.addFreshEntity(endermite);
                            }
                        }

                        entity.changeDimension(
                            new DimensionTransition(
                                serverlevel, event.getTarget(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING
                            )
                        );
                        entity.resetFallDistance();
                        serverplayer.resetCurrentImpulseContext();
                        entity.hurt(this.damageSources().fall(), event.getAttackDamage());
                        this.playSound(serverlevel, this.position());
                    }
                } else {
                    entity.changeDimension(
                        new DimensionTransition(
                            serverlevel, this.position(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING
                        )
                    );
                    entity.resetFallDistance();
                    this.playSound(serverlevel, this.position());
                }

                this.discard();
                return;
            }

            this.discard();
            return;
        }
    }

    private static boolean isAllowedToTeleportOwner(Entity pEntity, Level pLevel) {
        if (pEntity.level().dimension() == pLevel.dimension()) {
            return !(pEntity instanceof LivingEntity livingentity) ? pEntity.isAlive() : livingentity.isAlive() && !livingentity.isSleeping();
        } else {
            return pEntity.canUsePortal(true);
        }
    }

    @Override
    public void tick() {
        Entity entity = this.getOwner();
        if (entity instanceof ServerPlayer && !entity.isAlive() && this.level().getGameRules().getBoolean(GameRules.RULE_ENDER_PEARLS_VANISH_ON_DEATH)) {
            this.discard();
        } else {
            super.tick();
        }
    }

    private void playSound(Level pLevel, Vec3 pPos) {
        pLevel.playSound(null, pPos.x, pPos.y, pPos.z, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
    }

    @Override
    public boolean canChangeDimensions(Level pOldLevel, Level pNewLevel) {
        return pOldLevel.dimension() == Level.END && this.getOwner() instanceof ServerPlayer serverplayer
            ? super.canChangeDimensions(pOldLevel, pNewLevel) && serverplayer.seenCredits
            : super.canChangeDimensions(pOldLevel, pNewLevel);
    }

    @Override
    protected void onInsideBlock(BlockState pState) {
        super.onInsideBlock(pState);
        if (pState.is(Blocks.END_GATEWAY) && this.getOwner() instanceof ServerPlayer serverplayer) {
            serverplayer.onInsideBlock(pState);
        }
    }
}
