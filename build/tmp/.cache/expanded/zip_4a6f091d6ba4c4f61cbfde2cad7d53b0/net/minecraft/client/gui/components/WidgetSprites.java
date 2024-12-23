package net.minecraft.client.gui.components;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record WidgetSprites(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation enabledFocused, ResourceLocation disabledFocused) {
    public WidgetSprites(ResourceLocation pEnabled, ResourceLocation pDisabled) {
        this(pEnabled, pEnabled, pDisabled, pDisabled);
    }

    public WidgetSprites(ResourceLocation pEnabled, ResourceLocation pDisabled, ResourceLocation pEnabledFocused) {
        this(pEnabled, pDisabled, pEnabledFocused, pDisabled);
    }

    public ResourceLocation get(boolean pEnabled, boolean pFocused) {
        if (pEnabled) {
            return pFocused ? this.enabledFocused : this.enabled;
        } else {
            return pFocused ? this.disabledFocused : this.disabled;
        }
    }
}