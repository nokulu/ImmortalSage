package net.minecraft.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.server.MinecraftServer;

public class MemoryServerHandshakePacketListenerImpl implements ServerHandshakePacketListener {
    private final MinecraftServer server;
    private final Connection connection;

    public MemoryServerHandshakePacketListenerImpl(MinecraftServer pServer, Connection pConnection) {
        this.server = pServer;
        this.connection = pConnection;
    }

    @Override
    public void handleIntention(ClientIntentionPacket pPacket) {
        if (!net.minecraftforge.server.ServerLifecycleHooks.handleServerLogin(pPacket, this.connection)) return;
        if (pPacket.intention() != ClientIntent.LOGIN) {
            throw new UnsupportedOperationException("Invalid intention " + pPacket.intention());
        } else {
            this.connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, new ServerLoginPacketListenerImpl(this.server, this.connection, false));
            this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
        }
    }

    @Override
    public void onDisconnect(DisconnectionDetails pDetails) {
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }
}
