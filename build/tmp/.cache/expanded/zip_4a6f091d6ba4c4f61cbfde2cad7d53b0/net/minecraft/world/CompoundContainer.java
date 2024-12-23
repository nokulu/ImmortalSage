package net.minecraft.world;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CompoundContainer implements Container {
    private final Container container1;
    private final Container container2;

    public CompoundContainer(Container pContainer1, Container pContainer2) {
        this.container1 = pContainer1;
        this.container2 = pContainer2;
    }

    @Override
    public int getContainerSize() {
        return this.container1.getContainerSize() + this.container2.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return this.container1.isEmpty() && this.container2.isEmpty();
    }

    public boolean contains(Container pInventory) {
        return this.container1 == pInventory || this.container2 == pInventory;
    }

    @Override
    public ItemStack getItem(int pIndex) {
        return pIndex >= this.container1.getContainerSize() ? this.container2.getItem(pIndex - this.container1.getContainerSize()) : this.container1.getItem(pIndex);
    }

    @Override
    public ItemStack removeItem(int pIndex, int pCount) {
        return pIndex >= this.container1.getContainerSize()
            ? this.container2.removeItem(pIndex - this.container1.getContainerSize(), pCount)
            : this.container1.removeItem(pIndex, pCount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pIndex) {
        return pIndex >= this.container1.getContainerSize() ? this.container2.removeItemNoUpdate(pIndex - this.container1.getContainerSize()) : this.container1.removeItemNoUpdate(pIndex);
    }

    @Override
    public void setItem(int pIndex, ItemStack pStack) {
        if (pIndex >= this.container1.getContainerSize()) {
            this.container2.setItem(pIndex - this.container1.getContainerSize(), pStack);
        } else {
            this.container1.setItem(pIndex, pStack);
        }
    }

    @Override
    public int getMaxStackSize() {
        return this.container1.getMaxStackSize();
    }

    @Override
    public void setChanged() {
        this.container1.setChanged();
        this.container2.setChanged();
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return this.container1.stillValid(pPlayer) && this.container2.stillValid(pPlayer);
    }

    @Override
    public void startOpen(Player pPlayer) {
        this.container1.startOpen(pPlayer);
        this.container2.startOpen(pPlayer);
    }

    @Override
    public void stopOpen(Player pPlayer) {
        this.container1.stopOpen(pPlayer);
        this.container2.stopOpen(pPlayer);
    }

    @Override
    public boolean canPlaceItem(int pIndex, ItemStack pStack) {
        return pIndex >= this.container1.getContainerSize()
            ? this.container2.canPlaceItem(pIndex - this.container1.getContainerSize(), pStack)
            : this.container1.canPlaceItem(pIndex, pStack);
    }

    @Override
    public void clearContent() {
        this.container1.clearContent();
        this.container2.clearContent();
    }
}