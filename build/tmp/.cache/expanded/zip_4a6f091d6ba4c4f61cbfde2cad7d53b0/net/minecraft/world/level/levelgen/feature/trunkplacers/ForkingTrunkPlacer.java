package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class ForkingTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<ForkingTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
        p_70161_ -> trunkPlacerParts(p_70161_).apply(p_70161_, ForkingTrunkPlacer::new)
    );

    public ForkingTrunkPlacer(int p_70148_, int p_70149_, int p_70150_) {
        super(p_70148_, p_70149_, p_70150_);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.FORKING_TRUNK_PLACER;
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
        setDirtAt(pLevel, pBlockSetter, pRandom, pPos.below(), pConfig);
        List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(pRandom);
        int i = pFreeTreeHeight - pRandom.nextInt(4) - 1;
        int j = 3 - pRandom.nextInt(3);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int k = pPos.getX();
        int l = pPos.getZ();
        OptionalInt optionalint = OptionalInt.empty();

        for (int i1 = 0; i1 < pFreeTreeHeight; i1++) {
            int j1 = pPos.getY() + i1;
            if (i1 >= i && j > 0) {
                k += direction.getStepX();
                l += direction.getStepZ();
                j--;
            }

            if (this.placeLog(pLevel, pBlockSetter, pRandom, blockpos$mutableblockpos.set(k, j1, l), pConfig)) {
                optionalint = OptionalInt.of(j1 + 1);
            }
        }

        if (optionalint.isPresent()) {
            list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(k, optionalint.getAsInt(), l), 1, false));
        }

        k = pPos.getX();
        l = pPos.getZ();
        Direction direction1 = Direction.Plane.HORIZONTAL.getRandomDirection(pRandom);
        if (direction1 != direction) {
            int j2 = i - pRandom.nextInt(2) - 1;
            int k1 = 1 + pRandom.nextInt(3);
            optionalint = OptionalInt.empty();

            for (int l1 = j2; l1 < pFreeTreeHeight && k1 > 0; k1--) {
                if (l1 >= 1) {
                    int i2 = pPos.getY() + l1;
                    k += direction1.getStepX();
                    l += direction1.getStepZ();
                    if (this.placeLog(pLevel, pBlockSetter, pRandom, blockpos$mutableblockpos.set(k, i2, l), pConfig)) {
                        optionalint = OptionalInt.of(i2 + 1);
                    }
                }

                l1++;
            }

            if (optionalint.isPresent()) {
                list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(k, optionalint.getAsInt(), l), 0, false));
            }
        }

        return list;
    }
}