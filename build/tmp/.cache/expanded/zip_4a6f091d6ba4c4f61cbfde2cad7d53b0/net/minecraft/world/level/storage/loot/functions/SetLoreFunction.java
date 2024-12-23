package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemFunction that sets a stack's lore tag, optionally replacing any previously present lore.
 * The Components for the lore tag are optionally resolved relative to a given {@link LootContext.EntityTarget} for
 * entity-sensitive component data such as scoreboard scores.
 */
public class SetLoreFunction extends LootItemConditionalFunction {
    public static final MapCodec<SetLoreFunction> CODEC = RecordCodecBuilder.mapCodec(
        p_327612_ -> commonFields(p_327612_)
                .and(
                    p_327612_.group(
                        ComponentSerialization.CODEC.sizeLimitedListOf(256).fieldOf("lore").forGetter(p_300292_ -> p_300292_.lore),
                        ListOperation.codec(256).forGetter(p_327611_ -> p_327611_.mode),
                        LootContext.EntityTarget.CODEC.optionalFieldOf("entity").forGetter(p_300757_ -> p_300757_.resolutionContext)
                    )
                )
                .apply(p_327612_, SetLoreFunction::new)
    );
    private final List<Component> lore;
    private final ListOperation mode;
    private final Optional<LootContext.EntityTarget> resolutionContext;

    public SetLoreFunction(List<LootItemCondition> p_81085_, List<Component> p_300257_, ListOperation p_333397_, Optional<LootContext.EntityTarget> p_301400_) {
        super(p_81085_);
        this.lore = List.copyOf(p_300257_);
        this.mode = p_333397_;
        this.resolutionContext = p_301400_;
    }

    @Override
    public LootItemFunctionType<SetLoreFunction> getType() {
        return LootItemFunctions.SET_LORE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.resolutionContext.<Set<LootContextParam<?>>>map(p_298916_ -> Set.of(p_298916_.getParam())).orElseGet(Set::of);
    }

    @Override
    public ItemStack run(ItemStack pStack, LootContext pContext) {
        pStack.update(DataComponents.LORE, ItemLore.EMPTY, p_327614_ -> new ItemLore(this.updateLore(p_327614_, pContext)));
        return pStack;
    }

    private List<Component> updateLore(@Nullable ItemLore pItemLore, LootContext pContext) {
        if (pItemLore == null && this.lore.isEmpty()) {
            return List.of();
        } else {
            UnaryOperator<Component> unaryoperator = SetNameFunction.createResolver(pContext, this.resolutionContext.orElse(null));
            List<Component> list = this.lore.stream().map(unaryoperator).toList();
            return this.mode.apply(pItemLore.lines(), list, 256);
        }
    }

    public static SetLoreFunction.Builder setLore() {
        return new SetLoreFunction.Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<SetLoreFunction.Builder> {
        private Optional<LootContext.EntityTarget> resolutionContext = Optional.empty();
        private final ImmutableList.Builder<Component> lore = ImmutableList.builder();
        private ListOperation mode = ListOperation.Append.INSTANCE;

        public SetLoreFunction.Builder setMode(ListOperation pMode) {
            this.mode = pMode;
            return this;
        }

        public SetLoreFunction.Builder setResolutionContext(LootContext.EntityTarget pResolutionContext) {
            this.resolutionContext = Optional.of(pResolutionContext);
            return this;
        }

        public SetLoreFunction.Builder addLine(Component pLine) {
            this.lore.add(pLine);
            return this;
        }

        protected SetLoreFunction.Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetLoreFunction(this.getConditions(), this.lore.build(), this.mode, this.resolutionContext);
        }
    }
}