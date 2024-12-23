package net.minecraft.world.level.levelgen.structure.templatesystem;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;

public abstract class StructureProcessor {

    /**
     * @deprecated Forge: Use {@link #process(LevelReader, BlockPos, BlockPos, StructureTemplate.StructureBlockInfo, StructureTemplate.StructureBlockInfo, StructurePlaceSettings, StructureTemplate)} instead.
     */
    @Deprecated
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader pLevel,
        BlockPos pOffset,
        BlockPos pPos,
        StructureTemplate.StructureBlockInfo pBlockInfo,
        StructureTemplate.StructureBlockInfo pRelativeBlockInfo,
        StructurePlaceSettings pSettings
    ) {
        return pRelativeBlockInfo;
    }

    protected abstract StructureProcessorType<?> getType();

    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
        ServerLevelAccessor pServerLevel,
        BlockPos pOffset,
        BlockPos pPos,
        List<StructureTemplate.StructureBlockInfo> pOriginalBlockInfos,
        List<StructureTemplate.StructureBlockInfo> pProcessedBlockInfos,
        StructurePlaceSettings pSettings
    ) {
        return pProcessedBlockInfos;
    }

    /**
     * FORGE: Add entity processing.
     * <p>
     * Use this method to process entities from a structure in much the same way as
     * blocks, parameters are analogous.
     *
     * @see #process(LevelReader, BlockPos, BlockPos, StructureTemplate.StructureBlockInfo, StructureTemplate.StructureBlockInfo, StructurePlaceSettings, StructureTemplate)
     */
    public StructureTemplate.StructureEntityInfo processEntity(LevelReader world, BlockPos seedPos, StructureTemplate.StructureEntityInfo rawEntityInfo, StructureTemplate.StructureEntityInfo entityInfo, StructurePlaceSettings placementSettings, StructureTemplate template) {
       return entityInfo;
    }

    @Nullable
    public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos seed, BlockPos offset, StructureTemplate.StructureBlockInfo rawBlock, StructureTemplate.StructureBlockInfo block, StructurePlaceSettings settings, @Nullable StructureTemplate template) {
       return processBlock(level, seed, offset, rawBlock, block, settings);
    }
}
