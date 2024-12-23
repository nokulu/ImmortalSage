package net.minecraft.network.protocol.game;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundLevelParticlesPacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLevelParticlesPacket> STREAM_CODEC = Packet.codec(
        ClientboundLevelParticlesPacket::write, ClientboundLevelParticlesPacket::new
    );
    private final double x;
    private final double y;
    private final double z;
    private final float xDist;
    private final float yDist;
    private final float zDist;
    private final float maxSpeed;
    private final int count;
    private final boolean overrideLimiter;
    private final ParticleOptions particle;

    public <T extends ParticleOptions> ClientboundLevelParticlesPacket(
        T pParticle,
        boolean pOverrideLimiter,
        double pX,
        double pY,
        double pZ,
        float pXDist,
        float pYDist,
        float pZDist,
        float pMaxSpeed,
        int pCount
    ) {
        this.particle = pParticle;
        this.overrideLimiter = pOverrideLimiter;
        this.x = pX;
        this.y = pY;
        this.z = pZ;
        this.xDist = pXDist;
        this.yDist = pYDist;
        this.zDist = pZDist;
        this.maxSpeed = pMaxSpeed;
        this.count = pCount;
    }

    private ClientboundLevelParticlesPacket(RegistryFriendlyByteBuf p_334002_) {
        this.overrideLimiter = p_334002_.readBoolean();
        this.x = p_334002_.readDouble();
        this.y = p_334002_.readDouble();
        this.z = p_334002_.readDouble();
        this.xDist = p_334002_.readFloat();
        this.yDist = p_334002_.readFloat();
        this.zDist = p_334002_.readFloat();
        this.maxSpeed = p_334002_.readFloat();
        this.count = p_334002_.readInt();
        this.particle = ParticleTypes.STREAM_CODEC.decode(p_334002_);
    }

    private void write(RegistryFriendlyByteBuf p_335688_) {
        p_335688_.writeBoolean(this.overrideLimiter);
        p_335688_.writeDouble(this.x);
        p_335688_.writeDouble(this.y);
        p_335688_.writeDouble(this.z);
        p_335688_.writeFloat(this.xDist);
        p_335688_.writeFloat(this.yDist);
        p_335688_.writeFloat(this.zDist);
        p_335688_.writeFloat(this.maxSpeed);
        p_335688_.writeInt(this.count);
        ParticleTypes.STREAM_CODEC.encode(p_335688_, this.particle);
    }

    @Override
    public PacketType<ClientboundLevelParticlesPacket> type() {
        return GamePacketTypes.CLIENTBOUND_LEVEL_PARTICLES;
    }

    public void handle(ClientGamePacketListener pHandler) {
        pHandler.handleParticleEvent(this);
    }

    public boolean isOverrideLimiter() {
        return this.overrideLimiter;
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

    public float getXDist() {
        return this.xDist;
    }

    public float getYDist() {
        return this.yDist;
    }

    public float getZDist() {
        return this.zDist;
    }

    public float getMaxSpeed() {
        return this.maxSpeed;
    }

    public int getCount() {
        return this.count;
    }

    public ParticleOptions getParticle() {
        return this.particle;
    }
}