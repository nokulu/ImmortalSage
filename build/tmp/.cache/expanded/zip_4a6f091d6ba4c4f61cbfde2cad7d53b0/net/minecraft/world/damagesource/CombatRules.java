package net.minecraft.world.damagesource;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class CombatRules {
    public static final float MAX_ARMOR = 20.0F;
    public static final float ARMOR_PROTECTION_DIVIDER = 25.0F;
    public static final float BASE_ARMOR_TOUGHNESS = 2.0F;
    public static final float MIN_ARMOR_RATIO = 0.2F;
    private static final int NUM_ARMOR_ITEMS = 4;

    public static float getDamageAfterAbsorb(LivingEntity pEntity, float pDamage, DamageSource pDamageSource, float pArmorValue, float pArmorToughness) {
        float f = 2.0F + pArmorToughness / 4.0F;
        float f1 = Mth.clamp(pArmorValue - pDamage / f, pArmorValue * 0.2F, 20.0F);
        float f2 = f1 / 25.0F;
        ItemStack itemstack = pDamageSource.getWeaponItem();
        float f3;
        if (itemstack != null && pEntity.level() instanceof ServerLevel serverlevel) {
            f3 = Mth.clamp(EnchantmentHelper.modifyArmorEffectiveness(serverlevel, itemstack, pEntity, pDamageSource, f2), 0.0F, 1.0F);
        } else {
            f3 = f2;
        }

        float f4 = 1.0F - f3;
        return pDamage * f4;
    }

    public static float getDamageAfterMagicAbsorb(float pDamage, float pEnchantModifiers) {
        float f = Mth.clamp(pEnchantModifiers, 0.0F, 20.0F);
        return pDamage * (1.0F - f / 25.0F);
    }
}