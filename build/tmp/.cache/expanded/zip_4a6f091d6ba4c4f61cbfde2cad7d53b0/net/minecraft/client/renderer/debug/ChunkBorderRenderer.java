package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class ChunkBorderRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int CELL_BORDER = FastColor.ARGB32.color(255, 0, 155, 155);
    private static final int YELLOW = FastColor.ARGB32.color(255, 255, 255, 0);

    public ChunkBorderRenderer(Minecraft pMinecraft) {
        this.minecraft = pMinecraft;
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBufferSource, double pCamX, double pCamY, double pCamZ) {
        Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
        float f = (float)((double)this.minecraft.level.getMinBuildHeight() - pCamY);
        float f1 = (float)((double)this.minecraft.level.getMaxBuildHeight() - pCamY);
        ChunkPos chunkpos = entity.chunkPosition();
        float f2 = (float)((double)chunkpos.getMinBlockX() - pCamX);
        float f3 = (float)((double)chunkpos.getMinBlockZ() - pCamZ);
        VertexConsumer vertexconsumer = pBufferSource.getBuffer(RenderType.debugLineStrip(1.0));
        Matrix4f matrix4f = pPoseStack.last().pose();

        for (int i = -16; i <= 32; i += 16) {
            for (int j = -16; j <= 32; j += 16) {
                vertexconsumer.addVertex(matrix4f, f2 + (float)i, f, f3 + (float)j).setColor(1.0F, 0.0F, 0.0F, 0.0F);
                vertexconsumer.addVertex(matrix4f, f2 + (float)i, f, f3 + (float)j).setColor(1.0F, 0.0F, 0.0F, 0.5F);
                vertexconsumer.addVertex(matrix4f, f2 + (float)i, f1, f3 + (float)j).setColor(1.0F, 0.0F, 0.0F, 0.5F);
                vertexconsumer.addVertex(matrix4f, f2 + (float)i, f1, f3 + (float)j).setColor(1.0F, 0.0F, 0.0F, 0.0F);
            }
        }

        for (int l = 2; l < 16; l += 2) {
            int i2 = l % 4 == 0 ? CELL_BORDER : YELLOW;
            vertexconsumer.addVertex(matrix4f, f2 + (float)l, f, f3).setColor(1.0F, 1.0F, 0.0F, 0.0F);
            vertexconsumer.addVertex(matrix4f, f2 + (float)l, f, f3).setColor(i2);
            vertexconsumer.addVertex(matrix4f, f2 + (float)l, f1, f3).setColor(i2);
            vertexconsumer.addVertex(matrix4f, f2 + (float)l, f1, f3).setColor(1.0F, 1.0F, 0.0F, 0.0F);
            vertexconsumer.addVertex(matrix4f, f2 + (float)l, f, f3 + 16.0F).setColor(1.0F, 1.0F, 0.0F, 0.0F);
            vertexconsumer.addVertex(matrix4f, f2 + (float)l, f, f3 + 16.0F).setColor(i2);
            vertexconsumer.addVertex(matrix4f, f2 + (float)l, f1, f3 + 16.0F).setColor(i2);
            vertexconsumer.addVertex(matrix4f, f2 + (float)l, f1, f3 + 16.0F).setColor(1.0F, 1.0F, 0.0F, 0.0F);
        }

        for (int i1 = 2; i1 < 16; i1 += 2) {
            int j2 = i1 % 4 == 0 ? CELL_BORDER : YELLOW;
            vertexconsumer.addVertex(matrix4f, f2, f, f3 + (float)i1).setColor(1.0F, 1.0F, 0.0F, 0.0F);
            vertexconsumer.addVertex(matrix4f, f2, f, f3 + (float)i1).setColor(j2);
            vertexconsumer.addVertex(matrix4f, f2, f1, f3 + (float)i1).setColor(j2);
            vertexconsumer.addVertex(matrix4f, f2, f1, f3 + (float)i1).setColor(1.0F, 1.0F, 0.0F, 0.0F);
            vertexconsumer.addVertex(matrix4f, f2 + 16.0F, f, f3 + (float)i1).setColor(1.0F, 1.0F, 0.0F, 0.0F);
            vertexconsumer.addVertex(matrix4f, f2 + 16.0F, f, f3 + (float)i1).setColor(j2);
            vertexconsumer.addVertex(matrix4f, f2 + 16.0F, f1, f3 + (float)i1).setColor(j2);
            vertexconsumer.addVertex(matrix4f, f2 + 16.0F, f1, f3 + (float)i1).setColor(1.0F, 1.0F, 0.0F, 0.0F);
        }

        for (int j1 = this.minecraft.level.getMinBuildHeight(); j1 <= this.minecraft.level.getMaxBuildHeight(); j1 += 2) {
            float f4 = (float)((double)j1 - pCamY);
            int k = j1 % 8 == 0 ? CELL_BORDER : YELLOW;
            vertexconsumer.addVertex(matrix4f, f2, f4, f3).setColor(1.0F, 1.0F, 0.0F, 0.0F);
            vertexconsumer.addVertex(matrix4f, f2, f4, f3).setColor(k);
            vertexconsumer.addVertex(matrix4f, f2, f4, f3 + 16.0F).setColor(k);
            vertexconsumer.addVertex(matrix4f, f2 + 16.0F, f4, f3 + 16.0F).setColor(k);
            vertexconsumer.addVertex(matrix4f, f2 + 16.0F, f4, f3).setColor(k);
            vertexconsumer.addVertex(matrix4f, f2, f4, f3).setColor(k);
            vertexconsumer.addVertex(matrix4f, f2, f4, f3).setColor(1.0F, 1.0F, 0.0F, 0.0F);
        }

        vertexconsumer = pBufferSource.getBuffer(RenderType.debugLineStrip(2.0));

        for (int k1 = 0; k1 <= 16; k1 += 16) {
            for (int k2 = 0; k2 <= 16; k2 += 16) {
                vertexconsumer.addVertex(matrix4f, f2 + (float)k1, f, f3 + (float)k2).setColor(0.25F, 0.25F, 1.0F, 0.0F);
                vertexconsumer.addVertex(matrix4f, f2 + (float)k1, f, f3 + (float)k2).setColor(0.25F, 0.25F, 1.0F, 1.0F);
                vertexconsumer.addVertex(matrix4f, f2 + (float)k1, f1, f3 + (float)k2).setColor(0.25F, 0.25F, 1.0F, 1.0F);
                vertexconsumer.addVertex(matrix4f, f2 + (float)k1, f1, f3 + (float)k2).setColor(0.25F, 0.25F, 1.0F, 0.0F);
            }
        }

        for (int l1 = this.minecraft.level.getMinBuildHeight(); l1 <= this.minecraft.level.getMaxBuildHeight(); l1 += 16) {
            float f5 = (float)((double)l1 - pCamY);
            vertexconsumer.addVertex(matrix4f, f2, f5, f3).setColor(0.25F, 0.25F, 1.0F, 0.0F);
            vertexconsumer.addVertex(matrix4f, f2, f5, f3).setColor(0.25F, 0.25F, 1.0F, 1.0F);
            vertexconsumer.addVertex(matrix4f, f2, f5, f3 + 16.0F).setColor(0.25F, 0.25F, 1.0F, 1.0F);
            vertexconsumer.addVertex(matrix4f, f2 + 16.0F, f5, f3 + 16.0F).setColor(0.25F, 0.25F, 1.0F, 1.0F);
            vertexconsumer.addVertex(matrix4f, f2 + 16.0F, f5, f3).setColor(0.25F, 0.25F, 1.0F, 1.0F);
            vertexconsumer.addVertex(matrix4f, f2, f5, f3).setColor(0.25F, 0.25F, 1.0F, 1.0F);
            vertexconsumer.addVertex(matrix4f, f2, f5, f3).setColor(0.25F, 0.25F, 1.0F, 0.0F);
        }
    }
}