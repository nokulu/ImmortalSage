package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;

public class FixedPlacement extends PlacementModifier {
    public static final MapCodec<FixedPlacement> CODEC = RecordCodecBuilder.mapCodec(
        p_344809_ -> p_344809_.group(BlockPos.CODEC.listOf().fieldOf("positions").forGetter(p_343370_ -> p_343370_.positions))
                .apply(p_344809_, FixedPlacement::new)
    );
    private final List<BlockPos> positions;

    public static FixedPlacement of(BlockPos... pPositions) {
        return new FixedPlacement(List.of(pPositions));
    }

    private FixedPlacement(List<BlockPos> p_345049_) {
        this.positions = p_345049_;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext pContext, RandomSource pRandom, BlockPos pPos) {
        int i = SectionPos.blockToSectionCoord(pPos.getX());
        int j = SectionPos.blockToSectionCoord(pPos.getZ());
        boolean flag = false;

        for (BlockPos blockpos : this.positions) {
            if (isSameChunk(i, j, blockpos)) {
                flag = true;
                break;
            }
        }

        return !flag ? Stream.empty() : this.positions.stream().filter(p_344119_ -> isSameChunk(i, j, p_344119_));
    }

    private static boolean isSameChunk(int pX, int pZ, BlockPos pPos) {
        return pX == SectionPos.blockToSectionCoord(pPos.getX()) && pZ == SectionPos.blockToSectionCoord(pPos.getZ());
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.FIXED_PLACEMENT;
    }
}