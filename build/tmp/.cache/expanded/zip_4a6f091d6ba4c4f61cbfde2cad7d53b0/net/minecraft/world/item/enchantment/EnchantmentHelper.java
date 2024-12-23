package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;

public class EnchantmentHelper {
    public static int getItemEnchantmentLevel(Holder<Enchantment> pEnchantment, ItemStack pStack) {
        ItemEnchantments itemenchantments = pStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        return itemenchantments.getLevel(pEnchantment);
    }

    public static ItemEnchantments updateEnchantments(ItemStack pStack, Consumer<ItemEnchantments.Mutable> pUpdater) {
        DataComponentType<ItemEnchantments> datacomponenttype = getComponentType(pStack);
        ItemEnchantments itemenchantments = pStack.get(datacomponenttype);
        if (itemenchantments == null) {
            return ItemEnchantments.EMPTY;
        } else {
            ItemEnchantments.Mutable itemenchantments$mutable = new ItemEnchantments.Mutable(itemenchantments);
            pUpdater.accept(itemenchantments$mutable);
            ItemEnchantments itemenchantments1 = itemenchantments$mutable.toImmutable();
            pStack.set(datacomponenttype, itemenchantments1);
            return itemenchantments1;
        }
    }

    public static boolean canStoreEnchantments(ItemStack pStack) {
        return pStack.has(getComponentType(pStack));
    }

    public static void setEnchantments(ItemStack pStack, ItemEnchantments pEnchantments) {
        pStack.set(getComponentType(pStack), pEnchantments);
    }

    public static ItemEnchantments getEnchantmentsForCrafting(ItemStack pStack) {
        return pStack.getOrDefault(getComponentType(pStack), ItemEnchantments.EMPTY);
    }

    private static DataComponentType<ItemEnchantments> getComponentType(ItemStack pStack) {
        return pStack.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
    }

    public static boolean hasAnyEnchantments(ItemStack pStack) {
        return !pStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()
            || !pStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }

    public static int processDurabilityChange(ServerLevel pLevel, ItemStack pStack, int pDamage) {
        MutableFloat mutablefloat = new MutableFloat((float)pDamage);
        runIterationOnItem(pStack, (p_341764_, p_341765_) -> p_341764_.value().modifyDurabilityChange(pLevel, p_341765_, pStack, mutablefloat));
        return mutablefloat.intValue();
    }

    public static int processAmmoUse(ServerLevel pLevel, ItemStack pWeapon, ItemStack pAmmo, int pCount) {
        MutableFloat mutablefloat = new MutableFloat((float)pCount);
        runIterationOnItem(pWeapon, (p_341622_, p_341623_) -> p_341622_.value().modifyAmmoCount(pLevel, p_341623_, pAmmo, mutablefloat));
        return mutablefloat.intValue();
    }

    public static int processBlockExperience(ServerLevel pLevel, ItemStack pStack, int pExperience) {
        MutableFloat mutablefloat = new MutableFloat((float)pExperience);
        runIterationOnItem(pStack, (p_341808_, p_341809_) -> p_341808_.value().modifyBlockExperience(pLevel, p_341809_, pStack, mutablefloat));
        return mutablefloat.intValue();
    }

    public static int processMobExperience(ServerLevel pLevel, @Nullable Entity pKiller, Entity pMob, int pExperience) {
        if (pKiller instanceof LivingEntity livingentity) {
            MutableFloat mutablefloat = new MutableFloat((float)pExperience);
            runIterationOnEquipment(
                livingentity,
                (p_341777_, p_341778_, p_341779_) -> p_341777_.value().modifyMobExperience(pLevel, p_341778_, p_341779_.itemStack(), pMob, mutablefloat)
            );
            return mutablefloat.intValue();
        } else {
            return pExperience;
        }
    }

    private static void runIterationOnItem(ItemStack pStack, EnchantmentHelper.EnchantmentVisitor pVisitor) {
        ItemEnchantments itemenchantments = pStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        for (Entry<Holder<Enchantment>> entry : itemenchantments.entrySet()) {
            pVisitor.accept(entry.getKey(), entry.getIntValue());
        }
    }

    private static void runIterationOnItem(ItemStack pStack, EquipmentSlot pSlot, LivingEntity pEntity, EnchantmentHelper.EnchantmentInSlotVisitor pVisitor) {
        if (!pStack.isEmpty()) {
            ItemEnchantments itemenchantments = pStack.get(DataComponents.ENCHANTMENTS);
            if (itemenchantments != null && !itemenchantments.isEmpty()) {
                EnchantedItemInUse enchantediteminuse = new EnchantedItemInUse(pStack, pSlot, pEntity);

                for (Entry<Holder<Enchantment>> entry : itemenchantments.entrySet()) {
                    Holder<Enchantment> holder = entry.getKey();
                    if (holder.value().matchingSlot(pSlot)) {
                        pVisitor.accept(holder, entry.getIntValue(), enchantediteminuse);
                    }
                }
            }
        }
    }

    private static void runIterationOnEquipment(LivingEntity pEntity, EnchantmentHelper.EnchantmentInSlotVisitor pVisitor) {
        for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            runIterationOnItem(pEntity.getItemBySlot(equipmentslot), equipmentslot, pEntity, pVisitor);
        }
    }

    public static boolean isImmuneToDamage(ServerLevel pLevel, LivingEntity pEntity, DamageSource pDamageSource) {
        MutableBoolean mutableboolean = new MutableBoolean();
        runIterationOnEquipment(
            pEntity,
            (p_341729_, p_341730_, p_341731_) -> mutableboolean.setValue(
                    mutableboolean.isTrue() || p_341729_.value().isImmuneToDamage(pLevel, p_341730_, pEntity, pDamageSource)
                )
        );
        return mutableboolean.isTrue();
    }

    public static float getDamageProtection(ServerLevel pLevel, LivingEntity pEntity, DamageSource pDamageSource) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);
        runIterationOnEquipment(
            pEntity,
            (p_341717_, p_341718_, p_341719_) -> p_341717_.value()
                    .modifyDamageProtection(pLevel, p_341718_, p_341719_.itemStack(), pEntity, pDamageSource, mutablefloat)
        );
        return mutablefloat.floatValue();
    }

    public static float modifyDamage(ServerLevel pLevel, ItemStack pTool, Entity pEntity, DamageSource pDamageSource, float pDamage) {
        MutableFloat mutablefloat = new MutableFloat(pDamage);
        runIterationOnItem(pTool, (p_341744_, p_341745_) -> p_341744_.value().modifyDamage(pLevel, p_341745_, pTool, pEntity, pDamageSource, mutablefloat));
        return mutablefloat.floatValue();
    }

    public static float modifyFallBasedDamage(ServerLevel pLevel, ItemStack pTool, Entity pEnity, DamageSource pDamageSource, float pFallBasedDamage) {
        MutableFloat mutablefloat = new MutableFloat(pFallBasedDamage);
        runIterationOnItem(pTool, (p_341771_, p_341772_) -> p_341771_.value().modifyFallBasedDamage(pLevel, p_341772_, pTool, pEnity, pDamageSource, mutablefloat));
        return mutablefloat.floatValue();
    }

    public static float modifyArmorEffectiveness(ServerLevel pLevel, ItemStack pTool, Entity pEntity, DamageSource pDamageSource, float pArmorEffectiveness) {
        MutableFloat mutablefloat = new MutableFloat(pArmorEffectiveness);
        runIterationOnItem(pTool, (p_341681_, p_341682_) -> p_341681_.value().modifyArmorEffectivness(pLevel, p_341682_, pTool, pEntity, pDamageSource, mutablefloat));
        return mutablefloat.floatValue();
    }

    public static float modifyKnockback(ServerLevel pLevel, ItemStack pTool, Entity pEntity, DamageSource pDamageSource, float pKnockback) {
        MutableFloat mutablefloat = new MutableFloat(pKnockback);
        runIterationOnItem(pTool, (p_341790_, p_341791_) -> p_341790_.value().modifyKnockback(pLevel, p_341791_, pTool, pEntity, pDamageSource, mutablefloat));
        return mutablefloat.floatValue();
    }

    public static void doPostAttackEffects(ServerLevel pLevel, Entity pEntity, DamageSource pDamageSource) {
        if (pDamageSource.getEntity() instanceof LivingEntity livingentity) {
            doPostAttackEffectsWithItemSource(pLevel, pEntity, pDamageSource, livingentity.getWeaponItem());
        } else {
            doPostAttackEffectsWithItemSource(pLevel, pEntity, pDamageSource, null);
        }
    }

    public static void doPostAttackEffectsWithItemSource(ServerLevel pLevel, Entity pEntity, DamageSource pDamageSource, @Nullable ItemStack pItemSource) {
        if (pEntity instanceof LivingEntity livingentity) {
            runIterationOnEquipment(
                livingentity,
                (p_341753_, p_341754_, p_341755_) -> p_341753_.value()
                        .doPostAttack(pLevel, p_341754_, p_341755_, EnchantmentTarget.VICTIM, pEntity, pDamageSource)
            );
        }

        if (pItemSource != null && pDamageSource.getEntity() instanceof LivingEntity livingentity1) {
            runIterationOnItem(
                pItemSource,
                EquipmentSlot.MAINHAND,
                livingentity1,
                (p_341641_, p_341642_, p_341643_) -> p_341641_.value()
                        .doPostAttack(pLevel, p_341642_, p_341643_, EnchantmentTarget.ATTACKER, pEntity, pDamageSource)
            );
        }
    }

    public static void runLocationChangedEffects(ServerLevel pLevel, LivingEntity pEntity) {
        runIterationOnEquipment(pEntity, (p_341602_, p_341603_, p_341604_) -> p_341602_.value().runLocationChangedEffects(pLevel, p_341603_, p_341604_, pEntity));
    }

    public static void runLocationChangedEffects(ServerLevel pLevel, ItemStack pStack, LivingEntity pEntity, EquipmentSlot pSlot) {
        runIterationOnItem(
            pStack, pSlot, pEntity, (p_341794_, p_341795_, p_341796_) -> p_341794_.value().runLocationChangedEffects(pLevel, p_341795_, p_341796_, pEntity)
        );
    }

    public static void stopLocationBasedEffects(LivingEntity pEntity) {
        runIterationOnEquipment(pEntity, (p_341606_, p_341607_, p_341608_) -> p_341606_.value().stopLocationBasedEffects(p_341607_, p_341608_, pEntity));
    }

    public static void stopLocationBasedEffects(ItemStack pStack, LivingEntity pEntity, EquipmentSlot pSlot) {
        runIterationOnItem(pStack, pSlot, pEntity, (p_341625_, p_341626_, p_341627_) -> p_341625_.value().stopLocationBasedEffects(p_341626_, p_341627_, pEntity));
    }

    public static void tickEffects(ServerLevel pLevel, LivingEntity pEntity) {
        runIterationOnEquipment(pEntity, (p_341782_, p_341783_, p_341784_) -> p_341782_.value().tick(pLevel, p_341783_, p_341784_, pEntity));
    }

    public static int getEnchantmentLevel(Holder<Enchantment> pEnchantment, LivingEntity pEntity) {
        Iterable<ItemStack> iterable = pEnchantment.value().getSlotItems(pEntity).values();
        int i = 0;

        for (ItemStack itemstack : iterable) {
            int j = getItemEnchantmentLevel(pEnchantment, itemstack);
            if (j > i) {
                i = j;
            }
        }

        return i;
    }

    public static int processProjectileCount(ServerLevel pLevel, ItemStack pTool, Entity pEntity, int pProjectileCount) {
        MutableFloat mutablefloat = new MutableFloat((float)pProjectileCount);
        runIterationOnItem(pTool, (p_341617_, p_341618_) -> p_341617_.value().modifyProjectileCount(pLevel, p_341618_, pTool, pEntity, mutablefloat));
        return Math.max(0, mutablefloat.intValue());
    }

    public static float processProjectileSpread(ServerLevel pLevel, ItemStack pTool, Entity pEntity, float pProjectileSpread) {
        MutableFloat mutablefloat = new MutableFloat(pProjectileSpread);
        runIterationOnItem(pTool, (p_341674_, p_341675_) -> p_341674_.value().modifyProjectileSpread(pLevel, p_341675_, pTool, pEntity, mutablefloat));
        return Math.max(0.0F, mutablefloat.floatValue());
    }

    public static int getPiercingCount(ServerLevel pLevel, ItemStack pFiredFromWeapon, ItemStack pPickupItemStack) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);
        runIterationOnItem(pFiredFromWeapon, (p_341723_, p_341724_) -> p_341723_.value().modifyPiercingCount(pLevel, p_341724_, pPickupItemStack, mutablefloat));
        return Math.max(0, mutablefloat.intValue());
    }

    public static void onProjectileSpawned(ServerLevel pLevel, ItemStack pFiredFromWeapon, AbstractArrow pArrow, Consumer<Item> pOnBreak) {
        LivingEntity livingentity = pArrow.getOwner() instanceof LivingEntity livingentity1 ? livingentity1 : null;
        EnchantedItemInUse enchantediteminuse = new EnchantedItemInUse(pFiredFromWeapon, null, livingentity, pOnBreak);
        runIterationOnItem(pFiredFromWeapon, (p_341759_, p_341760_) -> p_341759_.value().onProjectileSpawned(pLevel, p_341760_, enchantediteminuse, pArrow));
    }

    public static void onHitBlock(
        ServerLevel pLevel,
        ItemStack pStack,
        @Nullable LivingEntity pOwner,
        Entity pEntity,
        @Nullable EquipmentSlot pSlot,
        Vec3 pPos,
        BlockState pState,
        Consumer<Item> pOnBreak
    ) {
        EnchantedItemInUse enchantediteminuse = new EnchantedItemInUse(pStack, pSlot, pOwner, pOnBreak);
        runIterationOnItem(
            pStack, (p_341663_, p_341664_) -> p_341663_.value().onHitBlock(pLevel, p_341664_, enchantediteminuse, pEntity, pPos, pState)
        );
    }

    public static int modifyDurabilityToRepairFromXp(ServerLevel pLevel, ItemStack pStack, int pDuabilityToRepairFromXp) {
        MutableFloat mutablefloat = new MutableFloat((float)pDuabilityToRepairFromXp);
        runIterationOnItem(pStack, (p_341656_, p_341657_) -> p_341656_.value().modifyDurabilityToRepairFromXp(pLevel, p_341657_, pStack, mutablefloat));
        return Math.max(0, mutablefloat.intValue());
    }

    public static float processEquipmentDropChance(ServerLevel pLevel, LivingEntity pEntity, DamageSource pDamageSource, float pEquipmentDropChance) {
        MutableFloat mutablefloat = new MutableFloat(pEquipmentDropChance);
        RandomSource randomsource = pEntity.getRandom();
        runIterationOnEquipment(
            pEntity,
            (p_341693_, p_341694_, p_341695_) -> {
                LootContext lootcontext = Enchantment.damageContext(pLevel, p_341694_, pEntity, pDamageSource);
                p_341693_.value()
                    .getEffects(EnchantmentEffectComponents.EQUIPMENT_DROPS)
                    .forEach(
                        p_341820_ -> {
                            if (p_341820_.enchanted() == EnchantmentTarget.VICTIM
                                && p_341820_.affected() == EnchantmentTarget.VICTIM
                                && p_341820_.matches(lootcontext)) {
                                mutablefloat.setValue(p_341820_.effect().process(p_341694_, randomsource, mutablefloat.floatValue()));
                            }
                        }
                    );
            }
        );
        if (pDamageSource.getEntity() instanceof LivingEntity livingentity) {
            runIterationOnEquipment(
                livingentity,
                (p_341650_, p_341651_, p_341652_) -> {
                    LootContext lootcontext = Enchantment.damageContext(pLevel, p_341651_, pEntity, pDamageSource);
                    p_341650_.value()
                        .getEffects(EnchantmentEffectComponents.EQUIPMENT_DROPS)
                        .forEach(
                            p_341669_ -> {
                                if (p_341669_.enchanted() == EnchantmentTarget.ATTACKER
                                    && p_341669_.affected() == EnchantmentTarget.VICTIM
                                    && p_341669_.matches(lootcontext)) {
                                    mutablefloat.setValue(p_341669_.effect().process(p_341651_, randomsource, mutablefloat.floatValue()));
                                }
                            }
                        );
                }
            );
        }

        return mutablefloat.floatValue();
    }

    public static void forEachModifier(ItemStack pStack, EquipmentSlotGroup pSlotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> pAction) {
        runIterationOnItem(pStack, (p_341748_, p_341749_) -> p_341748_.value().getEffects(EnchantmentEffectComponents.ATTRIBUTES).forEach(p_341738_ -> {
                if (((Enchantment)p_341748_.value()).definition().slots().contains(pSlotGroup)) {
                    pAction.accept(p_341738_.attribute(), p_341738_.getModifier(p_341749_, pSlotGroup));
                }
            }));
    }

    public static void forEachModifier(ItemStack pStack, EquipmentSlot pSlot, BiConsumer<Holder<Attribute>, AttributeModifier> pAction) {
        runIterationOnItem(pStack, (p_341598_, p_341599_) -> p_341598_.value().getEffects(EnchantmentEffectComponents.ATTRIBUTES).forEach(p_341804_ -> {
                if (((Enchantment)p_341598_.value()).matchingSlot(pSlot)) {
                    pAction.accept(p_341804_.attribute(), p_341804_.getModifier(p_341599_, pSlot));
                }
            }));
    }

    public static int getFishingLuckBonus(ServerLevel pLevel, ItemStack pStack, Entity pEntity) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);
        runIterationOnItem(pStack, (p_341704_, p_341705_) -> p_341704_.value().modifyFishingLuckBonus(pLevel, p_341705_, pStack, pEntity, mutablefloat));
        return Math.max(0, mutablefloat.intValue());
    }

    public static float getFishingTimeReduction(ServerLevel pLevel, ItemStack pStack, Entity pEntity) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);
        runIterationOnItem(pStack, (p_341814_, p_341815_) -> p_341814_.value().modifyFishingTimeReduction(pLevel, p_341815_, pStack, pEntity, mutablefloat));
        return Math.max(0.0F, mutablefloat.floatValue());
    }

    public static int getTridentReturnToOwnerAcceleration(ServerLevel pLevel, ItemStack pStack, Entity pEntity) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);
        runIterationOnItem(pStack, (p_341632_, p_341633_) -> p_341632_.value().modifyTridentReturnToOwnerAcceleration(pLevel, p_341633_, pStack, pEntity, mutablefloat));
        return Math.max(0, mutablefloat.intValue());
    }

    public static float modifyCrossbowChargingTime(ItemStack pStack, LivingEntity pEntity, float pCrossbowChargingTime) {
        MutableFloat mutablefloat = new MutableFloat(pCrossbowChargingTime);
        runIterationOnItem(pStack, (p_341698_, p_341699_) -> p_341698_.value().modifyCrossbowChargeTime(pEntity.getRandom(), p_341699_, mutablefloat));
        return Math.max(0.0F, mutablefloat.floatValue());
    }

    public static float getTridentSpinAttackStrength(ItemStack pStack, LivingEntity pEntity) {
        MutableFloat mutablefloat = new MutableFloat(0.0F);
        runIterationOnItem(pStack, (p_341611_, p_341612_) -> p_341611_.value().modifyTridentSpinAttackStrength(pEntity.getRandom(), p_341612_, mutablefloat));
        return mutablefloat.floatValue();
    }

    public static boolean hasTag(ItemStack pStack, TagKey<Enchantment> pTag) {
        ItemEnchantments itemenchantments = pStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        for (Entry<Holder<Enchantment>> entry : itemenchantments.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            if (holder.is(pTag)) {
                return true;
            }
        }

        return false;
    }

    public static boolean has(ItemStack pStack, DataComponentType<?> pComponentType) {
        MutableBoolean mutableboolean = new MutableBoolean(false);
        runIterationOnItem(pStack, (p_341711_, p_341712_) -> {
            if (p_341711_.value().effects().has(pComponentType)) {
                mutableboolean.setTrue();
            }
        });
        return mutableboolean.booleanValue();
    }

    public static <T> Optional<T> pickHighestLevel(ItemStack pStack, DataComponentType<List<T>> pComponentType) {
        Pair<List<T>, Integer> pair = getHighestLevel(pStack, pComponentType);
        if (pair != null) {
            List<T> list = pair.getFirst();
            int i = pair.getSecond();
            return Optional.of(list.get(Math.min(i, list.size()) - 1));
        } else {
            return Optional.empty();
        }
    }

    @Nullable
    public static <T> Pair<T, Integer> getHighestLevel(ItemStack pStack, DataComponentType<T> pComponentType) {
        MutableObject<Pair<T, Integer>> mutableobject = new MutableObject<>();
        runIterationOnItem(pStack, (p_341636_, p_341637_) -> {
            if (mutableobject.getValue() == null || mutableobject.getValue().getSecond() < p_341637_) {
                T t = p_341636_.value().effects().get(pComponentType);
                if (t != null) {
                    mutableobject.setValue(Pair.of(t, p_341637_));
                }
            }
        });
        return mutableobject.getValue();
    }

    public static Optional<EnchantedItemInUse> getRandomItemWith(DataComponentType<?> pComponentType, LivingEntity pEntity, Predicate<ItemStack> pFilter) {
        List<EnchantedItemInUse> list = new ArrayList<>();

        for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            ItemStack itemstack = pEntity.getItemBySlot(equipmentslot);
            if (pFilter.test(itemstack)) {
                ItemEnchantments itemenchantments = itemstack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

                for (Entry<Holder<Enchantment>> entry : itemenchantments.entrySet()) {
                    Holder<Enchantment> holder = entry.getKey();
                    if (holder.value().effects().has(pComponentType) && holder.value().matchingSlot(equipmentslot)) {
                        list.add(new EnchantedItemInUse(itemstack, equipmentslot, pEntity));
                    }
                }
            }
        }

        return Util.getRandomSafe(list, pEntity.getRandom());
    }

    public static int getEnchantmentCost(RandomSource pRandom, int pEnchantNum, int pPower, ItemStack pStack) {
        Item item = pStack.getItem();
        int i = pStack.getEnchantmentValue();
        if (i <= 0) {
            return 0;
        } else {
            if (pPower > 15) {
                pPower = 15;
            }

            int j = pRandom.nextInt(8) + 1 + (pPower >> 1) + pRandom.nextInt(pPower + 1);
            if (pEnchantNum == 0) {
                return Math.max(j / 3, 1);
            } else {
                return pEnchantNum == 1 ? j * 2 / 3 + 1 : Math.max(j, pPower * 2);
            }
        }
    }

    public static ItemStack enchantItem(
        RandomSource pRandom, ItemStack pStack, int pLevel, RegistryAccess pRegistryAccess, Optional<? extends HolderSet<Enchantment>> pPossibleEnchantments
    ) {
        return enchantItem(
            pRandom,
            pStack,
            pLevel,
            pPossibleEnchantments.map(HolderSet::stream)
                .orElseGet(() -> pRegistryAccess.registryOrThrow(Registries.ENCHANTMENT).holders().map(p_341773_ -> (Holder<Enchantment>)p_341773_))
        );
    }

    public static ItemStack enchantItem(RandomSource pRandom, ItemStack pStack, int pLevel, Stream<Holder<Enchantment>> pPossibleEnchantments) {
        List<EnchantmentInstance> list = selectEnchantment(pRandom, pStack, pLevel, pPossibleEnchantments);
        if (pStack.is(Items.BOOK)) {
            pStack = new ItemStack(Items.ENCHANTED_BOOK);
        }

        for (EnchantmentInstance enchantmentinstance : list) {
            pStack.enchant(enchantmentinstance.enchantment, enchantmentinstance.level);
        }

        return pStack;
    }

    public static List<EnchantmentInstance> selectEnchantment(RandomSource pRandom, ItemStack pStack, int pLevel, Stream<Holder<Enchantment>> pPossibleEnchantments) {
        List<EnchantmentInstance> list = Lists.newArrayList();
        Item item = pStack.getItem();
        int i = pStack.getEnchantmentValue();
        if (i <= 0) {
            return list;
        } else {
            pLevel += 1 + pRandom.nextInt(i / 4 + 1) + pRandom.nextInt(i / 4 + 1);
            float f = (pRandom.nextFloat() + pRandom.nextFloat() - 1.0F) * 0.15F;
            pLevel = Mth.clamp(Math.round((float)pLevel + (float)pLevel * f), 1, Integer.MAX_VALUE);
            List<EnchantmentInstance> list1 = getAvailableEnchantmentResults(pLevel, pStack, pPossibleEnchantments);
            if (!list1.isEmpty()) {
                WeightedRandom.getRandomItem(pRandom, list1).ifPresent(list::add);

                while (pRandom.nextInt(50) <= pLevel) {
                    if (!list.isEmpty()) {
                        filterCompatibleEnchantments(list1, Util.lastOf(list));
                    }

                    if (list1.isEmpty()) {
                        break;
                    }

                    WeightedRandom.getRandomItem(pRandom, list1).ifPresent(list::add);
                    pLevel /= 2;
                }
            }

            return list;
        }
    }

    public static void filterCompatibleEnchantments(List<EnchantmentInstance> pDataList, EnchantmentInstance pData) {
        pDataList.removeIf(p_341733_ -> !Enchantment.areCompatible(pData.enchantment, p_341733_.enchantment));
    }

    public static boolean isEnchantmentCompatible(Collection<Holder<Enchantment>> pCurrentEnchantments, Holder<Enchantment> pNewEnchantment) {
        for (Holder<Enchantment> holder : pCurrentEnchantments) {
            if (!Enchantment.areCompatible(holder, pNewEnchantment)) {
                return false;
            }
        }

        return true;
    }

    public static List<EnchantmentInstance> getAvailableEnchantmentResults(int pLevel, ItemStack pStack, Stream<Holder<Enchantment>> pPossibleEnchantments) {
        List<EnchantmentInstance> list = Lists.newArrayList();
        boolean flag = pStack.is(Items.BOOK);
        pPossibleEnchantments.filter(p_341799_ -> pStack.canApplyAtEnchantingTable(p_341799_.value()) || flag).forEach(p_341708_ -> {
            Enchantment enchantment = p_341708_.value();

            for (int i = enchantment.getMaxLevel(); i >= enchantment.getMinLevel(); i--) {
                if (pLevel >= enchantment.getMinCost(i) && pLevel <= enchantment.getMaxCost(i)) {
                    list.add(new EnchantmentInstance((Holder<Enchantment>)p_341708_, i));
                    break;
                }
            }
        });
        return list;
    }

    public static void enchantItemFromProvider(
        ItemStack pStack, RegistryAccess pRegistries, ResourceKey<EnchantmentProvider> pKey, DifficultyInstance pDifficulty, RandomSource pRandom
    ) {
        EnchantmentProvider enchantmentprovider = pRegistries.registryOrThrow(Registries.ENCHANTMENT_PROVIDER).get(pKey);
        if (enchantmentprovider != null) {
            updateEnchantments(pStack, p_341687_ -> enchantmentprovider.enchant(pStack, p_341687_, pRandom, pDifficulty));
        }
    }

    @FunctionalInterface
    interface EnchantmentInSlotVisitor {
        void accept(Holder<Enchantment> pEnchantment, int pLevel, EnchantedItemInUse pItem);
    }

    @FunctionalInterface
    interface EnchantmentVisitor {
        void accept(Holder<Enchantment> pEnchantment, int pLevel);
    }
}
