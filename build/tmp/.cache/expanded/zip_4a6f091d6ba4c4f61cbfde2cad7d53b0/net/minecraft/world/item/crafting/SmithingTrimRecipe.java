package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.Level;

public class SmithingTrimRecipe implements SmithingRecipe {
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;

    public SmithingTrimRecipe(Ingredient pTemplate, Ingredient pBase, Ingredient pAddition) {
        this.template = pTemplate;
        this.base = pBase;
        this.addition = pAddition;
    }

    public boolean matches(SmithingRecipeInput pInput, Level pLevel) {
        return this.template.test(pInput.template()) && this.base.test(pInput.base()) && this.addition.test(pInput.addition());
    }

    public ItemStack assemble(SmithingRecipeInput pInput, HolderLookup.Provider pRegistries) {
        ItemStack itemstack = pInput.base();
        if (this.base.test(itemstack)) {
            Optional<Holder.Reference<TrimMaterial>> optional = TrimMaterials.getFromIngredient(pRegistries, pInput.addition());
            Optional<Holder.Reference<TrimPattern>> optional1 = TrimPatterns.getFromTemplate(pRegistries, pInput.template());
            if (optional.isPresent() && optional1.isPresent()) {
                ArmorTrim armortrim = itemstack.get(DataComponents.TRIM);
                if (armortrim != null && armortrim.hasPatternAndMaterial(optional1.get(), optional.get())) {
                    return ItemStack.EMPTY;
                }

                ItemStack itemstack1 = itemstack.copyWithCount(1);
                itemstack1.set(DataComponents.TRIM, new ArmorTrim(optional.get(), optional1.get()));
                return itemstack1;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        ItemStack itemstack = new ItemStack(Items.IRON_CHESTPLATE);
        Optional<Holder.Reference<TrimPattern>> optional = pRegistries.lookupOrThrow(Registries.TRIM_PATTERN).listElements().findFirst();
        Optional<Holder.Reference<TrimMaterial>> optional1 = pRegistries.lookupOrThrow(Registries.TRIM_MATERIAL).get(TrimMaterials.REDSTONE);
        if (optional.isPresent() && optional1.isPresent()) {
            itemstack.set(DataComponents.TRIM, new ArmorTrim(optional1.get(), optional.get()));
        }

        return itemstack;
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
        return RecipeSerializer.SMITHING_TRIM;
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(net.minecraftforge.common.ForgeHooks::hasNoElements);
    }

    public static class Serializer implements RecipeSerializer<SmithingTrimRecipe> {
        private static final MapCodec<SmithingTrimRecipe> CODEC = RecordCodecBuilder.mapCodec(
            p_297394_ -> p_297394_.group(
                        Ingredient.CODEC.fieldOf("template").forGetter(p_298441_ -> p_298441_.template),
                        Ingredient.CODEC.fieldOf("base").forGetter(p_297838_ -> p_297838_.base),
                        Ingredient.CODEC.fieldOf("addition").forGetter(p_299309_ -> p_299309_.addition)
                    )
                    .apply(p_297394_, SmithingTrimRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> STREAM_CODEC = StreamCodec.of(
            SmithingTrimRecipe.Serializer::toNetwork, SmithingTrimRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<SmithingTrimRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static SmithingTrimRecipe fromNetwork(RegistryFriendlyByteBuf p_333367_) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(p_333367_);
            Ingredient ingredient1 = Ingredient.CONTENTS_STREAM_CODEC.decode(p_333367_);
            Ingredient ingredient2 = Ingredient.CONTENTS_STREAM_CODEC.decode(p_333367_);
            return new SmithingTrimRecipe(ingredient, ingredient1, ingredient2);
        }

        private static void toNetwork(RegistryFriendlyByteBuf p_335485_, SmithingTrimRecipe p_335201_) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(p_335485_, p_335201_.template);
            Ingredient.CONTENTS_STREAM_CODEC.encode(p_335485_, p_335201_.base);
            Ingredient.CONTENTS_STREAM_CODEC.encode(p_335485_, p_335201_.addition);
        }
    }
}
