package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public abstract class EntityRenderer<T extends Entity> {
    protected static final float NAMETAG_SCALE = 0.025F;
    public static final int LEASH_RENDER_STEPS = 24;
    protected final EntityRenderDispatcher entityRenderDispatcher;
    private final Font font;
    protected float shadowRadius;
    protected float shadowStrength = 1.0F;

    protected EntityRenderer(EntityRendererProvider.Context pContext) {
        this.entityRenderDispatcher = pContext.getEntityRenderDispatcher();
        this.font = pContext.getFont();
    }

    public final int getPackedLightCoords(T pEntity, float pPartialTicks) {
        BlockPos blockpos = BlockPos.containing(pEntity.getLightProbePosition(pPartialTicks));
        return LightTexture.pack(this.getBlockLightLevel(pEntity, blockpos), this.getSkyLightLevel(pEntity, blockpos));
    }

    protected int getSkyLightLevel(T pEntity, BlockPos pPos) {
        return pEntity.level().getBrightness(LightLayer.SKY, pPos);
    }

    protected int getBlockLightLevel(T pEntity, BlockPos pPos) {
        return pEntity.isOnFire() ? 15 : pEntity.level().getBrightness(LightLayer.BLOCK, pPos);
    }

    public boolean shouldRender(T pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        if (!pLivingEntity.shouldRender(pCamX, pCamY, pCamZ)) {
            return false;
        } else if (pLivingEntity.noCulling) {
            return true;
        } else {
            AABB aabb = pLivingEntity.getBoundingBoxForCulling().inflate(0.5);
            if (aabb.hasNaN() || aabb.getSize() == 0.0) {
                aabb = new AABB(
                    pLivingEntity.getX() - 2.0,
                    pLivingEntity.getY() - 2.0,
                    pLivingEntity.getZ() - 2.0,
                    pLivingEntity.getX() + 2.0,
                    pLivingEntity.getY() + 2.0,
                    pLivingEntity.getZ() + 2.0
                );
            }

            if (pCamera.isVisible(aabb)) {
                return true;
            } else {
                if (pLivingEntity instanceof Leashable leashable) {
                    Entity entity = leashable.getLeashHolder();
                    if (entity != null) {
                        return pCamera.isVisible(entity.getBoundingBoxForCulling());
                    }
                }

                return false;
            }
        }
    }

    public Vec3 getRenderOffset(T pEntity, float pPartialTicks) {
        return Vec3.ZERO;
    }

    public void render(T pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight) {
        if (pEntity instanceof Leashable leashable) {
            Entity entity = leashable.getLeashHolder();
            if (entity != null) {
                this.renderLeash(pEntity, pPartialTick, pPoseStack, pBufferSource, entity);
            }
        }

        var event = net.minecraftforge.client.event.ForgeEventFactoryClient.fireRenderNameTagEvent(pEntity, pEntity.getDisplayName(), this, pPoseStack, pBufferSource, pPackedLight, pPartialTick);
        if (!event.getResult().isDenied() && (event.getResult().isAllowed() || this.shouldShowName(pEntity))) {
           this.renderNameTag(pEntity, event.getContent(), pPoseStack, pBufferSource, pPackedLight, pPartialTick);
        }
    }

    private <E extends Entity> void renderLeash(T pEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, E pLeashHolder) {
        pPoseStack.pushPose();
        Vec3 vec3 = pLeashHolder.getRopeHoldPosition(pPartialTick);
        double d0 = (double)(pEntity.getPreciseBodyRotation(pPartialTick) * (float) (Math.PI / 180.0)) + (Math.PI / 2);
        Vec3 vec31 = pEntity.getLeashOffset(pPartialTick);
        double d1 = Math.cos(d0) * vec31.z + Math.sin(d0) * vec31.x;
        double d2 = Math.sin(d0) * vec31.z - Math.cos(d0) * vec31.x;
        double d3 = Mth.lerp((double)pPartialTick, pEntity.xo, pEntity.getX()) + d1;
        double d4 = Mth.lerp((double)pPartialTick, pEntity.yo, pEntity.getY()) + vec31.y;
        double d5 = Mth.lerp((double)pPartialTick, pEntity.zo, pEntity.getZ()) + d2;
        pPoseStack.translate(d1, vec31.y, d2);
        float f = (float)(vec3.x - d3);
        float f1 = (float)(vec3.y - d4);
        float f2 = (float)(vec3.z - d5);
        float f3 = 0.025F;
        VertexConsumer vertexconsumer = pBufferSource.getBuffer(RenderType.leash());
        Matrix4f matrix4f = pPoseStack.last().pose();
        float f4 = Mth.invSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;
        BlockPos blockpos = BlockPos.containing(pEntity.getEyePosition(pPartialTick));
        BlockPos blockpos1 = BlockPos.containing(pLeashHolder.getEyePosition(pPartialTick));
        int i = this.getBlockLightLevel(pEntity, blockpos);
        int j = this.entityRenderDispatcher.getRenderer(pLeashHolder).getBlockLightLevel(pLeashHolder, blockpos1);
        int k = pEntity.level().getBrightness(LightLayer.SKY, blockpos);
        int l = pEntity.level().getBrightness(LightLayer.SKY, blockpos1);

        for (int i1 = 0; i1 <= 24; i1++) {
            addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6, i1, false);
        }

        for (int j1 = 24; j1 >= 0; j1--) {
            addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6, j1, true);
        }

        pPoseStack.popPose();
    }

    private static void addVertexPair(
        VertexConsumer pBuffer,
        Matrix4f pPose,
        float pStartX,
        float pStartY,
        float pStartZ,
        int pEntityBlockLight,
        int pHolderBlockLight,
        int pEntitySkyLight,
        int pHolderSkyLight,
        float pYOffset,
        float pDy,
        float pDx,
        float pDz,
        int pIndex,
        boolean pReverse
    ) {
        float f = (float)pIndex / 24.0F;
        int i = (int)Mth.lerp(f, (float)pEntityBlockLight, (float)pHolderBlockLight);
        int j = (int)Mth.lerp(f, (float)pEntitySkyLight, (float)pHolderSkyLight);
        int k = LightTexture.pack(i, j);
        float f1 = pIndex % 2 == (pReverse ? 1 : 0) ? 0.7F : 1.0F;
        float f2 = 0.5F * f1;
        float f3 = 0.4F * f1;
        float f4 = 0.3F * f1;
        float f5 = pStartX * f;
        float f6 = pStartY > 0.0F ? pStartY * f * f : pStartY - pStartY * (1.0F - f) * (1.0F - f);
        float f7 = pStartZ * f;
        pBuffer.addVertex(pPose, f5 - pDx, f6 + pDy, f7 + pDz).setColor(f2, f3, f4, 1.0F).setLight(k);
        pBuffer.addVertex(pPose, f5 + pDx, f6 + pYOffset - pDy, f7 - pDz).setColor(f2, f3, f4, 1.0F).setLight(k);
    }

    protected boolean shouldShowName(T pEntity) {
        return pEntity.shouldShowName() || pEntity.hasCustomName() && pEntity == this.entityRenderDispatcher.crosshairPickEntity;
    }

    public abstract ResourceLocation getTextureLocation(T pEntity);

    public Font getFont() {
        return this.font;
    }

    protected void renderNameTag(T pEntity, Component pDisplayName, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, float pPartialTick) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
        if (net.minecraftforge.client.ForgeHooksClient.isNameplateInRenderDistance(pEntity, d0)) {
            Vec3 vec3 = pEntity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, pEntity.getViewYRot(pPartialTick));
            if (vec3 != null) {
                boolean flag = !pEntity.isDiscrete();
                int i = "deadmau5".equals(pDisplayName.getString()) ? -10 : 0;
                pPoseStack.pushPose();
                pPoseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);
                pPoseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
                pPoseStack.scale(0.025F, -0.025F, 0.025F);
                Matrix4f matrix4f = pPoseStack.last().pose();
                float f = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                int j = (int)(f * 255.0F) << 24;
                Font font = this.getFont();
                float f1 = (float)(-font.width(pDisplayName) / 2);
                font.drawInBatch(
                    pDisplayName, f1, (float)i, 553648127, false, matrix4f, pBufferSource, flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, pPackedLight
                );
                if (flag) {
                    font.drawInBatch(pDisplayName, f1, (float)i, -1, false, matrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, pPackedLight);
                }

                pPoseStack.popPose();
            }
        }
    }

    protected float getShadowRadius(T pEntity) {
        return this.shadowRadius;
    }
}
