package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SalmonRenderer extends MobRenderer<Salmon, SalmonModel<Salmon>> {
    private static final ResourceLocation SALMON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/fish/salmon.png");

    public SalmonRenderer(EntityRendererProvider.Context p_174364_) {
        super(p_174364_, new SalmonModel<>(p_174364_.bakeLayer(ModelLayers.SALMON)), 0.4F);
    }

    public ResourceLocation getTextureLocation(Salmon pEntity) {
        return SALMON_LOCATION;
    }

    protected void setupRotations(Salmon pEntity, PoseStack pPoseStack, float pBob, float pYBodyRot, float pPartialTick, float pScale) {
        super.setupRotations(pEntity, pPoseStack, pBob, pYBodyRot, pPartialTick, pScale);
        float f = 1.0F;
        float f1 = 1.0F;
        if (!pEntity.isInWater()) {
            f = 1.3F;
            f1 = 1.7F;
        }

        float f2 = f * 4.3F * Mth.sin(f1 * 0.6F * pBob);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(f2));
        pPoseStack.translate(0.0F, 0.0F, -0.4F);
        if (!pEntity.isInWater()) {
            pPoseStack.translate(0.2F, 0.1F, 0.0F);
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }
    }
}