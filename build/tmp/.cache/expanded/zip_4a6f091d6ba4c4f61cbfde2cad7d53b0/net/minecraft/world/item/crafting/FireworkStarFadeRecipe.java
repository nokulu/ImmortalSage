package net.minecraft.world.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;

public class FireworkStarFadeRecipe extends CustomRecipe {
    private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

    public FireworkStarFadeRecipe(CraftingBookCategory pCategory) {
        super(pCategory);
    }

    public boolean matches(CraftingInput pInput, Level pLevel) {
        boolean flag = false;
        boolean flag1 = false;

        for (int i = 0; i < pInput.size(); i++) {
            ItemStack itemstack = pInput.getItem(i);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof DyeItem) {
                    flag = true;
                } else {
                    if (!STAR_INGREDIENT.test(itemstack)) {
                        return false;
                    }

                    if (flag1) {
                        return false;
                    }

                    flag1 = true;
                }
            }
        }

        return flag1 && flag;
    }

    public ItemStack assemble(CraftingInput pInput, HolderLookup.Provider pRegistries) {
        IntList intlist = new IntArrayList();
        ItemStack itemstack = null;

        for (int i = 0; i < pInput.size(); i++) {
            ItemStack itemstack1 = pInput.getItem(i);
            Item item = itemstack1.getItem();
            if (item instanceof DyeItem) {
                intlist.add(((DyeItem)item).getDyeColor().getFireworkColor());
            } else if (STAR_INGREDIENT.test(itemstack1)) {
                itemstack = itemstack1.copyWithCount(1);
            }
        }

        if (itemstack != null && !intlist.isEmpty()) {
            itemstack.update(DataComponents.FIREWORK_EXPLOSION, FireworkExplosion.DEFAULT, intlist, FireworkExplosion::withFadeColors);
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.FIREWORK_STAR_FADE;
    }
}