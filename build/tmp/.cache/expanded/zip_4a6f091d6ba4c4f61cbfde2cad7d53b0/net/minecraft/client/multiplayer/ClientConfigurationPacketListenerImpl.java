package net.minecraft.client.multiplayer;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundResetChatPacket;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientConfigurationPacketListenerImpl extends ClientCommonPacketListenerImpl implements ClientConfigurationPacketListener, TickablePacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GameProfile localGameProfile;
    private FeatureFlagSet enabledFeatures;
    private final RegistryAccess.Frozen receivedRegistries;
    private final RegistryDataCollector registryDataCollector = new RegistryDataCollector();
    @Nullable
    private KnownPacksManager knownPacks;
    @Nullable
    protected ChatComponent.State chatState;

    public ClientConfigurationPacketListenerImpl(Minecraft pMinecraft, Connection pConnection, CommonListenerCookie pCommonListenerCookie) {
        super(pMinecraft, pConnection, pCommonListenerCookie);
        this.localGameProfile = pCommonListenerCookie.localGameProfile();
        this.receivedRegistries = pCommonListenerCookie.receivedRegistries();
        this.enabledFeatures = pCommonListenerCookie.enabledFeatures();
        this.chatState = pCommonListenerCookie.chatState();
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    @Override
    protected void handleCustomPayload(CustomPacketPayload pPayload) {
        this.handleUnknownCustomPayload(pPayload);
    }

    private void handleUnknownCustomPayload(CustomPacketPayload pPayload) {
        LOGGER.warn("Unknown custom packet payload: {}", pPayload.type().id());
    }

    @Override
    public void handleRegistryData(ClientboundRegistryDataPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.registryDataCollector.appendContents(pPacket.registry(), pPacket.entries());
    }

    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.registryDataCollector.appendTags(pPacket.getTags());
    }

    @Override
    public void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket pPacket) {
        this.enabledFeatures = FeatureFlags.REGISTRY.fromNames(pPacket.features());
    }

    @Override
    public void handleSelectKnownPacks(ClientboundSelectKnownPacks pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        if (this.knownPacks == null) {
            this.knownPacks = new KnownPacksManager();
        }

        List<KnownPack> list = this.knownPacks.trySelectingPacks(pPacket.knownPacks());
        this.send(new ServerboundSelectKnownPacks(list));
    }

    @Override
    public void handleResetChat(ClientboundResetChatPacket pPacket) {
        this.chatState = null;
    }

    private <T> T runWithResources(Function<ResourceProvider, T> pResources) {
        if (this.knownPacks == null) {
            return pResources.apply(ResourceProvider.EMPTY);
        } else {
            Object object;
            try (CloseableResourceManager closeableresourcemanager = this.knownPacks.createResourceManager()) {
                object = pResources.apply(closeableresourcemanager);
            }

            return (T)object;
        }
    }

    @Override
    public void handleConfigurationFinished(ClientboundFinishConfigurationPacket pPacket) {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        RegistryAccess.Frozen registryaccess$frozen = this.runWithResources(
            p_325470_ -> this.registryDataCollector.collectGameRegistries(p_325470_, this.receivedRegistries, this.connection.isMemoryConnection())
        );
        this.connection
            .setupInboundProtocol(
                GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(registryaccess$frozen)),
                new ClientPacketListener(
                    this.minecraft,
                    this.connection,
                    new CommonListenerCookie(
                        this.localGameProfile,
                        this.telemetryManager,
                        registryaccess$frozen,
                        this.enabledFeatures,
                        this.serverBrand,
                        this.serverData,
                        this.postDisconnectScreen,
                        this.serverCookies,
                        this.chatState,
                        this.strictErrorHandling,
                        this.customReportDetails,
                        this.serverLinks
                    )
                )
            );
        this.connection.send(ServerboundFinishConfigurationPacket.INSTANCE);
        this.connection.setupOutboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(registryaccess$frozen)));
        net.minecraftforge.common.ForgeHooks.handleClientConfigurationComplete(this.connection);
    }

    @Override
    public void tick() {
        this.sendDeferredPackets();
    }

    @Override
    public void onDisconnect(DisconnectionDetails pDetails) {
        super.onDisconnect(pDetails);
        this.minecraft.clearDownloadedResourcePacks();
    }
}
