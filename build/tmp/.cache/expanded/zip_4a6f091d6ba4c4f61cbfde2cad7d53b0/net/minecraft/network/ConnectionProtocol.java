package net.minecraft.network;

/**
 * Describes the set of packets a connection understands at a given point.
 * A connection always starts out in state {@link #HANDSHAKING}. In this state the client sends its desired protocol
 * using
 * {@link ClientIntentionPacket}. The server then either accepts the connection and switches to the desired protocol or
 * it disconnects the client (for example in case of an outdated client).
 * 
 * Each protocol has a {@link PacketListener} implementation tied to it for server and client respectively.
 * 
 * Every packet must correspond to exactly one protocol.
 */
public enum ConnectionProtocol {
    HANDSHAKING("handshake"),
    PLAY("play"),
    STATUS("status"),
    LOGIN("login"),
    CONFIGURATION("configuration");

    private final String id;

    private ConnectionProtocol(final String pId) {
        this.id = pId;
    }

    public String id() {
        return this.id;
    }
}