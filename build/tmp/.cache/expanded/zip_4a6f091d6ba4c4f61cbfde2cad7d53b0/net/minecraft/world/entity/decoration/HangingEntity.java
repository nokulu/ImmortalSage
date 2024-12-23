package net.minecraft.world.entity.decoration;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;

public abstract class HangingEntity extends BlockAttachedEntity {
    protected static final Predicate<Entity> HANGING_ENTITY = p_31734_ -> p_31734_ instanceof HangingEntity;
    protected Direction direction = Direction.SOUTH;

    protected HangingEntity(EntityType<? extends HangingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    protected HangingEntity(EntityType<? extends HangingEntity> pEntityType, Level pLevel, BlockPos pPos) {
        this(pEntityType, pLevel);
        this.pos = pPos;
    }

    protected void setDirection(Direction pFacingDirection) {
        Objects.requireNonNull(pFacingDirection);
        Validate.isTrue(pFacingDirection.getAxis().isHorizontal());
        this.direction = pFacingDirection;
        this.setYRot((float)(this.direction.get2DDataValue() * 90));
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    @Override
    protected final void recalculateBoundingBox() {
        if (this.direction != null) {
            AABB aabb = this.calculateBoundingBox(this.pos, this.direction);
            Vec3 vec3 = aabb.getCenter();
            this.setPosRaw(vec3.x, vec3.y, vec3.z);
            this.setBoundingBox(aabb);
        }
    }

    protected abstract AABB calculateBoundingBox(BlockPos pPos, Direction pDirection);

    @Override
    public boolean survives() {
        if (!this.level().noCollision(this)) {
            return false;
        } else {
            boolean flag = BlockPos.betweenClosedStream(this.calculateSupportBox()).allMatch(p_341437_ -> {
                BlockState blockstate = this.level().getBlockState(p_341437_);
                if (net.minecraft.world.level.block.Block.canSupportCenter(this.level(), p_341437_, this.direction))
                    return true;
                return blockstate.isSolid() || DiodeBlock.isDiode(blockstate);
            });
            return !flag ? false : this.level().getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
        }
    }

    protected AABB calculateSupportBox() {
        return this.getBoundingBox().move(this.direction.step().mul(-0.5F)).deflate(1.0E-7);
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    public abstract void playPlacementSound();

    @Override
    public ItemEntity spawnAtLocation(ItemStack pStack, float pOffsetY) {
        ItemEntity itementity = new ItemEntity(
            this.level(),
            this.getX() + (double)((float)this.direction.getStepX() * 0.15F),
            this.getY() + (double)pOffsetY,
            this.getZ() + (double)((float)this.direction.getStepZ() * 0.15F),
            pStack
        );
        itementity.setDefaultPickUpDelay();
        this.level().addFreshEntity(itementity);
        return itementity;
    }

    @Override
    public float rotate(Rotation pTransformRotation) {
        if (this.direction.getAxis() != Direction.Axis.Y) {
            switch (pTransformRotation) {
                case CLOCKWISE_180:
                    this.direction = this.direction.getOpposite();
                    break;
                case COUNTERCLOCKWISE_90:
                    this.direction = this.direction.getCounterClockWise();
                    break;
                case CLOCKWISE_90:
                    this.direction = this.direction.getClockWise();
            }
        }

        float f = Mth.wrapDegrees(this.getYRot());

        return switch (pTransformRotation) {
            case CLOCKWISE_180 -> f + 180.0F;
            case COUNTERCLOCKWISE_90 -> f + 90.0F;
            case CLOCKWISE_90 -> f + 270.0F;
            default -> f;
        };
    }

    @Override
    public float mirror(Mirror pTransformMirror) {
        return this.rotate(pTransformMirror.getRotation(this.direction));
    }
}
