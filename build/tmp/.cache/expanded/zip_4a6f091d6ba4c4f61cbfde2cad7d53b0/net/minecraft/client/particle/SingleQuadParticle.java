package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public abstract class SingleQuadParticle extends Particle {
    protected float quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;

    protected SingleQuadParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ);
    }

    protected SingleQuadParticle(
        ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed
    ) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
    }

    public SingleQuadParticle.FacingCameraMode getFacingCameraMode() {
        return SingleQuadParticle.FacingCameraMode.LOOKAT_XYZ;
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        Quaternionf quaternionf = new Quaternionf();
        this.getFacingCameraMode().setRotation(quaternionf, pRenderInfo, pPartialTicks);
        if (this.roll != 0.0F) {
            quaternionf.rotateZ(Mth.lerp(pPartialTicks, this.oRoll, this.roll));
        }

        this.renderRotatedQuad(pBuffer, pRenderInfo, quaternionf, pPartialTicks);
    }

    protected void renderRotatedQuad(VertexConsumer pBuffer, Camera pCamera, Quaternionf pQuaternion, float pPartialTicks) {
        Vec3 vec3 = pCamera.getPosition();
        float f = (float)(Mth.lerp((double)pPartialTicks, this.xo, this.x) - vec3.x());
        float f1 = (float)(Mth.lerp((double)pPartialTicks, this.yo, this.y) - vec3.y());
        float f2 = (float)(Mth.lerp((double)pPartialTicks, this.zo, this.z) - vec3.z());
        this.renderRotatedQuad(pBuffer, pQuaternion, f, f1, f2, pPartialTicks);
    }

    protected void renderRotatedQuad(VertexConsumer pBuffer, Quaternionf pQuaternion, float pX, float pY, float pZ, float pPartialTicks) {
        float f = this.getQuadSize(pPartialTicks);
        float f1 = this.getU0();
        float f2 = this.getU1();
        float f3 = this.getV0();
        float f4 = this.getV1();
        int i = this.getLightColor(pPartialTicks);
        this.renderVertex(pBuffer, pQuaternion, pX, pY, pZ, 1.0F, -1.0F, f, f2, f4, i);
        this.renderVertex(pBuffer, pQuaternion, pX, pY, pZ, 1.0F, 1.0F, f, f2, f3, i);
        this.renderVertex(pBuffer, pQuaternion, pX, pY, pZ, -1.0F, 1.0F, f, f1, f3, i);
        this.renderVertex(pBuffer, pQuaternion, pX, pY, pZ, -1.0F, -1.0F, f, f1, f4, i);
    }

    private void renderVertex(
        VertexConsumer pBuffer,
        Quaternionf pQuaternion,
        float pX,
        float pY,
        float pZ,
        float pXOffset,
        float pYOffset,
        float pQuadSize,
        float pU,
        float pV,
        int pPackedLight
    ) {
        Vector3f vector3f = new Vector3f(pXOffset, pYOffset, 0.0F).rotate(pQuaternion).mul(pQuadSize).add(pX, pY, pZ);
        pBuffer.addVertex(vector3f.x(), vector3f.y(), vector3f.z())
            .setUv(pU, pV)
            .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
            .setLight(pPackedLight);
    }

    public float getQuadSize(float pScaleFactor) {
        return this.quadSize;
    }

    @Override
    public Particle scale(float pScale) {
        this.quadSize *= pScale;
        return super.scale(pScale);
    }

    protected abstract float getU0();

    protected abstract float getU1();

    protected abstract float getV0();

    protected abstract float getV1();

    @OnlyIn(Dist.CLIENT)
    public interface FacingCameraMode {
        SingleQuadParticle.FacingCameraMode LOOKAT_XYZ = (p_312026_, p_311956_, p_310043_) -> p_312026_.set(p_311956_.rotation());
        SingleQuadParticle.FacingCameraMode LOOKAT_Y = (p_310770_, p_309904_, p_311153_) -> p_310770_.set(
                0.0F, p_309904_.rotation().y, 0.0F, p_309904_.rotation().w
            );

        void setRotation(Quaternionf pQuaternion, Camera pCamera, float pPartialTick);
    }
}