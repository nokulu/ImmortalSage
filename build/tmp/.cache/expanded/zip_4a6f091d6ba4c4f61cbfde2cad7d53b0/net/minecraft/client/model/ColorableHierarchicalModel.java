package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ColorableHierarchicalModel<E extends Entity> extends HierarchicalModel<E> {
    private int color = -1;

    public void setColor(int pColor) {
        this.color = pColor;
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, int pColor) {
        super.renderToBuffer(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, FastColor.ARGB32.multiply(pColor, this.color));
    }
}