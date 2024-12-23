package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemFunction that applies the {@code "SkullOwner"} NBT tag to any player heads based on the given {@link
 * LootContext.EntityTarget}.
 * If the given target does not resolve to a player, nothing happens.
 */
public class FillPlayerHead extends LootItemConditionalFunction {
    public static final MapCodec<FillPlayerHead> CODEC = RecordCodecBuilder.mapCodec(
        p_297099_ -> commonFields(p_297099_)
                .and(LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(p_297096_ -> p_297096_.entityTarget))
                .apply(p_297099_, FillPlayerHead::new)
    );
    private final LootContext.EntityTarget entityTarget;

    public FillPlayerHead(List<LootItemCondition> p_301112_, LootContext.EntityTarget p_80605_) {
        super(p_301112_);
        this.entityTarget = p_80605_;
    }

    @Override
    public LootItemFunctionType<FillPlayerHead> getType() {
        return LootItemFunctions.FILL_PLAYER_HEAD;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.entityTarget.getParam());
    }

    @Override
    public ItemStack run(ItemStack pStack, LootContext pContext) {
        if (pStack.is(Items.PLAYER_HEAD) && pContext.getParamOrNull(this.entityTarget.getParam()) instanceof Player player) {
            pStack.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
        }

        return pStack;
    }

    public static LootItemConditionalFunction.Builder<?> fillPlayerHead(LootContext.EntityTarget pEntityTarget) {
        return simpleBuilder(p_297098_ -> new FillPlayerHead(p_297098_, pEntityTarget));
    }
}