package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class DarkOakTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<DarkOakTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
        p_70090_ -> trunkPlacerParts(p_70090_).apply(p_70090_, DarkOakTrunkPlacer::new)
    );

    public DarkOakTrunkPlacer(int p_70077_, int p_70078_, int p_70079_) {
        super(p_70077_, p_70078_, p_70079_);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.DARK_OAK_TRUNK_PLACER;
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
        BlockPos blockpos = pPos.below();
        setDirtAt(pLevel, pBlockSetter, pRandom, blockpos, pConfig);
        setDirtAt(pLevel, pBlockSetter, pRandom, blockpos.east(), pConfig);
        setDirtAt(pLevel, pBlockSetter, pRandom, blockpos.south(), pConfig);
        setDirtAt(pLevel, pBlockSetter, pRandom, blockpos.south().east(), pConfig);
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(pRandom);
        int i = pFreeTreeHeight - pRandom.nextInt(4);
        int j = 2 - pRandom.nextInt(3);
        int k = pPos.getX();
        int l = pPos.getY();
        int i1 = pPos.getZ();
        int j1 = k;
        int k1 = i1;
        int l1 = l + pFreeTreeHeight - 1;

        for (int i2 = 0; i2 < pFreeTreeHeight; i2++) {
            if (i2 >= i && j > 0) {
                j1 += direction.getStepX();
                k1 += direction.getStepZ();
                j--;
            }

            int j2 = l + i2;
            BlockPos blockpos1 = new BlockPos(j1, j2, k1);
            if (TreeFeature.isAirOrLeaves(pLevel, blockpos1)) {
                this.placeLog(pLevel, pBlockSetter, pRandom, blockpos1, pConfig);
                this.placeLog(pLevel, pBlockSetter, pRandom, blockpos1.east(), pConfig);
                this.placeLog(pLevel, pBlockSetter, pRandom, blockpos1.south(), pConfig);
                this.placeLog(pLevel, pBlockSetter, pRandom, blockpos1.east().south(), pConfig);
            }
        }

        list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(j1, l1, k1), 0, true));

        for (int l2 = -1; l2 <= 2; l2++) {
            for (int i3 = -1; i3 <= 2; i3++) {
                if ((l2 < 0 || l2 > 1 || i3 < 0 || i3 > 1) && pRandom.nextInt(3) <= 0) {
                    int j3 = pRandom.nextInt(3) + 2;

                    for (int k2 = 0; k2 < j3; k2++) {
                        this.placeLog(pLevel, pBlockSetter, pRandom, new BlockPos(k + l2, l1 - k2 - 1, i1 + i3), pConfig);
                    }

                    list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(j1 + l2, l1, k1 + i3), 0, false));
                }
            }
        }

        return list;
    }
}