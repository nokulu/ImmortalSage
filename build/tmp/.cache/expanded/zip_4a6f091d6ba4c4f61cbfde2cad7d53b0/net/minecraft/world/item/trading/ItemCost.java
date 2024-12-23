package net.minecraft.world.item.trading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record ItemCost(Holder<Item> item, int count, DataComponentPredicate components, ItemStack itemStack) {
    public static final Codec<ItemCost> CODEC = RecordCodecBuilder.create(
        p_328053_ -> p_328053_.group(
                    ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemCost::item),
                    ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(ItemCost::count),
                    DataComponentPredicate.CODEC.optionalFieldOf("components", DataComponentPredicate.EMPTY).forGetter(ItemCost::components)
                )
                .apply(p_328053_, ItemCost::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemCost> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.holderRegistry(Registries.ITEM),
        ItemCost::item,
        ByteBufCodecs.VAR_INT,
        ItemCost::count,
        DataComponentPredicate.STREAM_CODEC,
        ItemCost::components,
        ItemCost::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ItemCost>> OPTIONAL_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs::optional);

    public ItemCost(ItemLike pItem) {
        this(pItem, 1);
    }

    public ItemCost(ItemLike pItem, int pCount) {
        this(pItem.asItem().builtInRegistryHolder(), pCount, DataComponentPredicate.EMPTY);
    }

    public ItemCost(Holder<Item> p_331233_, int p_334492_, DataComponentPredicate p_330788_) {
        this(p_331233_, p_334492_, p_330788_, createStack(p_331233_, p_334492_, p_330788_));
    }

    public ItemCost withComponents(UnaryOperator<DataComponentPredicate.Builder> pComponents) {
        return new ItemCost(this.item, this.count, pComponents.apply(DataComponentPredicate.builder()).build());
    }

    private static ItemStack createStack(Holder<Item> pItem, int pCount, DataComponentPredicate pComponentPredicate) {
        return new ItemStack(pItem, pCount, pComponentPredicate.asPatch());
    }

    public boolean test(ItemStack pStack) {
        return pStack.is(this.item) && this.components.test(pStack);
    }
}