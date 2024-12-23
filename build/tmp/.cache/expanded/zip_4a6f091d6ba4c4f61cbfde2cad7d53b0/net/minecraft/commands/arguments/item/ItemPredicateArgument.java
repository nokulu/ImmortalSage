package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemPredicateArgument implements ArgumentType<ItemPredicateArgument.Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "#stick", "#stick{foo:'bar'}");
    static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType(
        p_325619_ -> Component.translatableEscape("argument.item.id.invalid", p_325619_)
    );
    static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
        p_325632_ -> Component.translatableEscape("arguments.item.tag.unknown", p_325632_)
    );
    static final DynamicCommandExceptionType ERROR_UNKNOWN_COMPONENT = new DynamicCommandExceptionType(
        p_325626_ -> Component.translatableEscape("arguments.item.component.unknown", p_325626_)
    );
    static final Dynamic2CommandExceptionType ERROR_MALFORMED_COMPONENT = new Dynamic2CommandExceptionType(
        (p_325624_, p_325625_) -> Component.translatableEscape("arguments.item.component.malformed", p_325624_, p_325625_)
    );
    static final DynamicCommandExceptionType ERROR_UNKNOWN_PREDICATE = new DynamicCommandExceptionType(
        p_325623_ -> Component.translatableEscape("arguments.item.predicate.unknown", p_325623_)
    );
    static final Dynamic2CommandExceptionType ERROR_MALFORMED_PREDICATE = new Dynamic2CommandExceptionType(
        (p_325617_, p_325618_) -> Component.translatableEscape("arguments.item.predicate.malformed", p_325617_, p_325618_)
    );
    private static final ResourceLocation COUNT_ID = ResourceLocation.withDefaultNamespace("count");
    static final Map<ResourceLocation, ItemPredicateArgument.ComponentWrapper> PSEUDO_COMPONENTS = Stream.of(
            new ItemPredicateArgument.ComponentWrapper(
                COUNT_ID, p_325630_ -> true, MinMaxBounds.Ints.CODEC.map(p_325633_ -> p_325622_ -> p_325633_.matches(p_325622_.getCount()))
            )
        )
        .collect(
            Collectors.toUnmodifiableMap(ItemPredicateArgument.ComponentWrapper::id, p_325629_ -> (ItemPredicateArgument.ComponentWrapper)p_325629_)
        );
    static final Map<ResourceLocation, ItemPredicateArgument.PredicateWrapper> PSEUDO_PREDICATES = Stream.of(
            new ItemPredicateArgument.PredicateWrapper(
                COUNT_ID, MinMaxBounds.Ints.CODEC.map(p_325620_ -> p_325628_ -> p_325620_.matches(p_325628_.getCount()))
            )
        )
        .collect(
            Collectors.toUnmodifiableMap(ItemPredicateArgument.PredicateWrapper::id, p_325631_ -> (ItemPredicateArgument.PredicateWrapper)p_325631_)
        );
    private final Grammar<List<Predicate<ItemStack>>> grammarWithContext;

    public ItemPredicateArgument(CommandBuildContext pContext) {
        ItemPredicateArgument.Context itempredicateargument$context = new ItemPredicateArgument.Context(pContext);
        this.grammarWithContext = ComponentPredicateParser.createGrammar(itempredicateargument$context);
    }

    public static ItemPredicateArgument itemPredicate(CommandBuildContext pContext) {
        return new ItemPredicateArgument(pContext);
    }

    public ItemPredicateArgument.Result parse(StringReader pReader) throws CommandSyntaxException {
        return Util.allOf(this.grammarWithContext.parseForCommands(pReader))::test;
    }

    public static ItemPredicateArgument.Result getItemPredicate(CommandContext<CommandSourceStack> pContext, String pName) {
        return pContext.getArgument(pName, ItemPredicateArgument.Result.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
        return this.grammarWithContext.parseForSuggestions(pBuilder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    static record ComponentWrapper(ResourceLocation id, Predicate<ItemStack> presenceChecker, Decoder<? extends Predicate<ItemStack>> valueChecker) {
        public static <T> ItemPredicateArgument.ComponentWrapper create(
            ImmutableStringReader pReader, ResourceLocation pId, DataComponentType<T> pComponentType
        ) throws CommandSyntaxException {
            Codec<T> codec = pComponentType.codec();
            if (codec == null) {
                throw ItemPredicateArgument.ERROR_UNKNOWN_COMPONENT.createWithContext(pReader, pId);
            } else {
                return new ItemPredicateArgument.ComponentWrapper(pId, p_327858_ -> p_327858_.has(pComponentType), codec.map(p_335085_ -> p_331446_ -> {
                        T t = p_331446_.get(pComponentType);
                        return Objects.equals(p_335085_, t);
                    }));
            }
        }

        public Predicate<ItemStack> decode(ImmutableStringReader pReader, RegistryOps<Tag> pOps, Tag pValue) throws CommandSyntaxException {
            DataResult<? extends Predicate<ItemStack>> dataresult = this.valueChecker.parse(pOps, pValue);
            return (Predicate<ItemStack>)dataresult.getOrThrow(
                p_331995_ -> ItemPredicateArgument.ERROR_MALFORMED_COMPONENT.createWithContext(pReader, this.id.toString(), p_331995_)
            );
        }
    }

    static class Context
        implements ComponentPredicateParser.Context<Predicate<ItemStack>, ItemPredicateArgument.ComponentWrapper, ItemPredicateArgument.PredicateWrapper> {
        private final HolderLookup.RegistryLookup<Item> items;
        private final HolderLookup.RegistryLookup<DataComponentType<?>> components;
        private final HolderLookup.RegistryLookup<ItemSubPredicate.Type<?>> predicates;
        private final RegistryOps<Tag> registryOps;

        Context(HolderLookup.Provider pRegistries) {
            this.items = pRegistries.lookupOrThrow(Registries.ITEM);
            this.components = pRegistries.lookupOrThrow(Registries.DATA_COMPONENT_TYPE);
            this.predicates = pRegistries.lookupOrThrow(Registries.ITEM_SUB_PREDICATE_TYPE);
            this.registryOps = pRegistries.createSerializationContext(NbtOps.INSTANCE);
        }

        public Predicate<ItemStack> forElementType(ImmutableStringReader pReader, ResourceLocation pElementType) throws CommandSyntaxException {
            Holder.Reference<Item> reference = this.items
                .get(ResourceKey.create(Registries.ITEM, pElementType))
                .orElseThrow(() -> ItemPredicateArgument.ERROR_UNKNOWN_ITEM.createWithContext(pReader, pElementType));
            return p_333639_ -> p_333639_.is(reference);
        }

        public Predicate<ItemStack> forTagType(ImmutableStringReader pReader, ResourceLocation pTagType) throws CommandSyntaxException {
            HolderSet<Item> holderset = this.items
                .get(TagKey.create(Registries.ITEM, pTagType))
                .orElseThrow(() -> ItemPredicateArgument.ERROR_UNKNOWN_TAG.createWithContext(pReader, pTagType));
            return p_334213_ -> p_334213_.is(holderset);
        }

        public ItemPredicateArgument.ComponentWrapper lookupComponentType(ImmutableStringReader pReader, ResourceLocation pComponentType) throws CommandSyntaxException {
            ItemPredicateArgument.ComponentWrapper itempredicateargument$componentwrapper = ItemPredicateArgument.PSEUDO_COMPONENTS.get(pComponentType);
            if (itempredicateargument$componentwrapper != null) {
                return itempredicateargument$componentwrapper;
            } else {
                DataComponentType<?> datacomponenttype = this.components
                    .get(ResourceKey.create(Registries.DATA_COMPONENT_TYPE, pComponentType))
                    .map(Holder::value)
                    .orElseThrow(() -> ItemPredicateArgument.ERROR_UNKNOWN_COMPONENT.createWithContext(pReader, pComponentType));
                return ItemPredicateArgument.ComponentWrapper.create(pReader, pComponentType, datacomponenttype);
            }
        }

        public Predicate<ItemStack> createComponentTest(ImmutableStringReader pReader, ItemPredicateArgument.ComponentWrapper pContext, Tag pValue) throws CommandSyntaxException {
            return pContext.decode(pReader, this.registryOps, pValue);
        }

        public Predicate<ItemStack> createComponentTest(ImmutableStringReader pReader, ItemPredicateArgument.ComponentWrapper pContext) {
            return pContext.presenceChecker;
        }

        public ItemPredicateArgument.PredicateWrapper lookupPredicateType(ImmutableStringReader pReader, ResourceLocation pPredicateType) throws CommandSyntaxException {
            ItemPredicateArgument.PredicateWrapper itempredicateargument$predicatewrapper = ItemPredicateArgument.PSEUDO_PREDICATES.get(pPredicateType);
            return itempredicateargument$predicatewrapper != null
                ? itempredicateargument$predicatewrapper
                : this.predicates
                    .get(ResourceKey.create(Registries.ITEM_SUB_PREDICATE_TYPE, pPredicateType))
                    .map(ItemPredicateArgument.PredicateWrapper::new)
                    .orElseThrow(() -> ItemPredicateArgument.ERROR_UNKNOWN_PREDICATE.createWithContext(pReader, pPredicateType));
        }

        public Predicate<ItemStack> createPredicateTest(ImmutableStringReader pReader, ItemPredicateArgument.PredicateWrapper pPredicate, Tag pValue) throws CommandSyntaxException {
            return pPredicate.decode(pReader, this.registryOps, pValue);
        }

        @Override
        public Stream<ResourceLocation> listElementTypes() {
            return this.items.listElementIds().map(ResourceKey::location);
        }

        @Override
        public Stream<ResourceLocation> listTagTypes() {
            return this.items.listTagIds().map(TagKey::location);
        }

        @Override
        public Stream<ResourceLocation> listComponentTypes() {
            return Stream.concat(
                ItemPredicateArgument.PSEUDO_COMPONENTS.keySet().stream(),
                this.components.listElements().filter(p_334864_ -> !p_334864_.value().isTransient()).map(p_329470_ -> p_329470_.key().location())
            );
        }

        @Override
        public Stream<ResourceLocation> listPredicateTypes() {
            return Stream.concat(ItemPredicateArgument.PSEUDO_PREDICATES.keySet().stream(), this.predicates.listElementIds().map(ResourceKey::location));
        }

        public Predicate<ItemStack> negate(Predicate<ItemStack> pValue) {
            return pValue.negate();
        }

        public Predicate<ItemStack> anyOf(List<Predicate<ItemStack>> pValues) {
            return Util.anyOf(pValues);
        }
    }

    static record PredicateWrapper(ResourceLocation id, Decoder<? extends Predicate<ItemStack>> type) {
        public PredicateWrapper(Holder.Reference<ItemSubPredicate.Type<?>> pPredicate) {
            this(pPredicate.key().location(), pPredicate.value().codec().map(p_330179_ -> p_330179_::matches));
        }

        public Predicate<ItemStack> decode(ImmutableStringReader pReader, RegistryOps<Tag> pOps, Tag pValue) throws CommandSyntaxException {
            DataResult<? extends Predicate<ItemStack>> dataresult = this.type.parse(pOps, pValue);
            return (Predicate<ItemStack>)dataresult.getOrThrow(
                p_334639_ -> ItemPredicateArgument.ERROR_MALFORMED_PREDICATE.createWithContext(pReader, this.id.toString(), p_334639_)
            );
        }
    }

    public interface Result extends Predicate<ItemStack> {
    }
}