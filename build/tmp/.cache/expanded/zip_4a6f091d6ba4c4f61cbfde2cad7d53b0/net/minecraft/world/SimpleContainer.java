package net.minecraft.world;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SimpleContainer implements Container, StackedContentsCompatible {
    private final int size;
    private final NonNullList<ItemStack> items;
    @Nullable
    private List<ContainerListener> listeners;

    public SimpleContainer(int pSize) {
        this.size = pSize;
        this.items = NonNullList.withSize(pSize, ItemStack.EMPTY);
    }

    public SimpleContainer(ItemStack... pItems) {
        this.size = pItems.length;
        this.items = NonNullList.of(ItemStack.EMPTY, pItems);
    }

    public void addListener(ContainerListener pListener) {
        if (this.listeners == null) {
            this.listeners = Lists.newArrayList();
        }

        this.listeners.add(pListener);
    }

    public void removeListener(ContainerListener pListener) {
        if (this.listeners != null) {
            this.listeners.remove(pListener);
        }
    }

    @Override
    public ItemStack getItem(int pIndex) {
        return pIndex >= 0 && pIndex < this.items.size() ? this.items.get(pIndex) : ItemStack.EMPTY;
    }

    public List<ItemStack> removeAllItems() {
        List<ItemStack> list = this.items.stream().filter(p_19197_ -> !p_19197_.isEmpty()).collect(Collectors.toList());
        this.clearContent();
        return list;
    }

    @Override
    public ItemStack removeItem(int pIndex, int pCount) {
        ItemStack itemstack = ContainerHelper.removeItem(this.items, pIndex, pCount);
        if (!itemstack.isEmpty()) {
            this.setChanged();
        }

        return itemstack;
    }

    public ItemStack removeItemType(Item pItem, int pAmount) {
        ItemStack itemstack = new ItemStack(pItem, 0);

        for (int i = this.size - 1; i >= 0; i--) {
            ItemStack itemstack1 = this.getItem(i);
            if (itemstack1.getItem().equals(pItem)) {
                int j = pAmount - itemstack.getCount();
                ItemStack itemstack2 = itemstack1.split(j);
                itemstack.grow(itemstack2.getCount());
                if (itemstack.getCount() == pAmount) {
                    break;
                }
            }
        }

        if (!itemstack.isEmpty()) {
            this.setChanged();
        }

        return itemstack;
    }

    public ItemStack addItem(ItemStack p_19174_) {
        if (p_19174_.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemstack = p_19174_.copy();
            this.moveItemToOccupiedSlotsWithSameType(itemstack);
            if (itemstack.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                this.moveItemToEmptySlots(itemstack);
                return itemstack.isEmpty() ? ItemStack.EMPTY : itemstack;
            }
        }
    }

    public boolean canAddItem(ItemStack pStack) {
        boolean flag = false;

        for (ItemStack itemstack : this.items) {
            if (itemstack.isEmpty() || ItemStack.isSameItemSameComponents(itemstack, pStack) && itemstack.getCount() < itemstack.getMaxStackSize()) {
                flag = true;
                break;
            }
        }

        return flag;
    }

    @Override
    public ItemStack removeItemNoUpdate(int pIndex) {
        ItemStack itemstack = this.items.get(pIndex);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.items.set(pIndex, ItemStack.EMPTY);
            return itemstack;
        }
    }

    @Override
    public void setItem(int pIndex, ItemStack pStack) {
        this.items.set(pIndex, pStack);
        pStack.limitSize(this.getMaxStackSize(pStack));
        this.setChanged();
    }

    @Override
    public int getContainerSize() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void setChanged() {
        if (this.listeners != null) {
            for (ContainerListener containerlistener : this.listeners) {
                containerlistener.containerChanged(this);
            }
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    @Override
    public void fillStackedContents(StackedContents pHelper) {
        for (ItemStack itemstack : this.items) {
            pHelper.accountStack(itemstack);
        }
    }

    @Override
    public String toString() {
        return this.items.stream().filter(p_19194_ -> !p_19194_.isEmpty()).collect(Collectors.toList()).toString();
    }

    private void moveItemToEmptySlots(ItemStack pStack) {
        for (int i = 0; i < this.size; i++) {
            ItemStack itemstack = this.getItem(i);
            if (itemstack.isEmpty()) {
                this.setItem(i, pStack.copyAndClear());
                return;
            }
        }
    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack pStack) {
        for (int i = 0; i < this.size; i++) {
            ItemStack itemstack = this.getItem(i);
            if (ItemStack.isSameItemSameComponents(itemstack, pStack)) {
                this.moveItemsBetweenStacks(pStack, itemstack);
                if (pStack.isEmpty()) {
                    return;
                }
            }
        }
    }

    private void moveItemsBetweenStacks(ItemStack pStack, ItemStack pOther) {
        int i = this.getMaxStackSize(pOther);
        int j = Math.min(pStack.getCount(), i - pOther.getCount());
        if (j > 0) {
            pOther.grow(j);
            pStack.shrink(j);
            this.setChanged();
        }
    }

    public void fromTag(ListTag pTag, HolderLookup.Provider pLevelRegistry) {
        this.clearContent();

        for (int i = 0; i < pTag.size(); i++) {
            ItemStack.parse(pLevelRegistry, pTag.getCompound(i)).ifPresent(this::addItem);
        }
    }

    public ListTag createTag(HolderLookup.Provider pLevelRegistry) {
        ListTag listtag = new ListTag();

        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack itemstack = this.getItem(i);
            if (!itemstack.isEmpty()) {
                listtag.add(itemstack.save(pLevelRegistry));
            }
        }

        return listtag;
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }
}