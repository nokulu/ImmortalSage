package net.minecraft.world.entity.decoration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Painting extends HangingEntity implements VariantHolder<Holder<PaintingVariant>> {
    private static final EntityDataAccessor<Holder<PaintingVariant>> DATA_PAINTING_VARIANT_ID = SynchedEntityData.defineId(Painting.class, EntityDataSerializers.PAINTING_VARIANT);
    public static final MapCodec<Holder<PaintingVariant>> VARIANT_MAP_CODEC = PaintingVariant.CODEC.fieldOf("variant");
    public static final Codec<Holder<PaintingVariant>> VARIANT_CODEC = VARIANT_MAP_CODEC.codec();
    public static final float DEPTH = 0.0625F;

    public Painting(EntityType<? extends Painting> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        pBuilder.define(DATA_PAINTING_VARIANT_ID, this.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT).getAny().orElseThrow());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (DATA_PAINTING_VARIANT_ID.equals(pKey)) {
            this.recalculateBoundingBox();
        }
    }

    public void setVariant(Holder<PaintingVariant> p_218892_) {
        this.entityData.set(DATA_PAINTING_VARIANT_ID, p_218892_);
    }

    public Holder<PaintingVariant> getVariant() {
        return this.entityData.get(DATA_PAINTING_VARIANT_ID);
    }

    public static Optional<Painting> create(Level pLevel, BlockPos pPos, Direction pDirection) {
        Painting painting = new Painting(pLevel, pPos);
        List<Holder<PaintingVariant>> list = new ArrayList<>();
        pLevel.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT).getTagOrEmpty(PaintingVariantTags.PLACEABLE).forEach(list::add);
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            painting.setDirection(pDirection);
            list.removeIf(p_341439_ -> {
                painting.setVariant((Holder<PaintingVariant>)p_341439_);
                return !painting.survives();
            });
            if (list.isEmpty()) {
                return Optional.empty();
            } else {
                int i = list.stream().mapToInt(Painting::variantArea).max().orElse(0);
                list.removeIf(p_218883_ -> variantArea((Holder<PaintingVariant>)p_218883_) < i);
                Optional<Holder<PaintingVariant>> optional = Util.getRandomSafe(list, painting.random);
                if (optional.isEmpty()) {
                    return Optional.empty();
                } else {
                    painting.setVariant(optional.get());
                    painting.setDirection(pDirection);
                    return Optional.of(painting);
                }
            }
        }
    }

    private static int variantArea(Holder<PaintingVariant> p_218899_) {
        return p_218899_.value().area();
    }

    private Painting(Level pLevel, BlockPos pPos) {
        super(EntityType.PAINTING, pLevel, pPos);
    }

    public Painting(Level pLevel, BlockPos pPos, Direction pDirection, Holder<PaintingVariant> pVariant) {
        this(pLevel, pPos);
        this.setVariant(pVariant);
        this.setDirection(pDirection);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        VARIANT_CODEC.encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), this.getVariant()).ifSuccess(p_327008_ -> pCompound.merge((CompoundTag)p_327008_));
        pCompound.putByte("facing", (byte)this.direction.get2DDataValue());
        super.addAdditionalSaveData(pCompound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        VARIANT_CODEC.parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), pCompound).ifSuccess(this::setVariant);
        this.direction = Direction.from2DDataValue(pCompound.getByte("facing"));
        super.readAdditionalSaveData(pCompound);
        this.setDirection(this.direction);
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos pPos, Direction pDirection) {
        float f = 0.46875F;
        Vec3 vec3 = Vec3.atCenterOf(pPos).relative(pDirection, -0.46875);
        PaintingVariant paintingvariant = this.getVariant().value();
        double d0 = this.offsetForPaintingSize(paintingvariant.width());
        double d1 = this.offsetForPaintingSize(paintingvariant.height());
        Direction direction = pDirection.getCounterClockWise();
        Vec3 vec31 = vec3.relative(direction, d0).relative(Direction.UP, d1);
        Direction.Axis direction$axis = pDirection.getAxis();
        double d2 = direction$axis == Direction.Axis.X ? 0.0625 : (double)paintingvariant.width();
        double d3 = (double)paintingvariant.height();
        double d4 = direction$axis == Direction.Axis.Z ? 0.0625 : (double)paintingvariant.width();
        return AABB.ofSize(vec31, d2, d3, d4);
    }

    private double offsetForPaintingSize(int pSize) {
        return pSize % 2 == 0 ? 0.5 : 0.0;
    }

    @Override
    public void dropItem(@Nullable Entity pBrokenEntity) {
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            if (pBrokenEntity instanceof Player player && player.hasInfiniteMaterials()) {
                return;
            }

            this.spawnAtLocation(Items.PAINTING);
        }
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public void moveTo(double pX, double pY, double pZ, float pYaw, float pPitch) {
        this.setPos(pX, pY, pZ);
    }

    @Override
    public void lerpTo(double pX, double pY, double pZ, float pYRot, float pXRot, int pSteps) {
        this.setPos(pX, pY, pZ);
    }

    @Override
    public Vec3 trackingPosition() {
        return Vec3.atLowerCornerOf(this.pos);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity pEntity) {
        return new ClientboundAddEntityPacket(this, this.direction.get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        this.setDirection(Direction.from3DDataValue(pPacket.getData()));
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.PAINTING);
    }
}