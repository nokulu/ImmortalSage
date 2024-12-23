package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import java.util.Optional;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OutlineBufferSource implements MultiBufferSource {
    private final MultiBufferSource.BufferSource bufferSource;
    private final MultiBufferSource.BufferSource outlineBufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(1536));
    private int teamR = 255;
    private int teamG = 255;
    private int teamB = 255;
    private int teamA = 255;

    public OutlineBufferSource(MultiBufferSource.BufferSource pBufferSource) {
        this.bufferSource = pBufferSource;
    }

    @Override
    public VertexConsumer getBuffer(RenderType pRenderType) {
        if (pRenderType.isOutline()) {
            VertexConsumer vertexconsumer2 = this.outlineBufferSource.getBuffer(pRenderType);
            return new OutlineBufferSource.EntityOutlineGenerator(vertexconsumer2, this.teamR, this.teamG, this.teamB, this.teamA);
        } else {
            VertexConsumer vertexconsumer = this.bufferSource.getBuffer(pRenderType);
            Optional<RenderType> optional = pRenderType.outline();
            if (optional.isPresent()) {
                VertexConsumer vertexconsumer1 = this.outlineBufferSource.getBuffer(optional.get());
                OutlineBufferSource.EntityOutlineGenerator outlinebuffersource$entityoutlinegenerator = new OutlineBufferSource.EntityOutlineGenerator(
                    vertexconsumer1, this.teamR, this.teamG, this.teamB, this.teamA
                );
                return VertexMultiConsumer.create(outlinebuffersource$entityoutlinegenerator, vertexconsumer);
            } else {
                return vertexconsumer;
            }
        }
    }

    public void setColor(int pRed, int pGreen, int pBlue, int pAlpha) {
        this.teamR = pRed;
        this.teamG = pGreen;
        this.teamB = pBlue;
        this.teamA = pAlpha;
    }

    public void endOutlineBatch() {
        this.outlineBufferSource.endBatch();
    }

    @OnlyIn(Dist.CLIENT)
    static record EntityOutlineGenerator(VertexConsumer delegate, int color) implements VertexConsumer {
        public EntityOutlineGenerator(VertexConsumer pDelegate, int pDefaultR, int pDefaultG, int pDefaultB, int pDefaultA) {
            this(pDelegate, FastColor.ARGB32.color(pDefaultA, pDefaultR, pDefaultG, pDefaultB));
        }

        @Override
        public VertexConsumer addVertex(float pX, float pY, float pZ) {
            this.delegate.addVertex(pX, pY, pZ).setColor(this.color);
            return this;
        }

        @Override
        public VertexConsumer setColor(int pRed, int pGreen, int pBlue, int pAlpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float pU, float pV) {
            this.delegate.setUv(pU, pV);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int pU, int pV) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int pU, int pV) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float pNormalX, float pNormalY, float pNormalZ) {
            return this;
        }
    }
}