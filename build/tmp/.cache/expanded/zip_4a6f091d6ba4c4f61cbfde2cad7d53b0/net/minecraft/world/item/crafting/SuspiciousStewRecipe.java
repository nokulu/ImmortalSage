package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;

public class SuspiciousStewRecipe extends CustomRecipe {
    public SuspiciousStewRecipe(CraftingBookCategory pCategory) {
        super(pCategory);
    }

    public boolean matches(CraftingInput pInput, Level pLevel) {
        boolean flag = false;
        boolean flag1 = false;
        boolean flag2 = false;
        boolean flag3 = false;

        for (int i = 0; i < pInput.size(); i++) {
            ItemStack itemstack = pInput.getItem(i);
            if (!itemstack.isEmpty()) {
                if (itemstack.is(Blocks.BROWN_MUSHROOM.asItem()) && !flag2) {
                    flag2 = true;
                } else if (itemstack.is(Blocks.RED_MUSHROOM.asItem()) && !flag1) {
                    flag1 = true;
                } else if (itemstack.is(ItemTags.SMALL_FLOWERS) && !flag) {
                    flag = true;
                } else {
                    if (!itemstack.is(Items.BOWL) || flag3) {
                        return false;
                    }

                    flag3 = true;
                }
            }
        }

        return flag && flag2 && flag1 && flag3;
    }

    public ItemStack assemble(CraftingInput pInput, HolderLookup.Provider pRegistries) {
        ItemStack itemstack = new ItemStack(Items.SUSPICIOUS_STEW, 1);

        for (int i = 0; i < pInput.size(); i++) {
            ItemStack itemstack1 = pInput.getItem(i);
            if (!itemstack1.isEmpty()) {
                SuspiciousEffectHolder suspiciouseffectholder = SuspiciousEffectHolder.tryGet(itemstack1.getItem());
                if (suspiciouseffectholder != null) {
                    itemstack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, suspiciouseffectholder.getSuspiciousEffects());
                    break;
                }
            }
        }

        return itemstack;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth >= 2 && pHeight >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SUSPICIOUS_STEW;
    }
}