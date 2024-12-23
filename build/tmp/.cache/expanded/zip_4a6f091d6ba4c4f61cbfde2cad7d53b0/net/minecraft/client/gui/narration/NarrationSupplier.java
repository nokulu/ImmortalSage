package net.minecraft.client.gui.narration;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * An interface for providing narration information.
 */
@OnlyIn(Dist.CLIENT)
public interface NarrationSupplier {
    void updateNarration(NarrationElementOutput pNarrationElementOutput);
}