package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundSetCarriedItemPacket implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetCarriedItemPacket> STREAM_CODEC = Packet.codec(
        ServerboundSetCarriedItemPacket::write, ServerboundSetCarriedItemPacket::new
    );
    private final int slot;

    public ServerboundSetCarriedItemPacket(int pSlot) {
        this.slot = pSlot;
    }

    private ServerboundSetCarriedItemPacket(FriendlyByteBuf p_179751_) {
        this.slot = p_179751_.readShort();
    }

    private void write(FriendlyByteBuf p_134500_) {
        p_134500_.writeShort(this.slot);
    }

    @Override
    public PacketType<ServerboundSetCarriedItemPacket> type() {
        return GamePacketTypes.SERVERBOUND_SET_CARRIED_ITEM;
    }

    public void handle(ServerGamePacketListener pHandler) {
        pHandler.handleSetCarriedItem(this);
    }

    public int getSlot() {
        return this.slot;
    }
}