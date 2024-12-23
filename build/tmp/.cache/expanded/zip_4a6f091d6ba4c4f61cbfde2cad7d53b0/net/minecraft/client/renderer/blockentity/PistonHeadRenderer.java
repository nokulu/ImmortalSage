package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PistonHeadRenderer implements BlockEntityRenderer<PistonMovingBlockEntity> {
    private BlockRenderDispatcher blockRenderer;

    public PistonHeadRenderer(BlockEntityRendererProvider.Context pContext) {
        this.blockRenderer = pContext.getBlockRenderDispatcher();
    }

    public void render(PistonMovingBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        Level level = pBlockEntity.getLevel();
        if (level != null) {
            BlockPos blockpos = pBlockEntity.getBlockPos().relative(pBlockEntity.getMovementDirection().getOpposite());
            BlockState blockstate = pBlockEntity.getMovedState();
            if (!blockstate.isAir()) {
                ModelBlockRenderer.enableCaching();
                pPoseStack.pushPose();
                pPoseStack.translate(pBlockEntity.getXOff(pPartialTick), pBlockEntity.getYOff(pPartialTick), pBlockEntity.getZOff(pPartialTick));
                if (blockstate.is(Blocks.PISTON_HEAD) && pBlockEntity.getProgress(pPartialTick) <= 4.0F) {
                    blockstate = blockstate.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(pBlockEntity.getProgress(pPartialTick) <= 0.5F));
                    this.renderBlock(blockpos, blockstate, pPoseStack, pBufferSource, level, false, pPackedOverlay);
                } else if (pBlockEntity.isSourcePiston() && !pBlockEntity.isExtending()) {
                    PistonType pistontype = blockstate.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT;
                    BlockState blockstate1 = Blocks.PISTON_HEAD
                        .defaultBlockState()
                        .setValue(PistonHeadBlock.TYPE, pistontype)
                        .setValue(PistonHeadBlock.FACING, blockstate.getValue(PistonBaseBlock.FACING));
                    blockstate1 = blockstate1.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(pBlockEntity.getProgress(pPartialTick) >= 0.5F));
                    this.renderBlock(blockpos, blockstate1, pPoseStack, pBufferSource, level, false, pPackedOverlay);
                    BlockPos blockpos1 = blockpos.relative(pBlockEntity.getMovementDirection());
                    pPoseStack.popPose();
                    pPoseStack.pushPose();
                    blockstate = blockstate.setValue(PistonBaseBlock.EXTENDED, Boolean.valueOf(true));
                    this.renderBlock(blockpos1, blockstate, pPoseStack, pBufferSource, level, true, pPackedOverlay);
                } else {
                    this.renderBlock(blockpos, blockstate, pPoseStack, pBufferSource, level, false, pPackedOverlay);
                }

                pPoseStack.popPose();
                ModelBlockRenderer.clearCache();
            }
        }
    }

    private void renderBlock(
        BlockPos pPos, BlockState pState, PoseStack pPoseStack, MultiBufferSource pBufferSource, Level pLevel, boolean pExtended, int pPackedOverlay
    ) {
        var model = blockRenderer.getBlockModel(pState);
        var data = net.minecraftforge.client.model.data.ModelData.EMPTY;
        for (var rendertype : model.getRenderTypes(pState, RandomSource.create(pState.getSeed(pPos)), data)) {
        VertexConsumer vertexconsumer = pBufferSource.getBuffer(net.minecraftforge.client.RenderTypeHelper.getMovingBlockRenderType(rendertype));
        this.blockRenderer
            .getModelRenderer()
            .tesselateBlock(
                pLevel,
                model,
                pState,
                pPos,
                pPoseStack,
                vertexconsumer,
                pExtended,
                RandomSource.create(),
                pState.getSeed(pPos),
                pPackedOverlay,
                data,
                rendertype
            );
        }
    }

    @Override
    public int getViewDistance() {
        return 68;
    }
}
