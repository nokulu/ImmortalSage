package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AgeableListModel<E extends Entity> extends EntityModel<E> {
    private final boolean scaleHead;
    private final float babyYHeadOffset;
    private final float babyZHeadOffset;
    private final float babyHeadScale;
    private final float babyBodyScale;
    private final float bodyYOffset;

    protected AgeableListModel(boolean pScaleHead, float pBabyYHeadOffset, float pBabyZHeadOffset) {
        this(pScaleHead, pBabyYHeadOffset, pBabyZHeadOffset, 2.0F, 2.0F, 24.0F);
    }

    protected AgeableListModel(boolean pScaleHead, float pBabyYHeadOffset, float pBabyZHeadOffset, float pBabyHeadScale, float pBabyBodyScale, float pBodyYOffset) {
        this(RenderType::entityCutoutNoCull, pScaleHead, pBabyYHeadOffset, pBabyZHeadOffset, pBabyHeadScale, pBabyBodyScale, pBodyYOffset);
    }

    protected AgeableListModel(
        Function<ResourceLocation, RenderType> pRenderType,
        boolean pScaleHead,
        float pBabyYHeadOffset,
        float pBabyZHeadOffset,
        float pBabyHeadScale,
        float pBabyBodyScale,
        float pBodyYOffset
    ) {
        super(pRenderType);
        this.scaleHead = pScaleHead;
        this.babyYHeadOffset = pBabyYHeadOffset;
        this.babyZHeadOffset = pBabyZHeadOffset;
        this.babyHeadScale = pBabyHeadScale;
        this.babyBodyScale = pBabyBodyScale;
        this.bodyYOffset = pBodyYOffset;
    }

    protected AgeableListModel() {
        this(false, 5.0F, 2.0F);
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, int pColor) {
        if (this.young) {
            pPoseStack.pushPose();
            if (this.scaleHead) {
                float f = 1.5F / this.babyHeadScale;
                pPoseStack.scale(f, f, f);
            }

            pPoseStack.translate(0.0F, this.babyYHeadOffset / 16.0F, this.babyZHeadOffset / 16.0F);
            this.headParts().forEach(p_340834_ -> p_340834_.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pColor));
            pPoseStack.popPose();
            pPoseStack.pushPose();
            float f1 = 1.0F / this.babyBodyScale;
            pPoseStack.scale(f1, f1, f1);
            pPoseStack.translate(0.0F, this.bodyYOffset / 16.0F, 0.0F);
            this.bodyParts().forEach(p_340840_ -> p_340840_.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pColor));
            pPoseStack.popPose();
        } else {
            this.headParts().forEach(p_340846_ -> p_340846_.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pColor));
            this.bodyParts().forEach(p_340852_ -> p_340852_.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pColor));
        }
    }

    protected abstract Iterable<ModelPart> headParts();

    protected abstract Iterable<ModelPart> bodyParts();
}