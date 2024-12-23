package net.minecraft.client.server;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.stats.Stats;
import net.minecraft.util.ModCheck;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class IntegratedServer extends MinecraftServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MIN_SIM_DISTANCE = 2;
    private final Minecraft minecraft;
    private boolean paused = true;
    private int publishedPort = -1;
    @Nullable
    private GameType publishedGameType;
    @Nullable
    private LanServerPinger lanPinger;
    @Nullable
    private UUID uuid;
    private int previousSimulationDistance = 0;

    public IntegratedServer(
        Thread pServerThread,
        Minecraft pMinecraft,
        LevelStorageSource.LevelStorageAccess pStorageSource,
        PackRepository pPackRepository,
        WorldStem pWorldStem,
        Services pServices,
        ChunkProgressListenerFactory pProgressListenerFactory
    ) {
        super(pServerThread, pStorageSource, pPackRepository, pWorldStem, pMinecraft.getProxy(), pMinecraft.getFixerUpper(), pServices, pProgressListenerFactory);
        this.setSingleplayerProfile(pMinecraft.getGameProfile());
        this.setDemo(pMinecraft.isDemo());
        this.setPlayerList(new IntegratedPlayerList(this, this.registries(), this.playerDataStorage));
        this.minecraft = pMinecraft;
    }

    @Override
    public boolean initServer() {
        LOGGER.info("Starting integrated minecraft server version {}", SharedConstants.getCurrentVersion().getName());
        this.setUsesAuthentication(true);
        this.setPvpAllowed(true);
        this.setFlightAllowed(true);
        this.initializeKeyPair();
        if (!net.minecraftforge.server.ServerLifecycleHooks.handleServerAboutToStart(this)) return false;
        this.loadLevel();
        GameProfile gameprofile = this.getSingleplayerProfile();
        String s = this.getWorldData().getLevelName();
        this.setMotd(gameprofile != null ? gameprofile.getName() + " - " + s : s);
        return net.minecraftforge.server.ServerLifecycleHooks.handleServerStarting(this);
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public void tickServer(BooleanSupplier pHasTimeLeft) {
        boolean flag = this.paused;
        this.paused = Minecraft.getInstance().isPaused();
        ProfilerFiller profilerfiller = this.getProfiler();
        if (!flag && this.paused) {
            profilerfiller.push("autoSave");
            LOGGER.info("Saving and pausing game...");
            this.saveEverything(false, false, false);
            profilerfiller.pop();
        }

        boolean flag1 = Minecraft.getInstance().getConnection() != null;
        if (flag1 && this.paused) {
            this.tickPaused();
        } else {
            if (flag && !this.paused) {
                this.forceTimeSynchronization();
            }

            super.tickServer(pHasTimeLeft);
            int i = Math.max(2, this.minecraft.options.renderDistance().get());
            if (i != this.getPlayerList().getViewDistance()) {
                LOGGER.info("Changing view distance to {}, from {}", i, this.getPlayerList().getViewDistance());
                this.getPlayerList().setViewDistance(i);
            }

            int j = Math.max(2, this.minecraft.options.simulationDistance().get());
            if (j != this.previousSimulationDistance) {
                LOGGER.info("Changing simulation distance to {}, from {}", j, this.previousSimulationDistance);
                this.getPlayerList().setSimulationDistance(j);
                this.previousSimulationDistance = j;
            }
        }
    }

    protected LocalSampleLogger getTickTimeLogger() {
        return this.minecraft.getDebugOverlay().getTickTimeLogger();
    }

    @Override
    public boolean isTickTimeLoggingEnabled() {
        return true;
    }

    private void tickPaused() {
        for (ServerPlayer serverplayer : this.getPlayerList().getPlayers()) {
            serverplayer.awardStat(Stats.TOTAL_WORLD_TIME);
        }
    }

    @Override
    public boolean shouldRconBroadcast() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return true;
    }

    @Override
    public Path getServerDirectory() {
        return this.minecraft.gameDirectory.toPath();
    }

    @Override
    public boolean isDedicatedServer() {
        return false;
    }

    @Override
    public int getRateLimitPacketsPerSecond() {
        return 0;
    }

    @Override
    public boolean isEpollEnabled() {
        return false;
    }

    @Override
    public void onServerCrash(CrashReport pReport) {
        this.minecraft.delayCrashRaw(pReport);
    }

    @Override
    public SystemReport fillServerSystemReport(SystemReport pReport) {
        pReport.setDetail("Type", "Integrated Server (map_client.txt)");
        pReport.setDetail("Is Modded", () -> this.getModdedStatus().fullDescription());
        pReport.setDetail("Launched Version", this.minecraft::getLaunchedVersion);
        return pReport;
    }

    @Override
    public ModCheck getModdedStatus() {
        return Minecraft.checkModStatus().merge(super.getModdedStatus());
    }

    @Override
    public boolean publishServer(@Nullable GameType pGameMode, boolean pCheats, int pPort) {
        try {
            this.minecraft.prepareForMultiplayer();
            this.minecraft.getProfileKeyPairManager().prepareKeyPair().thenAcceptAsync(p_263550_ -> p_263550_.ifPresent(p_263549_ -> {
                    ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
                    if (clientpacketlistener != null) {
                        clientpacketlistener.setKeyPair(p_263549_);
                    }
                }), this.minecraft);
            this.getConnection().startTcpServerListener(null, pPort);
            LOGGER.info("Started serving on {}", pPort);
            this.publishedPort = pPort;
            this.lanPinger = new LanServerPinger(this.getMotd(), pPort + "");
            this.lanPinger.start();
            this.publishedGameType = pGameMode;
            this.getPlayerList().setAllowCommandsForAllPlayers(pCheats);
            int i = this.getProfilePermissions(this.minecraft.player.getGameProfile());
            this.minecraft.player.setPermissionLevel(i);

            for (ServerPlayer serverplayer : this.getPlayerList().getPlayers()) {
                this.getCommands().sendCommands(serverplayer);
            }

            return true;
        } catch (IOException ioexception) {
            return false;
        }
    }

    @Override
    public void stopServer() {
        super.stopServer();
        if (this.lanPinger != null) {
            this.lanPinger.interrupt();
            this.lanPinger = null;
        }
    }

    @Override
    public void halt(boolean pWaitForServer) {
        if (isRunning())
        this.executeBlocking(() -> {
            for (ServerPlayer serverplayer : Lists.newArrayList(this.getPlayerList().getPlayers())) {
                if (!serverplayer.getUUID().equals(this.uuid)) {
                    this.getPlayerList().remove(serverplayer);
                }
            }
        });
        super.halt(pWaitForServer);
        if (this.lanPinger != null) {
            this.lanPinger.interrupt();
            this.lanPinger = null;
        }
    }

    @Override
    public boolean isPublished() {
        return this.publishedPort > -1;
    }

    @Override
    public int getPort() {
        return this.publishedPort;
    }

    @Override
    public void setDefaultGameType(GameType pGameMode) {
        super.setDefaultGameType(pGameMode);
        this.publishedGameType = null;
    }

    @Override
    public boolean isCommandBlockEnabled() {
        return true;
    }

    @Override
    public int getOperatorUserPermissionLevel() {
        return 2;
    }

    @Override
    public int getFunctionCompilationLevel() {
        return 2;
    }

    public void setUUID(UUID pUuid) {
        this.uuid = pUuid;
    }

    @Override
    public boolean isSingleplayerOwner(GameProfile pProfile) {
        return this.getSingleplayerProfile() != null && pProfile.getName().equalsIgnoreCase(this.getSingleplayerProfile().getName());
    }

    @Override
    public int getScaledTrackingDistance(int pTrackingDistance) {
        return (int)(this.minecraft.options.entityDistanceScaling().get() * (double)pTrackingDistance);
    }

    @Override
    public boolean forceSynchronousWrites() {
        return this.minecraft.options.syncWrites;
    }

    @Nullable
    @Override
    public GameType getForcedGameType() {
        return this.isPublished() ? MoreObjects.firstNonNull(this.publishedGameType, this.worldData.getGameType()) : null;
    }

    @Override
    public boolean saveEverything(boolean pSuppressLog, boolean pFlush, boolean pForced) {
        boolean flag = super.saveEverything(pSuppressLog, pFlush, pForced);
        this.warnOnLowDiskSpace();
        return flag;
    }

    private void warnOnLowDiskSpace() {
        if (this.storageSource.checkForLowDiskSpace()) {
            this.minecraft.execute(() -> SystemToast.onLowDiskSpace(this.minecraft));
        }
    }

    @Override
    public void reportChunkLoadFailure(Throwable pThrowable, RegionStorageInfo pRegionStorageInfo, ChunkPos pChunkPos) {
        super.reportChunkLoadFailure(pThrowable, pRegionStorageInfo, pChunkPos);
        this.warnOnLowDiskSpace();
        this.minecraft.execute(() -> SystemToast.onChunkLoadFailure(this.minecraft, pChunkPos));
    }

    @Override
    public void reportChunkSaveFailure(Throwable pThrowable, RegionStorageInfo pRegionStorageInfo, ChunkPos pChunkPos) {
        super.reportChunkSaveFailure(pThrowable, pRegionStorageInfo, pChunkPos);
        this.warnOnLowDiskSpace();
        this.minecraft.execute(() -> SystemToast.onChunkSaveFailure(this.minecraft, pChunkPos));
    }
}
