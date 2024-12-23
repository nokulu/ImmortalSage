package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetInstrumentFunction extends LootItemConditionalFunction {
    public static final MapCodec<SetInstrumentFunction> CODEC = RecordCodecBuilder.mapCodec(
        p_297135_ -> commonFields(p_297135_)
                .and(TagKey.hashedCodec(Registries.INSTRUMENT).fieldOf("options").forGetter(p_297134_ -> p_297134_.options))
                .apply(p_297135_, SetInstrumentFunction::new)
    );
    private final TagKey<Instrument> options;

    private SetInstrumentFunction(List<LootItemCondition> p_297631_, TagKey<Instrument> p_231009_) {
        super(p_297631_);
        this.options = p_231009_;
    }

    @Override
    public LootItemFunctionType<SetInstrumentFunction> getType() {
        return LootItemFunctions.SET_INSTRUMENT;
    }

    @Override
    public ItemStack run(ItemStack pStack, LootContext pContext) {
        InstrumentItem.setRandom(pStack, this.options, pContext.getRandom());
        return pStack;
    }

    public static LootItemConditionalFunction.Builder<?> setInstrumentOptions(TagKey<Instrument> pInstrumentOptions) {
        return simpleBuilder(p_297137_ -> new SetInstrumentFunction(p_297137_, pInstrumentOptions));
    }
}