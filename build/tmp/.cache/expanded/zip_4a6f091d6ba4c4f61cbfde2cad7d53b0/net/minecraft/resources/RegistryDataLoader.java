package net.minecraft.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.slf4j.Logger;

public class RegistryDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RegistrationInfo NETWORK_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());
    private static final Function<Optional<KnownPack>, RegistrationInfo> REGISTRATION_INFO_CACHE = Util.memoize(p_326161_ -> {
        Lifecycle lifecycle = p_326161_.map(KnownPack::isVanilla).map(p_326166_ -> Lifecycle.stable()).orElse(Lifecycle.experimental());
        return new RegistrationInfo(p_326161_, lifecycle);
    });
    public static final List<RegistryDataLoader.RegistryData<?>> WORLDGEN_REGISTRIES = net.minecraftforge.registries.DataPackRegistriesHooks.grabWorldgenRegistries(
        new RegistryDataLoader.RegistryData<>(Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.BIOME, Biome.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.CHAT_TYPE, ChatType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.CONFIGURED_CARVER, ConfiguredWorldCarver.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.CONFIGURED_FEATURE, ConfiguredFeature.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.PLACED_FEATURE, PlacedFeature.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.STRUCTURE, Structure.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.PROCESSOR_LIST, StructureProcessorType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.TEMPLATE_POOL, StructureTemplatePool.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.NOISE_SETTINGS, NoiseGeneratorSettings.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.NOISE, NormalNoise.NoiseParameters.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.DENSITY_FUNCTION, DensityFunction.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.WORLD_PRESET, WorldPreset.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPreset.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.WOLF_VARIANT, WolfVariant.DIRECT_CODEC, true),
        new RegistryDataLoader.RegistryData<>(Registries.PAINTING_VARIANT, PaintingVariant.DIRECT_CODEC, true),
        new RegistryDataLoader.RegistryData<>(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterList.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.BANNER_PATTERN, BannerPattern.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.ENCHANTMENT, Enchantment.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.ENCHANTMENT_PROVIDER, EnchantmentProvider.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.JUKEBOX_SONG, JukeboxSong.DIRECT_CODEC)
    );
    public static final List<RegistryDataLoader.RegistryData<?>> DIMENSION_REGISTRIES = List.of(
        new RegistryDataLoader.RegistryData<>(Registries.LEVEL_STEM, LevelStem.CODEC)
    );
    public static final List<RegistryDataLoader.RegistryData<?>> SYNCHRONIZED_REGISTRIES = net.minecraftforge.registries.DataPackRegistriesHooks.grabSynchronizedRegistries(
        new RegistryDataLoader.RegistryData<>(Registries.BIOME, Biome.NETWORK_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.CHAT_TYPE, ChatType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.WOLF_VARIANT, WolfVariant.DIRECT_CODEC, true),
        new RegistryDataLoader.RegistryData<>(Registries.PAINTING_VARIANT, PaintingVariant.DIRECT_CODEC, true),
        new RegistryDataLoader.RegistryData<>(Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.BANNER_PATTERN, BannerPattern.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.ENCHANTMENT, Enchantment.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.JUKEBOX_SONG, JukeboxSong.DIRECT_CODEC)
    );

    public static java.util.stream.Stream<RegistryDataLoader.RegistryData<?>> getWorldGenAndDimensionStream() {
        return java.util.stream.Stream.concat(RegistryDataLoader.WORLDGEN_REGISTRIES.stream(), RegistryDataLoader.DIMENSION_REGISTRIES.stream());
    }

    public static RegistryAccess.Frozen load(ResourceManager pResourceManager, RegistryAccess pRegistryAccess, List<RegistryDataLoader.RegistryData<?>> pRegistryData) {
        return load((p_326156_, p_326157_) -> p_326156_.loadFromResources(pResourceManager, p_326157_), pRegistryAccess, pRegistryData);
    }

    public static RegistryAccess.Frozen load(
        Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> pElements,
        ResourceProvider pResourceProvider,
        RegistryAccess pRegistryAccess,
        List<RegistryDataLoader.RegistryData<?>> pRegistryData
    ) {
        return load((p_326153_, p_326154_) -> p_326153_.loadFromNetwork(pElements, pResourceProvider, p_326154_), pRegistryAccess, pRegistryData);
    }

    private static RegistryAccess.Frozen load(
        RegistryDataLoader.LoadingFunction pLoadingFunction, RegistryAccess pRegistryAccess, List<RegistryDataLoader.RegistryData<?>> pRegistryData
    ) {
        Map<ResourceKey<?>, Exception> map = new HashMap<>();
        List<RegistryDataLoader.Loader<?>> list = pRegistryData.stream()
            .map(p_326168_ -> p_326168_.create(Lifecycle.stable(), map))
            .collect(Collectors.toUnmodifiableList());
        RegistryOps.RegistryInfoLookup registryops$registryinfolookup = createContext(pRegistryAccess, list);
        list.forEach(p_326160_ -> pLoadingFunction.apply((RegistryDataLoader.Loader<?>)p_326160_, registryops$registryinfolookup));
        list.forEach(p_341109_ -> {
            Registry<?> registry = p_341109_.registry();

            try {
                registry.freeze();
            } catch (Exception exception) {
                map.put(registry.key(), exception);
            }

            if (p_341109_.data.requiredNonEmpty && registry.size() == 0) {
                map.put(registry.key(), new IllegalStateException("Registry must be non-empty"));
            }
        });
        if (!map.isEmpty()) {
            logErrors(map);
            var buf = new StringBuilder("Failed to load registries, see debug.log for more details:");
            map.forEach((k,v) -> buf.append("\n\t").append(k.toString()).append(": ").append(v.getMessage()));
            throw new IllegalStateException(buf.toString());
        } else {
            return new RegistryAccess.ImmutableRegistryAccess(list.stream().map(RegistryDataLoader.Loader::registry).toList()).freeze();
        }
    }

    private static RegistryOps.RegistryInfoLookup createContext(RegistryAccess pRegistryAccess, List<RegistryDataLoader.Loader<?>> pRegistryLoaders) {
        final Map<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> map = new HashMap<>();
        pRegistryAccess.registries().forEach(p_255505_ -> map.put(p_255505_.key(), createInfoForContextRegistry(p_255505_.value())));
        pRegistryLoaders.forEach(p_341107_ -> map.put(p_341107_.registry.key(), createInfoForNewRegistry(p_341107_.registry)));
        return new RegistryOps.RegistryInfoLookup() {
            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> p_256014_) {
                return Optional.ofNullable((RegistryOps.RegistryInfo<T>)map.get(p_256014_));
            }
        };
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForNewRegistry(WritableRegistry<T> pRegistry) {
        return new RegistryOps.RegistryInfo<>(pRegistry.asLookup(), pRegistry.createRegistrationLookup(), pRegistry.registryLifecycle());
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(Registry<T> pRegistry) {
        return new RegistryOps.RegistryInfo<>(pRegistry.asLookup(), pRegistry.asTagAddingLookup(), pRegistry.registryLifecycle());
    }

    private static void logErrors(Map<ResourceKey<?>, Exception> pErrors) {
        StringWriter stringwriter = new StringWriter();
        PrintWriter printwriter = new PrintWriter(stringwriter);
        Map<ResourceLocation, Map<ResourceLocation, Exception>> map = pErrors.entrySet()
            .stream()
            .collect(
                Collectors.groupingBy(
                    p_249353_ -> p_249353_.getKey().registry(), Collectors.toMap(p_251444_ -> p_251444_.getKey().location(), Entry::getValue)
                )
            );
        map.entrySet().stream().sorted(Entry.comparingByKey()).forEach(p_249838_ -> {
            printwriter.printf("> Errors in registry %s:%n", p_249838_.getKey());
            p_249838_.getValue().entrySet().stream().sorted(Entry.comparingByKey()).forEach(p_250688_ -> {
                printwriter.printf(">> Errors in element %s:%n", p_250688_.getKey());
                p_250688_.getValue().printStackTrace(printwriter);
            });
        });
        printwriter.flush();
        LOGGER.error("Registry loading errors:\n{}", stringwriter);
    }

    private static <E> void loadElementFromResource(
        WritableRegistry<E> pRegistry,
        Decoder<Optional<E>> pCodec,
        RegistryOps<JsonElement> pOps,
        ResourceKey<E> pResourceKey,
        Resource pResource,
        RegistrationInfo pRegistrationInfo
    ) throws IOException {
        try (Reader reader = pResource.openAsReader()) {
            JsonElement jsonelement = JsonParser.parseReader(reader);

            var result = pCodec.decode(pOps, jsonelement);

            if (result.isError()) {
                LOGGER.error("Couldn't parse data file {}: {}", pResourceKey, result.error().get().message());
                throw new IllegalStateException("Couldn't parse data file " + pResourceKey + ": " + result.error().get().message());
            }

            var mappedResult = result.result().map(a -> a.getFirst());

            if (mappedResult.get().isEmpty()) {
                LOGGER.debug("Skipping {} conditions not met", pResourceKey);
                return;
            }

            DataResult<E> dataresult = result.map(p -> p.mapFirst(Optional::get)).map(p -> p.getFirst());

            E e = dataresult.getOrThrow();
            pRegistry.register(pResourceKey, e, pRegistrationInfo);
        }
    }

    static <E> void loadContentsFromManager(
        ResourceManager pResourceManager,
        RegistryOps.RegistryInfoLookup pRegistryInfoLookup,
        WritableRegistry<E> pRegistry,
        Decoder<E> pCodec,
        Map<ResourceKey<?>, Exception> pLoadingErrors
    ) {
        String s = Registries.elementsDirPath(pRegistry.key());
        FileToIdConverter filetoidconverter = FileToIdConverter.json(s);
        RegistryOps<JsonElement> registryops = RegistryOps.create(JsonOps.INSTANCE, pRegistryInfoLookup);
        Decoder<Optional<E>> conditional = net.minecraftforge.common.crafting.conditions.ConditionCodec.wrap(pCodec);

        for (Entry<ResourceLocation, Resource> entry : filetoidconverter.listMatchingResources(pResourceManager).entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            ResourceKey<E> resourcekey = ResourceKey.create(pRegistry.key(), filetoidconverter.fileToId(resourcelocation));
            Resource resource = entry.getValue();
            RegistrationInfo registrationinfo = REGISTRATION_INFO_CACHE.apply(resource.knownPackInfo());

            try {
                loadElementFromResource(pRegistry, conditional, registryops, resourcekey, resource, registrationinfo);
            } catch (Exception exception) {
                pLoadingErrors.put(
                    resourcekey,
                    new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", resourcelocation, resource.sourcePackId()), exception)
                );
            }
        }
    }

    static <E> void loadContentsFromNetwork(
        Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> pElements,
        ResourceProvider pResourceProvider,
        RegistryOps.RegistryInfoLookup pRegistryInfoLookup,
        WritableRegistry<E> pRegistry,
        Decoder<E> pCodec,
        Map<ResourceKey<?>, Exception> pLoadingErrors
    ) {
        List<RegistrySynchronization.PackedRegistryEntry> list = pElements.get(pRegistry.key());
        if (list != null) {
            RegistryOps<Tag> registryops = RegistryOps.create(NbtOps.INSTANCE, pRegistryInfoLookup);
            RegistryOps<JsonElement> registryops1 = RegistryOps.create(JsonOps.INSTANCE, pRegistryInfoLookup)
                .withContext(net.minecraftforge.common.crafting.conditions.ICondition.IContext.KEY, net.minecraftforge.common.crafting.conditions.ICondition.IContext.TAGS_INVALID);
            Decoder<Optional<E>> conditional = net.minecraftforge.common.crafting.conditions.ConditionCodec.wrap(pCodec);
            String s = Registries.elementsDirPath(pRegistry.key());
            FileToIdConverter filetoidconverter = FileToIdConverter.json(s);

            for (RegistrySynchronization.PackedRegistryEntry registrysynchronization$packedregistryentry : list) {
                ResourceKey<E> resourcekey = ResourceKey.create(pRegistry.key(), registrysynchronization$packedregistryentry.id());
                Optional<Tag> optional = registrysynchronization$packedregistryentry.data();
                if (optional.isPresent()) {
                    try {
                        DataResult<E> dataresult = pCodec.parse(registryops, optional.get());
                        E e = dataresult.getOrThrow();
                        pRegistry.register(resourcekey, e, NETWORK_REGISTRATION_INFO);
                    } catch (Exception exception) {
                        pLoadingErrors.put(
                            resourcekey,
                            new IllegalStateException(String.format(Locale.ROOT, "Failed to parse value %s from server", optional.get()), exception)
                        );
                    }
                } else {
                    ResourceLocation resourcelocation = filetoidconverter.idToFile(registrysynchronization$packedregistryentry.id());

                    try {
                        Resource resource = pResourceProvider.getResourceOrThrow(resourcelocation);
                        loadElementFromResource(pRegistry, conditional, registryops1, resourcekey, resource, NETWORK_REGISTRATION_INFO);
                    } catch (Exception exception1) {
                        pLoadingErrors.put(resourcekey, new IllegalStateException("Failed to parse local data", exception1));
                    }
                }
            }
        }
    }

    static record Loader<T>(RegistryDataLoader.RegistryData<T> data, WritableRegistry<T> registry, Map<ResourceKey<?>, Exception> loadingErrors) {
        public void loadFromResources(ResourceManager pResouceManager, RegistryOps.RegistryInfoLookup pRegistryInfoLookup) {
            RegistryDataLoader.loadContentsFromManager(pResouceManager, pRegistryInfoLookup, this.registry, this.data.elementCodec, this.loadingErrors);
        }

        public void loadFromNetwork(
            Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> pElements,
            ResourceProvider pResourceProvider,
            RegistryOps.RegistryInfoLookup pRegistryInfoLookup
        ) {
            RegistryDataLoader.loadContentsFromNetwork(pElements, pResourceProvider, pRegistryInfoLookup, this.registry, this.data.elementCodec, this.loadingErrors);
        }
    }

    @FunctionalInterface
    interface LoadingFunction {
        void apply(RegistryDataLoader.Loader<?> pLoader, RegistryOps.RegistryInfoLookup pRegistryInfoLookup);
    }

    public static record RegistryData<T>(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, boolean requiredNonEmpty) {
        RegistryData(ResourceKey<? extends Registry<T>> pKey, Codec<T> pElementCodec) {
            this(pKey, pElementCodec, false);
        }

        RegistryDataLoader.Loader<T> create(Lifecycle pRegistryLifecycle, Map<ResourceKey<?>, Exception> pLoadingErrors) {
            WritableRegistry<T> writableregistry = new MappedRegistry<>(this.key, pRegistryLifecycle);
            return new RegistryDataLoader.Loader<>(this, writableregistry, pLoadingErrors);
        }

        public void runWithArguments(BiConsumer<ResourceKey<? extends Registry<T>>, Codec<T>> pRunner) {
            pRunner.accept(this.key, this.elementCodec);
        }
    }
}