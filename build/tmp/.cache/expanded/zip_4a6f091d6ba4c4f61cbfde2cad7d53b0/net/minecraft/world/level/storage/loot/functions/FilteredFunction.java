package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FilteredFunction extends LootItemConditionalFunction {
    public static final MapCodec<FilteredFunction> CODEC = RecordCodecBuilder.mapCodec(
        p_328747_ -> commonFields(p_328747_)
                .and(
                    p_328747_.group(
                        ItemPredicate.CODEC.fieldOf("item_filter").forGetter(p_334024_ -> p_334024_.filter),
                        LootItemFunctions.ROOT_CODEC.fieldOf("modifier").forGetter(p_334445_ -> p_334445_.modifier)
                    )
                )
                .apply(p_328747_, FilteredFunction::new)
    );
    private final ItemPredicate filter;
    private final LootItemFunction modifier;

    private FilteredFunction(List<LootItemCondition> p_333409_, ItemPredicate p_333352_, LootItemFunction p_328232_) {
        super(p_333409_);
        this.filter = p_333352_;
        this.modifier = p_328232_;
    }

    @Override
    public LootItemFunctionType<FilteredFunction> getType() {
        return LootItemFunctions.FILTERED;
    }

    @Override
    public ItemStack run(ItemStack pStack, LootContext pContext) {
        return this.filter.test(pStack) ? this.modifier.apply(pStack, pContext) : pStack;
    }

    @Override
    public void validate(ValidationContext pContext) {
        super.validate(pContext);
        this.modifier.validate(pContext.forChild(".modifier"));
    }
}