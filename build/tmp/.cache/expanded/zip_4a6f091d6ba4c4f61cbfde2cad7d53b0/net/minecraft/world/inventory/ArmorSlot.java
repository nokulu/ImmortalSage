package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

class ArmorSlot extends Slot {
    private final LivingEntity owner;
    private final EquipmentSlot slot;
    @Nullable
    private final ResourceLocation emptyIcon;

    public ArmorSlot(
        Container pContainer, LivingEntity pOwner, EquipmentSlot pSlot, int pSlotIndex, int pX, int pY, @Nullable ResourceLocation pEmptyIcon
    ) {
        super(pContainer, pSlotIndex, pX, pY);
        this.owner = pOwner;
        this.slot = pSlot;
        this.emptyIcon = pEmptyIcon;
    }

    @Override
    public void setByPlayer(ItemStack pNewStack, ItemStack pOldStack) {
        this.owner.onEquipItem(this.slot, pOldStack, pNewStack);
        super.setByPlayer(pNewStack, pOldStack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack pStack) {
        return pStack.canEquip(slot, owner);
    }

    @Override
    public boolean mayPickup(Player pPlayer) {
        ItemStack itemstack = this.getItem();
        return !itemstack.isEmpty() && !pPlayer.isCreative() && EnchantmentHelper.has(itemstack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)
            ? false
            : super.mayPickup(pPlayer);
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return this.emptyIcon != null ? Pair.of(InventoryMenu.BLOCK_ATLAS, this.emptyIcon) : super.getNoItemIcon();
    }
}
