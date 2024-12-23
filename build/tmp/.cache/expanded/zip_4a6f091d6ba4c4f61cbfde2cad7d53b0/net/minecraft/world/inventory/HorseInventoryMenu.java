package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HorseInventoryMenu extends AbstractContainerMenu {
    private final Container horseContainer;
    private final Container armorContainer;
    private final AbstractHorse horse;
    private static final int SLOT_BODY_ARMOR = 1;
    private static final int SLOT_HORSE_INVENTORY_START = 2;

    public HorseInventoryMenu(int pContainerId, Inventory pInventory, Container pHorseContainer, final AbstractHorse pHorse, int pColumns) {
        super(null, pContainerId);
        this.horseContainer = pHorseContainer;
        this.armorContainer = pHorse.getBodyArmorAccess();
        this.horse = pHorse;
        int i = 3;
        pHorseContainer.startOpen(pInventory.player);
        int j = -18;
        this.addSlot(new Slot(pHorseContainer, 0, 8, 18) {
            /**
             * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
             */
            @Override
            public boolean mayPlace(ItemStack p_39677_) {
                return p_39677_.is(Items.SADDLE) && !this.hasItem() && pHorse.isSaddleable();
            }

            /**
             * Actually only call when we want to render the white square effect over the slots. Return always True,
             * except for the armor slot of the Donkey/Mule (we can't interact with the Undead and Skeleton horses)
             */
            @Override
            public boolean isActive() {
                return pHorse.isSaddleable();
            }
        });
        this.addSlot(new ArmorSlot(this.armorContainer, pHorse, EquipmentSlot.BODY, 0, 8, 36, null) {
            /**
             * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
             */
            @Override
            public boolean mayPlace(ItemStack p_39690_) {
                return pHorse.isBodyArmorItem(p_39690_);
            }

            /**
             * Actually only call when we want to render the white square effect over the slots. Return always True,
             * except for the armor slot of the Donkey/Mule (we can't interact with the Undead and Skeleton horses)
             */
            @Override
            public boolean isActive() {
                return pHorse.canUseSlot(EquipmentSlot.BODY);
            }
        });
        if (pColumns > 0) {
            for (int k = 0; k < 3; k++) {
                for (int l = 0; l < pColumns; l++) {
                    this.addSlot(new Slot(pHorseContainer, 1 + l + k * pColumns, 80 + l * 18, 18 + k * 18));
                }
            }
        }

        for (int i1 = 0; i1 < 3; i1++) {
            for (int k1 = 0; k1 < 9; k1++) {
                this.addSlot(new Slot(pInventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 102 + i1 * 18 + -18));
            }
        }

        for (int j1 = 0; j1 < 9; j1++) {
            this.addSlot(new Slot(pInventory, j1, 8 + j1 * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return !this.horse.hasInventoryChanged(this.horseContainer)
            && this.horseContainer.stillValid(pPlayer)
            && this.armorContainer.stillValid(pPlayer)
            && this.horse.isAlive()
            && pPlayer.canInteractWithEntity(this.horse, 4.0);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            int i = this.horseContainer.getContainerSize() + 1;
            if (pIndex < i) {
                if (!this.moveItemStackTo(itemstack1, i, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).mayPlace(itemstack1) && !this.getSlot(1).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).mayPlace(itemstack1)) {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i <= 1 || !this.moveItemStackTo(itemstack1, 2, i, false)) {
                int j = i + 27;
                int k = j + 9;
                if (pIndex >= j && pIndex < k) {
                    if (!this.moveItemStackTo(itemstack1, i, j, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (pIndex >= i && pIndex < j) {
                    if (!this.moveItemStackTo(itemstack1, j, k, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, j, j, false)) {
                    return ItemStack.EMPTY;
                }

                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.horseContainer.stopOpen(pPlayer);
    }
}