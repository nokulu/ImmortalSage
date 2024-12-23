package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RecipeCraftedTrigger extends SimpleCriterionTrigger<RecipeCraftedTrigger.TriggerInstance> {
    @Override
    public Codec<RecipeCraftedTrigger.TriggerInstance> codec() {
        return RecipeCraftedTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer pPlayer, ResourceLocation pRecipeId, List<ItemStack> pItems) {
        this.trigger(pPlayer, p_282798_ -> p_282798_.matches(pRecipeId, pItems));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceLocation recipeId, List<ItemPredicate> ingredients)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<RecipeCraftedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325243_ -> p_325243_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(RecipeCraftedTrigger.TriggerInstance::player),
                        ResourceLocation.CODEC.fieldOf("recipe_id").forGetter(RecipeCraftedTrigger.TriggerInstance::recipeId),
                        ItemPredicate.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(RecipeCraftedTrigger.TriggerInstance::ingredients)
                    )
                    .apply(p_325243_, RecipeCraftedTrigger.TriggerInstance::new)
        );

        public static Criterion<RecipeCraftedTrigger.TriggerInstance> craftedItem(ResourceLocation pRecipeId, List<ItemPredicate.Builder> pIngredients) {
            return CriteriaTriggers.RECIPE_CRAFTED
                .createCriterion(
                    new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), pRecipeId, pIngredients.stream().map(ItemPredicate.Builder::build).toList())
                );
        }

        public static Criterion<RecipeCraftedTrigger.TriggerInstance> craftedItem(ResourceLocation pRecipeId) {
            return CriteriaTriggers.RECIPE_CRAFTED.createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), pRecipeId, List.of()));
        }

        public static Criterion<RecipeCraftedTrigger.TriggerInstance> crafterCraftedItem(ResourceLocation pRecipeId) {
            return CriteriaTriggers.CRAFTER_RECIPE_CRAFTED.createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), pRecipeId, List.of()));
        }

        boolean matches(ResourceLocation pRecipeId, List<ItemStack> pItems) {
            if (!pRecipeId.equals(this.recipeId)) {
                return false;
            } else {
                List<ItemStack> list = new ArrayList<>(pItems);

                for (ItemPredicate itempredicate : this.ingredients) {
                    boolean flag = false;
                    Iterator<ItemStack> iterator = list.iterator();

                    while (iterator.hasNext()) {
                        if (itempredicate.test(iterator.next())) {
                            iterator.remove();
                            flag = true;
                            break;
                        }
                    }

                    if (!flag) {
                        return false;
                    }
                }

                return true;
            }
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}