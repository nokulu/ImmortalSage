package net.minecraft.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.DeltaTracker;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayeredDraw {
    public static final float Z_SEPARATION = 200.0F;
    private final List<LayeredDraw.Layer> layers = new ArrayList<>();

    public LayeredDraw add(LayeredDraw.Layer pLayer) {
        this.layers.add(pLayer);
        return this;
    }

    public LayeredDraw add(LayeredDraw pLayeredDraw, BooleanSupplier pRenderInner) {
        return this.add((p_340772_, p_340773_) -> {
            if (pRenderInner.getAsBoolean()) {
                pLayeredDraw.renderInner(p_340772_, p_340773_);
            }
        });
    }

    public void render(GuiGraphics pGuiGraphics, DeltaTracker pDeltaTracker) {
        pGuiGraphics.pose().pushPose();
        this.renderInner(pGuiGraphics, pDeltaTracker);
        pGuiGraphics.pose().popPose();
    }

    private void renderInner(GuiGraphics pGuiGraphics, DeltaTracker pDeltaTracker) {
        for (LayeredDraw.Layer layereddraw$layer : this.layers) {
            layereddraw$layer.render(pGuiGraphics, pDeltaTracker);
            pGuiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface Layer {
        void render(GuiGraphics pGuiGraphics, DeltaTracker pDeltaTracker);
    }
}