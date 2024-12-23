package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Slot {
    private final int slot;
    public final Container container;
    public int index;
    public final int x;
    public final int y;

    public Slot(Container pContainer, int pSlot, int pX, int pY) {
        this.container = pContainer;
        this.slot = pSlot;
        this.x = pX;
        this.y = pY;
    }

    public void onQuickCraft(ItemStack pOldStack, ItemStack pNewStack) {
        int i = pNewStack.getCount() - pOldStack.getCount();
        if (i > 0) {
            this.onQuickCraft(pNewStack, i);
        }
    }

    protected void onQuickCraft(ItemStack pStack, int pAmount) {
    }

    protected void onSwapCraft(int pNumItemsCrafted) {
    }

    protected void checkTakeAchievements(ItemStack pStack) {
    }

    public void onTake(Player pPlayer, ItemStack pStack) {
        this.setChanged();
    }

    public boolean mayPlace(ItemStack pStack) {
        return true;
    }

    public ItemStack getItem() {
        return this.container.getItem(this.slot);
    }

    public boolean hasItem() {
        return !this.getItem().isEmpty();
    }

    public void setByPlayer(ItemStack pStack) {
        this.setByPlayer(pStack, this.getItem());
    }

    public void setByPlayer(ItemStack pNewStack, ItemStack pOldStack) {
        this.set(pNewStack);
    }

    public void set(ItemStack pStack) {
        this.container.setItem(this.slot, pStack);
        this.setChanged();
    }

    public void setChanged() {
        this.container.setChanged();
    }

    public int getMaxStackSize() {
        return this.container.getMaxStackSize();
    }

    public int getMaxStackSize(ItemStack pStack) {
        return Math.min(this.getMaxStackSize(), pStack.getMaxStackSize());
    }

    @Nullable
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return backgroundPair;
    }

    public ItemStack remove(int pAmount) {
        return this.container.removeItem(this.slot, pAmount);
    }

    public boolean mayPickup(Player pPlayer) {
        return true;
    }

    public boolean isActive() {
        return true;
    }

    public Optional<ItemStack> tryRemove(int pCount, int pDecrement, Player pPlayer) {
        if (!this.mayPickup(pPlayer)) {
            return Optional.empty();
        } else if (!this.allowModification(pPlayer) && pDecrement < this.getItem().getCount()) {
            return Optional.empty();
        } else {
            pCount = Math.min(pCount, pDecrement);
            ItemStack itemstack = this.remove(pCount);
            if (itemstack.isEmpty()) {
                return Optional.empty();
            } else {
                if (this.getItem().isEmpty()) {
                    this.setByPlayer(ItemStack.EMPTY, itemstack);
                }

                return Optional.of(itemstack);
            }
        }
    }

    public ItemStack safeTake(int pCount, int pDecrement, Player pPlayer) {
        Optional<ItemStack> optional = this.tryRemove(pCount, pDecrement, pPlayer);
        optional.ifPresent(p_150655_ -> this.onTake(pPlayer, p_150655_));
        return optional.orElse(ItemStack.EMPTY);
    }

    public ItemStack safeInsert(ItemStack pStack) {
        return this.safeInsert(pStack, pStack.getCount());
    }

    public ItemStack safeInsert(ItemStack pStack, int pIncrement) {
        if (!pStack.isEmpty() && this.mayPlace(pStack)) {
            ItemStack itemstack = this.getItem();
            int i = Math.min(Math.min(pIncrement, pStack.getCount()), this.getMaxStackSize(pStack) - itemstack.getCount());
            if (itemstack.isEmpty()) {
                this.setByPlayer(pStack.split(i));
            } else if (ItemStack.isSameItemSameComponents(itemstack, pStack)) {
                pStack.shrink(i);
                itemstack.grow(i);
                this.setByPlayer(itemstack);
            }

            return pStack;
        } else {
            return pStack;
        }
    }

    public boolean allowModification(Player pPlayer) {
        return this.mayPickup(pPlayer) && this.mayPlace(this.getItem());
    }

    public int getContainerSlot() {
        return this.slot;
    }

    public boolean isHighlightable() {
        return true;
    }

    public boolean isFake() {
        return false;
    }

    /**
     * Retrieves the index in the inventory for this slot, this value should typically not
     * be used, but can be useful for some occasions.
     *
     * @return Index in associated inventory for this slot.
     */
    public int getSlotIndex() {
       return slot;
    }

    /**
     * Checks if the other slot is in the same inventory, by comparing the inventory reference.
     * @param other
     * @return true if the other slot is in the same inventory
     */
    public boolean isSameInventory(Slot other) {
        return this.container == other.container;
    }

    private Pair<ResourceLocation, ResourceLocation> backgroundPair;
    /**
     * Sets the background atlas and sprite location.
     *
     * @param atlas The atlas name
     * @param sprite The sprite located on that atlas.
     * @return this, to allow chaining.
     */
    public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
        this.backgroundPair = Pair.of(atlas, sprite);
        return this;
    }
}
