package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.StriderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Strider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StriderRenderer extends MobRenderer<Strider, StriderModel<Strider>> {
    private static final ResourceLocation STRIDER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/strider/strider.png");
    private static final ResourceLocation COLD_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/strider/strider_cold.png");
    private static final float SHADOW_RADIUS = 0.5F;

    public StriderRenderer(EntityRendererProvider.Context p_174411_) {
        super(p_174411_, new StriderModel<>(p_174411_.bakeLayer(ModelLayers.STRIDER)), 0.5F);
        this.addLayer(
            new SaddleLayer<>(
                this, new StriderModel<>(p_174411_.bakeLayer(ModelLayers.STRIDER_SADDLE)), ResourceLocation.withDefaultNamespace("textures/entity/strider/strider_saddle.png")
            )
        );
    }

    public ResourceLocation getTextureLocation(Strider pEntity) {
        return pEntity.isSuffocating() ? COLD_LOCATION : STRIDER_LOCATION;
    }

    protected float getShadowRadius(Strider pEntity) {
        float f = super.getShadowRadius(pEntity);
        return pEntity.isBaby() ? f * 0.5F : f;
    }

    protected void scale(Strider pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
        float f = pLivingEntity.getAgeScale();
        pPoseStack.scale(f, f, f);
    }

    protected boolean isShaking(Strider pEntity) {
        return super.isShaking(pEntity) || pEntity.isSuffocating();
    }
}