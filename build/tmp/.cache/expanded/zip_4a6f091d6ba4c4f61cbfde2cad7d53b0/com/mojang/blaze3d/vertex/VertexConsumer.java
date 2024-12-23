package com.mojang.blaze3d.vertex;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

@OnlyIn(Dist.CLIENT)
public interface VertexConsumer extends net.minecraftforge.client.extensions.IForgeVertexConsumer {
    VertexConsumer addVertex(float pX, float pY, float pZ);

    VertexConsumer setColor(int pRed, int pGreen, int pBlue, int pAlpha);

    VertexConsumer setUv(float pU, float pV);

    VertexConsumer setUv1(int pU, int pV);

    VertexConsumer setUv2(int pU, int pV);

    VertexConsumer setNormal(float pNormalX, float pNormalY, float pNormalZ);

    default void addVertex(
        float pX,
        float pY,
        float pZ,
        int pColor,
        float pU,
        float pV,
        int pPackedOverlay,
        int pPackedLight,
        float pNormalX,
        float pNormalY,
        float pNormalZ
    ) {
        this.addVertex(pX, pY, pZ);
        this.setColor(pColor);
        this.setUv(pU, pV);
        this.setOverlay(pPackedOverlay);
        this.setLight(pPackedLight);
        this.setNormal(pNormalX, pNormalY, pNormalZ);
    }

    default VertexConsumer setColor(float pRed, float pGreen, float pBlue, float pAlpha) {
        return this.setColor((int)(pRed * 255.0F), (int)(pGreen * 255.0F), (int)(pBlue * 255.0F), (int)(pAlpha * 255.0F));
    }

    default VertexConsumer setColor(int pColor) {
        return this.setColor(
            FastColor.ARGB32.red(pColor),
            FastColor.ARGB32.green(pColor),
            FastColor.ARGB32.blue(pColor),
            FastColor.ARGB32.alpha(pColor)
        );
    }

    default VertexConsumer setWhiteAlpha(int pAlpha) {
        return this.setColor(FastColor.ARGB32.color(pAlpha, -1));
    }

    default VertexConsumer setLight(int pPackedLight) {
        return this.setUv2(pPackedLight & 65535, pPackedLight >> 16 & 65535);
    }

    default VertexConsumer setOverlay(int pPackedOverlay) {
        return this.setUv1(pPackedOverlay & 65535, pPackedOverlay >> 16 & 65535);
    }

    default void putBulkData(
        PoseStack.Pose pPose, BakedQuad pQuad, float pRed, float pGreen, float pBlue, float pAlpha, int pPackedLight, int pPackedOverlay
    ) {
        this.putBulkData(
            pPose,
            pQuad,
            new float[]{1.0F, 1.0F, 1.0F, 1.0F},
            pRed,
            pGreen,
            pBlue,
            pAlpha,
            new int[]{pPackedLight, pPackedLight, pPackedLight, pPackedLight},
            pPackedOverlay,
            false
        );
    }

    default void putBulkData(
        PoseStack.Pose pPose,
        BakedQuad pQuad,
        float[] p_85998_,
        float pRed,
        float pGreen,
        float pBlue,
        float alpha,
        int[] p_86002_,
        int pPackedLight,
        boolean p_86004_
    ) {
        int[] aint = pQuad.getVertices();
        Vec3i vec3i = pQuad.getDirection().getNormal();
        Matrix4f matrix4f = pPose.pose();
        Vector3f vector3f = pPose.transformNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ(), new Vector3f());
        int i = 8;
        int j = aint.length / 8;
        int k = (int)(alpha * 255.0F);

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intbuffer = bytebuffer.asIntBuffer();

            for (int l = 0; l < j; l++) {
                intbuffer.clear();
                intbuffer.put(aint, l * 8, 8);
                float f = bytebuffer.getFloat(0);
                float f1 = bytebuffer.getFloat(4);
                float f2 = bytebuffer.getFloat(8);
                float f3;
                float f4;
                float f5;
                if (p_86004_) {
                    float f6 = (float)(bytebuffer.get(12) & 255);
                    float f7 = (float)(bytebuffer.get(13) & 255);
                    float f8 = (float)(bytebuffer.get(14) & 255);
                    f3 = f6 * p_85998_[l] * pRed;
                    f4 = f7 * p_85998_[l] * pGreen;
                    f5 = f8 * p_85998_[l] * pBlue;
                } else {
                    f3 = p_85998_[l] * pRed * 255.0F;
                    f4 = p_85998_[l] * pGreen * 255.0F;
                    f5 = p_85998_[l] * pBlue * 255.0F;
                }

                int i1 = FastColor.ARGB32.color(k, (int)f3, (int)f4, (int)f5);
                int j1 = applyBakedLighting(p_86002_[l], bytebuffer);
                float f10 = bytebuffer.getFloat(16);
                float f9 = bytebuffer.getFloat(20);
                Vector3f vector3f1 = matrix4f.transformPosition(f, f1, f2, new Vector3f());
                applyBakedNormals(vector3f, bytebuffer, pPose.normal());
                this.addVertex(vector3f1.x(), vector3f1.y(), vector3f1.z(), i1, f10, f9, pPackedLight, j1, vector3f.x(), vector3f.y(), vector3f.z());
            }
        }
    }

    default VertexConsumer addVertex(Vector3f pPos) {
        return this.addVertex(pPos.x(), pPos.y(), pPos.z());
    }

    default VertexConsumer addVertex(PoseStack.Pose pPose, Vector3f pPos) {
        return this.addVertex(pPose, pPos.x(), pPos.y(), pPos.z());
    }

    default VertexConsumer addVertex(PoseStack.Pose pPose, float pX, float pY, float pZ) {
        return this.addVertex(pPose.pose(), pX, pY, pZ);
    }

    default VertexConsumer addVertex(Matrix4f pPose, float pX, float pY, float pZ) {
        Vector3f vector3f = pPose.transformPosition(pX, pY, pZ, new Vector3f());
        return this.addVertex(vector3f.x(), vector3f.y(), vector3f.z());
    }

    default VertexConsumer setNormal(PoseStack.Pose pPose, float pNormalX, float pNormalY, float pNormalZ) {
        Vector3f vector3f = pPose.transformNormal(pNormalX, pNormalY, pNormalZ, new Vector3f());
        return this.setNormal(vector3f.x(), vector3f.y(), vector3f.z());
    }
}
