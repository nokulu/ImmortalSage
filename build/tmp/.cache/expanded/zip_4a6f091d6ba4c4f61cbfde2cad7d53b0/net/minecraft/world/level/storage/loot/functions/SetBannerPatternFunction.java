package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemFunction that sets the banner patterns for a banner item. Optionally appends to any existing patterns.
 */
public class SetBannerPatternFunction extends LootItemConditionalFunction {
    public static final MapCodec<SetBannerPatternFunction> CODEC = RecordCodecBuilder.mapCodec(
        p_327589_ -> commonFields(p_327589_)
                .and(
                    p_327589_.group(
                        BannerPatternLayers.CODEC.fieldOf("patterns").forGetter(p_327588_ -> p_327588_.patterns),
                        Codec.BOOL.fieldOf("append").forGetter(p_301183_ -> p_301183_.append)
                    )
                )
                .apply(p_327589_, SetBannerPatternFunction::new)
    );
    private final BannerPatternLayers patterns;
    private final boolean append;

    SetBannerPatternFunction(List<LootItemCondition> p_165276_, BannerPatternLayers p_332596_, boolean p_165277_) {
        super(p_165276_);
        this.patterns = p_332596_;
        this.append = p_165277_;
    }

    @Override
    protected ItemStack run(ItemStack pStack, LootContext pContext) {
        if (this.append) {
            pStack.update(
                DataComponents.BANNER_PATTERNS,
                BannerPatternLayers.EMPTY,
                this.patterns,
                (p_327586_, p_327587_) -> new BannerPatternLayers.Builder().addAll(p_327586_).addAll(p_327587_).build()
            );
        } else {
            pStack.set(DataComponents.BANNER_PATTERNS, this.patterns);
        }

        return pStack;
    }

    @Override
    public LootItemFunctionType<SetBannerPatternFunction> getType() {
        return LootItemFunctions.SET_BANNER_PATTERN;
    }

    public static SetBannerPatternFunction.Builder setBannerPattern(boolean pAppend) {
        return new SetBannerPatternFunction.Builder(pAppend);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<SetBannerPatternFunction.Builder> {
        private final BannerPatternLayers.Builder patterns = new BannerPatternLayers.Builder();
        private final boolean append;

        Builder(boolean pAppend) {
            this.append = pAppend;
        }

        protected SetBannerPatternFunction.Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetBannerPatternFunction(this.getConditions(), this.patterns.build(), this.append);
        }

        public SetBannerPatternFunction.Builder addPattern(Holder<BannerPattern> pPattern, DyeColor pColor) {
            this.patterns.add(pPattern, pColor);
            return this;
        }
    }
}