package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;

public abstract class TagsProvider<T> implements DataProvider {
    protected final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final CompletableFuture<Void> contentsDone = new CompletableFuture<>();
    private final CompletableFuture<TagsProvider.TagLookup<T>> parentProvider;
    protected final ResourceKey<? extends Registry<T>> registryKey;
    protected final Map<ResourceLocation, TagBuilder> builders = Maps.newLinkedHashMap();
    protected final String modId;
    @org.jetbrains.annotations.Nullable
    protected final net.minecraftforge.common.data.ExistingFileHelper existingFileHelper;
    private final net.minecraftforge.common.data.ExistingFileHelper.IResourceType resourceType;
    private final net.minecraftforge.common.data.ExistingFileHelper.IResourceType elementResourceType; // FORGE: Resource type for validating required references to datapack registry elements.

    /**
     * @deprecated Forge: Use the {@linkplain #TagsProvider(PackOutput, ResourceKey, CompletableFuture, String, net.minecraftforge.common.data.ExistingFileHelper) mod id variant}
     */
    protected TagsProvider(PackOutput pOutput, ResourceKey<? extends Registry<T>> pRegistryKey, CompletableFuture<HolderLookup.Provider> pLookupProvider) {
        this(pOutput, pRegistryKey, pLookupProvider, "vanilla", null);
    }

    protected TagsProvider(PackOutput pOutput, ResourceKey<? extends Registry<T>> pRegistryKey, CompletableFuture<HolderLookup.Provider> pLookupProvider, String modId, @org.jetbrains.annotations.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
       this(pOutput, pRegistryKey, pLookupProvider, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()), modId, existingFileHelper);
    }

    /**
     * @deprecated Forge: Use the {@linkplain #TagsProvider(PackOutput, ResourceKey, CompletableFuture, CompletableFuture, String, net.minecraftforge.common.data.ExistingFileHelper) mod id variant}
     */
    protected TagsProvider(PackOutput pOutput, ResourceKey<? extends Registry<T>> pRegistryKey, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagsProvider.TagLookup<T>> pParentProvider) {
        this(pOutput, pRegistryKey, pLookupProvider, pParentProvider, "vanilla", null);
    }

    protected TagsProvider(
        PackOutput pOutput,
        ResourceKey<? extends Registry<T>> pRegistryKey,
        CompletableFuture<HolderLookup.Provider> pLookupProvider,
        CompletableFuture<TagsProvider.TagLookup<T>> pParentProvider,
        String modId,
        @org.jetbrains.annotations.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper
    ) {
        this.pathProvider = pOutput.createRegistryTagsPathProvider(pRegistryKey);
        this.registryKey = pRegistryKey;
        this.parentProvider = pParentProvider;
        this.lookupProvider = pLookupProvider;
        this.modId = modId;
        this.existingFileHelper = existingFileHelper;
        this.resourceType = new net.minecraftforge.common.data.ExistingFileHelper.ResourceType(net.minecraft.server.packs.PackType.SERVER_DATA, ".json", net.minecraft.core.registries.Registries.tagsDirPath(pRegistryKey));
        this.elementResourceType = new net.minecraftforge.common.data.ExistingFileHelper.ResourceType(net.minecraft.server.packs.PackType.SERVER_DATA, ".json", net.minecraft.core.registries.Registries.elementsDirPath(pRegistryKey));
    }

    // Forge: Allow customizing the path for a given tag or returning null
    @org.jetbrains.annotations.Nullable
    protected Path getPath(ResourceLocation id) {
        return this.pathProvider.json(id);
    }

    @Override
    public String getName() {
        return "Tags for " + this.registryKey.location() + " mod id " + this.modId;
    }

    protected abstract void addTags(HolderLookup.Provider pProvider);

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        record CombinedData<T>(HolderLookup.Provider contents, TagsProvider.TagLookup<T> parent) {
        }

        return this.createContentsProvider()
            .thenApply(p_275895_ -> {
                this.contentsDone.complete(null);
                return (HolderLookup.Provider)p_275895_;
            })
            .thenCombineAsync(this.parentProvider, (p_274778_, p_274779_) -> new CombinedData<>(p_274778_, (TagsProvider.TagLookup<T>)p_274779_), Util.backgroundExecutor())
            .thenCompose(
                p_325926_ -> {
                    HolderLookup.RegistryLookup<T> registrylookup = p_325926_.contents.lookup(this.registryKey).orElseThrow(() -> {
                       // FORGE: Throw a more descriptive error message if this is a Forge registry without tags enabled
                       if (net.minecraftforge.registries.RegistryManager.ACTIVE.getRegistry(this.registryKey) != null) {
                          return new IllegalStateException("Forge registry " + this.registryKey.location() + " does not have support for tags");
                       }
                       return new IllegalStateException("Registry " + this.registryKey.location() + " not found");
                    });
                    Predicate<ResourceLocation> predicate = p_255496_ -> registrylookup.get(ResourceKey.create(this.registryKey, p_255496_)).isPresent();
                    Predicate<ResourceLocation> predicate1 = p_274776_ -> this.builders.containsKey(p_274776_)
                            || p_325926_.parent.contains(TagKey.create(this.registryKey, p_274776_));
                    return CompletableFuture.allOf(
                        this.builders
                            .entrySet()
                            .stream()
                            .map(
                                p_325931_ -> {
                                    ResourceLocation resourcelocation = p_325931_.getKey();
                                    TagBuilder tagbuilder = p_325931_.getValue();
                                    List<TagEntry> list = tagbuilder.build();
                                    List<TagEntry> list1 = java.util.stream.Stream.concat(list.stream(), tagbuilder.getRemoveEntries()).filter(p_274771_ -> !p_274771_.verifyIfPresent(predicate, predicate1)).filter(this::missing).toList();
                                    if (!list1.isEmpty()) {
                                        throw new IllegalArgumentException(
                                            String.format(
                                                Locale.ROOT,
                                                "Couldn't define tag %s as it is missing following references: %s",
                                                resourcelocation,
                                                list1.stream().map(Objects::toString).collect(Collectors.joining(","))
                                            )
                                        );
                                    } else {
                                        Path path = this.getPath(resourcelocation);
                                        if (path == null) {
                                            return CompletableFuture.completedFuture(null); // Forge: Allow running this data provider without writing it. Recipe provider needs valid tags.
                                        }
                                        return DataProvider.saveStable(pOutput, p_325926_.contents, TagFile.CODEC, new TagFile(list, tagbuilder.isReplace(), tagbuilder.getRemoveEntries().toList()), path);
                                    }
                                }
                            )
                            .toArray(CompletableFuture[]::new)
                    );
                }
            );
    }

    protected TagsProvider.TagAppender<T> tag(TagKey<T> pTag) {
        TagBuilder tagbuilder = this.getOrCreateRawBuilder(pTag);
        return new TagsProvider.TagAppender<>(tagbuilder, modId);
    }

    protected TagBuilder getOrCreateRawBuilder(TagKey<T> pTag) {
        return this.builders.computeIfAbsent(pTag.location(), p_236442_ -> {
            if (existingFileHelper != null) {
                existingFileHelper.trackGenerated(p_236442_, resourceType);
            }
            return TagBuilder.create();
        });
    }

    public CompletableFuture<TagsProvider.TagLookup<T>> contentsGetter() {
        return this.contentsDone.thenApply(p_276016_ -> p_274772_ -> Optional.ofNullable(this.builders.get(p_274772_.location())));
    }

    protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
        return this.lookupProvider.thenApply(p_274768_ -> {
            this.builders.clear();
            this.addTags(p_274768_);
            return (HolderLookup.Provider)p_274768_;
        });
    }

    private boolean missing(TagEntry reference) {
        // Optional tags should not be validated

        if (reference.isRequired()) {
           return existingFileHelper == null || !existingFileHelper.exists(reference.getId(), reference.isTag() ? resourceType : elementResourceType);
        }
        return false;
     }

    public static class TagAppender<T> implements net.minecraftforge.common.extensions.IForgeTagAppender<T> {
        private final TagBuilder builder;
        private final String modid;

        protected TagAppender(TagBuilder pBuilder) {
            this(pBuilder, "vanilla");
        }

        protected TagAppender(TagBuilder p_236454_, String modId) {
            this.builder = p_236454_;
            this.modid = modId;
        }

        public final TagsProvider.TagAppender<T> add(ResourceKey<T> pKey) {
            this.builder.addElement(pKey.location());
            return this;
        }

        @SafeVarargs
        public final TagsProvider.TagAppender<T> add(ResourceKey<T>... pKeys) {
            for (ResourceKey<T> resourcekey : pKeys) {
                this.builder.addElement(resourcekey.location());
            }

            return this;
        }

        public final TagsProvider.TagAppender<T> addAll(List<ResourceKey<T>> pKeys) {
            for (ResourceKey<T> resourcekey : pKeys) {
                this.builder.addElement(resourcekey.location());
            }

            return this;
        }

        public TagsProvider.TagAppender<T> addOptional(ResourceLocation pLocation) {
            this.builder.addOptionalElement(pLocation);
            return this;
        }

        public TagsProvider.TagAppender<T> addTag(TagKey<T> pTag) {
            this.builder.addTag(pTag.location());
            return this;
        }

        public TagsProvider.TagAppender<T> addOptionalTag(ResourceLocation pLocation) {
            this.builder.addOptionalTag(pLocation);
            return this;
        }

        public TagsProvider.TagAppender<T> add(TagEntry tag) {
            builder.add(tag);
            return this;
        }

        public TagBuilder getInternalBuilder() {
            return builder;
        }

        public String getModID() {
            return modid;
        }
    }

    @FunctionalInterface
    public interface TagLookup<T> extends Function<TagKey<T>, Optional<TagBuilder>> {
        static <T> TagsProvider.TagLookup<T> empty() {
            return p_275247_ -> Optional.empty();
        }

        default boolean contains(TagKey<T> pKey) {
            return this.apply(pKey).isPresent();
        }
    }
}
