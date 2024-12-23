package com.mojang.blaze3d.vertex;

import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class SheetedDecalTextureGenerator implements VertexConsumer {
    private final VertexConsumer delegate;
    private final Matrix4f cameraInversePose;
    private final Matrix3f normalInversePose;
    private final float textureScale;
    private final Vector3f worldPos = new Vector3f();
    private final Vector3f normal = new Vector3f();
    private float x;
    private float y;
    private float z;

    public SheetedDecalTextureGenerator(VertexConsumer pDelegate, PoseStack.Pose pPose, float pTextureScale) {
        this.delegate = pDelegate;
        this.cameraInversePose = new Matrix4f(pPose.pose()).invert();
        this.normalInversePose = new Matrix3f(pPose.normal()).invert();
        this.textureScale = pTextureScale;
    }

    @Override
    public VertexConsumer addVertex(float pX, float pY, float pZ) {
        this.x = pX;
        this.y = pY;
        this.z = pZ;
        this.delegate.addVertex(pX, pY, pZ);
        return this;
    }

    @Override
    public VertexConsumer setColor(int pRed, int pGreen, int pBlue, int pAlpha) {
        this.delegate.setColor(-1);
        return this;
    }

    @Override
    public VertexConsumer setUv(float pU, float pV) {
        return this;
    }

    @Override
    public VertexConsumer setUv1(int pU, int pV) {
        this.delegate.setUv1(pU, pV);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int pU, int pV) {
        this.delegate.setUv2(pU, pV);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float pNormalX, float pNormalY, float pNormalZ) {
        this.delegate.setNormal(pNormalX, pNormalY, pNormalZ);
        Vector3f vector3f = this.normalInversePose.transform(pNormalX, pNormalY, pNormalZ, this.normal);
        Direction direction = net.minecraftforge.client.ForgeHooksClient.getNearestStable(vector3f.x(), vector3f.y(), vector3f.z());
        Vector3f vector3f1 = this.cameraInversePose.transformPosition(this.x, this.y, this.z, this.worldPos);
        vector3f1.rotateY((float) Math.PI);
        vector3f1.rotateX((float) (-Math.PI / 2));
        vector3f1.rotate(direction.getRotation());
        this.delegate.setUv(-vector3f1.x() * this.textureScale, -vector3f1.y() * this.textureScale);
        return this;
    }
}
