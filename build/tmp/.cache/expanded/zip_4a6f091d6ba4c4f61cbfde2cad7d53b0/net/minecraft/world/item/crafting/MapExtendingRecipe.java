package net.minecraft.world.item.crafting;

import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapExtendingRecipe extends ShapedRecipe {
    public MapExtendingRecipe(CraftingBookCategory pCategory) {
        super(
            "",
            pCategory,
            ShapedRecipePattern.of(Map.of('#', Ingredient.of(Items.PAPER), 'x', Ingredient.of(Items.FILLED_MAP)), "###", "#x#", "###"),
            new ItemStack(Items.MAP)
        );
    }

    @Override
    public boolean matches(CraftingInput pInput, Level pLevel) {
        if (!super.matches(pInput, pLevel)) {
            return false;
        } else {
            ItemStack itemstack = findFilledMap(pInput);
            if (itemstack.isEmpty()) {
                return false;
            } else {
                MapItemSavedData mapitemsaveddata = MapItem.getSavedData(itemstack, pLevel);
                if (mapitemsaveddata == null) {
                    return false;
                } else {
                    return mapitemsaveddata.isExplorationMap() ? false : mapitemsaveddata.scale < 4;
                }
            }
        }
    }

    @Override
    public ItemStack assemble(CraftingInput pInput, HolderLookup.Provider pRegistries) {
        ItemStack itemstack = findFilledMap(pInput).copyWithCount(1);
        itemstack.set(DataComponents.MAP_POST_PROCESSING, MapPostProcessing.SCALE);
        return itemstack;
    }

    private static ItemStack findFilledMap(CraftingInput pInput) {
        for (int i = 0; i < pInput.size(); i++) {
            ItemStack itemstack = pInput.getItem(i);
            if (itemstack.is(Items.FILLED_MAP)) {
                return itemstack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.MAP_EXTENDING;
    }
}