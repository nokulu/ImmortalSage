package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Base class for loot pool entry containers that delegate to one or more children.
 * The actual functionality is provided by composing the children into one composed container (see {@link #compose}).
 */
public abstract class CompositeEntryBase extends LootPoolEntryContainer {
    protected final List<LootPoolEntryContainer> children;
    private final ComposableEntryContainer composedChildren;

    protected CompositeEntryBase(List<LootPoolEntryContainer> pChildren, List<LootItemCondition> pConditions) {
        super(pConditions);
        this.children = pChildren;
        this.composedChildren = this.compose(pChildren);
    }

    @Override
    public void validate(ValidationContext pValidationContext) {
        super.validate(pValidationContext);
        if (this.children.isEmpty()) {
            pValidationContext.reportProblem("Empty children list");
        }

        for (int i = 0; i < this.children.size(); i++) {
            this.children.get(i).validate(pValidationContext.forChild(".entry[" + i + "]"));
        }
    }

    protected abstract ComposableEntryContainer compose(List<? extends ComposableEntryContainer> pChildren);

    @Override
    public final boolean expand(LootContext pLootContext, Consumer<LootPoolEntry> pEntryConsumer) {
        return !this.canRun(pLootContext) ? false : this.composedChildren.expand(pLootContext, pEntryConsumer);
    }

    public static <T extends CompositeEntryBase> MapCodec<T> createCodec(CompositeEntryBase.CompositeEntryConstructor<T> pFactory) {
        return RecordCodecBuilder.mapCodec(
            p_327559_ -> p_327559_.group(LootPoolEntries.CODEC.listOf().optionalFieldOf("children", List.of()).forGetter(p_300130_ -> p_300130_.children))
                    .and(commonFields(p_327559_).t1())
                    .apply(p_327559_, pFactory::create)
        );
    }

    @FunctionalInterface
    public interface CompositeEntryConstructor<T extends CompositeEntryBase> {
        T create(List<LootPoolEntryContainer> pChildren, List<LootItemCondition> pConditions);
    }
}