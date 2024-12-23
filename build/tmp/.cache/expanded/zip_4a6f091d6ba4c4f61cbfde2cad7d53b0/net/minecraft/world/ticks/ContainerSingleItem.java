package net.minecraft.world.ticks;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ContainerSingleItem extends Container {
    ItemStack getTheItem();

    default ItemStack splitTheItem(int pAmount) {
        return this.getTheItem().split(pAmount);
    }

    void setTheItem(ItemStack pItem);

    default ItemStack removeTheItem() {
        return this.splitTheItem(this.getMaxStackSize());
    }

    @Override
    default int getContainerSize() {
        return 1;
    }

    @Override
    default boolean isEmpty() {
        return this.getTheItem().isEmpty();
    }

    @Override
    default void clearContent() {
        this.removeTheItem();
    }

    @Override
    default ItemStack removeItemNoUpdate(int pSlot) {
        return this.removeItem(pSlot, this.getMaxStackSize());
    }

    @Override
    default ItemStack getItem(int pSlot) {
        return pSlot == 0 ? this.getTheItem() : ItemStack.EMPTY;
    }

    @Override
    default ItemStack removeItem(int pSlot, int pAmount) {
        return pSlot != 0 ? ItemStack.EMPTY : this.splitTheItem(pAmount);
    }

    @Override
    default void setItem(int pSlot, ItemStack pStack) {
        if (pSlot == 0) {
            this.setTheItem(pStack);
        }
    }

    public interface BlockContainerSingleItem extends ContainerSingleItem {
        BlockEntity getContainerBlockEntity();

        @Override
        default boolean stillValid(Player p_335018_) {
            return Container.stillValidBlockEntity(this.getContainerBlockEntity(), p_335018_);
        }
    }
}