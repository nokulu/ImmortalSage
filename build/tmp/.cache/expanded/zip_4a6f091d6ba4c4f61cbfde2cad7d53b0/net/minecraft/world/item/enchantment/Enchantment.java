package net.minecraft.world.item.enchantment;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.effects.DamageImmunity;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableFloat;

public record Enchantment(Component description, Enchantment.EnchantmentDefinition definition, HolderSet<Enchantment> exclusiveSet, DataComponentMap effects) {
    public static final int MAX_LEVEL = 255;
    public static final Codec<Enchantment> DIRECT_CODEC = RecordCodecBuilder.create(
        p_344995_ -> p_344995_.group(
                    ComponentSerialization.CODEC.fieldOf("description").forGetter(Enchantment::description),
                    Enchantment.EnchantmentDefinition.CODEC.forGetter(Enchantment::definition),
                    RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("exclusive_set", HolderSet.direct()).forGetter(Enchantment::exclusiveSet),
                    EnchantmentEffectComponents.CODEC.optionalFieldOf("effects", DataComponentMap.EMPTY).forGetter(Enchantment::effects)
                )
                .apply(p_344995_, Enchantment::new)
    );
    public static final Codec<Holder<Enchantment>> CODEC = RegistryFixedCodec.create(Registries.ENCHANTMENT);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Enchantment>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT);

    public static Enchantment.Cost constantCost(int pCost) {
        return new Enchantment.Cost(pCost, 0);
    }

    public static Enchantment.Cost dynamicCost(int pBase, int pPerLevel) {
        return new Enchantment.Cost(pBase, pPerLevel);
    }

    public static Enchantment.EnchantmentDefinition definition(
        HolderSet<Item> pSupportedItems,
        HolderSet<Item> pPrimaryItems,
        int pWeight,
        int pMaxLevel,
        Enchantment.Cost pMinCost,
        Enchantment.Cost pMaxCost,
        int pAnvilCost,
        EquipmentSlotGroup... pSlots
    ) {
        return new Enchantment.EnchantmentDefinition(
            pSupportedItems, Optional.of(pPrimaryItems), pWeight, pMaxLevel, pMinCost, pMaxCost, pAnvilCost, List.of(pSlots)
        );
    }

    public static Enchantment.EnchantmentDefinition definition(
        HolderSet<Item> pSupportedItems,
        int pWeight,
        int pMaxLevel,
        Enchantment.Cost pMinCost,
        Enchantment.Cost pMaxCost,
        int pAnvilCost,
        EquipmentSlotGroup... pSlots
    ) {
        return new Enchantment.EnchantmentDefinition(pSupportedItems, Optional.empty(), pWeight, pMaxLevel, pMinCost, pMaxCost, pAnvilCost, List.of(pSlots));
    }

    public Map<EquipmentSlot, ItemStack> getSlotItems(LivingEntity pEntity) {
        Map<EquipmentSlot, ItemStack> map = Maps.newEnumMap(EquipmentSlot.class);

        for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            if (this.matchingSlot(equipmentslot)) {
                ItemStack itemstack = pEntity.getItemBySlot(equipmentslot);
                if (!itemstack.isEmpty()) {
                    map.put(equipmentslot, itemstack);
                }
            }
        }

        return map;
    }

    public HolderSet<Item> getSupportedItems() {
        return this.definition.supportedItems();
    }

    public boolean matchingSlot(EquipmentSlot pSlot) {
        return this.definition.slots().stream().anyMatch(p_345380_ -> p_345380_.test(pSlot));
    }

    public boolean isPrimaryItem(ItemStack pStack) {
        return this.isSupportedItem(pStack) && (this.definition.primaryItems.isEmpty() || pStack.is(this.definition.primaryItems.get()));
    }

    public boolean isSupportedItem(ItemStack pItem) {
        return pItem.is(this.definition.supportedItems);
    }

    public int getWeight() {
        return this.definition.weight();
    }

    public int getAnvilCost() {
        return this.definition.anvilCost();
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return this.definition.maxLevel();
    }

    public int getMinCost(int pLevel) {
        return this.definition.minCost().calculate(pLevel);
    }

    public int getMaxCost(int pLevel) {
        return this.definition.maxCost().calculate(pLevel);
    }

    @Override
    public String toString() {
        return "Enchantment " + this.description.getString();
    }

    public static boolean areCompatible(Holder<Enchantment> pFirst, Holder<Enchantment> pSecond) {
        return !pFirst.equals(pSecond) && !pFirst.value().exclusiveSet.contains(pSecond) && !pSecond.value().exclusiveSet.contains(pFirst);
    }

    public static Component getFullname(Holder<Enchantment> pEnchantment, int pLevel) {
        MutableComponent mutablecomponent = pEnchantment.value().description.copy();
        if (pEnchantment.is(EnchantmentTags.CURSE)) {
            ComponentUtils.mergeStyles(mutablecomponent, Style.EMPTY.withColor(ChatFormatting.RED));
        } else {
            ComponentUtils.mergeStyles(mutablecomponent, Style.EMPTY.withColor(ChatFormatting.GRAY));
        }

        if (pLevel != 1 || pEnchantment.value().getMaxLevel() != 1) {
            mutablecomponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + pLevel));
        }

        return mutablecomponent;
    }

    public boolean canEnchant(ItemStack pStack) {
        return this.definition.supportedItems().contains(pStack.getItemHolder());
    }

    public <T> List<T> getEffects(DataComponentType<List<T>> pComponent) {
        return this.effects.getOrDefault(pComponent, List.of());
    }

    public boolean isImmuneToDamage(ServerLevel pLevel, int pEnchantmentLevel, Entity pEntity, DamageSource pDamageSource) {
        LootContext lootcontext = damageContext(pLevel, pEnchantmentLevel, pEntity, pDamageSource);

        for (ConditionalEffect<DamageImmunity> conditionaleffect : this.getEffects(EnchantmentEffectComponents.DAMAGE_IMMUNITY)) {
            if (conditionaleffect.matches(lootcontext)) {
                return true;
            }
        }

        return false;
    }

    public void modifyDamageProtection(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pStack, Entity pEntity, DamageSource pDamageSource, MutableFloat pDamageProtection) {
        LootContext lootcontext = damageContext(pLevel, pEnchantmentLevel, pEntity, pDamageSource);

        for (ConditionalEffect<EnchantmentValueEffect> conditionaleffect : this.getEffects(EnchantmentEffectComponents.DAMAGE_PROTECTION)) {
            if (conditionaleffect.matches(lootcontext)) {
                pDamageProtection.setValue(conditionaleffect.effect().process(pEnchantmentLevel, pEntity.getRandom(), pDamageProtection.floatValue()));
            }
        }
    }

    public void modifyDurabilityChange(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, MutableFloat pDurabilityChange) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.ITEM_DAMAGE, pLevel, pEnchantmentLevel, pTool, pDurabilityChange);
    }

    public void modifyAmmoCount(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, MutableFloat pAmmoCount) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.AMMO_USE, pLevel, pEnchantmentLevel, pTool, pAmmoCount);
    }

    public void modifyPiercingCount(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, MutableFloat pPiercingCount) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.PROJECTILE_PIERCING, pLevel, pEnchantmentLevel, pTool, pPiercingCount);
    }

    public void modifyBlockExperience(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, MutableFloat pBlockExperience) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.BLOCK_EXPERIENCE, pLevel, pEnchantmentLevel, pTool, pBlockExperience);
    }

    public void modifyMobExperience(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, Entity pEntity, MutableFloat pMobExperience) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.MOB_EXPERIENCE, pLevel, pEnchantmentLevel, pTool, pEntity, pMobExperience);
    }

    public void modifyDurabilityToRepairFromXp(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, MutableFloat pDurabilityToRepairFromXp) {
        this.modifyItemFilteredCount(EnchantmentEffectComponents.REPAIR_WITH_XP, pLevel, pEnchantmentLevel, pTool, pDurabilityToRepairFromXp);
    }

    public void modifyTridentReturnToOwnerAcceleration(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, Entity pEntity, MutableFloat pTridentReturnToOwnerAcceleration) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.TRIDENT_RETURN_ACCELERATION, pLevel, pEnchantmentLevel, pTool, pEntity, pTridentReturnToOwnerAcceleration);
    }

    public void modifyTridentSpinAttackStrength(RandomSource pRandom, int pEnchantmentLevel, MutableFloat pValue) {
        this.modifyUnfilteredValue(EnchantmentEffectComponents.TRIDENT_SPIN_ATTACK_STRENGTH, pRandom, pEnchantmentLevel, pValue);
    }

    public void modifyFishingTimeReduction(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, Entity pEntity, MutableFloat pFishingTimeReduction) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.FISHING_TIME_REDUCTION, pLevel, pEnchantmentLevel, pTool, pEntity, pFishingTimeReduction);
    }

    public void modifyFishingLuckBonus(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, Entity pEntity, MutableFloat pFishingLuckBonus) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.FISHING_LUCK_BONUS, pLevel, pEnchantmentLevel, pTool, pEntity, pFishingLuckBonus);
    }

    public void modifyDamage(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, Entity pEntity, DamageSource pDamageSource, MutableFloat pDamage) {
        this.modifyDamageFilteredValue(EnchantmentEffectComponents.DAMAGE, pLevel, pEnchantmentLevel, pTool, pEntity, pDamageSource, pDamage);
    }

    public void modifyFallBasedDamage(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, Entity pEntity, DamageSource pDamageSource, MutableFloat pFallBasedDamage) {
        this.modifyDamageFilteredValue(EnchantmentEffectComponents.SMASH_DAMAGE_PER_FALLEN_BLOCK, pLevel, pEnchantmentLevel, pTool, pEntity, pDamageSource, pFallBasedDamage);
    }

    public void modifyKnockback(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, Entity pEntity, DamageSource pDamageSource, MutableFloat pKnockback) {
        this.modifyDamageFilteredValue(EnchantmentEffectComponents.KNOCKBACK, pLevel, pEnchantmentLevel, pTool, pEntity, pDamageSource, pKnockback);
    }

    public void modifyArmorEffectivness(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, Entity pEntity, DamageSource pDamageSource, MutableFloat pArmorEffectiveness) {
        this.modifyDamageFilteredValue(EnchantmentEffectComponents.ARMOR_EFFECTIVENESS, pLevel, pEnchantmentLevel, pTool, pEntity, pDamageSource, pArmorEffectiveness);
    }

    public static void doPostAttack(
        TargetedConditionalEffect<EnchantmentEntityEffect> pEffect,
        ServerLevel pLevel,
        int pEnchantmentLevel,
        EnchantedItemInUse pItem,
        Entity pEntity,
        DamageSource pDamageSource
    ) {
        if (pEffect.matches(damageContext(pLevel, pEnchantmentLevel, pEntity, pDamageSource))) {
            Entity entity = switch (pEffect.affected()) {
                case ATTACKER -> pDamageSource.getEntity();
                case DAMAGING_ENTITY -> pDamageSource.getDirectEntity();
                case VICTIM -> pEntity;
            };
            if (entity != null) {
                pEffect.effect().apply(pLevel, pEnchantmentLevel, pItem, entity, entity.position());
            }
        }
    }

    public void doPostAttack(
        ServerLevel pLevel, int pEnchantmentLevel, EnchantedItemInUse pItem, EnchantmentTarget pTarget, Entity pEntity, DamageSource pDamageSource
    ) {
        for (TargetedConditionalEffect<EnchantmentEntityEffect> targetedconditionaleffect : this.getEffects(EnchantmentEffectComponents.POST_ATTACK)) {
            if (pTarget == targetedconditionaleffect.enchanted()) {
                doPostAttack(targetedconditionaleffect, pLevel, pEnchantmentLevel, pItem, pEntity, pDamageSource);
            }
        }
    }

    public void modifyProjectileCount(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, Entity pEntity, MutableFloat pProjectileCount) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.PROJECTILE_COUNT, pLevel, pEnchantmentLevel, pTool, pEntity, pProjectileCount);
    }

    public void modifyProjectileSpread(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool, Entity pEntity, MutableFloat pProjectileSpread) {
        this.modifyEntityFilteredValue(EnchantmentEffectComponents.PROJECTILE_SPREAD, pLevel, pEnchantmentLevel, pTool, pEntity, pProjectileSpread);
    }

    public void modifyCrossbowChargeTime(RandomSource pRandom, int pEnchantmentLevel, MutableFloat pValue) {
        this.modifyUnfilteredValue(EnchantmentEffectComponents.CROSSBOW_CHARGE_TIME, pRandom, pEnchantmentLevel, pValue);
    }

    public void modifyUnfilteredValue(DataComponentType<EnchantmentValueEffect> pComponentType, RandomSource pRandom, int pEnchantmentLevel, MutableFloat pValue) {
        EnchantmentValueEffect enchantmentvalueeffect = this.effects.get(pComponentType);
        if (enchantmentvalueeffect != null) {
            pValue.setValue(enchantmentvalueeffect.process(pEnchantmentLevel, pRandom, pValue.floatValue()));
        }
    }

    public void tick(ServerLevel pLevel, int pEnchantmentLevel, EnchantedItemInUse pItem, Entity pEntity) {
        applyEffects(
            this.getEffects(EnchantmentEffectComponents.TICK),
            entityContext(pLevel, pEnchantmentLevel, pEntity, pEntity.position()),
            p_342906_ -> p_342906_.apply(pLevel, pEnchantmentLevel, pItem, pEntity, pEntity.position())
        );
    }

    public void onProjectileSpawned(ServerLevel pLevel, int pEnchantmentLevel, EnchantedItemInUse pItem, Entity pEntity) {
        applyEffects(
            this.getEffects(EnchantmentEffectComponents.PROJECTILE_SPAWNED),
            entityContext(pLevel, pEnchantmentLevel, pEntity, pEntity.position()),
            p_344229_ -> p_344229_.apply(pLevel, pEnchantmentLevel, pItem, pEntity, pEntity.position())
        );
    }

    public void onHitBlock(ServerLevel pLevel, int pEnchantmentLevel, EnchantedItemInUse pItem, Entity pEntity, Vec3 pPos, BlockState pState) {
        applyEffects(
            this.getEffects(EnchantmentEffectComponents.HIT_BLOCK),
            blockHitContext(pLevel, pEnchantmentLevel, pEntity, pPos, pState),
            p_343722_ -> p_343722_.apply(pLevel, pEnchantmentLevel, pItem, pEntity, pPos)
        );
    }

    private void modifyItemFilteredCount(
        DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> pComponentType,
        ServerLevel pLevel,
        int pEnchantmentLevel,
        ItemStack pTool,
        MutableFloat pValue
    ) {
        applyEffects(
            this.getEffects(pComponentType),
            itemContext(pLevel, pEnchantmentLevel, pTool),
            p_343438_ -> pValue.setValue(p_343438_.process(pEnchantmentLevel, pLevel.getRandom(), pValue.getValue()))
        );
    }

    private void modifyEntityFilteredValue(
        DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> pComponentType,
        ServerLevel pLevel,
        int pEnchantmentLevel,
        ItemStack pTool,
        Entity pEntity,
        MutableFloat pValue
    ) {
        applyEffects(
            this.getEffects(pComponentType),
            entityContext(pLevel, pEnchantmentLevel, pEntity, pEntity.position()),
            p_344133_ -> pValue.setValue(p_344133_.process(pEnchantmentLevel, pEntity.getRandom(), pValue.floatValue()))
        );
    }

    private void modifyDamageFilteredValue(
        DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> pComponentType,
        ServerLevel pLevel,
        int pEnchantmentLevel,
        ItemStack pTool,
        Entity pEntity,
        DamageSource pDamageSource,
        MutableFloat pValue
    ) {
        applyEffects(
            this.getEffects(pComponentType),
            damageContext(pLevel, pEnchantmentLevel, pEntity, pDamageSource),
            p_344340_ -> pValue.setValue(p_344340_.process(pEnchantmentLevel, pEntity.getRandom(), pValue.floatValue()))
        );
    }

    public static LootContext damageContext(ServerLevel pLevel, int pEnchantmentLevel, Entity pEntity, DamageSource pDamageSource) {
        LootParams lootparams = new LootParams.Builder(pLevel)
            .withParameter(LootContextParams.THIS_ENTITY, pEntity)
            .withParameter(LootContextParams.ENCHANTMENT_LEVEL, pEnchantmentLevel)
            .withParameter(LootContextParams.ORIGIN, pEntity.position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, pDamageSource)
            .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, pDamageSource.getEntity())
            .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, pDamageSource.getDirectEntity())
            .create(LootContextParamSets.ENCHANTED_DAMAGE);
        return new LootContext.Builder(lootparams).create(Optional.empty());
    }

    private static LootContext itemContext(ServerLevel pLevel, int pEnchantmentLevel, ItemStack pTool) {
        LootParams lootparams = new LootParams.Builder(pLevel)
            .withParameter(LootContextParams.TOOL, pTool)
            .withParameter(LootContextParams.ENCHANTMENT_LEVEL, pEnchantmentLevel)
            .create(LootContextParamSets.ENCHANTED_ITEM);
        return new LootContext.Builder(lootparams).create(Optional.empty());
    }

    private static LootContext locationContext(ServerLevel pLevel, int pEnchantmentLevel, Entity pEntity, boolean pEnchantmentActive) {
        LootParams lootparams = new LootParams.Builder(pLevel)
            .withParameter(LootContextParams.THIS_ENTITY, pEntity)
            .withParameter(LootContextParams.ENCHANTMENT_LEVEL, pEnchantmentLevel)
            .withParameter(LootContextParams.ORIGIN, pEntity.position())
            .withParameter(LootContextParams.ENCHANTMENT_ACTIVE, pEnchantmentActive)
            .create(LootContextParamSets.ENCHANTED_LOCATION);
        return new LootContext.Builder(lootparams).create(Optional.empty());
    }

    private static LootContext entityContext(ServerLevel pLevel, int pEnchantmentLevel, Entity pEntity, Vec3 pOrigin) {
        LootParams lootparams = new LootParams.Builder(pLevel)
            .withParameter(LootContextParams.THIS_ENTITY, pEntity)
            .withParameter(LootContextParams.ENCHANTMENT_LEVEL, pEnchantmentLevel)
            .withParameter(LootContextParams.ORIGIN, pOrigin)
            .create(LootContextParamSets.ENCHANTED_ENTITY);
        return new LootContext.Builder(lootparams).create(Optional.empty());
    }

    private static LootContext blockHitContext(ServerLevel pLevel, int pEnchantmentLevel, Entity pEntity, Vec3 pOrigin, BlockState pState) {
        LootParams lootparams = new LootParams.Builder(pLevel)
            .withParameter(LootContextParams.THIS_ENTITY, pEntity)
            .withParameter(LootContextParams.ENCHANTMENT_LEVEL, pEnchantmentLevel)
            .withParameter(LootContextParams.ORIGIN, pOrigin)
            .withParameter(LootContextParams.BLOCK_STATE, pState)
            .create(LootContextParamSets.HIT_BLOCK);
        return new LootContext.Builder(lootparams).create(Optional.empty());
    }

    private static <T> void applyEffects(List<ConditionalEffect<T>> pEffects, LootContext pContext, Consumer<T> pApplier) {
        for (ConditionalEffect<T> conditionaleffect : pEffects) {
            if (conditionaleffect.matches(pContext)) {
                pApplier.accept(conditionaleffect.effect());
            }
        }
    }

    public void runLocationChangedEffects(ServerLevel pLevel, int pEnchantmentLevel, EnchantedItemInUse pItem, LivingEntity pEntity) {
        if (pItem.inSlot() != null && !this.matchingSlot(pItem.inSlot())) {
            Set<EnchantmentLocationBasedEffect> set1 = pEntity.activeLocationDependentEnchantments().remove(this);
            if (set1 != null) {
                set1.forEach(p_345378_ -> p_345378_.onDeactivated(pItem, pEntity, pEntity.position(), pEnchantmentLevel));
            }
        } else {
            Set<EnchantmentLocationBasedEffect> set = pEntity.activeLocationDependentEnchantments().get(this);

            for (ConditionalEffect<EnchantmentLocationBasedEffect> conditionaleffect : this.getEffects(EnchantmentEffectComponents.LOCATION_CHANGED)) {
                EnchantmentLocationBasedEffect enchantmentlocationbasedeffect = conditionaleffect.effect();
                boolean flag = set != null && set.contains(enchantmentlocationbasedeffect);
                if (conditionaleffect.matches(locationContext(pLevel, pEnchantmentLevel, pEntity, flag))) {
                    if (!flag) {
                        if (set == null) {
                            set = new ObjectArraySet<>();
                            pEntity.activeLocationDependentEnchantments().put(this, set);
                        }

                        set.add(enchantmentlocationbasedeffect);
                    }

                    enchantmentlocationbasedeffect.onChangedBlock(pLevel, pEnchantmentLevel, pItem, pEntity, pEntity.position(), !flag);
                } else if (set != null && set.remove(enchantmentlocationbasedeffect)) {
                    enchantmentlocationbasedeffect.onDeactivated(pItem, pEntity, pEntity.position(), pEnchantmentLevel);
                }
            }

            if (set != null && set.isEmpty()) {
                pEntity.activeLocationDependentEnchantments().remove(this);
            }
        }
    }

    public void stopLocationBasedEffects(int pEnchantmentLevel, EnchantedItemInUse pItem, LivingEntity pEntity) {
        Set<EnchantmentLocationBasedEffect> set = pEntity.activeLocationDependentEnchantments().remove(this);
        if (set != null) {
            for (EnchantmentLocationBasedEffect enchantmentlocationbasedeffect : set) {
                enchantmentlocationbasedeffect.onDeactivated(pItem, pEntity, pEntity.position(), pEnchantmentLevel);
            }
        }
    }

    public static Enchantment.Builder enchantment(Enchantment.EnchantmentDefinition pDefinition) {
        return new Enchantment.Builder(pDefinition);
    }

    public static class Builder {
        private final Enchantment.EnchantmentDefinition definition;
        private HolderSet<Enchantment> exclusiveSet = HolderSet.direct();
        private final Map<DataComponentType<?>, List<?>> effectLists = new HashMap<>();
        private final DataComponentMap.Builder effectMapBuilder = DataComponentMap.builder();

        public Builder(Enchantment.EnchantmentDefinition pDefinition) {
            this.definition = pDefinition;
        }

        public Enchantment.Builder exclusiveWith(HolderSet<Enchantment> pExclusiveSet) {
            this.exclusiveSet = pExclusiveSet;
            return this;
        }

        public <E> Enchantment.Builder withEffect(DataComponentType<List<ConditionalEffect<E>>> pComponentType, E pEffect, LootItemCondition.Builder pRequirements) {
            this.getEffectsList(pComponentType).add(new ConditionalEffect<>(pEffect, Optional.of(pRequirements.build())));
            return this;
        }

        public <E> Enchantment.Builder withEffect(DataComponentType<List<ConditionalEffect<E>>> pComponentType, E pEffect) {
            this.getEffectsList(pComponentType).add(new ConditionalEffect<>(pEffect, Optional.empty()));
            return this;
        }

        public <E> Enchantment.Builder withEffect(
            DataComponentType<List<TargetedConditionalEffect<E>>> pComponentType,
            EnchantmentTarget pEnchanted,
            EnchantmentTarget pAffected,
            E pEffect,
            LootItemCondition.Builder pRequirements
        ) {
            this.getEffectsList(pComponentType).add(new TargetedConditionalEffect<>(pEnchanted, pAffected, pEffect, Optional.of(pRequirements.build())));
            return this;
        }

        public <E> Enchantment.Builder withEffect(
            DataComponentType<List<TargetedConditionalEffect<E>>> pComponentType, EnchantmentTarget pEnchanted, EnchantmentTarget pAffected, E pEffect
        ) {
            this.getEffectsList(pComponentType).add(new TargetedConditionalEffect<>(pEnchanted, pAffected, pEffect, Optional.empty()));
            return this;
        }

        public Enchantment.Builder withEffect(DataComponentType<List<EnchantmentAttributeEffect>> pComponentType, EnchantmentAttributeEffect pEffect) {
            this.getEffectsList(pComponentType).add(pEffect);
            return this;
        }

        public <E> Enchantment.Builder withSpecialEffect(DataComponentType<E> pComponent, E pValue) {
            this.effectMapBuilder.set(pComponent, pValue);
            return this;
        }

        public Enchantment.Builder withEffect(DataComponentType<Unit> pComponentType) {
            this.effectMapBuilder.set(pComponentType, Unit.INSTANCE);
            return this;
        }

        private <E> List<E> getEffectsList(DataComponentType<List<E>> pComponentType) {
            return (List<E>)this.effectLists.computeIfAbsent(pComponentType, p_344394_ -> {
                ArrayList<E> arraylist = new ArrayList<>();
                this.effectMapBuilder.set(pComponentType, arraylist);
                return arraylist;
            });
        }

        public Enchantment build(ResourceLocation pLocation) {
            return new Enchantment(Component.translatable(Util.makeDescriptionId("enchantment", pLocation)), this.definition, this.exclusiveSet, this.effectMapBuilder.build());
        }
    }

    public static record Cost(int base, int perLevelAboveFirst) {
        public static final Codec<Enchantment.Cost> CODEC = RecordCodecBuilder.create(
            p_345482_ -> p_345482_.group(
                        Codec.INT.fieldOf("base").forGetter(Enchantment.Cost::base),
                        Codec.INT.fieldOf("per_level_above_first").forGetter(Enchantment.Cost::perLevelAboveFirst)
                    )
                    .apply(p_345482_, Enchantment.Cost::new)
        );

        public int calculate(int pLevel) {
            return this.base + this.perLevelAboveFirst * (pLevel - 1);
        }
    }

    public static record EnchantmentDefinition(
        HolderSet<Item> supportedItems,
        Optional<HolderSet<Item>> primaryItems,
        int weight,
        int maxLevel,
        Enchantment.Cost minCost,
        Enchantment.Cost maxCost,
        int anvilCost,
        List<EquipmentSlotGroup> slots
    ) {
        public static final MapCodec<Enchantment.EnchantmentDefinition> CODEC = RecordCodecBuilder.mapCodec(
            p_344743_ -> p_344743_.group(
                        RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("supported_items").forGetter(Enchantment.EnchantmentDefinition::supportedItems),
                        RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("primary_items").forGetter(Enchantment.EnchantmentDefinition::primaryItems),
                        ExtraCodecs.intRange(1, 1024).fieldOf("weight").forGetter(Enchantment.EnchantmentDefinition::weight),
                        ExtraCodecs.intRange(1, 255).fieldOf("max_level").forGetter(Enchantment.EnchantmentDefinition::maxLevel),
                        Enchantment.Cost.CODEC.fieldOf("min_cost").forGetter(Enchantment.EnchantmentDefinition::minCost),
                        Enchantment.Cost.CODEC.fieldOf("max_cost").forGetter(Enchantment.EnchantmentDefinition::maxCost),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("anvil_cost").forGetter(Enchantment.EnchantmentDefinition::anvilCost),
                        EquipmentSlotGroup.CODEC.listOf().fieldOf("slots").forGetter(Enchantment.EnchantmentDefinition::slots)
                    )
                    .apply(p_344743_, Enchantment.EnchantmentDefinition::new)
        );
    }
}