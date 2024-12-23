package net.minecraft.world.item;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.crafting.Ingredient;

public class ArmorMaterials {
    public static final Holder<ArmorMaterial> LEATHER = register(
        "leather",
        Util.make(new EnumMap<>(ArmorItem.Type.class), p_327101_ -> {
            p_327101_.put(ArmorItem.Type.BOOTS, 1);
            p_327101_.put(ArmorItem.Type.LEGGINGS, 2);
            p_327101_.put(ArmorItem.Type.CHESTPLATE, 3);
            p_327101_.put(ArmorItem.Type.HELMET, 1);
            p_327101_.put(ArmorItem.Type.BODY, 3);
        }),
        15,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        0.0F,
        0.0F,
        () -> Ingredient.of(Items.LEATHER),
        List.of(
            new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("leather"), "", true),
            new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("leather"), "_overlay", false)
        )
    );
    public static final Holder<ArmorMaterial> CHAIN = register("chainmail", Util.make(new EnumMap<>(ArmorItem.Type.class), p_327098_ -> {
        p_327098_.put(ArmorItem.Type.BOOTS, 1);
        p_327098_.put(ArmorItem.Type.LEGGINGS, 4);
        p_327098_.put(ArmorItem.Type.CHESTPLATE, 5);
        p_327098_.put(ArmorItem.Type.HELMET, 2);
        p_327098_.put(ArmorItem.Type.BODY, 4);
    }), 12, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT));
    public static final Holder<ArmorMaterial> IRON = register("iron", Util.make(new EnumMap<>(ArmorItem.Type.class), p_327096_ -> {
        p_327096_.put(ArmorItem.Type.BOOTS, 2);
        p_327096_.put(ArmorItem.Type.LEGGINGS, 5);
        p_327096_.put(ArmorItem.Type.CHESTPLATE, 6);
        p_327096_.put(ArmorItem.Type.HELMET, 2);
        p_327096_.put(ArmorItem.Type.BODY, 5);
    }), 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT));
    public static final Holder<ArmorMaterial> GOLD = register("gold", Util.make(new EnumMap<>(ArmorItem.Type.class), p_327100_ -> {
        p_327100_.put(ArmorItem.Type.BOOTS, 1);
        p_327100_.put(ArmorItem.Type.LEGGINGS, 3);
        p_327100_.put(ArmorItem.Type.CHESTPLATE, 5);
        p_327100_.put(ArmorItem.Type.HELMET, 2);
        p_327100_.put(ArmorItem.Type.BODY, 7);
    }), 25, SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, () -> Ingredient.of(Items.GOLD_INGOT));
    public static final Holder<ArmorMaterial> DIAMOND = register("diamond", Util.make(new EnumMap<>(ArmorItem.Type.class), p_327102_ -> {
        p_327102_.put(ArmorItem.Type.BOOTS, 3);
        p_327102_.put(ArmorItem.Type.LEGGINGS, 6);
        p_327102_.put(ArmorItem.Type.CHESTPLATE, 8);
        p_327102_.put(ArmorItem.Type.HELMET, 3);
        p_327102_.put(ArmorItem.Type.BODY, 11);
    }), 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, () -> Ingredient.of(Items.DIAMOND));
    public static final Holder<ArmorMaterial> TURTLE = register("turtle", Util.make(new EnumMap<>(ArmorItem.Type.class), p_327099_ -> {
        p_327099_.put(ArmorItem.Type.BOOTS, 2);
        p_327099_.put(ArmorItem.Type.LEGGINGS, 5);
        p_327099_.put(ArmorItem.Type.CHESTPLATE, 6);
        p_327099_.put(ArmorItem.Type.HELMET, 2);
        p_327099_.put(ArmorItem.Type.BODY, 5);
    }), 9, SoundEvents.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, () -> Ingredient.of(Items.TURTLE_SCUTE));
    public static final Holder<ArmorMaterial> NETHERITE = register("netherite", Util.make(new EnumMap<>(ArmorItem.Type.class), p_327103_ -> {
        p_327103_.put(ArmorItem.Type.BOOTS, 3);
        p_327103_.put(ArmorItem.Type.LEGGINGS, 6);
        p_327103_.put(ArmorItem.Type.CHESTPLATE, 8);
        p_327103_.put(ArmorItem.Type.HELMET, 3);
        p_327103_.put(ArmorItem.Type.BODY, 11);
    }), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, () -> Ingredient.of(Items.NETHERITE_INGOT));
    public static final Holder<ArmorMaterial> ARMADILLO = register("armadillo", Util.make(new EnumMap<>(ArmorItem.Type.class), p_327097_ -> {
        p_327097_.put(ArmorItem.Type.BOOTS, 3);
        p_327097_.put(ArmorItem.Type.LEGGINGS, 6);
        p_327097_.put(ArmorItem.Type.CHESTPLATE, 8);
        p_327097_.put(ArmorItem.Type.HELMET, 3);
        p_327097_.put(ArmorItem.Type.BODY, 11);
    }), 10, SoundEvents.ARMOR_EQUIP_WOLF, 0.0F, 0.0F, () -> Ingredient.of(Items.ARMADILLO_SCUTE));

    public static Holder<ArmorMaterial> bootstrap(Registry<ArmorMaterial> pRegistry) {
        return LEATHER;
    }

    private static Holder<ArmorMaterial> register(
        String pName,
        EnumMap<ArmorItem.Type, Integer> pDefense,
        int pEnchantmentValue,
        Holder<SoundEvent> pEquipSound,
        float pToughness,
        float pKnockbackResistance,
        Supplier<Ingredient> pRepairIngredient
    ) {
        List<ArmorMaterial.Layer> list = List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace(pName)));
        return register(pName, pDefense, pEnchantmentValue, pEquipSound, pToughness, pKnockbackResistance, pRepairIngredient, list);
    }

    private static Holder<ArmorMaterial> register(
        String pName,
        EnumMap<ArmorItem.Type, Integer> pDefense,
        int pEnchantmentValue,
        Holder<SoundEvent> pEquipSound,
        float pToughness,
        float pKnockbackResistance,
        Supplier<Ingredient> pRepairIngridient,
        List<ArmorMaterial.Layer> pLayers
    ) {
        EnumMap<ArmorItem.Type, Integer> enummap = new EnumMap<>(ArmorItem.Type.class);

        for (ArmorItem.Type armoritem$type : ArmorItem.Type.values()) {
            enummap.put(armoritem$type, pDefense.get(armoritem$type));
        }

        return Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace(pName),
            new ArmorMaterial(enummap, pEnchantmentValue, pEquipSound, pRepairIngridient, pLayers, pToughness, pKnockbackResistance)
        );
    }
}