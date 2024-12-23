package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureBlockRenderer implements BlockEntityRenderer<StructureBlockEntity> {
    public StructureBlockRenderer(BlockEntityRendererProvider.Context pContext) {
    }

    public void render(StructureBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if (Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator()) {
            BlockPos blockpos = pBlockEntity.getStructurePos();
            Vec3i vec3i = pBlockEntity.getStructureSize();
            if (vec3i.getX() >= 1 && vec3i.getY() >= 1 && vec3i.getZ() >= 1) {
                if (pBlockEntity.getMode() == StructureMode.SAVE || pBlockEntity.getMode() == StructureMode.LOAD) {
                    double d0 = (double)blockpos.getX();
                    double d1 = (double)blockpos.getZ();
                    double d5 = (double)blockpos.getY();
                    double d8 = d5 + (double)vec3i.getY();
                    double d2;
                    double d3;
                    switch (pBlockEntity.getMirror()) {
                        case LEFT_RIGHT:
                            d2 = (double)vec3i.getX();
                            d3 = (double)(-vec3i.getZ());
                            break;
                        case FRONT_BACK:
                            d2 = (double)(-vec3i.getX());
                            d3 = (double)vec3i.getZ();
                            break;
                        default:
                            d2 = (double)vec3i.getX();
                            d3 = (double)vec3i.getZ();
                    }

                    double d4;
                    double d6;
                    double d7;
                    double d9;
                    switch (pBlockEntity.getRotation()) {
                        case CLOCKWISE_90:
                            d4 = d3 < 0.0 ? d0 : d0 + 1.0;
                            d6 = d2 < 0.0 ? d1 + 1.0 : d1;
                            d7 = d4 - d3;
                            d9 = d6 + d2;
                            break;
                        case CLOCKWISE_180:
                            d4 = d2 < 0.0 ? d0 : d0 + 1.0;
                            d6 = d3 < 0.0 ? d1 : d1 + 1.0;
                            d7 = d4 - d2;
                            d9 = d6 - d3;
                            break;
                        case COUNTERCLOCKWISE_90:
                            d4 = d3 < 0.0 ? d0 + 1.0 : d0;
                            d6 = d2 < 0.0 ? d1 : d1 + 1.0;
                            d7 = d4 + d3;
                            d9 = d6 - d2;
                            break;
                        default:
                            d4 = d2 < 0.0 ? d0 + 1.0 : d0;
                            d6 = d3 < 0.0 ? d1 + 1.0 : d1;
                            d7 = d4 + d2;
                            d9 = d6 + d3;
                    }

                    float f = 1.0F;
                    float f1 = 0.9F;
                    float f2 = 0.5F;
                    if (pBlockEntity.getMode() == StructureMode.SAVE || pBlockEntity.getShowBoundingBox()) {
                        VertexConsumer vertexconsumer = pBufferSource.getBuffer(RenderType.lines());
                        LevelRenderer.renderLineBox(pPoseStack, vertexconsumer, d4, d5, d6, d7, d8, d9, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
                    }

                    if (pBlockEntity.getMode() == StructureMode.SAVE && pBlockEntity.getShowAir()) {
                        this.renderInvisibleBlocks(pBlockEntity, pBufferSource, pPoseStack);
                    }
                }
            }
        }
    }

    private void renderInvisibleBlocks(StructureBlockEntity pBlockEntity, MultiBufferSource pBufferSource, PoseStack pPoseStack) {
        BlockGetter blockgetter = pBlockEntity.getLevel();
        VertexConsumer vertexconsumer = pBufferSource.getBuffer(RenderType.lines());
        BlockPos blockpos = pBlockEntity.getBlockPos();
        BlockPos blockpos1 = StructureUtils.getStructureOrigin(pBlockEntity);

        for (BlockPos blockpos2 : BlockPos.betweenClosed(blockpos1, blockpos1.offset(pBlockEntity.getStructureSize()).offset(-1, -1, -1))) {
            BlockState blockstate = blockgetter.getBlockState(blockpos2);
            boolean flag = blockstate.isAir();
            boolean flag1 = blockstate.is(Blocks.STRUCTURE_VOID);
            boolean flag2 = blockstate.is(Blocks.BARRIER);
            boolean flag3 = blockstate.is(Blocks.LIGHT);
            boolean flag4 = flag1 || flag2 || flag3;
            if (flag || flag4) {
                float f = flag ? 0.05F : 0.0F;
                double d0 = (double)((float)(blockpos2.getX() - blockpos.getX()) + 0.45F - f);
                double d1 = (double)((float)(blockpos2.getY() - blockpos.getY()) + 0.45F - f);
                double d2 = (double)((float)(blockpos2.getZ() - blockpos.getZ()) + 0.45F - f);
                double d3 = (double)((float)(blockpos2.getX() - blockpos.getX()) + 0.55F + f);
                double d4 = (double)((float)(blockpos2.getY() - blockpos.getY()) + 0.55F + f);
                double d5 = (double)((float)(blockpos2.getZ() - blockpos.getZ()) + 0.55F + f);
                if (flag) {
                    LevelRenderer.renderLineBox(pPoseStack, vertexconsumer, d0, d1, d2, d3, d4, d5, 0.5F, 0.5F, 1.0F, 1.0F, 0.5F, 0.5F, 1.0F);
                } else if (flag1) {
                    LevelRenderer.renderLineBox(pPoseStack, vertexconsumer, d0, d1, d2, d3, d4, d5, 1.0F, 0.75F, 0.75F, 1.0F, 1.0F, 0.75F, 0.75F);
                } else if (flag2) {
                    LevelRenderer.renderLineBox(pPoseStack, vertexconsumer, d0, d1, d2, d3, d4, d5, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
                } else if (flag3) {
                    LevelRenderer.renderLineBox(pPoseStack, vertexconsumer, d0, d1, d2, d3, d4, d5, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F);
                }
            }
        }
    }

    private void renderStructureVoids(StructureBlockEntity pBlockEntity, VertexConsumer pBuffer, PoseStack pPoseStack) {
        BlockGetter blockgetter = pBlockEntity.getLevel();
        if (blockgetter != null) {
            BlockPos blockpos = pBlockEntity.getBlockPos();
            BlockPos blockpos1 = StructureUtils.getStructureOrigin(pBlockEntity);
            Vec3i vec3i = pBlockEntity.getStructureSize();
            DiscreteVoxelShape discretevoxelshape = new BitSetDiscreteVoxelShape(vec3i.getX(), vec3i.getY(), vec3i.getZ());

            for (BlockPos blockpos2 : BlockPos.betweenClosed(blockpos1, blockpos1.offset(vec3i).offset(-1, -1, -1))) {
                if (blockgetter.getBlockState(blockpos2).is(Blocks.STRUCTURE_VOID)) {
                    discretevoxelshape.fill(
                        blockpos2.getX() - blockpos1.getX(),
                        blockpos2.getY() - blockpos1.getY(),
                        blockpos2.getZ() - blockpos1.getZ()
                    );
                }
            }

            discretevoxelshape.forAllFaces((p_342299_, p_345263_, p_344165_, p_344494_) -> {
                float f = 0.48F;
                float f1 = (float)(p_345263_ + blockpos1.getX() - blockpos.getX()) + 0.5F - 0.48F;
                float f2 = (float)(p_344165_ + blockpos1.getY() - blockpos.getY()) + 0.5F - 0.48F;
                float f3 = (float)(p_344494_ + blockpos1.getZ() - blockpos.getZ()) + 0.5F - 0.48F;
                float f4 = (float)(p_345263_ + blockpos1.getX() - blockpos.getX()) + 0.5F + 0.48F;
                float f5 = (float)(p_344165_ + blockpos1.getY() - blockpos.getY()) + 0.5F + 0.48F;
                float f6 = (float)(p_344494_ + blockpos1.getZ() - blockpos.getZ()) + 0.5F + 0.48F;
                LevelRenderer.renderFace(pPoseStack, pBuffer, p_342299_, f1, f2, f3, f4, f5, f6, 0.75F, 0.75F, 1.0F, 0.2F);
            });
        }
    }

    public boolean shouldRenderOffScreen(StructureBlockEntity pBlockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}