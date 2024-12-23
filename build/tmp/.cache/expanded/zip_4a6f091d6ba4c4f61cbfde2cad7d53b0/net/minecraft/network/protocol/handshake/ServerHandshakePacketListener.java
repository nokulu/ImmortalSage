package net.minecraft.network.protocol.handshake;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.game.ServerPacketListener;

/**
 * PacketListener for the server side of the HANDSHAKING protocol.
 */
public interface ServerHandshakePacketListener extends ServerPacketListener {
    @Override
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.HANDSHAKING;
    }

    void handleIntention(ClientIntentionPacket pPacket);
}