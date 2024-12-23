package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.LivingEntity;

public record ClientboundHurtAnimationPacket(int id, float yaw) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundHurtAnimationPacket> STREAM_CODEC = Packet.codec(
        ClientboundHurtAnimationPacket::write, ClientboundHurtAnimationPacket::new
    );

    public ClientboundHurtAnimationPacket(LivingEntity pEntity) {
        this(pEntity.getId(), pEntity.getHurtDir());
    }

    private ClientboundHurtAnimationPacket(FriendlyByteBuf p_265181_) {
        this(p_265181_.readVarInt(), p_265181_.readFloat());
    }

    private void write(FriendlyByteBuf p_265156_) {
        p_265156_.writeVarInt(this.id);
        p_265156_.writeFloat(this.yaw);
    }

    @Override
    public PacketType<ClientboundHurtAnimationPacket> type() {
        return GamePacketTypes.CLIENTBOUND_HURT_ANIMATION;
    }

    public void handle(ClientGamePacketListener pHandler) {
        pHandler.handleHurtAnimation(this);
    }
}