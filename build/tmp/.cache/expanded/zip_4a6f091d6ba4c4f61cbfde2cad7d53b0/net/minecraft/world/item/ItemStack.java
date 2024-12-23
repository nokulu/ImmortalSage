package net.minecraft.world.item;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.NullOps;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public final class ItemStack implements DataComponentHolder, net.minecraftforge.common.extensions.IForgeItemStack {
    public static final Codec<Holder<Item>> ITEM_NON_AIR_CODEC = BuiltInRegistries.ITEM
        .holderByNameCodec()
        .validate(
            p_327177_ -> p_327177_.is(Items.AIR.builtInRegistryHolder())
                    ? DataResult.error(() -> "Item must not be minecraft:air")
                    : DataResult.success(p_327177_)
        );
    public static final Codec<ItemStack> CODEC = Codec.lazyInitialized(
        () -> RecordCodecBuilder.create(
                p_341560_ -> p_341560_.group(
                            ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
                            ExtraCodecs.intRange(1, 99).fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
                            DataComponentPatch.CODEC
                                .optionalFieldOf("components", DataComponentPatch.EMPTY)
                                .forGetter(p_327171_ -> p_327171_.components.asPatch())
                        )
                        .apply(p_341560_, ItemStack::new)
            )
    );
    public static final Codec<ItemStack> SINGLE_ITEM_CODEC = Codec.lazyInitialized(
        () -> RecordCodecBuilder.create(
                p_327178_ -> p_327178_.group(
                            ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
                            DataComponentPatch.CODEC
                                .optionalFieldOf("components", DataComponentPatch.EMPTY)
                                .forGetter(p_327155_ -> p_327155_.components.asPatch())
                        )
                        .apply(p_327178_, (p_327172_, p_327173_) -> new ItemStack(p_327172_, 1, p_327173_))
            )
    );
    public static final Codec<ItemStack> STRICT_CODEC = CODEC.validate(ItemStack::validateStrict);
    public static final Codec<ItemStack> STRICT_SINGLE_ITEM_CODEC = SINGLE_ITEM_CODEC.validate(ItemStack::validateStrict);
    public static final Codec<ItemStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
        .xmap(p_327153_ -> p_327153_.orElse(ItemStack.EMPTY), p_327154_ -> p_327154_.isEmpty() ? Optional.empty() : Optional.of(p_327154_));
    public static final Codec<ItemStack> SIMPLE_ITEM_CODEC = ITEM_NON_AIR_CODEC.xmap(ItemStack::new, ItemStack::getItemHolder);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> OPTIONAL_STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ItemStack>() {
        private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Item>> ITEM_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM);

        public ItemStack decode(RegistryFriendlyByteBuf p_328393_) {
            int i = p_328393_.readVarInt();
            if (i <= 0) {
                return ItemStack.EMPTY;
            } else {
                Holder<Item> holder = ITEM_STREAM_CODEC.decode(p_328393_);
                DataComponentPatch datacomponentpatch = DataComponentPatch.STREAM_CODEC.decode(p_328393_);
                return new ItemStack(holder, i, datacomponentpatch);
            }
        }

        public void encode(RegistryFriendlyByteBuf p_332266_, ItemStack p_335702_) {
            if (p_335702_.isEmpty()) {
                p_332266_.writeVarInt(0);
            } else {
                p_332266_.writeVarInt(p_335702_.getCount());
                ITEM_STREAM_CODEC.encode(p_332266_, p_335702_.getItemHolder());
                DataComponentPatch.STREAM_CODEC.encode(p_332266_, p_335702_.components.asPatch());
            }
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ItemStack>() {
        public ItemStack decode(RegistryFriendlyByteBuf p_327992_) {
            ItemStack itemstack = ItemStack.OPTIONAL_STREAM_CODEC.decode(p_327992_);
            if (itemstack.isEmpty()) {
                throw new DecoderException("Empty ItemStack not allowed");
            } else {
                return itemstack;
            }
        }

        public void encode(RegistryFriendlyByteBuf p_331904_, ItemStack p_328866_) {
            if (p_328866_.isEmpty()) {
                throw new EncoderException("Empty ItemStack not allowed");
            } else {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(p_331904_, p_328866_);
            }
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> OPTIONAL_LIST_STREAM_CODEC = OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity));
    public static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity));
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ItemStack EMPTY = new ItemStack((Void)null);
    private static final Component DISABLED_ITEM_TOOLTIP = Component.translatable("item.disabled").withStyle(ChatFormatting.RED);
    private int count;
    private int popTime;
    @Deprecated
    @Nullable
    private final Item item;
    final PatchedDataComponentMap components;
    @Nullable
    private Entity entityRepresentation;

    private static DataResult<ItemStack> validateStrict(ItemStack p_332181_) {
        DataResult<Unit> dataresult = validateComponents(p_332181_.getComponents());
        if (dataresult.isError()) {
            return dataresult.map(p_327165_ -> p_332181_);
        } else {
            return p_332181_.getCount() > p_332181_.getMaxStackSize()
                ? DataResult.error(() -> "Item stack with stack size of " + p_332181_.getCount() + " was larger than maximum: " + p_332181_.getMaxStackSize())
                : DataResult.success(p_332181_);
        }
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ItemStack> validatedStreamCodec(final StreamCodec<RegistryFriendlyByteBuf, ItemStack> pCodec) {
        return new StreamCodec<RegistryFriendlyByteBuf, ItemStack>() {
            public ItemStack decode(RegistryFriendlyByteBuf p_330762_) {
                ItemStack itemstack = pCodec.decode(p_330762_);
                if (!itemstack.isEmpty()) {
                    RegistryOps<Unit> registryops = p_330762_.registryAccess().createSerializationContext(NullOps.INSTANCE);
                    ItemStack.CODEC.encodeStart(registryops, itemstack).getOrThrow(DecoderException::new);
                }

                return itemstack;
            }

            public void encode(RegistryFriendlyByteBuf p_336131_, ItemStack p_329943_) {
                pCodec.encode(p_336131_, p_329943_);
            }
        };
    }

    public Optional<TooltipComponent> getTooltipImage() {
        return this.getItem().getTooltipImage(this);
    }

    @Override
    public DataComponentMap getComponents() {
        return (DataComponentMap)(!this.isEmpty() ? this.components : DataComponentMap.EMPTY);
    }

    public DataComponentMap getPrototype() {
        return !this.isEmpty() ? this.getItem().components() : DataComponentMap.EMPTY;
    }

    public DataComponentPatch getComponentsPatch() {
        return !this.isEmpty() ? this.components.asPatch() : DataComponentPatch.EMPTY;
    }

    public ItemStack(ItemLike pItem) {
        this(pItem, 1);
    }

    public ItemStack(Holder<Item> p_204116_) {
        this(p_204116_.value(), 1);
    }

    public ItemStack(Holder<Item> p_310702_, int p_41605_, DataComponentPatch p_328221_) {
        this(p_310702_.value(), p_41605_, PatchedDataComponentMap.fromPatch(p_310702_.value().components(), p_328221_));
    }

    public ItemStack(Holder<Item> pItem, int pCount) {
        this(pItem.value(), pCount);
    }

    public ItemStack(ItemLike pItem, int pCount) {
        this(pItem, pCount, new PatchedDataComponentMap(pItem.asItem().components()));
    }

    private ItemStack(ItemLike pItem, int pCount, PatchedDataComponentMap pComponents) {
        this.item = pItem.asItem();
        this.count = pCount;
        this.components = pComponents;
        this.getItem().verifyComponentsAfterLoad(this);
    }

    private ItemStack(@Nullable Void pUnused) {
        this.item = null;
        this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
    }

    public static DataResult<Unit> validateComponents(DataComponentMap pComponents) {
        if (pComponents.has(DataComponents.MAX_DAMAGE) && pComponents.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1) {
            return DataResult.error(() -> "Item cannot be both damageable and stackable");
        } else {
            ItemContainerContents itemcontainercontents = pComponents.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

            for (ItemStack itemstack : itemcontainercontents.nonEmptyItems()) {
                int i = itemstack.getCount();
                int j = itemstack.getMaxStackSize();
                if (i > j) {
                    return DataResult.error(() -> "Item stack with count of " + i + " was larger than maximum: " + j);
                }
            }

            return DataResult.success(Unit.INSTANCE);
        }
    }

    public static Optional<ItemStack> parse(HolderLookup.Provider pLookupProvider, Tag pTag) {
        return CODEC.parse(pLookupProvider.createSerializationContext(NbtOps.INSTANCE), pTag)
            .resultOrPartial(p_327167_ -> LOGGER.error("Tried to load invalid item: '{}'", p_327167_));
    }

    public static ItemStack parseOptional(HolderLookup.Provider pLookupProvider, CompoundTag pTag) {
        return pTag.isEmpty() ? EMPTY : parse(pLookupProvider, pTag).orElse(EMPTY);
    }

    public boolean isEmpty() {
        return this == EMPTY || this.item == Items.AIR || this.count <= 0;
    }

    public boolean isItemEnabled(FeatureFlagSet pEnabledFlags) {
        return this.isEmpty() || this.getItem().isEnabled(pEnabledFlags);
    }

    public ItemStack split(int pAmount) {
        int i = Math.min(pAmount, this.getCount());
        ItemStack itemstack = this.copyWithCount(i);
        this.shrink(i);
        return itemstack;
    }

    public ItemStack copyAndClear() {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            ItemStack itemstack = this.copy();
            this.setCount(0);
            return itemstack;
        }
    }

    public Item getItem() {
        return this.isEmpty() ? Items.AIR : this.item;
    }

    public Holder<Item> getItemHolder() {
        return this.getItem().builtInRegistryHolder();
    }

    public boolean is(TagKey<Item> pTag) {
        return this.getItem().builtInRegistryHolder().is(pTag);
    }

    public boolean is(Item pItem) {
        return this.getItem() == pItem;
    }

    public boolean is(Predicate<Holder<Item>> pItem) {
        return pItem.test(this.getItem().builtInRegistryHolder());
    }

    public boolean is(Holder<Item> pItem) {
        return this.getItem().builtInRegistryHolder() == pItem;
    }

    public boolean is(HolderSet<Item> pItem) {
        return pItem.contains(this.getItemHolder());
    }

    public Stream<TagKey<Item>> getTags() {
        return this.getItem().builtInRegistryHolder().tags();
    }

    public InteractionResult useOn(UseOnContext pContext) {
        if (!pContext.getLevel().isClientSide) return net.minecraftforge.common.ForgeHooks.onPlaceItemIntoWorld(pContext);
        return onItemUse(pContext, (c) -> getItem().useOn(pContext));
    }

    public InteractionResult onItemUseFirst(UseOnContext pContext) {
        return onItemUse(pContext, (c) -> getItem().onItemUseFirst(this, pContext));
    }

    private InteractionResult onItemUse(UseOnContext pContext, java.util.function.Function<UseOnContext, InteractionResult> callback) {
        Player player = pContext.getPlayer();
        BlockPos blockpos = pContext.getClickedPos();
        if (player != null && !player.getAbilities().mayBuild && !this.canPlaceOnBlockInAdventureMode(new BlockInWorld(pContext.getLevel(), blockpos, false))) {
            return InteractionResult.PASS;
        } else {
            Item item = this.getItem();
            InteractionResult interactionresult = callback.apply(pContext);
            if (player != null && interactionresult.indicateItemUse()) {
                player.awardStat(Stats.ITEM_USED.get(item));
            }

            return interactionresult;
        }
    }

    public float getDestroySpeed(BlockState pState) {
        return this.getItem().getDestroySpeed(this, pState);
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        return this.getItem().use(pLevel, pPlayer, pUsedHand);
    }

    public ItemStack finishUsingItem(Level pLevel, LivingEntity pLivingEntity) {
        return this.getItem().finishUsingItem(this, pLevel, pLivingEntity);
    }

    public Tag save(HolderLookup.Provider pLevelRegistryAccess, Tag pOutputTag) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        } else {
            return CODEC.encode(this, pLevelRegistryAccess.createSerializationContext(NbtOps.INSTANCE), pOutputTag).getOrThrow();
        }
    }

    public Tag save(HolderLookup.Provider pLevelRegistryAccess) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        } else {
            return CODEC.encodeStart(pLevelRegistryAccess.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }
    }

    public Tag saveOptional(HolderLookup.Provider pLevelRegistryAccess) {
        return (Tag)(this.isEmpty() ? new CompoundTag() : this.save(pLevelRegistryAccess, new CompoundTag()));
    }

    public int getMaxStackSize() {
        return this.getOrDefault(DataComponents.MAX_STACK_SIZE, Integer.valueOf(1));
    }

    public boolean isStackable() {
        return this.getMaxStackSize() > 1 && (!this.isDamageableItem() || !this.isDamaged());
    }

    public boolean isDamageableItem() {
        return this.has(DataComponents.MAX_DAMAGE) && !this.has(DataComponents.UNBREAKABLE) && this.has(DataComponents.DAMAGE);
    }

    public boolean isDamaged() {
        return this.isDamageableItem() && this.getDamageValue() > 0;
    }

    public int getDamageValue() {
        return Mth.clamp(this.getOrDefault(DataComponents.DAMAGE, Integer.valueOf(0)), 0, this.getMaxDamage());
    }

    public void setDamageValue(int pDamage) {
        this.set(DataComponents.DAMAGE, Mth.clamp(pDamage, 0, this.getMaxDamage()));
    }

    public int getMaxDamage() {
        return this.getOrDefault(DataComponents.MAX_DAMAGE, Integer.valueOf(0));
    }

    public void hurtAndBreak(int pDamage, ServerLevel pLevel, @Nullable ServerPlayer pPlayer, Consumer<Item> pOnBreak) {
        if (this.isDamageableItem()) {
            if (pPlayer == null || !pPlayer.hasInfiniteMaterials()) {
                if (pDamage > 0) {
                    pDamage = EnchantmentHelper.processDurabilityChange(pLevel, this, pDamage);
                    if (pDamage <= 0) {
                        return;
                    }
                }

                if (pPlayer != null && pDamage != 0) {
                    CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(pPlayer, this, this.getDamageValue() + pDamage);
                }

                int i = this.getDamageValue() + pDamage;
                this.setDamageValue(i);
                if (i >= this.getMaxDamage()) {
                    Item item = this.getItem();
                    this.shrink(1);
                    pOnBreak.accept(item);
                }
            }
        }
    }

    public void hurtAndBreak(int pAmount, LivingEntity pEntity, EquipmentSlot pSlot) {
        if (pEntity.level() instanceof ServerLevel serverlevel) {
            this.hurtAndBreak(
                pAmount,
                serverlevel,
                pEntity instanceof ServerPlayer serverplayer ? serverplayer : null,
                p_341563_ -> {
                    if (pEntity instanceof Player player) {
                        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, this, pSlot);
                        player.stopUsingItem(); // Forge: fix MC-168573
                    }
                    pEntity.onEquippedItemBroken(p_341563_, pSlot);
                }
            );
        }
    }

    public ItemStack hurtAndConvertOnBreak(int pAmount, ItemLike pItem, LivingEntity pEntity, EquipmentSlot pSlot) {
        this.hurtAndBreak(pAmount, pEntity, pSlot);
        if (this.isEmpty()) {
            ItemStack itemstack = this.transmuteCopyIgnoreEmpty(pItem, 1);
            if (itemstack.isDamageableItem()) {
                itemstack.setDamageValue(0);
            }

            return itemstack;
        } else {
            return this;
        }
    }

    public boolean isBarVisible() {
        return this.getItem().isBarVisible(this);
    }

    public int getBarWidth() {
        return this.getItem().getBarWidth(this);
    }

    public int getBarColor() {
        return this.getItem().getBarColor(this);
    }

    public boolean overrideStackedOnOther(Slot pSlot, ClickAction pAction, Player pPlayer) {
        return this.getItem().overrideStackedOnOther(this, pSlot, pAction, pPlayer);
    }

    public boolean overrideOtherStackedOnMe(ItemStack pStack, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        return this.getItem().overrideOtherStackedOnMe(this, pStack, pSlot, pAction, pPlayer, pAccess);
    }

    public boolean hurtEnemy(LivingEntity pTarget, Player pAttacker) {
        Item item = this.getItem();
        if (item.hurtEnemy(this, pTarget, pAttacker)) {
            pAttacker.awardStat(Stats.ITEM_USED.get(item));
            return true;
        } else {
            return false;
        }
    }

    public void postHurtEnemy(LivingEntity pTarget, Player pAttacker) {
        this.getItem().postHurtEnemy(this, pTarget, pAttacker);
    }

    public void mineBlock(Level pLevel, BlockState pState, BlockPos pPos, Player pPlayer) {
        Item item = this.getItem();
        if (item.mineBlock(this, pLevel, pState, pPos, pPlayer)) {
            pPlayer.awardStat(Stats.ITEM_USED.get(item));
        }
    }

    public boolean isCorrectToolForDrops(BlockState pState) {
        return this.getItem().isCorrectToolForDrops(this, pState);
    }

    public InteractionResult interactLivingEntity(Player pPlayer, LivingEntity pEntity, InteractionHand pUsedHand) {
        return this.getItem().interactLivingEntity(this, pPlayer, pEntity, pUsedHand);
    }

    public ItemStack copy() {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            ItemStack itemstack = new ItemStack(this.getItem(), this.count, this.components.copy());
            itemstack.setPopTime(this.getPopTime());
            return itemstack;
        }
    }

    public ItemStack copyWithCount(int pCount) {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            ItemStack itemstack = this.copy();
            itemstack.setCount(pCount);
            return itemstack;
        }
    }

    public ItemStack transmuteCopy(ItemLike pItem) {
        return this.transmuteCopy(pItem, this.getCount());
    }

    public ItemStack transmuteCopy(ItemLike pItem, int pCount) {
        return this.isEmpty() ? EMPTY : this.transmuteCopyIgnoreEmpty(pItem, pCount);
    }

    private ItemStack transmuteCopyIgnoreEmpty(ItemLike pItem, int pCount) {
        return new ItemStack(pItem.asItem().builtInRegistryHolder(), pCount, this.components.asPatch());
    }

    public static boolean matches(ItemStack pStack, ItemStack pOther) {
        if (pStack == pOther) {
            return true;
        } else {
            return pStack.getCount() != pOther.getCount() ? false : isSameItemSameComponents(pStack, pOther);
        }
    }

    @Deprecated
    public static boolean listMatches(List<ItemStack> pList, List<ItemStack> pOther) {
        if (pList.size() != pOther.size()) {
            return false;
        } else {
            for (int i = 0; i < pList.size(); i++) {
                if (!matches(pList.get(i), pOther.get(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static boolean isSameItem(ItemStack pStack, ItemStack pOther) {
        return pStack.is(pOther.getItem());
    }

    public static boolean isSameItemSameComponents(ItemStack pStack, ItemStack pOther) {
        if (!pStack.is(pOther.getItem())) {
            return false;
        } else {
            return pStack.isEmpty() && pOther.isEmpty() ? true : Objects.equals(pStack.components, pOther.components);
        }
    }

    public static MapCodec<ItemStack> lenientOptionalFieldOf(String pFieldName) {
        return CODEC.lenientOptionalFieldOf(pFieldName)
            .xmap(p_327174_ -> p_327174_.orElse(EMPTY), p_327162_ -> p_327162_.isEmpty() ? Optional.empty() : Optional.of(p_327162_));
    }

    public static int hashItemAndComponents(@Nullable ItemStack pStack) {
        if (pStack != null) {
            int i = 31 + pStack.getItem().hashCode();
            return 31 * i + pStack.getComponents().hashCode();
        } else {
            return 0;
        }
    }

    @Deprecated
    public static int hashStackList(List<ItemStack> pList) {
        int i = 0;

        for (ItemStack itemstack : pList) {
            i = i * 31 + hashItemAndComponents(itemstack);
        }

        return i;
    }

    public String getDescriptionId() {
        return this.getItem().getDescriptionId(this);
    }

    @Override
    public String toString() {
        return this.getCount() + " " + this.getItem();
    }

    public void inventoryTick(Level pLevel, Entity pEntity, int pInventorySlot, boolean pIsCurrentItem) {
        if (this.popTime > 0) {
            this.popTime--;
        }

        if (this.getItem() != null) {
            this.getItem().inventoryTick(this, pLevel, pEntity, pInventorySlot, pIsCurrentItem);
        }
    }

    public void onCraftedBy(Level pLevel, Player pPlayer, int pAmount) {
        pPlayer.awardStat(Stats.ITEM_CRAFTED.get(this.getItem()), pAmount);
        this.getItem().onCraftedBy(this, pLevel, pPlayer);
    }

    public void onCraftedBySystem(Level pLevel) {
        this.getItem().onCraftedPostProcess(this, pLevel);
    }

    public int getUseDuration(LivingEntity pEntity) {
        return this.getItem().getUseDuration(this, pEntity);
    }

    public UseAnim getUseAnimation() {
        return this.getItem().getUseAnimation(this);
    }

    public void releaseUsing(Level pLevel, LivingEntity pLivingEntity, int pTimeLeft) {
        this.getItem().releaseUsing(this, pLevel, pLivingEntity, pTimeLeft);
    }

    public boolean useOnRelease() {
        return this.getItem().useOnRelease(this);
    }

    @Nullable
    public <T> T set(DataComponentType<? super T> pComponent, @Nullable T pValue) {
        return this.components.set(pComponent, pValue);
    }

    @Nullable
    public <T, U> T update(DataComponentType<T> pComponent, T pDefaultValue, U pUpdateValue, BiFunction<T, U, T> pUpdater) {
        return this.set(pComponent, pUpdater.apply(this.getOrDefault(pComponent, pDefaultValue), pUpdateValue));
    }

    @Nullable
    public <T> T update(DataComponentType<T> pComponent, T pDefaultValue, UnaryOperator<T> pUpdater) {
        T t = this.getOrDefault(pComponent, pDefaultValue);
        return this.set(pComponent, pUpdater.apply(t));
    }

    @Nullable
    public <T> T remove(DataComponentType<? extends T> pComponent) {
        return this.components.remove(pComponent);
    }

    public void applyComponentsAndValidate(DataComponentPatch pComponents) {
        DataComponentPatch datacomponentpatch = this.components.asPatch();
        this.components.applyPatch(pComponents);
        Optional<Error<ItemStack>> optional = validateStrict(this).error();
        if (optional.isPresent()) {
            LOGGER.error("Failed to apply component patch '{}' to item: '{}'", pComponents, optional.get().message());
            this.components.restorePatch(datacomponentpatch);
        } else {
            this.getItem().verifyComponentsAfterLoad(this);
        }
    }

    public void applyComponents(DataComponentPatch pComponents) {
        this.components.applyPatch(pComponents);
        this.getItem().verifyComponentsAfterLoad(this);
    }

    public void applyComponents(DataComponentMap pComponents) {
        this.components.setAll(pComponents);
        this.getItem().verifyComponentsAfterLoad(this);
    }

    public Component getHoverName() {
        Component component = this.get(DataComponents.CUSTOM_NAME);
        if (component != null) {
            return component;
        } else {
            Component component1 = this.get(DataComponents.ITEM_NAME);
            return component1 != null ? component1 : this.getItem().getName(this);
        }
    }

    private <T extends TooltipProvider> void addToTooltip(
        DataComponentType<T> pComponent, Item.TooltipContext pContext, Consumer<Component> pTooltipAdder, TooltipFlag pTooltipFlag
    ) {
        T t = (T)this.get(pComponent);
        if (t != null) {
            t.addToTooltip(pContext, pTooltipAdder, pTooltipFlag);
        }
    }

    public List<Component> getTooltipLines(Item.TooltipContext pTooltipContext, @Nullable Player pPlayer, TooltipFlag pTooltipFlag) {
        if (!pTooltipFlag.isCreative() && this.has(DataComponents.HIDE_TOOLTIP)) {
            return List.of();
        } else {
            List<Component> list = Lists.newArrayList();
            MutableComponent mutablecomponent = Component.empty().append(this.getHoverName()).withStyle(this.getRarity().color());
            if (this.has(DataComponents.CUSTOM_NAME)) {
                mutablecomponent.withStyle(ChatFormatting.ITALIC);
            }

            list.add(mutablecomponent);
            if (!pTooltipFlag.isAdvanced() && !this.has(DataComponents.CUSTOM_NAME) && this.is(Items.FILLED_MAP)) {
                MapId mapid = this.get(DataComponents.MAP_ID);
                if (mapid != null) {
                    list.add(MapItem.getTooltipForId(mapid));
                }
            }

            Consumer<Component> consumer = list::add;
            if (!this.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
                this.getItem().appendHoverText(this, pTooltipContext, list, pTooltipFlag);
            }

            this.addToTooltip(DataComponents.JUKEBOX_PLAYABLE, pTooltipContext, consumer, pTooltipFlag);
            this.addToTooltip(DataComponents.TRIM, pTooltipContext, consumer, pTooltipFlag);
            this.addToTooltip(DataComponents.STORED_ENCHANTMENTS, pTooltipContext, consumer, pTooltipFlag);
            this.addToTooltip(DataComponents.ENCHANTMENTS, pTooltipContext, consumer, pTooltipFlag);
            this.addToTooltip(DataComponents.DYED_COLOR, pTooltipContext, consumer, pTooltipFlag);
            this.addToTooltip(DataComponents.LORE, pTooltipContext, consumer, pTooltipFlag);
            this.addAttributeTooltips(consumer, pPlayer);
            this.addToTooltip(DataComponents.UNBREAKABLE, pTooltipContext, consumer, pTooltipFlag);
            AdventureModePredicate adventuremodepredicate = this.get(DataComponents.CAN_BREAK);
            if (adventuremodepredicate != null && adventuremodepredicate.showInTooltip()) {
                consumer.accept(CommonComponents.EMPTY);
                consumer.accept(AdventureModePredicate.CAN_BREAK_HEADER);
                adventuremodepredicate.addToTooltip(consumer);
            }

            AdventureModePredicate adventuremodepredicate1 = this.get(DataComponents.CAN_PLACE_ON);
            if (adventuremodepredicate1 != null && adventuremodepredicate1.showInTooltip()) {
                consumer.accept(CommonComponents.EMPTY);
                consumer.accept(AdventureModePredicate.CAN_PLACE_HEADER);
                adventuremodepredicate1.addToTooltip(consumer);
            }

            if (pTooltipFlag.isAdvanced()) {
                if (this.isDamaged()) {
                    list.add(Component.translatable("item.durability", this.getMaxDamage() - this.getDamageValue(), this.getMaxDamage()));
                }

                list.add(Component.literal(BuiltInRegistries.ITEM.getKey(this.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
                int i = this.components.size();
                if (i > 0) {
                    list.add(Component.translatable("item.components", i).withStyle(ChatFormatting.DARK_GRAY));
                }
            }

            if (pPlayer != null && !this.getItem().isEnabled(pPlayer.level().enabledFeatures())) {
                list.add(DISABLED_ITEM_TOOLTIP);
            }

            net.minecraftforge.event.ForgeEventFactory.onItemTooltip(this, pPlayer, list, pTooltipFlag);

            return list;
        }
    }

    private void addAttributeTooltips(Consumer<Component> pTooltipAdder, @Nullable Player pPlayer) {
        ItemAttributeModifiers itemattributemodifiers = this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (itemattributemodifiers.showInTooltip()) {
            for (EquipmentSlotGroup equipmentslotgroup : EquipmentSlotGroup.values()) {
                MutableBoolean mutableboolean = new MutableBoolean(true);
                this.forEachModifier(equipmentslotgroup, (p_341553_, p_341554_) -> {
                    if (mutableboolean.isTrue()) {
                        pTooltipAdder.accept(CommonComponents.EMPTY);
                        pTooltipAdder.accept(Component.translatable("item.modifiers." + equipmentslotgroup.getSerializedName()).withStyle(ChatFormatting.GRAY));
                        mutableboolean.setFalse();
                    }

                    this.addModifierTooltip(pTooltipAdder, pPlayer, p_341553_, p_341554_);
                });
            }
        }
    }

    private void addModifierTooltip(Consumer<Component> pTooltipAdder, @Nullable Player pPlayer, Holder<Attribute> pAttribute, AttributeModifier pModifier) {
        double d0 = pModifier.amount();
        boolean flag = false;
        if (pPlayer != null) {
            if (pModifier.is(Item.BASE_ATTACK_DAMAGE_ID)) {
                d0 += pPlayer.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                flag = true;
            } else if (pModifier.is(Item.BASE_ATTACK_SPEED_ID)) {
                d0 += pPlayer.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                flag = true;
            }
        }

        double d1;
        if (pModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE || pModifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            )
         {
            d1 = d0 * 100.0;
        } else if (pAttribute.is(Attributes.KNOCKBACK_RESISTANCE)) {
            d1 = d0 * 10.0;
        } else {
            d1 = d0;
        }

        if (flag) {
            pTooltipAdder.accept(
                CommonComponents.space()
                    .append(
                        Component.translatable(
                            "attribute.modifier.equals." + pModifier.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
                            Component.translatable(pAttribute.value().getDescriptionId())
                        )
                    )
                    .withStyle(ChatFormatting.DARK_GREEN)
            );
        } else if (d0 > 0.0) {
            pTooltipAdder.accept(
                Component.translatable(
                        "attribute.modifier.plus." + pModifier.operation().id(),
                        ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
                        Component.translatable(pAttribute.value().getDescriptionId())
                    )
                    .withStyle(pAttribute.value().getStyle(true))
            );
        } else if (d0 < 0.0) {
            pTooltipAdder.accept(
                Component.translatable(
                        "attribute.modifier.take." + pModifier.operation().id(),
                        ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-d1),
                        Component.translatable(pAttribute.value().getDescriptionId())
                    )
                    .withStyle(pAttribute.value().getStyle(false))
            );
        }
    }

    public boolean hasFoil() {
        Boolean obool = this.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        return obool != null ? obool : this.getItem().isFoil(this);
    }

    public Rarity getRarity() {
        Rarity rarity = this.getOrDefault(DataComponents.RARITY, Rarity.COMMON);
        if (!this.isEnchanted()) {
            return rarity;
        } else {
            return switch (rarity) {
                case COMMON, UNCOMMON -> Rarity.RARE;
                case RARE -> Rarity.EPIC;
                default -> rarity;
            };
        }
    }

    public boolean isEnchantable() {
        if (!this.getItem().isEnchantable(this)) {
            return false;
        } else {
            ItemEnchantments itemenchantments = this.get(DataComponents.ENCHANTMENTS);
            return itemenchantments != null && itemenchantments.isEmpty();
        }
    }

    public void enchant(Holder<Enchantment> pEnchantment, int pLevel) {
        EnchantmentHelper.updateEnchantments(this, p_341557_ -> p_341557_.upgrade(pEnchantment, pLevel));
    }

    public boolean isEnchanted() {
        return !this.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }

    public ItemEnchantments getEnchantments() {
        return this.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    public boolean isFramed() {
        return this.entityRepresentation instanceof ItemFrame;
    }

    public void setEntityRepresentation(@Nullable Entity pEntity) {
        if (!this.isEmpty()) {
            this.entityRepresentation = pEntity;
        }
    }

    @Nullable
    public ItemFrame getFrame() {
        return this.entityRepresentation instanceof ItemFrame ? (ItemFrame)this.getEntityRepresentation() : null;
    }

    @Nullable
    public Entity getEntityRepresentation() {
        return !this.isEmpty() ? this.entityRepresentation : null;
    }

    public void forEachModifier(EquipmentSlotGroup pSlotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> pAction) {
        ItemAttributeModifiers itemattributemodifiers = this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (!itemattributemodifiers.modifiers().isEmpty()) {
            itemattributemodifiers.forEach(pSlotGroup, pAction);
        } else {
            this.getDefaultAttributeModifiers().forEach(pSlotGroup, pAction);
        }

        EnchantmentHelper.forEachModifier(this, pSlotGroup, pAction);
    }

    public void forEachModifier(EquipmentSlot pEquipmentSLot, BiConsumer<Holder<Attribute>, AttributeModifier> pAction) {
        ItemAttributeModifiers itemattributemodifiers = this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (!itemattributemodifiers.modifiers().isEmpty()) {
            itemattributemodifiers.forEach(pEquipmentSLot, pAction);
        } else {
            this.getItem().getAttributeModifiers(pEquipmentSLot, this).forEach(pEquipmentSLot, pAction);
        }

        EnchantmentHelper.forEachModifier(this, pEquipmentSLot, pAction);
    }

    public Component getDisplayName() {
        MutableComponent mutablecomponent = Component.empty().append(this.getHoverName());
        if (this.has(DataComponents.CUSTOM_NAME)) {
            mutablecomponent.withStyle(ChatFormatting.ITALIC);
        }

        MutableComponent mutablecomponent1 = ComponentUtils.wrapInSquareBrackets(mutablecomponent);
        if (!this.isEmpty()) {
            mutablecomponent1.withStyle(this.getRarity().color())
                .withStyle(p_220170_ -> p_220170_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this))));
        }

        return mutablecomponent1;
    }

    public boolean canPlaceOnBlockInAdventureMode(BlockInWorld pBlock) {
        AdventureModePredicate adventuremodepredicate = this.get(DataComponents.CAN_PLACE_ON);
        return adventuremodepredicate != null && adventuremodepredicate.test(pBlock);
    }

    public boolean canBreakBlockInAdventureMode(BlockInWorld pBlock) {
        AdventureModePredicate adventuremodepredicate = this.get(DataComponents.CAN_BREAK);
        return adventuremodepredicate != null && adventuremodepredicate.test(pBlock);
    }

    public int getPopTime() {
        return this.popTime;
    }

    public void setPopTime(int pPopTime) {
        this.popTime = pPopTime;
    }

    public int getCount() {
        return this.isEmpty() ? 0 : this.count;
    }

    public void setCount(int pCount) {
        this.count = pCount;
    }

    public void limitSize(int pMaxSize) {
        if (!this.isEmpty() && this.getCount() > pMaxSize) {
            this.setCount(pMaxSize);
        }
    }

    public void grow(int pIncrement) {
        this.setCount(this.getCount() + pIncrement);
    }

    public void shrink(int pDecrement) {
        this.grow(-pDecrement);
    }

    public void consume(int pAmount, @Nullable LivingEntity pEntity) {
        if (pEntity == null || !pEntity.hasInfiniteMaterials()) {
            this.shrink(pAmount);
        }
    }

    public ItemStack consumeAndReturn(int pAmount, @Nullable LivingEntity pEntity) {
        ItemStack itemstack = this.copyWithCount(pAmount);
        this.consume(pAmount, pEntity);
        return itemstack;
    }

    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, int pCount) {
        this.getItem().onUseTick(pLevel, pLivingEntity, this, pCount);
    }

    /** @deprecated Forge: Use {@linkplain net.minecraftforge.common.extensions.IForgeItemStack#onDestroyed(ItemEntity, net.minecraft.world.damagesource.DamageSource) damage source sensitive version} */
    public void onDestroyed(ItemEntity pItemEntity) {
        this.getItem().onDestroyed(pItemEntity);
    }

    public SoundEvent getDrinkingSound() {
        return this.getItem().getDrinkingSound();
    }

    public SoundEvent getEatingSound() {
        return this.getItem().getEatingSound();
    }

    public SoundEvent getBreakingSound() {
        return this.getItem().getBreakingSound();
    }

    public boolean canBeHurtBy(DamageSource pDamageSource) {
        return !this.has(DataComponents.FIRE_RESISTANT) || !pDamageSource.is(DamageTypeTags.IS_FIRE);
    }
}
