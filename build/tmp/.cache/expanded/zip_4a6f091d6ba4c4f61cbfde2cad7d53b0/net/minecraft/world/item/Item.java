package net.minecraft.world.item;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class Item implements FeatureElement, ItemLike, net.minecraftforge.common.extensions.IForgeItem {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Holder<Item>> STREAM_CODEC =
        net.minecraft.network.codec.ByteBufCodecs.holderRegistry(net.minecraft.core.registries.Registries.ITEM);
    public static final Map<Block, Item> BY_BLOCK = net.minecraftforge.registries.GameData.ItemCallbacks.getBlockItemMap();
    public static final ResourceLocation BASE_ATTACK_DAMAGE_ID = ResourceLocation.withDefaultNamespace("base_attack_damage");
    public static final ResourceLocation BASE_ATTACK_SPEED_ID = ResourceLocation.withDefaultNamespace("base_attack_speed");
    public static final int DEFAULT_MAX_STACK_SIZE = 64;
    public static final int ABSOLUTE_MAX_STACK_SIZE = 99;
    public static final int MAX_BAR_WIDTH = 13;
    private final Holder.Reference<Item> builtInRegistryHolder = BuiltInRegistries.ITEM.createIntrusiveHolder(this);
    private final DataComponentMap components;
    @Nullable
    private final Item craftingRemainingItem;
    @Nullable
    private String descriptionId;
    private final FeatureFlagSet requiredFeatures;

    public static int getId(Item pItem) {
        return pItem == null ? 0 : BuiltInRegistries.ITEM.getId(pItem);
    }

    public static Item byId(int pId) {
        return BuiltInRegistries.ITEM.byId(pId);
    }

    @Deprecated
    public static Item byBlock(Block pBlock) {
        return BY_BLOCK.getOrDefault(pBlock, Items.AIR);
    }

    public Item(Item.Properties pProperties) {
        this.components = pProperties.buildAndValidateComponents();
        this.craftingRemainingItem = pProperties.craftingRemainingItem;
        this.requiredFeatures = pProperties.requiredFeatures;
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            String s = this.getClass().getSimpleName();
            if (!s.endsWith("Item")) {
                LOGGER.error("Item classes should end with Item and {} doesn't.", s);
            }
        }
        initClient();
    }

    @Deprecated
    public Holder.Reference<Item> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    @Nullable
    private DataComponentMap builtComponents = null;

    public DataComponentMap components() {
        if (builtComponents == null) {
            builtComponents = net.minecraftforge.common.ForgeHooks.gatherItemComponents(this, components);
        }

        return builtComponents;
    }

    public int getDefaultMaxStackSize() {
        return builtComponents == null ? this.components.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) : this.builtComponents.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }

    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
    }

    /** @deprecated Forge: {@link net.minecraftforge.common.extensions.IForgeItem#onDestroyed(ItemEntity, DamageSource) Use damage source sensitive version} */
    public void onDestroyed(ItemEntity pItemEntity) {
    }

    public void verifyComponentsAfterLoad(ItemStack pStack) {
    }

    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return true;
    }

    @Override
    public Item asItem() {
        return this;
    }

    public InteractionResult useOn(UseOnContext pContext) {
        return InteractionResult.PASS;
    }

    public float getDestroySpeed(ItemStack pStack, BlockState pState) {
        Tool tool = pStack.get(DataComponents.TOOL);
        return tool != null ? tool.getMiningSpeed(pState) : 1.0F;
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        FoodProperties foodproperties = itemstack.get(DataComponents.FOOD);
        if (foodproperties != null) {
            if (pPlayer.canEat(foodproperties.canAlwaysEat())) {
                pPlayer.startUsingItem(pUsedHand);
                return InteractionResultHolder.consume(itemstack);
            } else {
                return InteractionResultHolder.fail(itemstack);
            }
        } else {
            return InteractionResultHolder.pass(pPlayer.getItemInHand(pUsedHand));
        }
    }

    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        FoodProperties foodproperties = pStack.get(DataComponents.FOOD);
        return foodproperties != null ? pLivingEntity.eat(pLevel, pStack, foodproperties) : pStack;
    }

    public boolean isBarVisible(ItemStack pStack) {
        return pStack.isDamaged();
    }

    public int getBarWidth(ItemStack pStack) {
        return Mth.clamp(Math.round(13.0F - (float)pStack.getDamageValue() * 13.0F / (float)pStack.getMaxDamage()), 0, 13);
    }

    public int getBarColor(ItemStack pStack) {
        int i = pStack.getMaxDamage();
        float f = Math.max(0.0F, ((float)i - (float)pStack.getDamageValue()) / (float)i);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    public boolean overrideStackedOnOther(ItemStack pStack, Slot pSlot, ClickAction pAction, Player pPlayer) {
        return false;
    }

    public boolean overrideOtherStackedOnMe(ItemStack pStack, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        return false;
    }

    public float getAttackDamageBonus(Entity pTarget, float pDamage, DamageSource pDamageSource) {
        return 0.0F;
    }

    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        return false;
    }

    public void postHurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
    }

    public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pMiningEntity) {
        Tool tool = pStack.get(DataComponents.TOOL);
        if (tool == null) {
            return false;
        } else {
            if (!pLevel.isClientSide && pState.getDestroySpeed(pLevel, pPos) != 0.0F && tool.damagePerBlock() > 0) {
                pStack.hurtAndBreak(tool.damagePerBlock(), pMiningEntity, EquipmentSlot.MAINHAND);
            }

            return true;
        }
    }

    public boolean isCorrectToolForDrops(ItemStack pStack, BlockState pState) {
        Tool tool = pStack.get(DataComponents.TOOL);
        return tool != null && tool.isCorrectForDrops(pState);
    }

    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        return InteractionResult.PASS;
    }

    public Component getDescription() {
        return Component.translatable(this.getDescriptionId());
    }

    @Override
    public String toString() {
        return BuiltInRegistries.ITEM.wrapAsHolder(this).getRegisteredName();
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("item", BuiltInRegistries.ITEM.getKey(this));
        }

        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public String getDescriptionId(ItemStack pStack) {
        return this.getDescriptionId();
    }

    @Nullable
    @Deprecated // Use ItemStack sensitive version.
    public final Item getCraftingRemainingItem() {
        return this.craftingRemainingItem;
    }

    @Deprecated // Use ItemStack sensitive version.
    public boolean hasCraftingRemainingItem() {
        return this.craftingRemainingItem != null;
    }

    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
    }

    public void onCraftedBy(ItemStack pStack, Level pLevel, Player pPlayer) {
        this.onCraftedPostProcess(pStack, pLevel);
    }

    public void onCraftedPostProcess(ItemStack pStack, Level pLevel) {
    }

    public boolean isComplex() {
        return false;
    }

    public UseAnim getUseAnimation(ItemStack pStack) {
        return pStack.has(DataComponents.FOOD) ? UseAnim.EAT : UseAnim.NONE;
    }

    public int getUseDuration(ItemStack pStack, LivingEntity pEntity) {
        FoodProperties foodproperties = pStack.get(DataComponents.FOOD);
        return foodproperties != null ? foodproperties.eatDurationTicks() : 0;
    }

    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
    }

    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        return Optional.empty();
    }

    public Component getName(ItemStack pStack) {
        return Component.translatable(this.getDescriptionId(pStack));
    }

    public boolean isFoil(ItemStack pStack) {
        return pStack.isEnchanted();
    }

    public boolean isEnchantable(ItemStack pStack) {
        return pStack.getMaxStackSize() == 1 && pStack.has(DataComponents.MAX_DAMAGE);
    }

    protected static BlockHitResult getPlayerPOVHitResult(Level pLevel, Player pPlayer, ClipContext.Fluid pFluidMode) {
        Vec3 vec3 = pPlayer.getEyePosition();
        Vec3 vec31 = vec3.add(pPlayer.calculateViewVector(pPlayer.getXRot(), pPlayer.getYRot()).scale(pPlayer.blockInteractionRange()));
        return pLevel.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, pFluidMode, pPlayer));
    }

    /** @deprecated in favor of {@link net.minecraftforge.common.extensions.IForgeItem#getEnchantmentValue(ItemStack)}  */
    public int getEnchantmentValue() {
        return 0;
    }

    public boolean isValidRepairItem(ItemStack pStack, ItemStack pRepairCandidate) {
        return false;
    }

    /** @deprecated in favor of {@link net.minecraftforge.common.extensions.IForgeItem#getDefaultAttributeModifiers(ItemStack)}  */
    @Deprecated
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return ItemAttributeModifiers.EMPTY;
    }

    public boolean useOnRelease(ItemStack pStack) {
        return false;
    }

    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }

    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_EAT;
    }

    public SoundEvent getBreakingSound() {
        return SoundEvents.ITEM_BREAK;
    }

    public boolean canFitInsideContainerItems() {
        return true;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    private Object renderProperties;

    /*
       DO NOT CALL, IT WILL DISAPPEAR IN THE FUTURE
       Call RenderProperties.get instead
     */
    public Object getRenderPropertiesInternal() {
        return renderProperties;
    }

    private void initClient() {
        // Minecraft instance isn't available in datagen, so don't call initializeClient if in datagen
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT && !net.minecraftforge.fml.loading.FMLLoader.getLaunchHandler().isData()) {
            initializeClient(properties -> {
                if (properties == this) {
                    throw new IllegalStateException("Don't extend IItemRenderProperties in your item, use an anonymous class instead.");
                }
                this.renderProperties = properties;
            });
        }
    }

    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) { }

    public static class Properties {
        private static final Interner<DataComponentMap> COMPONENT_INTERNER = Interners.newStrongInterner();
        @Nullable
        private DataComponentMap.Builder components;
        @Nullable
        Item craftingRemainingItem;
        FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;

        public Item.Properties food(FoodProperties pFood) {
            return this.component(DataComponents.FOOD, pFood);
        }

        public Item.Properties stacksTo(int pMaxStackSize) {
            return this.component(DataComponents.MAX_STACK_SIZE, pMaxStackSize);
        }

        public Item.Properties durability(int pMaxDamage) {
            this.component(DataComponents.MAX_DAMAGE, pMaxDamage);
            this.component(DataComponents.MAX_STACK_SIZE, 1);
            this.component(DataComponents.DAMAGE, 0);
            return this;
        }

        public Item.Properties craftRemainder(Item pCraftingRemainingItem) {
            this.craftingRemainingItem = pCraftingRemainingItem;
            return this;
        }

        public Item.Properties rarity(Rarity pRarity) {
            return this.component(DataComponents.RARITY, pRarity);
        }

        public Item.Properties fireResistant() {
            return this.component(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
        }

        public Item.Properties jukeboxPlayable(ResourceKey<JukeboxSong> pSong) {
            return this.component(DataComponents.JUKEBOX_PLAYABLE, new JukeboxPlayable(new EitherHolder<>(pSong), true));
        }

        public Item.Properties requiredFeatures(FeatureFlag... pRequiredFeatures) {
            this.requiredFeatures = FeatureFlags.REGISTRY.subset(pRequiredFeatures);
            return this;
        }

        public <T> Item.Properties component(DataComponentType<T> pComponent, T pValue) {
            if (this.components == null) {
                this.components = DataComponentMap.builder().addAll(DataComponents.COMMON_ITEM_COMPONENTS);
            }

            this.components.set(pComponent, pValue);
            return this;
        }

        public Item.Properties attributes(ItemAttributeModifiers pAttributes) {
            return this.component(DataComponents.ATTRIBUTE_MODIFIERS, pAttributes);
        }

        DataComponentMap buildAndValidateComponents() {
            DataComponentMap datacomponentmap = this.buildComponents();
            if (datacomponentmap.has(DataComponents.DAMAGE) && datacomponentmap.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1) {
                throw new IllegalStateException("Item cannot have both durability and be stackable");
            } else {
                return datacomponentmap;
            }
        }

        private DataComponentMap buildComponents() {
            return this.components == null ? DataComponents.COMMON_ITEM_COMPONENTS : COMPONENT_INTERNER.intern(this.components.build());
        }
    }

    public interface TooltipContext {
        Item.TooltipContext EMPTY = new Item.TooltipContext() {
            @Nullable
            @Override
            public HolderLookup.Provider registries() {
                return null;
            }

            @Override
            public float tickRate() {
                return 20.0F;
            }

            @Nullable
            @Override
            public MapItemSavedData mapData(MapId p_334227_) {
                return null;
            }
        };

        @Nullable
        HolderLookup.Provider registries();

        float tickRate();

        @Nullable
        MapItemSavedData mapData(MapId pMapId);

        @Nullable
        default Level level() {
            return null;
        }

        static Item.TooltipContext of(@Nullable final Level pLevel) {
            return pLevel == null ? EMPTY : new Item.TooltipContext() {
                @Override
                public HolderLookup.Provider registries() {
                    return pLevel.registryAccess();
                }

                @Override
                public float tickRate() {
                    return pLevel.tickRateManager().tickrate();
                }

                @Override
                public MapItemSavedData mapData(MapId p_330171_) {
                    return pLevel.getMapData(p_330171_);
                }

                @Override
                public Level level() {
                    return pLevel;
                }
            };
        }

        static Item.TooltipContext of(final HolderLookup.Provider pRegistries) {
            return new Item.TooltipContext() {
                @Override
                public HolderLookup.Provider registries() {
                    return pRegistries;
                }

                @Override
                public float tickRate() {
                    return 20.0F;
                }

                @Nullable
                @Override
                public MapItemSavedData mapData(MapId p_332386_) {
                    return null;
                }
            };
        }
    }
}
