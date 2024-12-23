package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class AdventureModePredicate {
    private static final Codec<AdventureModePredicate> SIMPLE_CODEC = BlockPredicate.CODEC
        .flatComapMap(p_334782_ -> new AdventureModePredicate(List.of(p_334782_), true), p_329449_ -> DataResult.error(() -> "Cannot encode"));
    private static final Codec<AdventureModePredicate> FULL_CODEC = RecordCodecBuilder.create(
        p_329746_ -> p_329746_.group(
                    ExtraCodecs.nonEmptyList(BlockPredicate.CODEC.listOf()).fieldOf("predicates").forGetter(p_329117_ -> p_329117_.predicates),
                    Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(AdventureModePredicate::showInTooltip)
                )
                .apply(p_329746_, AdventureModePredicate::new)
    );
    public static final Codec<AdventureModePredicate> CODEC = Codec.withAlternative(FULL_CODEC, SIMPLE_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, AdventureModePredicate> STREAM_CODEC = StreamCodec.composite(
        BlockPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()),
        p_333442_ -> p_333442_.predicates,
        ByteBufCodecs.BOOL,
        AdventureModePredicate::showInTooltip,
        AdventureModePredicate::new
    );
    public static final Component CAN_BREAK_HEADER = Component.translatable("item.canBreak").withStyle(ChatFormatting.GRAY);
    public static final Component CAN_PLACE_HEADER = Component.translatable("item.canPlace").withStyle(ChatFormatting.GRAY);
    private static final Component UNKNOWN_USE = Component.translatable("item.canUse.unknown").withStyle(ChatFormatting.GRAY);
    private final List<BlockPredicate> predicates;
    private final boolean showInTooltip;
    private final List<Component> tooltip;
    @Nullable
    private BlockInWorld lastCheckedBlock;
    private boolean lastResult;
    private boolean checksBlockEntity;

    private AdventureModePredicate(List<BlockPredicate> pPredicates, boolean pShowInTooltip, List<Component> pTooltip) {
        this.predicates = pPredicates;
        this.showInTooltip = pShowInTooltip;
        this.tooltip = pTooltip;
    }

    public AdventureModePredicate(List<BlockPredicate> p_336068_, boolean p_330877_) {
        this.predicates = p_336068_;
        this.showInTooltip = p_330877_;
        this.tooltip = computeTooltip(p_336068_);
    }

    private static boolean areSameBlocks(BlockInWorld pFirst, @Nullable BlockInWorld pSecond, boolean pCheckNbt) {
        if (pSecond == null || pFirst.getState() != pSecond.getState()) {
            return false;
        } else if (!pCheckNbt) {
            return true;
        } else if (pFirst.getEntity() == null && pSecond.getEntity() == null) {
            return true;
        } else if (pFirst.getEntity() != null && pSecond.getEntity() != null) {
            RegistryAccess registryaccess = pFirst.getLevel().registryAccess();
            return Objects.equals(pFirst.getEntity().saveWithId(registryaccess), pSecond.getEntity().saveWithId(registryaccess));
        } else {
            return false;
        }
    }

    public boolean test(BlockInWorld pBlock) {
        if (areSameBlocks(pBlock, this.lastCheckedBlock, this.checksBlockEntity)) {
            return this.lastResult;
        } else {
            this.lastCheckedBlock = pBlock;
            this.checksBlockEntity = false;

            for (BlockPredicate blockpredicate : this.predicates) {
                if (blockpredicate.matches(pBlock)) {
                    this.checksBlockEntity = this.checksBlockEntity | blockpredicate.requiresNbt();
                    this.lastResult = true;
                    return true;
                }
            }

            this.lastResult = false;
            return false;
        }
    }

    public void addToTooltip(Consumer<Component> pTooltipAdder) {
        this.tooltip.forEach(pTooltipAdder);
    }

    public AdventureModePredicate withTooltip(boolean pShowInTooltip) {
        return new AdventureModePredicate(this.predicates, pShowInTooltip, this.tooltip);
    }

    private static List<Component> computeTooltip(List<BlockPredicate> pPredicates) {
        for (BlockPredicate blockpredicate : pPredicates) {
            if (blockpredicate.blocks().isEmpty()) {
                return List.of(UNKNOWN_USE);
            }
        }

        return pPredicates.stream()
            .flatMap(p_333785_ -> p_333785_.blocks().orElseThrow().stream())
            .distinct()
            .map(p_335858_ -> (Component)p_335858_.value().getName().withStyle(ChatFormatting.DARK_GRAY))
            .toList();
    }

    public boolean showInTooltip() {
        return this.showInTooltip;
    }

    @Override
    public boolean equals(Object pOther) {
        if (this == pOther) {
            return true;
        } else {
            return !(pOther instanceof AdventureModePredicate adventuremodepredicate)
                ? false
                : this.predicates.equals(adventuremodepredicate.predicates) && this.showInTooltip == adventuremodepredicate.showInTooltip;
        }
    }

    @Override
    public int hashCode() {
        return this.predicates.hashCode() * 31 + (this.showInTooltip ? 1 : 0);
    }

    @Override
    public String toString() {
        return "AdventureModePredicate{predicates=" + this.predicates + ", showInTooltip=" + this.showInTooltip + "}";
    }
}