package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ClientboundDamageEventPacket(int entityId, Holder<DamageType> sourceType, int sourceCauseId, int sourceDirectId, Optional<Vec3> sourcePosition)
    implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDamageEventPacket> STREAM_CODEC = Packet.codec(
        ClientboundDamageEventPacket::write, ClientboundDamageEventPacket::new
    );

    public ClientboundDamageEventPacket(Entity pEntity, DamageSource pDamageSource) {
        this(
            pEntity.getId(),
            pDamageSource.typeHolder(),
            pDamageSource.getEntity() != null ? pDamageSource.getEntity().getId() : -1,
            pDamageSource.getDirectEntity() != null ? pDamageSource.getDirectEntity().getId() : -1,
            Optional.ofNullable(pDamageSource.sourcePositionRaw())
        );
    }

    private ClientboundDamageEventPacket(RegistryFriendlyByteBuf p_328419_) {
        this(
            p_328419_.readVarInt(),
            DamageType.STREAM_CODEC.decode(p_328419_),
            readOptionalEntityId(p_328419_),
            readOptionalEntityId(p_328419_),
            p_328419_.readOptional(p_270813_ -> new Vec3(p_270813_.readDouble(), p_270813_.readDouble(), p_270813_.readDouble()))
        );
    }

    private static void writeOptionalEntityId(FriendlyByteBuf pBuffer, int pOptionalEntityId) {
        pBuffer.writeVarInt(pOptionalEntityId + 1);
    }

    private static int readOptionalEntityId(FriendlyByteBuf pBuffer) {
        return pBuffer.readVarInt() - 1;
    }

    private void write(RegistryFriendlyByteBuf p_330396_) {
        p_330396_.writeVarInt(this.entityId);
        DamageType.STREAM_CODEC.encode(p_330396_, this.sourceType);
        writeOptionalEntityId(p_330396_, this.sourceCauseId);
        writeOptionalEntityId(p_330396_, this.sourceDirectId);
        p_330396_.writeOptional(this.sourcePosition, (p_296394_, p_296395_) -> {
            p_296394_.writeDouble(p_296395_.x());
            p_296394_.writeDouble(p_296395_.y());
            p_296394_.writeDouble(p_296395_.z());
        });
    }

    @Override
    public PacketType<ClientboundDamageEventPacket> type() {
        return GamePacketTypes.CLIENTBOUND_DAMAGE_EVENT;
    }

    public void handle(ClientGamePacketListener pHandler) {
        pHandler.handleDamageEvent(this);
    }

    public DamageSource getSource(Level pLevel) {
        if (this.sourcePosition.isPresent()) {
            return new DamageSource(this.sourceType, this.sourcePosition.get());
        } else {
            Entity entity = pLevel.getEntity(this.sourceCauseId);
            Entity entity1 = pLevel.getEntity(this.sourceDirectId);
            return new DamageSource(this.sourceType, entity1, entity);
        }
    }
}