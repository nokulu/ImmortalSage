package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapelessRecipe implements CraftingRecipe {
    final String group;
    final CraftingBookCategory category;
    final ItemStack result;
    final NonNullList<Ingredient> ingredients;
    private final boolean isSimple;

    public ShapelessRecipe(String pGroup, CraftingBookCategory pCategory, ItemStack pResult, NonNullList<Ingredient> pIngredients) {
        this.group = pGroup;
        this.category = pCategory;
        this.result = pResult;
        this.ingredients = pIngredients;
        this.isSimple = pIngredients.stream().allMatch(Ingredient::isSimple);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPELESS_RECIPE;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public boolean matches(CraftingInput pInput, Level pLevel) {
        if (pInput.ingredientCount() != this.ingredients.size()) {
            return false;
        } else if (pInput.size() == 1 && this.ingredients.size() == 1) {
            return this.ingredients.getFirst().test(pInput.getItem(0));
        } else if (!isSimple) {
            return net.minecraftforge.common.util.RecipeMatcher.findMatches(pInput.items(),  this.ingredients) != null;
        } else {
            return pInput.stackedContents().canCraft(this, null);
        }
    }

    public ItemStack assemble(CraftingInput pInput, HolderLookup.Provider pRegistries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= this.ingredients.size();
    }

    public static class Serializer implements RecipeSerializer<ShapelessRecipe> {
        private static final MapCodec<ShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(
            p_327212_ -> p_327212_.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(p_299460_ -> p_299460_.group),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(p_297437_ -> p_297437_.category),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(p_300770_ -> p_300770_.result),
                        Ingredient.CODEC_NONEMPTY
                            .listOf()
                            .fieldOf("ingredients")
                            .flatXmap(
                                p_297969_ -> {
                                    Ingredient[] aingredient = p_297969_.stream().filter(p_298915_ -> !p_298915_.isEmpty()).toArray(Ingredient[]::new);
                                    if (aingredient.length == 0) {
                                        return DataResult.error(() -> "No ingredients for shapeless recipe");
                                    } else {
                                        return aingredient.length > ShapedRecipe.MAX_WIDTH * ShapedRecipe.MAX_HEIGHT
                                            ? DataResult.error(() -> "Too many ingredients for shapeless recipe")
                                            : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                                    }
                                },
                                DataResult::success
                            )
                            .forGetter(p_298509_ -> p_298509_.ingredients)
                    )
                    .apply(p_327212_, ShapelessRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessRecipe> STREAM_CODEC = StreamCodec.of(
            ShapelessRecipe.Serializer::toNetwork, ShapelessRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<ShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ShapelessRecipe fromNetwork(RegistryFriendlyByteBuf p_335962_) {
            String s = p_335962_.readUtf();
            CraftingBookCategory craftingbookcategory = p_335962_.readEnum(CraftingBookCategory.class);
            int i = p_335962_.readVarInt();
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);
            nonnulllist.replaceAll(p_327214_ -> Ingredient.CONTENTS_STREAM_CODEC.decode(p_335962_));
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(p_335962_);
            return new ShapelessRecipe(s, craftingbookcategory, itemstack, nonnulllist);
        }

        private static void toNetwork(RegistryFriendlyByteBuf p_329239_, ShapelessRecipe p_44282_) {
            p_329239_.writeUtf(p_44282_.group);
            p_329239_.writeEnum(p_44282_.category);
            p_329239_.writeVarInt(p_44282_.ingredients.size());

            for (Ingredient ingredient : p_44282_.ingredients) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(p_329239_, ingredient);
            }

            ItemStack.STREAM_CODEC.encode(p_329239_, p_44282_.result);
        }
    }
}
