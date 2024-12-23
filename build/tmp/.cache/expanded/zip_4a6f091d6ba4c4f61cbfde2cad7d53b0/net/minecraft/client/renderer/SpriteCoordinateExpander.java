package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpriteCoordinateExpander implements VertexConsumer {
    private final VertexConsumer delegate;
    private final TextureAtlasSprite sprite;

    public SpriteCoordinateExpander(VertexConsumer pDelegate, TextureAtlasSprite pSprite) {
        this.delegate = pDelegate;
        this.sprite = pSprite;
    }

    @Override
    public VertexConsumer addVertex(float pX, float pY, float pZ) {
        this.delegate.addVertex(pX, pY, pZ);
        return this; // Forge: Fix MC-263524 not working with chained methods
    }

    @Override
    public VertexConsumer setColor(int pRed, int pGreen, int pBlue, int pAlpha) {
        this.delegate.setColor(pRed, pGreen, pBlue, pAlpha);
        return this; // Forge: Fix MC-263524 not working with chained methods
    }

    @Override
    public VertexConsumer setUv(float pU, float pV) {
        this.delegate.setUv(this.sprite.getU(pU), this.sprite.getV(pV));
        return this; // Forge: Fix MC-263524 not working with chained methods
    }

    @Override
    public VertexConsumer setUv1(int pU, int pV) {
        this.delegate.setUv1(pU, pV);
        return this; // Forge: Fix MC-263524 not working with chained methods
    }

    @Override
    public VertexConsumer setUv2(int pU, int pV) {
        this.delegate.setUv2(pU, pV);
        return this; // Forge: Fix MC-263524 not working with chained methods
    }

    @Override
    public VertexConsumer setNormal(float pNormalX, float pNormalY, float pNormalZ) {
        this.delegate.setNormal(pNormalX, pNormalY, pNormalZ);
        return this; // Forge: Fix MC-263524 not working with chained methods
    }

    @Override
    public void addVertex(
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
        this.delegate
            .addVertex(
                pX,
                pY,
                pZ,
                pColor,
                this.sprite.getU(pU),
                this.sprite.getV(pV),
                pPackedOverlay,
                pPackedLight,
                pNormalX,
                pNormalY,
                pNormalZ
            );
    }
}
