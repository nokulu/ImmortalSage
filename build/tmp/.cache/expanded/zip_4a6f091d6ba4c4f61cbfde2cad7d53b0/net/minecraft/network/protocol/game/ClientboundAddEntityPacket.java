package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public class ClientboundAddEntityPacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAddEntityPacket> STREAM_CODEC = Packet.codec(
        ClientboundAddEntityPacket::write, ClientboundAddEntityPacket::new
    );
    private static final double MAGICAL_QUANTIZATION = 8000.0;
    private static final double LIMIT = 3.9;
    private final int id;
    private final UUID uuid;
    private final EntityType<?> type;
    private final double x;
    private final double y;
    private final double z;
    private final int xa;
    private final int ya;
    private final int za;
    private final byte xRot;
    private final byte yRot;
    private final byte yHeadRot;
    private final int data;

    public ClientboundAddEntityPacket(Entity pEntity, ServerEntity pServerEntity) {
        this(pEntity, pServerEntity, 0);
    }

    public ClientboundAddEntityPacket(Entity pEntity, ServerEntity pServerEntity, int pData) {
        this(
            pEntity.getId(),
            pEntity.getUUID(),
            pServerEntity.getPositionBase().x(),
            pServerEntity.getPositionBase().y(),
            pServerEntity.getPositionBase().z(),
            pServerEntity.getLastSentXRot(),
            pServerEntity.getLastSentYRot(),
            pEntity.getType(),
            pData,
            pServerEntity.getLastSentMovement(),
            (double)pServerEntity.getLastSentYHeadRot()
        );
    }

    public ClientboundAddEntityPacket(Entity pEntity, int pData, BlockPos pPos) {
        this(
            pEntity.getId(),
            pEntity.getUUID(),
            (double)pPos.getX(),
            (double)pPos.getY(),
            (double)pPos.getZ(),
            pEntity.getXRot(),
            pEntity.getYRot(),
            pEntity.getType(),
            pData,
            pEntity.getDeltaMovement(),
            (double)pEntity.getYHeadRot()
        );
    }

    public ClientboundAddEntityPacket(
        int pId,
        UUID pUuid,
        double pX,
        double pY,
        double pZ,
        float pXRot,
        float pYRot,
        EntityType<?> pType,
        int pData,
        Vec3 pDeltaMovement,
        double pYHeadRot
    ) {
        this.id = pId;
        this.uuid = pUuid;
        this.x = pX;
        this.y = pY;
        this.z = pZ;
        this.xRot = (byte)Mth.floor(pXRot * 256.0F / 360.0F);
        this.yRot = (byte)Mth.floor(pYRot * 256.0F / 360.0F);
        this.yHeadRot = (byte)Mth.floor(pYHeadRot * 256.0 / 360.0);
        this.type = pType;
        this.data = pData;
        this.xa = (int)(Mth.clamp(pDeltaMovement.x, -3.9, 3.9) * 8000.0);
        this.ya = (int)(Mth.clamp(pDeltaMovement.y, -3.9, 3.9) * 8000.0);
        this.za = (int)(Mth.clamp(pDeltaMovement.z, -3.9, 3.9) * 8000.0);
    }

    private ClientboundAddEntityPacket(RegistryFriendlyByteBuf p_327810_) {
        this.id = p_327810_.readVarInt();
        this.uuid = p_327810_.readUUID();
        this.type = ByteBufCodecs.registry(Registries.ENTITY_TYPE).decode(p_327810_);
        this.x = p_327810_.readDouble();
        this.y = p_327810_.readDouble();
        this.z = p_327810_.readDouble();
        this.xRot = p_327810_.readByte();
        this.yRot = p_327810_.readByte();
        this.yHeadRot = p_327810_.readByte();
        this.data = p_327810_.readVarInt();
        this.xa = p_327810_.readShort();
        this.ya = p_327810_.readShort();
        this.za = p_327810_.readShort();
    }

    private void write(RegistryFriendlyByteBuf p_332393_) {
        p_332393_.writeVarInt(this.id);
        p_332393_.writeUUID(this.uuid);
        ByteBufCodecs.registry(Registries.ENTITY_TYPE).encode(p_332393_, this.type);
        p_332393_.writeDouble(this.x);
        p_332393_.writeDouble(this.y);
        p_332393_.writeDouble(this.z);
        p_332393_.writeByte(this.xRot);
        p_332393_.writeByte(this.yRot);
        p_332393_.writeByte(this.yHeadRot);
        p_332393_.writeVarInt(this.data);
        p_332393_.writeShort(this.xa);
        p_332393_.writeShort(this.ya);
        p_332393_.writeShort(this.za);
    }

    @Override
    public PacketType<ClientboundAddEntityPacket> type() {
        return GamePacketTypes.CLIENTBOUND_ADD_ENTITY;
    }

    public void handle(ClientGamePacketListener pHandler) {
        pHandler.handleAddEntity(this);
    }

    public int getId() {
        return this.id;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public EntityType<?> getType() {
        return this.type;
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

    public double getXa() {
        return (double)this.xa / 8000.0;
    }

    public double getYa() {
        return (double)this.ya / 8000.0;
    }

    public double getZa() {
        return (double)this.za / 8000.0;
    }

    public float getXRot() {
        return (float)(this.xRot * 360) / 256.0F;
    }

    public float getYRot() {
        return (float)(this.yRot * 360) / 256.0F;
    }

    public float getYHeadRot() {
        return (float)(this.yHeadRot * 360) / 256.0F;
    }

    public int getData() {
        return this.data;
    }
}