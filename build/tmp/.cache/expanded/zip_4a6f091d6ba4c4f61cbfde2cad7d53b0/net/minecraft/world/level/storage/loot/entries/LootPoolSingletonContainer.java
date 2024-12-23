package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products.P4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * A LootPoolEntryContainer that expands into a single LootPoolEntry.
 */
public abstract class LootPoolSingletonContainer extends LootPoolEntryContainer {
    public static final int DEFAULT_WEIGHT = 1;
    public static final int DEFAULT_QUALITY = 0;
    protected final int weight;
    protected final int quality;
    protected final List<LootItemFunction> functions;
    final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    private final LootPoolEntry entry = new LootPoolSingletonContainer.EntryBase() {
        @Override
        public void createItemStack(Consumer<ItemStack> p_79700_, LootContext p_79701_) {
            LootPoolSingletonContainer.this.createItemStack(LootItemFunction.decorate(LootPoolSingletonContainer.this.compositeFunction, p_79700_, p_79701_), p_79701_);
        }
    };

    protected LootPoolSingletonContainer(int pWeight, int pQuality, List<LootItemCondition> pConditions, List<LootItemFunction> pFunctions) {
        super(pConditions);
        this.weight = pWeight;
        this.quality = pQuality;
        this.functions = pFunctions;
        this.compositeFunction = LootItemFunctions.compose(pFunctions);
    }

    protected static <T extends LootPoolSingletonContainer> P4<Mu<T>, Integer, Integer, List<LootItemCondition>, List<LootItemFunction>> singletonFields(
        Instance<T> pInstance
    ) {
        return pInstance.group(
                Codec.INT.optionalFieldOf("weight", Integer.valueOf(1)).forGetter(p_300354_ -> p_300354_.weight),
                Codec.INT.optionalFieldOf("quality", Integer.valueOf(0)).forGetter(p_297680_ -> p_297680_.quality)
            )
            .and(commonFields(pInstance).t1())
            .and(LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(p_301387_ -> p_301387_.functions));
    }

    @Override
    public void validate(ValidationContext pValidationContext) {
        super.validate(pValidationContext);

        for (int i = 0; i < this.functions.size(); i++) {
            this.functions.get(i).validate(pValidationContext.forChild(".functions[" + i + "]"));
        }
    }

    protected abstract void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext);

    @Override
    public boolean expand(LootContext pLootContext, Consumer<LootPoolEntry> pEntryConsumer) {
        if (this.canRun(pLootContext)) {
            pEntryConsumer.accept(this.entry);
            return true;
        } else {
            return false;
        }
    }

    public static LootPoolSingletonContainer.Builder<?> simpleBuilder(LootPoolSingletonContainer.EntryConstructor pEntryBuilder) {
        return new LootPoolSingletonContainer.DummyBuilder(pEntryBuilder);
    }

    public abstract static class Builder<T extends LootPoolSingletonContainer.Builder<T>>
        extends LootPoolEntryContainer.Builder<T>
        implements FunctionUserBuilder<T> {
        protected int weight = 1;
        protected int quality = 0;
        private final ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();

        public T apply(LootItemFunction.Builder pFunctionBuilder) {
            this.functions.add(pFunctionBuilder.build());
            return this.getThis();
        }

        protected List<LootItemFunction> getFunctions() {
            return this.functions.build();
        }

        public T setWeight(int pWeight) {
            this.weight = pWeight;
            return this.getThis();
        }

        public T setQuality(int pQuality) {
            this.quality = pQuality;
            return this.getThis();
        }
    }

    static class DummyBuilder extends LootPoolSingletonContainer.Builder<LootPoolSingletonContainer.DummyBuilder> {
        private final LootPoolSingletonContainer.EntryConstructor constructor;

        public DummyBuilder(LootPoolSingletonContainer.EntryConstructor pConstructor) {
            this.constructor = pConstructor;
        }

        protected LootPoolSingletonContainer.DummyBuilder getThis() {
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return this.constructor.build(this.weight, this.quality, this.getConditions(), this.getFunctions());
        }
    }

    protected abstract class EntryBase implements LootPoolEntry {
        @Override
        public int getWeight(float pLuck) {
            return Math.max(Mth.floor((float)LootPoolSingletonContainer.this.weight + (float)LootPoolSingletonContainer.this.quality * pLuck), 0);
        }
    }

    @FunctionalInterface
    protected interface EntryConstructor {
        LootPoolSingletonContainer build(int pWeight, int pQuality, List<LootItemCondition> pConditions, List<LootItemFunction> pFunctions);
    }
}