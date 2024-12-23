package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final LootTable EMPTY = new LootTable(LootContextParamSets.EMPTY, Optional.empty(), List.of(), List.of());
    public static final LootContextParamSet DEFAULT_PARAM_SET = LootContextParamSets.ALL_PARAMS;
    public static final long RANDOMIZE_SEED = 0L;
    public static final Codec<LootTable> DIRECT_CODEC = RecordCodecBuilder.create(
        p_327557_ -> p_327557_.group(
                    LootContextParamSets.CODEC.lenientOptionalFieldOf("type", DEFAULT_PARAM_SET).forGetter(p_297013_ -> p_297013_.paramSet),
                    ResourceLocation.CODEC.optionalFieldOf("random_sequence").forGetter(p_297014_ -> p_297014_.randomSequence),
                    LootPool.CONDITIONAL_CODEC.listOf().optionalFieldOf("pools", List.of()).forGetter(p_297012_ -> p_297012_.pools),
                    LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(p_297010_ -> p_297010_.functions)
                )
                .apply(p_327557_, LootTable::new)
    );
    public static final Codec<Holder<LootTable>> CODEC = RegistryFileCodec.create(Registries.LOOT_TABLE, DIRECT_CODEC);
    private final LootContextParamSet paramSet;
    private final Optional<ResourceLocation> randomSequence;
    private final List<LootPool> pools;
    private final List<LootItemFunction> functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

    LootTable(LootContextParamSet p_287716_, Optional<ResourceLocation> p_298628_, List<LootPool> p_298771_, List<LootItemFunction> p_301234_) {
        this.paramSet = p_287716_;
        this.randomSequence = p_298628_;
        this.pools = Lists.newArrayList(p_298771_);
        this.functions = p_301234_;
        this.compositeFunction = LootItemFunctions.compose(p_301234_);
    }

    public static Consumer<ItemStack> createStackSplitter(ServerLevel pLevel, Consumer<ItemStack> pOutput) {
        return p_287570_ -> {
            if (p_287570_.isItemEnabled(pLevel.enabledFeatures())) {
                if (p_287570_.getCount() < p_287570_.getMaxStackSize()) {
                    pOutput.accept(p_287570_);
                } else {
                    int i = p_287570_.getCount();

                    while (i > 0) {
                        ItemStack itemstack = p_287570_.copyWithCount(Math.min(p_287570_.getMaxStackSize(), i));
                        i -= itemstack.getCount();
                        pOutput.accept(itemstack);
                    }
                }
            }
        };
    }

    @Deprecated // Use a non-'Raw' version of 'getRandomItems', so that the Forge Global Loot Modifiers will be applied
    public void getRandomItemsRaw(LootParams pParams, Consumer<ItemStack> pOutput) {
        this.getRandomItemsRaw(new LootContext.Builder(pParams).create(this.randomSequence), pOutput);
    }

    @Deprecated // Use a non-'Raw' version of 'getRandomItems', so that the Forge Global Loot Modifiers will be applied
    public void getRandomItemsRaw(LootContext pContext, Consumer<ItemStack> pOutput) {
        LootContext.VisitedEntry<?> visitedentry = LootContext.createVisitedEntry(this);
        if (pContext.pushVisitedElement(visitedentry)) {
            Consumer<ItemStack> consumer = LootItemFunction.decorate(this.compositeFunction, pOutput, pContext);

            for (LootPool lootpool : this.pools) {
                lootpool.addRandomItems(consumer, pContext);
            }

            pContext.popVisitedElement(visitedentry);
        } else {
            LOGGER.warn("Detected infinite loop in loot tables");
        }
    }

    public void getRandomItems(LootParams pParams, long pSeed, Consumer<ItemStack> pOutput) {
        this.getRandomItems(new LootContext.Builder(pParams).withOptionalRandomSeed(pSeed).create(this.randomSequence)).forEach(pOutput);
    }

    public void getRandomItems(LootParams pParams, Consumer<ItemStack> pOutput) {
        this.getRandomItems(pParams).forEach(pOutput);
    }

    public void getRandomItems(LootContext pContextData, Consumer<ItemStack> pOutput) {
        this.getRandomItems(pContextData).forEach(pOutput);
    }

    public ObjectArrayList<ItemStack> getRandomItems(LootParams pParams, RandomSource pRandom) {
        return this.getRandomItems(new LootContext.Builder(pParams).withOptionalRandomSource(pRandom).create(this.randomSequence));
    }

    public ObjectArrayList<ItemStack> getRandomItems(LootParams pParams, long pSeed) {
        return this.getRandomItems(new LootContext.Builder(pParams).withOptionalRandomSeed(pSeed).create(this.randomSequence));
    }

    public ObjectArrayList<ItemStack> getRandomItems(LootParams pParams) {
        return this.getRandomItems(new LootContext.Builder(pParams).create(this.randomSequence));
    }

    private ObjectArrayList<ItemStack> getRandomItems(LootContext pContext) {
        ObjectArrayList<ItemStack> objectarraylist = new ObjectArrayList<>();
        this.getRandomItemsRaw(pContext, createStackSplitter(pContext.getLevel(), objectarraylist::add));
        objectarraylist = net.minecraftforge.common.ForgeHooks.modifyLoot(this.getLootTableId(), objectarraylist, pContext);
        return objectarraylist;
    }

    public LootContextParamSet getParamSet() {
        return this.paramSet;
    }

    public void validate(ValidationContext pValidator) {
        for (int i = 0; i < this.pools.size(); i++) {
            this.pools.get(i).validate(pValidator.forChild(".pools[" + i + "]"));
        }

        for (int j = 0; j < this.functions.size(); j++) {
            this.functions.get(j).validate(pValidator.forChild(".functions[" + j + "]"));
        }
    }

    public void fill(Container pContainer, LootParams pParams, long pSeed) {
        LootContext lootcontext = new LootContext.Builder(pParams).withOptionalRandomSeed(pSeed).create(this.randomSequence);
        ObjectArrayList<ItemStack> objectarraylist = this.getRandomItems(lootcontext);
        RandomSource randomsource = lootcontext.getRandom();
        List<Integer> list = this.getAvailableSlots(pContainer, randomsource);
        this.shuffleAndSplitItems(objectarraylist, list.size(), randomsource);

        for (ItemStack itemstack : objectarraylist) {
            if (list.isEmpty()) {
                LOGGER.warn("Tried to over-fill a container");
                return;
            }

            if (itemstack.isEmpty()) {
                pContainer.setItem(list.remove(list.size() - 1), ItemStack.EMPTY);
            } else {
                pContainer.setItem(list.remove(list.size() - 1), itemstack);
            }
        }
    }

    private void shuffleAndSplitItems(ObjectArrayList<ItemStack> pStacks, int pEmptySlotsCount, RandomSource pRandom) {
        List<ItemStack> list = Lists.newArrayList();
        Iterator<ItemStack> iterator = pStacks.iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack = iterator.next();
            if (itemstack.isEmpty()) {
                iterator.remove();
            } else if (itemstack.getCount() > 1) {
                list.add(itemstack);
                iterator.remove();
            }
        }

        while (pEmptySlotsCount - pStacks.size() - list.size() > 0 && !list.isEmpty()) {
            ItemStack itemstack2 = list.remove(Mth.nextInt(pRandom, 0, list.size() - 1));
            int i = Mth.nextInt(pRandom, 1, itemstack2.getCount() / 2);
            ItemStack itemstack1 = itemstack2.split(i);
            if (itemstack2.getCount() > 1 && pRandom.nextBoolean()) {
                list.add(itemstack2);
            } else {
                pStacks.add(itemstack2);
            }

            if (itemstack1.getCount() > 1 && pRandom.nextBoolean()) {
                list.add(itemstack1);
            } else {
                pStacks.add(itemstack1);
            }
        }

        pStacks.addAll(list);
        Util.shuffle(pStacks, pRandom);
    }

    private List<Integer> getAvailableSlots(Container pInventory, RandomSource pRandom) {
        ObjectArrayList<Integer> objectarraylist = new ObjectArrayList<>();

        for (int i = 0; i < pInventory.getContainerSize(); i++) {
            if (pInventory.getItem(i).isEmpty()) {
                objectarraylist.add(i);
            }
        }

        Util.shuffle(objectarraylist, pRandom);
        return objectarraylist;
    }

    public static LootTable.Builder lootTable() {
        return new LootTable.Builder();
    }

    private ResourceLocation lootTableId;
    public void setLootTableId(final ResourceLocation id) {
        if (this.lootTableId != null) {
            throw new IllegalStateException("Attempted to rename loot table from '" + this.lootTableId + "' to '" + id + "': this is not supported");
        }
        this.lootTableId = java.util.Objects.requireNonNull(id);
    }

    public ResourceLocation getLootTableId() {
        return this.lootTableId;
    }

    @org.jetbrains.annotations.Nullable
    public LootPool removePool(String name) {
        checkFrozen();
        for (var pool : this.pools) {
            if (name.equals(pool.getName())) {
                this.pools.remove(pool);
                return pool;
            }
        }
        return null;
    }

    public void addPool(LootPool pool) {
        checkFrozen();
        if (pools.stream().anyMatch(e -> e == pool)) {
            throw new RuntimeException("Attempted to add a duplicate pool to loot table: " + pool);
        }
        this.pools.add(pool);
    }

    private boolean isFrozen = false;
    public void freeze() {
        this.isFrozen = true;
        this.pools.forEach(LootPool::freeze);
    }

    public boolean isFrozen() {
        return this.isFrozen;
    }

    private void checkFrozen() {
        if (this.isFrozen()) {
            throw new RuntimeException("Attempted to modify LootTable after being finalized!");
        }
    }

    public static class Builder implements FunctionUserBuilder<LootTable.Builder> {
        private final ImmutableList.Builder<LootPool> pools = ImmutableList.builder();
        private final ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();
        private LootContextParamSet paramSet = LootTable.DEFAULT_PARAM_SET;
        private Optional<ResourceLocation> randomSequence = Optional.empty();

        public LootTable.Builder withPool(LootPool.Builder pLootPool) {
            this.pools.add(pLootPool.build());
            return this;
        }

        public LootTable.Builder setParamSet(LootContextParamSet pParameterSet) {
            this.paramSet = pParameterSet;
            return this;
        }

        public LootTable.Builder setRandomSequence(ResourceLocation pRandomSequence) {
            this.randomSequence = Optional.of(pRandomSequence);
            return this;
        }

        public LootTable.Builder apply(LootItemFunction.Builder pFunctionBuilder) {
            this.functions.add(pFunctionBuilder.build());
            return this;
        }

        public LootTable.Builder unwrap() {
            return this;
        }

        public LootTable build() {
            return new LootTable(this.paramSet, this.randomSequence, this.pools.build(), this.functions.build());
        }
    }
}
