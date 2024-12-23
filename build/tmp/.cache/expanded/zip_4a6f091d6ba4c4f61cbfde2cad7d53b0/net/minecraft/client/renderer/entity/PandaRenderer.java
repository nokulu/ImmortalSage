package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.PandaHoldsItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Panda;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PandaRenderer extends MobRenderer<Panda, PandaModel<Panda>> {
    private static final Map<Panda.Gene, ResourceLocation> TEXTURES = Util.make(Maps.newEnumMap(Panda.Gene.class), p_340941_ -> {
        p_340941_.put(Panda.Gene.NORMAL, ResourceLocation.withDefaultNamespace("textures/entity/panda/panda.png"));
        p_340941_.put(Panda.Gene.LAZY, ResourceLocation.withDefaultNamespace("textures/entity/panda/lazy_panda.png"));
        p_340941_.put(Panda.Gene.WORRIED, ResourceLocation.withDefaultNamespace("textures/entity/panda/worried_panda.png"));
        p_340941_.put(Panda.Gene.PLAYFUL, ResourceLocation.withDefaultNamespace("textures/entity/panda/playful_panda.png"));
        p_340941_.put(Panda.Gene.BROWN, ResourceLocation.withDefaultNamespace("textures/entity/panda/brown_panda.png"));
        p_340941_.put(Panda.Gene.WEAK, ResourceLocation.withDefaultNamespace("textures/entity/panda/weak_panda.png"));
        p_340941_.put(Panda.Gene.AGGRESSIVE, ResourceLocation.withDefaultNamespace("textures/entity/panda/aggressive_panda.png"));
    });

    public PandaRenderer(EntityRendererProvider.Context p_174334_) {
        super(p_174334_, new PandaModel<>(p_174334_.bakeLayer(ModelLayers.PANDA)), 0.9F);
        this.addLayer(new PandaHoldsItemLayer(this, p_174334_.getItemInHandRenderer()));
    }

    public ResourceLocation getTextureLocation(Panda pEntity) {
        return TEXTURES.getOrDefault(pEntity.getVariant(), TEXTURES.get(Panda.Gene.NORMAL));
    }

    protected void setupRotations(Panda pEntity, PoseStack pPoseStack, float pBob, float pYBodyRot, float pPartialTick, float pScale) {
        super.setupRotations(pEntity, pPoseStack, pBob, pYBodyRot, pPartialTick, pScale);
        if (pEntity.rollCounter > 0) {
            int i = pEntity.rollCounter;
            int j = i + 1;
            float f = 7.0F;
            float f1 = pEntity.isBaby() ? 0.3F : 0.8F;
            if (i < 8) {
                float f3 = (float)(90 * i) / 7.0F;
                float f4 = (float)(90 * j) / 7.0F;
                float f2 = this.getAngle(f3, f4, j, pPartialTick, 8.0F);
                pPoseStack.translate(0.0F, (f1 + 0.2F) * (f2 / 90.0F), 0.0F);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(-f2));
            } else if (i < 16) {
                float f13 = ((float)i - 8.0F) / 7.0F;
                float f16 = 90.0F + 90.0F * f13;
                float f5 = 90.0F + 90.0F * ((float)j - 8.0F) / 7.0F;
                float f10 = this.getAngle(f16, f5, j, pPartialTick, 16.0F);
                pPoseStack.translate(0.0F, f1 + 0.2F + (f1 - 0.2F) * (f10 - 90.0F) / 90.0F, 0.0F);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(-f10));
            } else if ((float)i < 24.0F) {
                float f14 = ((float)i - 16.0F) / 7.0F;
                float f17 = 180.0F + 90.0F * f14;
                float f19 = 180.0F + 90.0F * ((float)j - 16.0F) / 7.0F;
                float f11 = this.getAngle(f17, f19, j, pPartialTick, 24.0F);
                pPoseStack.translate(0.0F, f1 + f1 * (270.0F - f11) / 90.0F, 0.0F);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(-f11));
            } else if (i < 32) {
                float f15 = ((float)i - 24.0F) / 7.0F;
                float f18 = 270.0F + 90.0F * f15;
                float f20 = 270.0F + 90.0F * ((float)j - 24.0F) / 7.0F;
                float f12 = this.getAngle(f18, f20, j, pPartialTick, 32.0F);
                pPoseStack.translate(0.0F, f1 * ((360.0F - f12) / 90.0F), 0.0F);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(-f12));
            }
        }

        float f6 = pEntity.getSitAmount(pPartialTick);
        if (f6 > 0.0F) {
            pPoseStack.translate(0.0F, 0.8F * f6, 0.0F);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(f6, pEntity.getXRot(), pEntity.getXRot() + 90.0F)));
            pPoseStack.translate(0.0F, -1.0F * f6, 0.0F);
            if (pEntity.isScared()) {
                float f7 = (float)(Math.cos((double)pEntity.tickCount * 1.25) * Math.PI * 0.05F);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(f7));
                if (pEntity.isBaby()) {
                    pPoseStack.translate(0.0F, 0.8F, 0.55F);
                }
            }
        }

        float f8 = pEntity.getLieOnBackAmount(pPartialTick);
        if (f8 > 0.0F) {
            float f9 = pEntity.isBaby() ? 0.5F : 1.3F;
            pPoseStack.translate(0.0F, f9 * f8, 0.0F);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(f8, pEntity.getXRot(), pEntity.getXRot() + 180.0F)));
        }
    }

    private float getAngle(float pCurrentAngle, float pNextAngle, int pNextRollCounter, float pPartialTick, float pRollEndCount) {
        return (float)pNextRollCounter < pRollEndCount ? Mth.lerp(pPartialTick, pCurrentAngle, pNextAngle) : pCurrentAngle;
    }
}