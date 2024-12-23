package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.SquidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SquidRenderer<T extends Squid> extends MobRenderer<T, SquidModel<T>> {
    private static final ResourceLocation SQUID_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/squid/squid.png");

    public SquidRenderer(EntityRendererProvider.Context pContext, SquidModel<T> pModel) {
        super(pContext, pModel, 0.7F);
    }

    public ResourceLocation getTextureLocation(T pEntity) {
        return SQUID_LOCATION;
    }

    protected void setupRotations(T pEntity, PoseStack pPoseStack, float pBob, float pYBodyRot, float pPartialTick, float pScale) {
        float f = Mth.lerp(pPartialTick, pEntity.xBodyRotO, pEntity.xBodyRot);
        float f1 = Mth.lerp(pPartialTick, pEntity.zBodyRotO, pEntity.zBodyRot);
        pPoseStack.translate(0.0F, 0.5F, 0.0F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F - pYBodyRot));
        pPoseStack.mulPose(Axis.XP.rotationDegrees(f));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(f1));
        pPoseStack.translate(0.0F, -1.2F, 0.0F);
    }

    protected float getBob(T pLivingBase, float pPartialTicks) {
        return Mth.lerp(pPartialTicks, pLivingBase.oldTentacleAngle, pLivingBase.tentacleAngle);
    }
}