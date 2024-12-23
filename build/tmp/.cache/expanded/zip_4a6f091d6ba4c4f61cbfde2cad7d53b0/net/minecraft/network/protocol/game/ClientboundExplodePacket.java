package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

public class ClientboundExplodePacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundExplodePacket> STREAM_CODEC = Packet.codec(
        ClientboundExplodePacket::write, ClientboundExplodePacket::new
    );
    private final double x;
    private final double y;
    private final double z;
    private final float power;
    private final List<BlockPos> toBlow;
    private final float knockbackX;
    private final float knockbackY;
    private final float knockbackZ;
    private final ParticleOptions smallExplosionParticles;
    private final ParticleOptions largeExplosionParticles;
    private final Explosion.BlockInteraction blockInteraction;
    private final Holder<SoundEvent> explosionSound;

    public ClientboundExplodePacket(
        double pX,
        double pY,
        double pZ,
        float pPower,
        List<BlockPos> pToBlow,
        @Nullable Vec3 pKnockback,
        Explosion.BlockInteraction pBlockInteraction,
        ParticleOptions pSmallExplosionParticles,
        ParticleOptions pLargeExplosionParticles,
        Holder<SoundEvent> pExplosionSound
    ) {
        this.x = pX;
        this.y = pY;
        this.z = pZ;
        this.power = pPower;
        this.toBlow = Lists.newArrayList(pToBlow);
        this.explosionSound = pExplosionSound;
        if (pKnockback != null) {
            this.knockbackX = (float)pKnockback.x;
            this.knockbackY = (float)pKnockback.y;
            this.knockbackZ = (float)pKnockback.z;
        } else {
            this.knockbackX = 0.0F;
            this.knockbackY = 0.0F;
            this.knockbackZ = 0.0F;
        }

        this.blockInteraction = pBlockInteraction;
        this.smallExplosionParticles = pSmallExplosionParticles;
        this.largeExplosionParticles = pLargeExplosionParticles;
    }

    private ClientboundExplodePacket(RegistryFriendlyByteBuf p_335077_) {
        this.x = p_335077_.readDouble();
        this.y = p_335077_.readDouble();
        this.z = p_335077_.readDouble();
        this.power = p_335077_.readFloat();
        int i = Mth.floor(this.x);
        int j = Mth.floor(this.y);
        int k = Mth.floor(this.z);
        this.toBlow = p_335077_.readList(p_178850_ -> {
            int l = p_178850_.readByte() + i;
            int i1 = p_178850_.readByte() + j;
            int j1 = p_178850_.readByte() + k;
            return new BlockPos(l, i1, j1);
        });
        this.knockbackX = p_335077_.readFloat();
        this.knockbackY = p_335077_.readFloat();
        this.knockbackZ = p_335077_.readFloat();
        this.blockInteraction = p_335077_.readEnum(Explosion.BlockInteraction.class);
        this.smallExplosionParticles = ParticleTypes.STREAM_CODEC.decode(p_335077_);
        this.largeExplosionParticles = ParticleTypes.STREAM_CODEC.decode(p_335077_);
        this.explosionSound = SoundEvent.STREAM_CODEC.decode(p_335077_);
    }

    private void write(RegistryFriendlyByteBuf p_334035_) {
        p_334035_.writeDouble(this.x);
        p_334035_.writeDouble(this.y);
        p_334035_.writeDouble(this.z);
        p_334035_.writeFloat(this.power);
        int i = Mth.floor(this.x);
        int j = Mth.floor(this.y);
        int k = Mth.floor(this.z);
        p_334035_.writeCollection(this.toBlow, (p_296399_, p_296400_) -> {
            int l = p_296400_.getX() - i;
            int i1 = p_296400_.getY() - j;
            int j1 = p_296400_.getZ() - k;
            p_296399_.writeByte(l);
            p_296399_.writeByte(i1);
            p_296399_.writeByte(j1);
        });
        p_334035_.writeFloat(this.knockbackX);
        p_334035_.writeFloat(this.knockbackY);
        p_334035_.writeFloat(this.knockbackZ);
        p_334035_.writeEnum(this.blockInteraction);
        ParticleTypes.STREAM_CODEC.encode(p_334035_, this.smallExplosionParticles);
        ParticleTypes.STREAM_CODEC.encode(p_334035_, this.largeExplosionParticles);
        SoundEvent.STREAM_CODEC.encode(p_334035_, this.explosionSound);
    }

    @Override
    public PacketType<ClientboundExplodePacket> type() {
        return GamePacketTypes.CLIENTBOUND_EXPLODE;
    }

    public void handle(ClientGamePacketListener pHandler) {
        pHandler.handleExplosion(this);
    }

    public float getKnockbackX() {
        return this.knockbackX;
    }

    public float getKnockbackY() {
        return this.knockbackY;
    }

    public float getKnockbackZ() {
        return this.knockbackZ;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getPower() {
        return this.power;
    }

    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }

    public Explosion.BlockInteraction getBlockInteraction() {
        return this.blockInteraction;
    }

    public ParticleOptions getSmallExplosionParticles() {
        return this.smallExplosionParticles;
    }

    public ParticleOptions getLargeExplosionParticles() {
        return this.largeExplosionParticles;
    }

    public Holder<SoundEvent> getExplosionSound() {
        return this.explosionSound;
    }
}