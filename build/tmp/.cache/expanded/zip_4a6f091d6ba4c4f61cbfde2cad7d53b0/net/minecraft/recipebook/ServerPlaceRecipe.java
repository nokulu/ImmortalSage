package net.minecraft.recipebook;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;

public class ServerPlaceRecipe<I extends RecipeInput, R extends Recipe<I>> implements PlaceRecipe<Integer> {
    private static final int ITEM_NOT_FOUND = -1;
    protected final StackedContents stackedContents = new StackedContents();
    protected Inventory inventory;
    protected RecipeBookMenu<I, R> menu;

    public ServerPlaceRecipe(RecipeBookMenu<I, R> pMenu) {
        this.menu = pMenu;
    }

    public void recipeClicked(ServerPlayer pPlayer, @Nullable RecipeHolder<R> pRecipe, boolean pPlaceAll) {
        if (pRecipe != null && pPlayer.getRecipeBook().contains(pRecipe)) {
            this.inventory = pPlayer.getInventory();
            if (this.testClearGrid() || pPlayer.isCreative()) {
                this.stackedContents.clear();
                pPlayer.getInventory().fillStackedContents(this.stackedContents);
                this.menu.fillCraftSlotsStackedContents(this.stackedContents);
                if (this.stackedContents.canCraft(pRecipe.value(), null)) {
                    this.handleRecipeClicked(pRecipe, pPlaceAll);
                } else {
                    this.clearGrid();
                    pPlayer.connection.send(new ClientboundPlaceGhostRecipePacket(pPlayer.containerMenu.containerId, pRecipe));
                }

                pPlayer.getInventory().setChanged();
            }
        }
    }

    protected void clearGrid() {
        for (int i = 0; i < this.menu.getSize(); i++) {
            if (this.menu.shouldMoveToInventory(i)) {
                ItemStack itemstack = this.menu.getSlot(i).getItem().copy();
                this.inventory.placeItemBackInInventory(itemstack, false);
                this.menu.getSlot(i).set(itemstack);
            }
        }

        this.menu.clearCraftingContent();
    }

    protected void handleRecipeClicked(RecipeHolder<R> pRecipe, boolean pPlaceAll) {
        boolean flag = this.menu.recipeMatches(pRecipe);
        int i = this.stackedContents.getBiggestCraftableStack(pRecipe, null);
        if (flag) {
            for (int j = 0; j < this.menu.getGridHeight() * this.menu.getGridWidth() + 1; j++) {
                if (j != this.menu.getResultSlotIndex()) {
                    ItemStack itemstack = this.menu.getSlot(j).getItem();
                    if (!itemstack.isEmpty() && Math.min(i, itemstack.getMaxStackSize()) < itemstack.getCount() + 1) {
                        return;
                    }
                }
            }
        }

        int j1 = this.getStackSize(pPlaceAll, i, flag);
        IntList intlist = new IntArrayList();
        if (this.stackedContents.canCraft(pRecipe.value(), intlist, j1)) {
            int k = j1;

            for (int l : intlist) {
                ItemStack itemstack1 = StackedContents.fromStackingIndex(l);
                if (!itemstack1.isEmpty()) {
                    int i1 = itemstack1.getMaxStackSize();
                    if (i1 < k) {
                        k = i1;
                    }
                }
            }

            if (this.stackedContents.canCraft(pRecipe.value(), intlist, k)) {
                this.clearGrid();
                this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), pRecipe, intlist.iterator(), k);
            }
        }
    }

    public void addItemToSlot(Integer pItem, int pSlot, int pMaxAmount, int pX, int pY) {
        Slot slot = this.menu.getSlot(pSlot);
        ItemStack itemstack = StackedContents.fromStackingIndex(pItem);
        if (!itemstack.isEmpty()) {
            int i = pMaxAmount;

            while (i > 0) {
                i = this.moveItemToGrid(slot, itemstack, i);
                if (i == -1) {
                    return;
                }
            }
        }
    }

    protected int getStackSize(boolean pPlaceAll, int pMaxPossible, boolean pRecipeMatches) {
        int i = 1;
        if (pPlaceAll) {
            i = pMaxPossible;
        } else if (pRecipeMatches) {
            i = Integer.MAX_VALUE;

            for (int j = 0; j < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; j++) {
                if (j != this.menu.getResultSlotIndex()) {
                    ItemStack itemstack = this.menu.getSlot(j).getItem();
                    if (!itemstack.isEmpty() && i > itemstack.getCount()) {
                        i = itemstack.getCount();
                    }
                }
            }

            if (i != Integer.MAX_VALUE) {
                i++;
            }
        }

        return i;
    }

    protected int moveItemToGrid(Slot pSlot, ItemStack pStack, int pMaxAmount) {
        int i = this.inventory.findSlotMatchingUnusedItem(pStack);
        if (i == -1) {
            return -1;
        } else {
            ItemStack itemstack = this.inventory.getItem(i);
            int j;
            if (pMaxAmount < itemstack.getCount()) {
                this.inventory.removeItem(i, pMaxAmount);
                j = pMaxAmount;
            } else {
                this.inventory.removeItemNoUpdate(i);
                j = itemstack.getCount();
            }

            if (pSlot.getItem().isEmpty()) {
                pSlot.set(itemstack.copyWithCount(j));
            } else {
                pSlot.getItem().grow(j);
            }

            return pMaxAmount - j;
        }
    }

    private boolean testClearGrid() {
        List<ItemStack> list = Lists.newArrayList();
        int i = this.getAmountOfFreeSlotsInInventory();

        for (int j = 0; j < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; j++) {
            if (j != this.menu.getResultSlotIndex()) {
                ItemStack itemstack = this.menu.getSlot(j).getItem().copy();
                if (!itemstack.isEmpty()) {
                    int k = this.inventory.getSlotWithRemainingSpace(itemstack);
                    if (k == -1 && list.size() <= i) {
                        for (ItemStack itemstack1 : list) {
                            if (ItemStack.isSameItem(itemstack1, itemstack)
                                && itemstack1.getCount() != itemstack1.getMaxStackSize()
                                && itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize()) {
                                itemstack1.grow(itemstack.getCount());
                                itemstack.setCount(0);
                                break;
                            }
                        }

                        if (!itemstack.isEmpty()) {
                            if (list.size() >= i) {
                                return false;
                            }

                            list.add(itemstack);
                        }
                    } else if (k == -1) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private int getAmountOfFreeSlotsInInventory() {
        int i = 0;

        for (ItemStack itemstack : this.inventory.items) {
            if (itemstack.isEmpty()) {
                i++;
            }
        }

        return i;
    }
}