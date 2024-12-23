package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SmithingTransformRecipe implements SmithingRecipe {
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;
    final ItemStack result;

    public SmithingTransformRecipe(Ingredient pTemplate, Ingredient pBase, Ingredient pAddition, ItemStack pResult) {
        this.template = pTemplate;
        this.base = pBase;
        this.addition = pAddition;
        this.result = pResult;
    }

    public boolean matches(SmithingRecipeInput pInput, Level pLevel) {
        return this.template.test(pInput.template()) && this.base.test(pInput.base()) && this.addition.test(pInput.addition());
    }

    public ItemStack assemble(SmithingRecipeInput pInput, HolderLookup.Provider pRegistries) {
        ItemStack itemstack = pInput.base().transmuteCopy(this.result.getItem(), this.result.getCount());
        itemstack.applyComponents(this.result.getComponentsPatch());
        return itemstack;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        return this.result;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack pStack) {
        return this.template.test(pStack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack pStack) {
        return this.base.test(pStack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack pStack) {
        return this.addition.test(pStack);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMITHING_TRANSFORM;
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(net.minecraftforge.common.ForgeHooks::hasNoElements);
    }

    public static class Serializer implements RecipeSerializer<SmithingTransformRecipe> {
        private static final MapCodec<SmithingTransformRecipe> CODEC = RecordCodecBuilder.mapCodec(
            p_327220_ -> p_327220_.group(
                        Ingredient.CODEC.fieldOf("template").forGetter(p_297231_ -> p_297231_.template),
                        Ingredient.CODEC.fieldOf("base").forGetter(p_298250_ -> p_298250_.base),
                        Ingredient.CODEC.fieldOf("addition").forGetter(p_299654_ -> p_299654_.addition),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(p_297480_ -> p_297480_.result)
                    )
                    .apply(p_327220_, SmithingTransformRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> STREAM_CODEC = StreamCodec.of(
            SmithingTransformRecipe.Serializer::toNetwork, SmithingTransformRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<SmithingTransformRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static SmithingTransformRecipe fromNetwork(RegistryFriendlyByteBuf p_333917_) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(p_333917_);
            Ingredient ingredient1 = Ingredient.CONTENTS_STREAM_CODEC.decode(p_333917_);
            Ingredient ingredient2 = Ingredient.CONTENTS_STREAM_CODEC.decode(p_333917_);
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(p_333917_);
            return new SmithingTransformRecipe(ingredient, ingredient1, ingredient2, itemstack);
        }

        private static void toNetwork(RegistryFriendlyByteBuf p_329920_, SmithingTransformRecipe p_266927_) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(p_329920_, p_266927_.template);
            Ingredient.CONTENTS_STREAM_CODEC.encode(p_329920_, p_266927_.base);
            Ingredient.CONTENTS_STREAM_CODEC.encode(p_329920_, p_266927_.addition);
            ItemStack.STREAM_CODEC.encode(p_329920_, p_266927_.result);
        }
    }
}
