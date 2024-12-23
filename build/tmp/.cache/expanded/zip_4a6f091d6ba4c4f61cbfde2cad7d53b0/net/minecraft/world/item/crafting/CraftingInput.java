package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;

public class CraftingInput implements RecipeInput {
    public static final CraftingInput EMPTY = new CraftingInput(0, 0, List.of());
    private final int width;
    private final int height;
    private final List<ItemStack> items;
    private final StackedContents stackedContents = new StackedContents();
    private final int ingredientCount;

    private CraftingInput(int pWidth, int pHeight, List<ItemStack> pItem) {
        this.width = pWidth;
        this.height = pHeight;
        this.items = pItem;
        int i = 0;

        for (ItemStack itemstack : pItem) {
            if (!itemstack.isEmpty()) {
                i++;
                this.stackedContents.accountStack(itemstack, 1);
            }
        }

        this.ingredientCount = i;
    }

    public static CraftingInput of(int pWidth, int pHeight, List<ItemStack> pItems) {
        return ofPositioned(pWidth, pHeight, pItems).input();
    }

    public static CraftingInput.Positioned ofPositioned(int pWidth, int pHeight, List<ItemStack> pItems) {
        if (pWidth != 0 && pHeight != 0) {
            int i = pWidth - 1;
            int j = 0;
            int k = pHeight - 1;
            int l = 0;

            for (int i1 = 0; i1 < pHeight; i1++) {
                boolean flag = true;

                for (int j1 = 0; j1 < pWidth; j1++) {
                    ItemStack itemstack = pItems.get(j1 + i1 * pWidth);
                    if (!itemstack.isEmpty()) {
                        i = Math.min(i, j1);
                        j = Math.max(j, j1);
                        flag = false;
                    }
                }

                if (!flag) {
                    k = Math.min(k, i1);
                    l = Math.max(l, i1);
                }
            }

            int i2 = j - i + 1;
            int j2 = l - k + 1;
            if (i2 <= 0 || j2 <= 0) {
                return CraftingInput.Positioned.EMPTY;
            } else if (i2 == pWidth && j2 == pHeight) {
                return new CraftingInput.Positioned(new CraftingInput(pWidth, pHeight, pItems), i, k);
            } else {
                List<ItemStack> list = new ArrayList<>(i2 * j2);

                for (int k2 = 0; k2 < j2; k2++) {
                    for (int k1 = 0; k1 < i2; k1++) {
                        int l1 = k1 + i + (k2 + k) * pWidth;
                        list.add(pItems.get(l1));
                    }
                }

                return new CraftingInput.Positioned(new CraftingInput(i2, j2, list), i, k);
            }
        } else {
            return CraftingInput.Positioned.EMPTY;
        }
    }

    @Override
    public ItemStack getItem(int pIndex) {
        return this.items.get(pIndex);
    }

    public ItemStack getItem(int pRow, int pColumn) {
        return this.items.get(pRow + pColumn * this.width);
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        return this.ingredientCount == 0;
    }

    public StackedContents stackedContents() {
        return this.stackedContents;
    }

    public List<ItemStack> items() {
        return this.items;
    }

    public int ingredientCount() {
        return this.ingredientCount;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    @Override
    public boolean equals(Object pOther) {
        if (pOther == this) {
            return true;
        } else {
            return !(pOther instanceof CraftingInput craftinginput)
                ? false
                : this.width == craftinginput.width
                    && this.height == craftinginput.height
                    && this.ingredientCount == craftinginput.ingredientCount
                    && ItemStack.listMatches(this.items, craftinginput.items);
        }
    }

    @Override
    public int hashCode() {
        int i = ItemStack.hashStackList(this.items);
        i = 31 * i + this.width;
        return 31 * i + this.height;
    }

    public static record Positioned(CraftingInput input, int left, int top) {
        public static final CraftingInput.Positioned EMPTY = new CraftingInput.Positioned(CraftingInput.EMPTY, 0, 0);
    }
}