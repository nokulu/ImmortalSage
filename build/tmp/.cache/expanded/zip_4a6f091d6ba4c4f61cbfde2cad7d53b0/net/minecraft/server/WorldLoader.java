package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.WorldDataConfiguration;
import org.slf4j.Logger;

public class WorldLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <D, R> CompletableFuture<R> load(
        WorldLoader.InitConfig pInitConfig,
        WorldLoader.WorldDataSupplier<D> pWorldDataSupplier,
        WorldLoader.ResultFactory<D, R> pResultFactory,
        Executor pBackgroundExecutor,
        Executor pGameExecutor
    ) {
        try {
            Pair<WorldDataConfiguration, CloseableResourceManager> pair = pInitConfig.packConfig.createResourceManager();
            CloseableResourceManager closeableresourcemanager = pair.getSecond();
            LayeredRegistryAccess<RegistryLayer> layeredregistryaccess = RegistryLayer.createRegistryAccess();
            LayeredRegistryAccess<RegistryLayer> layeredregistryaccess1 = loadAndReplaceLayer(
                closeableresourcemanager, layeredregistryaccess, RegistryLayer.WORLDGEN, RegistryDataLoader.WORLDGEN_REGISTRIES
            );
            RegistryAccess.Frozen registryaccess$frozen = layeredregistryaccess1.getAccessForLoading(RegistryLayer.DIMENSIONS);
            RegistryAccess.Frozen registryaccess$frozen1 = RegistryDataLoader.load(
                closeableresourcemanager, registryaccess$frozen, RegistryDataLoader.DIMENSION_REGISTRIES
            );
            WorldDataConfiguration worlddataconfiguration = pair.getFirst();
            WorldLoader.DataLoadOutput<D> dataloadoutput = pWorldDataSupplier.get(
                new WorldLoader.DataLoadContext(closeableresourcemanager, worlddataconfiguration, registryaccess$frozen, registryaccess$frozen1)
            );
            LayeredRegistryAccess<RegistryLayer> layeredregistryaccess2 = layeredregistryaccess1.replaceFrom(RegistryLayer.DIMENSIONS, dataloadoutput.finalDimensions);
            return ReloadableServerResources.loadResources(
                    closeableresourcemanager,
                    layeredregistryaccess2,
                    worlddataconfiguration.enabledFeatures(),
                    pInitConfig.commandSelection(),
                    pInitConfig.functionCompilationLevel(),
                    pBackgroundExecutor,
                    pGameExecutor
                )
                .whenComplete((p_214370_, p_214371_) -> {
                    if (p_214371_ != null) {
                        closeableresourcemanager.close();
                    }
                })
                .thenApplyAsync(p_326210_ -> {
                    p_326210_.updateRegistryTags();
                    return pResultFactory.create(closeableresourcemanager, p_326210_, layeredregistryaccess2, dataloadoutput.cookie);
                }, pGameExecutor);
        } catch (Exception exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    private static RegistryAccess.Frozen loadLayer(
        ResourceManager pResourceManager, LayeredRegistryAccess<RegistryLayer> pRegistryAccess, RegistryLayer pRegistryLayer, List<RegistryDataLoader.RegistryData<?>> pRegistryData
    ) {
        RegistryAccess.Frozen registryaccess$frozen = pRegistryAccess.getAccessForLoading(pRegistryLayer);
        return RegistryDataLoader.load(pResourceManager, registryaccess$frozen, pRegistryData);
    }

    private static LayeredRegistryAccess<RegistryLayer> loadAndReplaceLayer(
        ResourceManager pResourceManager, LayeredRegistryAccess<RegistryLayer> pRegistryAccess, RegistryLayer pRegistryLayer, List<RegistryDataLoader.RegistryData<?>> pRegistryData
    ) {
        RegistryAccess.Frozen registryaccess$frozen = loadLayer(pResourceManager, pRegistryAccess, pRegistryLayer, pRegistryData);
        return pRegistryAccess.replaceFrom(pRegistryLayer, registryaccess$frozen);
    }

    public static record DataLoadContext(
        ResourceManager resources, WorldDataConfiguration dataConfiguration, RegistryAccess.Frozen datapackWorldgen, RegistryAccess.Frozen datapackDimensions
    ) {
    }

    public static record DataLoadOutput<D>(D cookie, RegistryAccess.Frozen finalDimensions) {
    }

    public static record InitConfig(WorldLoader.PackConfig packConfig, Commands.CommandSelection commandSelection, int functionCompilationLevel) {
    }

    public static record PackConfig(PackRepository packRepository, WorldDataConfiguration initialDataConfig, boolean safeMode, boolean initMode) {
        public Pair<WorldDataConfiguration, CloseableResourceManager> createResourceManager() {
            WorldDataConfiguration worlddataconfiguration = MinecraftServer.configurePackRepository(this.packRepository, this.initialDataConfig, this.initMode, this.safeMode);
            List<PackResources> list = this.packRepository.openAllSelected();
            CloseableResourceManager closeableresourcemanager = new MultiPackResourceManager(PackType.SERVER_DATA, list);
            return Pair.of(worlddataconfiguration, closeableresourcemanager);
        }
    }

    @FunctionalInterface
    public interface ResultFactory<D, R> {
        R create(CloseableResourceManager pManager, ReloadableServerResources pResources, LayeredRegistryAccess<RegistryLayer> pRegistryAccess, D pCookie);
    }

    @FunctionalInterface
    public interface WorldDataSupplier<D> {
        WorldLoader.DataLoadOutput<D> get(WorldLoader.DataLoadContext pContext);
    }
}