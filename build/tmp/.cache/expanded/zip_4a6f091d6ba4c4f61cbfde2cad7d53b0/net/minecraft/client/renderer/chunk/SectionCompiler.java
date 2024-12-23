package net.minecraft.client.renderer.chunk;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SectionCompiler {
    private final BlockRenderDispatcher blockRenderer;
    private final BlockEntityRenderDispatcher blockEntityRenderer;

    public SectionCompiler(BlockRenderDispatcher pBlockRenderer, BlockEntityRenderDispatcher pBlockEntityRenderer) {
        this.blockRenderer = pBlockRenderer;
        this.blockEntityRenderer = pBlockEntityRenderer;
    }

    public SectionCompiler.Results compile(SectionPos pSectionPos, RenderChunkRegion pRegion, VertexSorting pVertexSorting, SectionBufferBuilderPack pSectionBufferBuilderPack) {
        var modelDataMap = net.minecraft.client.Minecraft.getInstance().level.getModelDataManager().getAt(pSectionPos);
        SectionCompiler.Results sectioncompiler$results = new SectionCompiler.Results();
        BlockPos blockpos = pSectionPos.origin();
        BlockPos blockpos1 = blockpos.offset(15, 15, 15);
        VisGraph visgraph = new VisGraph();
        PoseStack posestack = new PoseStack();
        ModelBlockRenderer.enableCaching();
        Map<RenderType, BufferBuilder> map = new Reference2ObjectArrayMap<>(RenderType.chunkBufferLayers().size());
        RandomSource randomsource = RandomSource.create();

        for (BlockPos blockpos2 : BlockPos.betweenClosed(blockpos, blockpos1)) {
            BlockState blockstate = pRegion.getBlockState(blockpos2);
            if (blockstate.isSolidRender(pRegion, blockpos2)) {
                visgraph.setOpaque(blockpos2);
            }

            if (blockstate.hasBlockEntity()) {
                BlockEntity blockentity = pRegion.getBlockEntity(blockpos2);
                if (blockentity != null) {
                    this.handleBlockEntity(sectioncompiler$results, blockentity);
                }
            }

            FluidState fluidstate = blockstate.getFluidState();
            if (!fluidstate.isEmpty()) {
                RenderType rendertype = ItemBlockRenderTypes.getRenderLayer(fluidstate);
                BufferBuilder bufferbuilder = this.getOrBeginLayer(map, pSectionBufferBuilderPack, rendertype);
                this.blockRenderer.renderLiquid(blockpos2, pRegion, bufferbuilder, blockstate, fluidstate);
            }

            if (blockstate.getRenderShape() == RenderShape.MODEL) {
                var model = this.blockRenderer.getBlockModel(blockstate);
                var data = modelDataMap.getOrDefault(blockpos2, net.minecraftforge.client.model.data.ModelData.EMPTY);
                data = model.getModelData(pRegion, blockpos2, blockstate, data);
                randomsource.setSeed(blockstate.getSeed(blockpos2));
                for (RenderType rendertype2 : model.getRenderTypes(blockstate, randomsource, data)) {
                BufferBuilder bufferbuilder1 = this.getOrBeginLayer(map, pSectionBufferBuilderPack, rendertype2);
                posestack.pushPose();
                posestack.translate(
                    (float)SectionPos.sectionRelative(blockpos2.getX()),
                    (float)SectionPos.sectionRelative(blockpos2.getY()),
                    (float)SectionPos.sectionRelative(blockpos2.getZ())
                );
                this.blockRenderer.renderBatched(blockstate, blockpos2, pRegion, posestack, bufferbuilder1, true, randomsource, data, rendertype2);
                posestack.popPose();
                }
            }
        }

        for (Entry<RenderType, BufferBuilder> entry : map.entrySet()) {
            RenderType rendertype1 = entry.getKey();
            MeshData meshdata = entry.getValue().build();
            if (meshdata != null) {
                if (rendertype1 == RenderType.translucent()) {
                    sectioncompiler$results.transparencyState = meshdata.sortQuads(pSectionBufferBuilderPack.buffer(RenderType.translucent()), pVertexSorting);
                }

                sectioncompiler$results.renderedLayers.put(rendertype1, meshdata);
            }
        }

        ModelBlockRenderer.clearCache();
        sectioncompiler$results.visibilitySet = visgraph.resolve();
        return sectioncompiler$results;
    }

    private BufferBuilder getOrBeginLayer(Map<RenderType, BufferBuilder> pBufferLayers, SectionBufferBuilderPack pSectionBufferBuilderPack, RenderType pRenderType) {
        BufferBuilder bufferbuilder = pBufferLayers.get(pRenderType);
        if (bufferbuilder == null) {
            ByteBufferBuilder bytebufferbuilder = pSectionBufferBuilderPack.buffer(pRenderType);
            bufferbuilder = new BufferBuilder(bytebufferbuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            pBufferLayers.put(pRenderType, bufferbuilder);
        }

        return bufferbuilder;
    }

    private <E extends BlockEntity> void handleBlockEntity(SectionCompiler.Results pResults, E pBlockEntity) {
        BlockEntityRenderer<E> blockentityrenderer = this.blockEntityRenderer.getRenderer(pBlockEntity);
        if (blockentityrenderer != null) {
            if (blockentityrenderer.shouldRenderOffScreen(pBlockEntity)) {
                pResults.globalBlockEntities.add(pBlockEntity);
            } else {
                pResults.blockEntities.add(pBlockEntity); //FORGE: Fix MC-112730
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Results {
        public final List<BlockEntity> globalBlockEntities = new ArrayList<>();
        public final List<BlockEntity> blockEntities = new ArrayList<>();
        public final Map<RenderType, MeshData> renderedLayers = new Reference2ObjectArrayMap<>();
        public VisibilitySet visibilitySet = new VisibilitySet();
        @Nullable
        public MeshData.SortState transparencyState;

        public void release() {
            this.renderedLayers.values().forEach(MeshData::close);
        }
    }
}
