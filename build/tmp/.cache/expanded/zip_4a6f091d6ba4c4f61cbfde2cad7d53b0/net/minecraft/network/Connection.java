package net.minecraft.network;

import com.google.common.base.Suppliers;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.StatusProtocols;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.util.Mth;
import net.minecraft.util.debugchart.LocalSampleLogger;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Connection extends SimpleChannelInboundHandler<Packet<?>> {
    private static final float AVERAGE_PACKETS_SMOOTHING = 0.75F;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Marker ROOT_MARKER = MarkerFactory.getMarker("NETWORK");
    public static final Marker PACKET_MARKER = Util.make(MarkerFactory.getMarker("NETWORK_PACKETS"), p_202569_ -> p_202569_.add(ROOT_MARKER));
    public static final Marker PACKET_RECEIVED_MARKER = Util.make(MarkerFactory.getMarker("PACKET_RECEIVED"), p_202562_ -> p_202562_.add(PACKET_MARKER));
    public static final Marker PACKET_SENT_MARKER = Util.make(MarkerFactory.getMarker("PACKET_SENT"), p_202557_ -> p_202557_.add(PACKET_MARKER));
    public static final Supplier<NioEventLoopGroup> NETWORK_WORKER_GROUP = Suppliers.memoize(
        () -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build())
    );
    public static final Supplier<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = Suppliers.memoize(
        () -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build())
    );
    public static final Supplier<DefaultEventLoopGroup> LOCAL_WORKER_GROUP = Suppliers.memoize(
        () -> new DefaultEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Local Client IO #%d").setDaemon(true).build())
    );
    private static final ProtocolInfo<ServerHandshakePacketListener> INITIAL_PROTOCOL = HandshakeProtocols.SERVERBOUND;
    private final PacketFlow receiving;
    private volatile boolean sendLoginDisconnect = true;
    private final Queue<Consumer<Connection>> pendingActions = Queues.newConcurrentLinkedQueue();
    private Channel channel;
    private SocketAddress address;
    @Nullable
    private volatile PacketListener disconnectListener;
    @Nullable
    private volatile PacketListener packetListener;
    @Nullable
    private DisconnectionDetails disconnectionDetails;
    private boolean encrypted;
    private boolean disconnectionHandled;
    private int receivedPackets;
    private int sentPackets;
    private float averageReceivedPackets;
    private float averageSentPackets;
    private int tickCount;
    private boolean handlingFault;
    @Nullable
    private volatile DisconnectionDetails delayedDisconnect;
    @Nullable
    BandwidthDebugMonitor bandwidthDebugMonitor;
    private java.util.function.Consumer<Connection> activationHandler;
    private final net.minecraftforge.common.util.PacketLogger packetLogger = new net.minecraftforge.common.util.PacketLogger(this);
    private ProtocolInfo<?> outboundProtocol = null;
    private ProtocolInfo<?> inboundProtocol = null;

    public Connection(PacketFlow pReceiving) {
        this.receiving = pReceiving;
        if (this.receiving == PacketFlow.SERVERBOUND)
            this.inboundProtocol = INITIAL_PROTOCOL;
        else
            this.outboundProtocol = INITIAL_PROTOCOL;
    }

    @Override
    public void channelActive(ChannelHandlerContext pContext) throws Exception {
        super.channelActive(pContext);
        this.channel = pContext.channel();
        this.address = this.channel.remoteAddress();
        if (activationHandler != null) activationHandler.accept(this);
        if (this.delayedDisconnect != null) {
            this.disconnect(this.delayedDisconnect);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext pContext) {
        this.disconnect(Component.translatable("disconnect.endOfStream"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext pContext, Throwable pException) {
        if (pException instanceof SkipPacketException) {
            LOGGER.debug("Skipping packet due to errors", pException.getCause());
        } else {
            boolean flag = !this.handlingFault;
            this.handlingFault = true;
            if (this.channel.isOpen()) {
                if (pException instanceof TimeoutException) {
                    LOGGER.debug("Timeout", pException);
                    this.disconnect(Component.translatable("disconnect.timeout"));
                } else {
                    Component component = Component.translatable("disconnect.genericReason", "Internal Exception: " + pException);
                    PacketListener packetlistener = this.packetListener;
                    DisconnectionDetails disconnectiondetails;
                    if (packetlistener != null) {
                        disconnectiondetails = packetlistener.createDisconnectionInfo(component, pException);
                    } else {
                        disconnectiondetails = new DisconnectionDetails(component);
                    }

                    if (flag) {
                        LOGGER.debug("Failed to sent packet", pException);
                        if (this.getSending() == PacketFlow.CLIENTBOUND) {
                            Packet<?> packet = (Packet<?>)(this.sendLoginDisconnect
                                ? new ClientboundLoginDisconnectPacket(component)
                                : new ClientboundDisconnectPacket(component));
                            this.send(packet, PacketSendListener.thenRun(() -> this.disconnect(disconnectiondetails)));
                        } else {
                            this.disconnect(disconnectiondetails);
                        }

                        this.setReadOnly();
                    } else {
                        LOGGER.debug("Double fault", pException);
                        this.disconnect(disconnectiondetails);
                    }
                }
            }
        }
    }

    protected void channelRead0(ChannelHandlerContext pContext, Packet<?> pPacket) {
        if (this.channel.isOpen()) {
            PacketListener packetlistener = this.packetListener;
            if (packetlistener == null) {
                throw new IllegalStateException("Received a packet before the packet listener was initialized");
            } else {
                if (packetlistener.shouldHandleMessage(pPacket)) {
                    try {
                        packetLogger.recv(pPacket);
                        genericsFtw(pPacket, packetlistener);
                    } catch (RunningOnDifferentThreadException runningondifferentthreadexception) {
                    } catch (RejectedExecutionException rejectedexecutionexception) {
                        this.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
                    } catch (ClassCastException classcastexception) {
                        LOGGER.error("Received {} that couldn't be processed", pPacket.getClass(), classcastexception);
                        this.disconnect(Component.translatable("multiplayer.disconnect.invalid_packet"));
                    }

                    this.receivedPackets++;
                }
            }
        }
    }

    private static <T extends PacketListener> void genericsFtw(Packet<T> pPacket, PacketListener pListener) {
        pPacket.handle((T)pListener);
    }

    private void validateListener(ProtocolInfo<?> pProtocolInfo, PacketListener pPacketListener) {
        Validate.notNull(pPacketListener, "packetListener");
        PacketFlow packetflow = pPacketListener.flow();
        if (packetflow != this.receiving) {
            throw new IllegalStateException("Trying to set listener for wrong side: connection is " + this.receiving + ", but listener is " + packetflow);
        } else {
            ConnectionProtocol connectionprotocol = pPacketListener.protocol();
            if (pProtocolInfo.id() != connectionprotocol) {
                throw new IllegalStateException("Listener protocol (" + connectionprotocol + ") does not match requested one " + pProtocolInfo);
            }
        }
    }

    private static void syncAfterConfigurationChange(ChannelFuture pFuture) {
        try {
            pFuture.syncUninterruptibly();
        } catch (Exception exception) {
            if (exception instanceof ClosedChannelException) {
                LOGGER.info("Connection closed during protocol change");
            } else {
                throw exception;
            }
        }
    }

    public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> pProtocolInfo, T pPacketInfo) {
        this.validateListener(pProtocolInfo, pPacketInfo);
        if (pProtocolInfo.flow() != this.getReceiving()) {
            throw new IllegalStateException("Invalid inbound protocol: " + pProtocolInfo.id());
        } else {
            this.inboundProtocol = pProtocolInfo;
            this.packetListener = pPacketInfo;
            this.disconnectListener = null;
            UnconfiguredPipelineHandler.InboundConfigurationTask unconfiguredpipelinehandler$inboundconfigurationtask = UnconfiguredPipelineHandler.setupInboundProtocol(
                pProtocolInfo
            );
            BundlerInfo bundlerinfo = pProtocolInfo.bundlerInfo();
            if (bundlerinfo != null) {
                PacketBundlePacker packetbundlepacker = new PacketBundlePacker(bundlerinfo);
                unconfiguredpipelinehandler$inboundconfigurationtask = unconfiguredpipelinehandler$inboundconfigurationtask.andThen(
                    p_326046_ -> p_326046_.pipeline().addAfter("decoder", "bundler", packetbundlepacker)
                );
            }

            syncAfterConfigurationChange(this.channel.writeAndFlush(unconfiguredpipelinehandler$inboundconfigurationtask));
        }
    }

    public void setupOutboundProtocol(ProtocolInfo<?> pProtocolInfo) {
        if (pProtocolInfo.flow() != this.getSending()) {
            throw new IllegalStateException("Invalid outbound protocol: " + pProtocolInfo.id());
        } else {
            UnconfiguredPipelineHandler.OutboundConfigurationTask unconfiguredpipelinehandler$outboundconfigurationtask = UnconfiguredPipelineHandler.setupOutboundProtocol(
                pProtocolInfo
            ).andThen(
                f -> this.outboundProtocol = pProtocolInfo
            );
            BundlerInfo bundlerinfo = pProtocolInfo.bundlerInfo();
            if (bundlerinfo != null) {
                PacketBundleUnpacker packetbundleunpacker = new PacketBundleUnpacker(bundlerinfo);
                unconfiguredpipelinehandler$outboundconfigurationtask = unconfiguredpipelinehandler$outboundconfigurationtask.andThen(
                    p_326044_ -> p_326044_.pipeline().addAfter("encoder", "unbundler", packetbundleunpacker)
                );
            }

            boolean flag = pProtocolInfo.id() == ConnectionProtocol.LOGIN;
            syncAfterConfigurationChange(this.channel.writeAndFlush(unconfiguredpipelinehandler$outboundconfigurationtask.andThen(p_326048_ -> this.sendLoginDisconnect = flag)));
        }
    }

    public void setListenerForServerboundHandshake(PacketListener pPacketListener) {
        if (this.packetListener != null) {
            throw new IllegalStateException("Listener already set");
        } else if (this.receiving == PacketFlow.SERVERBOUND
            && pPacketListener.flow() == PacketFlow.SERVERBOUND
            && pPacketListener.protocol() == INITIAL_PROTOCOL.id()) {
            this.packetListener = pPacketListener;
        } else {
            throw new IllegalStateException("Invalid initial listener");
        }
    }

    public void initiateServerboundStatusConnection(String pHostName, int pPort, ClientStatusPacketListener pPacketListener) {
        this.initiateServerboundConnection(pHostName, pPort, StatusProtocols.SERVERBOUND, StatusProtocols.CLIENTBOUND, pPacketListener, ClientIntent.STATUS);
    }

    public void initiateServerboundPlayConnection(String pHostName, int pPort, ClientLoginPacketListener pPacketListener) {
        this.initiateServerboundConnection(pHostName, pPort, LoginProtocols.SERVERBOUND, LoginProtocols.CLIENTBOUND, pPacketListener, ClientIntent.LOGIN);
    }

    public <S extends ServerboundPacketListener, C extends ClientboundPacketListener> void initiateServerboundPlayConnection(
        String pHostName, int pPort, ProtocolInfo<S> pServerboundProtocol, ProtocolInfo<C> pClientbountProtocol, C pPacketListener, boolean pIsTransfer
    ) {
        this.initiateServerboundConnection(pHostName, pPort, pServerboundProtocol, pClientbountProtocol, pPacketListener, pIsTransfer ? ClientIntent.TRANSFER : ClientIntent.LOGIN);
    }

    private <S extends ServerboundPacketListener, C extends ClientboundPacketListener> void initiateServerboundConnection(
        String pHostName, int pPort, ProtocolInfo<S> pServerboundProtocol, ProtocolInfo<C> pClientboundProtocol, C pPacketListener, ClientIntent pIntention
    ) {
        if (pServerboundProtocol.id() != pClientboundProtocol.id()) {
            throw new IllegalStateException("Mismatched initial protocols");
        } else {
            this.disconnectListener = pPacketListener;
            this.runOnceConnected(p_326042_ -> {
                this.setupInboundProtocol(pClientboundProtocol, pPacketListener);
                // TODO: Change this to be a immediately sent login custom payload packet?
                p_326042_.sendPacket(new ClientIntentionPacket(SharedConstants.getCurrentVersion().getProtocolVersion(), net.minecraftforge.network.NetworkContext.enhanceHostName(pHostName), pPort, pIntention), null, true);
                this.setupOutboundProtocol(pServerboundProtocol);
            });
        }
    }

    public void send(Packet<?> pPacket) {
        this.send(pPacket, null);
    }

    public void send(Packet<?> pPacket, @Nullable PacketSendListener pSendListener) {
        this.send(pPacket, pSendListener, true);
    }

    public void send(Packet<?> pPacket, @Nullable PacketSendListener pListener, boolean pFlush) {
        if (this.isConnected()) {
            this.flushQueue();
            this.sendPacket(pPacket, pListener, pFlush);
        } else {
            this.pendingActions.add(p_296381_ -> p_296381_.sendPacket(pPacket, pListener, pFlush));
        }
    }

    public void runOnceConnected(Consumer<Connection> pAction) {
        if (this.isConnected()) {
            this.flushQueue();
            pAction.accept(this);
        } else {
            this.pendingActions.add(pAction);
        }
    }

    private void sendPacket(Packet<?> pPacket, @Nullable PacketSendListener pSendListener, boolean pFlush) {
        this.sentPackets++;
        if (this.channel.eventLoop().inEventLoop()) {
            this.doSendPacket(pPacket, pSendListener, pFlush);
        } else {
            this.channel.eventLoop().execute(() -> this.doSendPacket(pPacket, pSendListener, pFlush));
        }
    }

    private void doSendPacket(Packet<?> pPacket, @Nullable PacketSendListener pSendListener, boolean pFlush) {
        ChannelFuture channelfuture = pFlush ? this.channel.writeAndFlush(pPacket) : this.channel.write(pPacket);
        channelfuture.addListener(f -> packetLogger.send(pPacket));
        if (pSendListener != null) {
            channelfuture.addListener(p_243167_ -> {
                if (p_243167_.isSuccess()) {
                    pSendListener.onSuccess();
                } else {
                    Packet<?> packet = pSendListener.onFailure();
                    if (packet != null) {
                        ChannelFuture channelfuture1 = this.channel.writeAndFlush(packet);
                        channelfuture1.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                    }
                }
            });
        }

        channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void flushChannel() {
        if (this.isConnected()) {
            this.flush();
        } else {
            this.pendingActions.add(Connection::flush);
        }
    }

    private void flush() {
        if (this.channel.eventLoop().inEventLoop()) {
            this.channel.flush();
        } else {
            this.channel.eventLoop().execute(() -> this.channel.flush());
        }
    }

    private void flushQueue() {
        if (this.channel != null && this.channel.isOpen()) {
            synchronized (this.pendingActions) {
                Consumer<Connection> consumer;
                while ((consumer = this.pendingActions.poll()) != null) {
                    consumer.accept(this);
                }
            }
        }
    }

    public void tick() {
        this.flushQueue();
        if (this.packetListener instanceof TickablePacketListener tickablepacketlistener) {
            tickablepacketlistener.tick();
        }

        if (!this.isConnected() && !this.disconnectionHandled) {
            this.handleDisconnection();
        }

        if (this.channel != null) {
            this.channel.flush();
        }

        if (this.tickCount++ % 20 == 0) {
            this.tickSecond();
        }

        if (this.bandwidthDebugMonitor != null) {
            this.bandwidthDebugMonitor.tick();
        }
    }

    protected void tickSecond() {
        this.averageSentPackets = Mth.lerp(0.75F, (float)this.sentPackets, this.averageSentPackets);
        this.averageReceivedPackets = Mth.lerp(0.75F, (float)this.receivedPackets, this.averageReceivedPackets);
        this.sentPackets = 0;
        this.receivedPackets = 0;
    }

    public SocketAddress getRemoteAddress() {
        return this.address;
    }

    public String getLoggableAddress(boolean pLogIps) {
        if (this.address == null) {
            return "local";
        } else {
            return pLogIps ? net.minecraftforge.network.DualStackUtils.getAddressString(this.address) : "IP hidden";
        }
    }

    public void disconnect(Component pMessage) {
        this.disconnect(new DisconnectionDetails(pMessage));
    }

    public void disconnect(DisconnectionDetails pDisconnectionDetails) {
        if (this.channel == null) {
            this.delayedDisconnect = pDisconnectionDetails;
        }

        if (this.isConnected()) {
            this.channel.close().awaitUninterruptibly();
            this.disconnectionDetails = pDisconnectionDetails;
        }
    }

    public boolean isMemoryConnection() {
        return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
    }

    public PacketFlow getReceiving() {
        return this.receiving;
    }

    public PacketFlow getSending() {
        return this.receiving.getOpposite();
    }

    public static Connection connectToServer(InetSocketAddress pAddress, boolean pUseEpollIfAvailable, @Nullable LocalSampleLogger pSampleLogger) {
        Connection connection = new Connection(PacketFlow.CLIENTBOUND);
        if (pSampleLogger != null) {
            connection.setBandwidthLogger(pSampleLogger);
        }

        ChannelFuture channelfuture = connect(pAddress, pUseEpollIfAvailable, connection);
        channelfuture.syncUninterruptibly();
        return connection;
    }

    public static ChannelFuture connect(InetSocketAddress pAddress, boolean pUseEpollIfAvailable, final Connection pConnection) {
        net.minecraftforge.network.DualStackUtils.checkIPv6(pAddress.getAddress());
        pConnection.activationHandler = net.minecraftforge.network.NetworkRegistry::onConnectionStart;
        Class<? extends SocketChannel> oclass;
        EventLoopGroup eventloopgroup;
        if (Epoll.isAvailable() && pUseEpollIfAvailable) {
            oclass = EpollSocketChannel.class;
            eventloopgroup = NETWORK_EPOLL_WORKER_GROUP.get();
        } else {
            oclass = NioSocketChannel.class;
            eventloopgroup = NETWORK_WORKER_GROUP.get();
        }

        return new Bootstrap().group(eventloopgroup).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel p_129552_) {
                try {
                    p_129552_.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException channelexception) {
                }

                ChannelPipeline channelpipeline = p_129552_.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
                Connection.configureSerialization(channelpipeline, PacketFlow.CLIENTBOUND, false, pConnection.bandwidthDebugMonitor);
                pConnection.configurePacketHandler(channelpipeline);
            }
        }).channel(oclass).connect(pAddress.getAddress(), pAddress.getPort());
    }

    private static String outboundHandlerName(boolean pClientbound) {
        return pClientbound ? "encoder" : "outbound_config";
    }

    private static String inboundHandlerName(boolean pServerbound) {
        return pServerbound ? "decoder" : "inbound_config";
    }

    public void configurePacketHandler(ChannelPipeline pPipeline) {
        pPipeline.addLast("hackfix", new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext p_335545_, Object p_329198_, ChannelPromise p_332397_) throws Exception {
                super.write(p_335545_, p_329198_, p_332397_);
            }
        }).addLast("packet_handler", this);
    }

    public static void configureSerialization(ChannelPipeline pPipeline, PacketFlow pFlow, boolean pMemoryOnly, @Nullable BandwidthDebugMonitor pBandwithDebugMonitor) {
        PacketFlow packetflow = pFlow.getOpposite();
        boolean flag = pFlow == PacketFlow.SERVERBOUND;
        boolean flag1 = packetflow == PacketFlow.SERVERBOUND;
        pPipeline.addLast("splitter", createFrameDecoder(pBandwithDebugMonitor, pMemoryOnly))
            .addLast(new FlowControlHandler())
            .addLast(inboundHandlerName(flag), (ChannelHandler)(flag ? new PacketDecoder<>(INITIAL_PROTOCOL) : new UnconfiguredPipelineHandler.Inbound()))
            .addLast("prepender", createFrameEncoder(pMemoryOnly))
            .addLast(outboundHandlerName(flag1), (ChannelHandler)(flag1 ? new PacketEncoder<>(INITIAL_PROTOCOL) : new UnconfiguredPipelineHandler.Outbound()));
    }

    private static ChannelOutboundHandler createFrameEncoder(boolean pMemoryOnly) {
        return (ChannelOutboundHandler)(pMemoryOnly ? new NoOpFrameEncoder() : new Varint21LengthFieldPrepender());
    }

    private static ChannelInboundHandler createFrameDecoder(@Nullable BandwidthDebugMonitor pBandwithDebugMonitor, boolean pMemoryOnly) {
        if (!pMemoryOnly) {
            return new Varint21FrameDecoder(pBandwithDebugMonitor);
        } else {
            return (ChannelInboundHandler)(pBandwithDebugMonitor != null ? new MonitorFrameDecoder(pBandwithDebugMonitor) : new NoOpFrameDecoder());
        }
    }

    public static void configureInMemoryPipeline(ChannelPipeline pPipeline, PacketFlow pFlow) {
        configureSerialization(pPipeline, pFlow, true, null);
    }

    public static Connection connectToLocalServer(SocketAddress pAddress) {
        final Connection connection = new Connection(PacketFlow.CLIENTBOUND);
        connection.activationHandler = net.minecraftforge.network.NetworkRegistry::onConnectionStart;
        new Bootstrap().group(LOCAL_WORKER_GROUP.get()).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel p_332618_) {
                ChannelPipeline channelpipeline = p_332618_.pipeline();
                Connection.configureInMemoryPipeline(channelpipeline, PacketFlow.CLIENTBOUND);
                connection.configurePacketHandler(channelpipeline);
            }
        }).channel(LocalChannel.class).connect(pAddress).syncUninterruptibly();
        return connection;
    }

    public void setEncryptionKey(Cipher pDecryptingCipher, Cipher pEncryptingCipher) {
        this.encrypted = true;
        this.channel.pipeline().addBefore("splitter", "decrypt", new CipherDecoder(pDecryptingCipher));
        this.channel.pipeline().addBefore("prepender", "encrypt", new CipherEncoder(pEncryptingCipher));
    }

    public boolean isEncrypted() {
        return this.encrypted;
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen();
    }

    public boolean isConnecting() {
        return this.channel == null;
    }

    @Nullable
    public PacketListener getPacketListener() {
        return this.packetListener;
    }

    @Nullable
    public DisconnectionDetails getDisconnectionDetails() {
        return this.disconnectionDetails;
    }

    public void setReadOnly() {
        if (this.channel != null) {
            this.channel.config().setAutoRead(false);
        }
    }

    public void setupCompression(int pThreshold, boolean pValidateDecompressed) {
        if (pThreshold >= 0) {
            if (this.channel.pipeline().get("decompress") instanceof CompressionDecoder compressiondecoder) {
                compressiondecoder.setThreshold(pThreshold, pValidateDecompressed);
            } else {
                this.channel.pipeline().addAfter("splitter", "decompress", new CompressionDecoder(pThreshold, pValidateDecompressed));
            }

            if (this.channel.pipeline().get("compress") instanceof CompressionEncoder compressionencoder) {
                compressionencoder.setThreshold(pThreshold);
            } else {
                this.channel.pipeline().addAfter("prepender", "compress", new CompressionEncoder(pThreshold));
            }
        } else {
            if (this.channel.pipeline().get("decompress") instanceof CompressionDecoder) {
                this.channel.pipeline().remove("decompress");
            }

            if (this.channel.pipeline().get("compress") instanceof CompressionEncoder) {
                this.channel.pipeline().remove("compress");
            }
        }
    }

    public void handleDisconnection() {
        if (this.channel != null && !this.channel.isOpen()) {
            if (this.disconnectionHandled) {
                LOGGER.warn("handleDisconnection() called twice");
            } else {
                this.disconnectionHandled = true;
                PacketListener packetlistener = this.getPacketListener();
                PacketListener packetlistener1 = packetlistener != null ? packetlistener : this.disconnectListener;
                if (packetlistener1 != null) {
                    DisconnectionDetails disconnectiondetails = Objects.requireNonNullElseGet(
                        this.getDisconnectionDetails(), () -> new DisconnectionDetails(Component.translatable("multiplayer.disconnect.generic"))
                    );
                    packetlistener1.onDisconnect(disconnectiondetails);
                }
            }
        }
    }

    public float getAverageReceivedPackets() {
        return this.averageReceivedPackets;
    }

    public float getAverageSentPackets() {
        return this.averageSentPackets;
    }

    public Channel channel() {
        return this.channel;
    }

    public ConnectionProtocol getProtocol() {
        return outboundProtocol != null ? outboundProtocol.id() : inboundProtocol.id();
    }

    public ProtocolInfo<?> getInboundProtocolInfo() {
        return this.inboundProtocol;
    }

    public ProtocolInfo<?> getOutputboundProtocolInfo() {
        return this.outboundProtocol;
    }

    public void setBandwidthLogger(LocalSampleLogger pBandwithLogger) {
        this.bandwidthDebugMonitor = new BandwidthDebugMonitor(pBandwithLogger);
    }
}
