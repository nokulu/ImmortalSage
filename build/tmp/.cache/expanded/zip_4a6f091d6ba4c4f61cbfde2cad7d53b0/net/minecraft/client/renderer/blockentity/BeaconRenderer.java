package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeaconRenderer implements BlockEntityRenderer<BeaconBlockEntity> {
    public static final ResourceLocation BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");
    public static final int MAX_RENDER_Y = 1024;

    public BeaconRenderer(BlockEntityRendererProvider.Context pContext) {
    }

    public void render(BeaconBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        long i = pBlockEntity.getLevel().getGameTime();
        List<BeaconBlockEntity.BeaconBeamSection> list = pBlockEntity.getBeamSections();
        int j = 0;

        for (int k = 0; k < list.size(); k++) {
            BeaconBlockEntity.BeaconBeamSection beaconblockentity$beaconbeamsection = list.get(k);
            renderBeaconBeam(
                pPoseStack,
                pBufferSource,
                pPartialTick,
                i,
                j,
                k == list.size() - 1 ? 1024 : beaconblockentity$beaconbeamsection.getHeight(),
                beaconblockentity$beaconbeamsection.getColor()
            );
            j += beaconblockentity$beaconbeamsection.getHeight();
        }
    }

    private static void renderBeaconBeam(
        PoseStack pPoseStack, MultiBufferSource pBufferSource, float pPartialTick, long pGameTime, int pYOffset, int pHeight, int pColor
    ) {
        renderBeaconBeam(pPoseStack, pBufferSource, BEAM_LOCATION, pPartialTick, 1.0F, pGameTime, pYOffset, pHeight, pColor, 0.2F, 0.25F);
    }

    public static void renderBeaconBeam(
        PoseStack pPoseStack,
        MultiBufferSource pBufferSource,
        ResourceLocation pBeamLocation,
        float pPartialTick,
        float pTextureScale,
        long pGameTime,
        int pYOffset,
        int pHeight,
        int pColor,
        float pBeamRadius,
        float pGlowRadius
    ) {
        int i = pYOffset + pHeight;
        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.0, 0.5);
        float f = (float)Math.floorMod(pGameTime, 40) + pPartialTick;
        float f1 = pHeight < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float)Mth.floor(f1 * 0.1F));
        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.YP.rotationDegrees(f * 2.25F - 45.0F));
        float f3 = 0.0F;
        float f5 = 0.0F;
        float f6 = -pBeamRadius;
        float f7 = 0.0F;
        float f8 = 0.0F;
        float f9 = -pBeamRadius;
        float f10 = 0.0F;
        float f11 = 1.0F;
        float f12 = -1.0F + f2;
        float f13 = (float)pHeight * pTextureScale * (0.5F / pBeamRadius) + f12;
        renderPart(
            pPoseStack,
            pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, false)),
            pColor,
            pYOffset,
            i,
            0.0F,
            pBeamRadius,
            pBeamRadius,
            0.0F,
            f6,
            0.0F,
            0.0F,
            f9,
            0.0F,
            1.0F,
            f13,
            f12
        );
        pPoseStack.popPose();
        f3 = -pGlowRadius;
        float f4 = -pGlowRadius;
        f5 = -pGlowRadius;
        f6 = -pGlowRadius;
        f10 = 0.0F;
        f11 = 1.0F;
        f12 = -1.0F + f2;
        f13 = (float)pHeight * pTextureScale + f12;
        renderPart(
            pPoseStack,
            pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, true)),
            FastColor.ARGB32.color(32, pColor),
            pYOffset,
            i,
            f3,
            f4,
            pGlowRadius,
            f5,
            f6,
            pGlowRadius,
            pGlowRadius,
            pGlowRadius,
            0.0F,
            1.0F,
            f13,
            f12
        );
        pPoseStack.popPose();
    }

    private static void renderPart(
        PoseStack pPoseStack,
        VertexConsumer pConsumer,
        int pColor,
        int pMinY,
        int pMaxY,
        float pX1,
        float pZ1,
        float pX2,
        float pZ2,
        float pX3,
        float pZ3,
        float pX4,
        float pZ4,
        float pMinU,
        float pMaxU,
        float pMinV,
        float pMaxV
    ) {
        PoseStack.Pose posestack$pose = pPoseStack.last();
        renderQuad(
            posestack$pose, pConsumer, pColor, pMinY, pMaxY, pX1, pZ1, pX2, pZ2, pMinU, pMaxU, pMinV, pMaxV
        );
        renderQuad(
            posestack$pose, pConsumer, pColor, pMinY, pMaxY, pX4, pZ4, pX3, pZ3, pMinU, pMaxU, pMinV, pMaxV
        );
        renderQuad(
            posestack$pose, pConsumer, pColor, pMinY, pMaxY, pX2, pZ2, pX4, pZ4, pMinU, pMaxU, pMinV, pMaxV
        );
        renderQuad(
            posestack$pose, pConsumer, pColor, pMinY, pMaxY, pX3, pZ3, pX1, pZ1, pMinU, pMaxU, pMinV, pMaxV
        );
    }

    private static void renderQuad(
        PoseStack.Pose pPose,
        VertexConsumer pConsumer,
        int pColor,
        int pMinY,
        int pMaxY,
        float pMinX,
        float pMinZ,
        float pMaxX,
        float pMaxZ,
        float pMinU,
        float pMaxU,
        float pMinV,
        float pMaxV
    ) {
        addVertex(pPose, pConsumer, pColor, pMaxY, pMinX, pMinZ, pMaxU, pMinV);
        addVertex(pPose, pConsumer, pColor, pMinY, pMinX, pMinZ, pMaxU, pMaxV);
        addVertex(pPose, pConsumer, pColor, pMinY, pMaxX, pMaxZ, pMinU, pMaxV);
        addVertex(pPose, pConsumer, pColor, pMaxY, pMaxX, pMaxZ, pMinU, pMinV);
    }

    private static void addVertex(
        PoseStack.Pose pPose, VertexConsumer pConsumer, int pColor, int pY, float pX, float pZ, float pU, float pV
    ) {
        pConsumer.addVertex(pPose, pX, (float)pY, pZ)
            .setColor(pColor)
            .setUv(pU, pV)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(15728880)
            .setNormal(pPose, 0.0F, 1.0F, 0.0F);
    }

    public boolean shouldRenderOffScreen(BeaconBlockEntity pBlockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    public boolean shouldRender(BeaconBlockEntity pBlockEntity, Vec3 pCameraPos) {
        return Vec3.atCenterOf(pBlockEntity.getBlockPos()).multiply(1.0, 0.0, 1.0).closerThan(pCameraPos.multiply(1.0, 0.0, 1.0), (double)this.getViewDistance());
    }
}