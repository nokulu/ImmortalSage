package net.minecraft.data.recipes;

import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public interface RecipeOutput {
    default void accept(ResourceLocation pLocation, Recipe<?> pRecipe, @Nullable AdvancementHolder pAdvancement) {
        if (pAdvancement == null) {
            accept(pLocation, pRecipe, null, null);
        } else {
            var ops = registry().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
            var json = Advancement.CODEC.encodeStart(ops, pAdvancement.value()).getOrThrow(IllegalStateException::new);
            accept(pLocation, pRecipe, pAdvancement.id(), json);
        }
    }

    void accept(ResourceLocation id, Recipe<?> recipe, @Nullable ResourceLocation advancementId, @Nullable com.google.gson.JsonElement advancement);

    net.minecraft.core.HolderLookup.Provider registry();

    Advancement.Builder advancement();
}
