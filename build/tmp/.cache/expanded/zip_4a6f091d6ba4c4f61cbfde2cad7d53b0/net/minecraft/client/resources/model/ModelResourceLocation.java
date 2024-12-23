package net.minecraft.client.resources.model;

import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ModelResourceLocation(ResourceLocation id, String variant) {
    public static final String INVENTORY_VARIANT = "inventory";

    public ModelResourceLocation(ResourceLocation id, String variant) {
        variant = lowercaseVariant(variant);
        this.id = id;
        this.variant = variant;
    }

    public static ModelResourceLocation vanilla(String pPath, String pVariant) {
        return new ModelResourceLocation(ResourceLocation.withDefaultNamespace(pPath), pVariant);
    }

    public static ModelResourceLocation inventory(ResourceLocation pId) {
        return new ModelResourceLocation(pId, "inventory");
    }

    private static String lowercaseVariant(String pVariant) {
        return pVariant.toLowerCase(Locale.ROOT);
    }

    public String getVariant() {
        return this.variant;
    }

    @Override
    public String toString() {
        return this.id + "#" + this.variant;
    }
}