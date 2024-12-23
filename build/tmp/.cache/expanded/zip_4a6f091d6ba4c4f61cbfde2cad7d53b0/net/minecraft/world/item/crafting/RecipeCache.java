package net.minecraft.world.item.crafting;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RecipeCache {
    private final RecipeCache.Entry[] entries;
    private WeakReference<RecipeManager> cachedRecipeManager = new WeakReference<>(null);

    public RecipeCache(int pSize) {
        this.entries = new RecipeCache.Entry[pSize];
    }

    public Optional<RecipeHolder<CraftingRecipe>> get(Level pLevel, CraftingInput pInput) {
        if (pInput.isEmpty()) {
            return Optional.empty();
        } else {
            this.validateRecipeManager(pLevel);

            for (int i = 0; i < this.entries.length; i++) {
                RecipeCache.Entry recipecache$entry = this.entries[i];
                if (recipecache$entry != null && recipecache$entry.matches(pInput)) {
                    this.moveEntryToFront(i);
                    return Optional.ofNullable(recipecache$entry.value());
                }
            }

            return this.compute(pInput, pLevel);
        }
    }

    private void validateRecipeManager(Level pLevel) {
        RecipeManager recipemanager = pLevel.getRecipeManager();
        if (recipemanager != this.cachedRecipeManager.get()) {
            this.cachedRecipeManager = new WeakReference<>(recipemanager);
            Arrays.fill(this.entries, null);
        }
    }

    private Optional<RecipeHolder<CraftingRecipe>> compute(CraftingInput pInput, Level pLevel) {
        Optional<RecipeHolder<CraftingRecipe>> optional = pLevel.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, pInput, pLevel);
        this.insert(pInput, optional.orElse(null));
        return optional;
    }

    private void moveEntryToFront(int pIndex) {
        if (pIndex > 0) {
            RecipeCache.Entry recipecache$entry = this.entries[pIndex];
            System.arraycopy(this.entries, 0, this.entries, 1, pIndex);
            this.entries[0] = recipecache$entry;
        }
    }

    private void insert(CraftingInput pInput, @Nullable RecipeHolder<CraftingRecipe> pRecipe) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(pInput.size(), ItemStack.EMPTY);

        for (int i = 0; i < pInput.size(); i++) {
            nonnulllist.set(i, pInput.getItem(i).copyWithCount(1));
        }

        System.arraycopy(this.entries, 0, this.entries, 1, this.entries.length - 1);
        this.entries[0] = new RecipeCache.Entry(nonnulllist, pInput.width(), pInput.height(), pRecipe);
    }

    static record Entry(NonNullList<ItemStack> key, int width, int height, @Nullable RecipeHolder<CraftingRecipe> value) {
        public boolean matches(CraftingInput pInput) {
            if (this.width == pInput.width() && this.height == pInput.height()) {
                for (int i = 0; i < this.key.size(); i++) {
                    if (!ItemStack.isSameItemSameComponents(this.key.get(i), pInput.getItem(i))) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        }
    }
}