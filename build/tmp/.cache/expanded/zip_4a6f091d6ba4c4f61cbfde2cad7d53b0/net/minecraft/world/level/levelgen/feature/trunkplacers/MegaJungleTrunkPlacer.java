package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class MegaJungleTrunkPlacer extends GiantTrunkPlacer {
    public static final MapCodec<MegaJungleTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
        p_70206_ -> trunkPlacerParts(p_70206_).apply(p_70206_, MegaJungleTrunkPlacer::new)
    );

    public MegaJungleTrunkPlacer(int p_70193_, int p_70194_, int p_70195_) {
        super(p_70193_, p_70194_, p_70195_);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.MEGA_JUNGLE_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedReader pLevel,
        BiConsumer<BlockPos, BlockState> pBlockSetter,
        RandomSource pRandom,
        int pFreeTreeHeight,
        BlockPos pPos,
        TreeConfiguration pConfig
    ) {
        List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
        list.addAll(super.placeTrunk(pLevel, pBlockSetter, pRandom, pFreeTreeHeight, pPos, pConfig));

        for (int i = pFreeTreeHeight - 2 - pRandom.nextInt(4); i > pFreeTreeHeight / 2; i -= 2 + pRandom.nextInt(4)) {
            float f = pRandom.nextFloat() * (float) (Math.PI * 2);
            int j = 0;
            int k = 0;

            for (int l = 0; l < 5; l++) {
                j = (int)(1.5F + Mth.cos(f) * (float)l);
                k = (int)(1.5F + Mth.sin(f) * (float)l);
                BlockPos blockpos = pPos.offset(j, i - 3 + l / 2, k);
                this.placeLog(pLevel, pBlockSetter, pRandom, blockpos, pConfig);
            }

            list.add(new FoliagePlacer.FoliageAttachment(pPos.offset(j, i, k), -2, false));
        }

        return list;
    }
}