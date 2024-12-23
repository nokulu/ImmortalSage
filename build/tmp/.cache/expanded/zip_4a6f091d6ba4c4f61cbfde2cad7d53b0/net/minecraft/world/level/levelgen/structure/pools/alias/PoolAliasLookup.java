package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

@FunctionalInterface
public interface PoolAliasLookup {
    PoolAliasLookup EMPTY = p_311984_ -> p_311984_;

    ResourceKey<StructureTemplatePool> lookup(ResourceKey<StructureTemplatePool> pPoolKey);

    static PoolAliasLookup create(List<PoolAliasBinding> pAliases, BlockPos pPos, long pSeed) {
        if (pAliases.isEmpty()) {
            return EMPTY;
        } else {
            RandomSource randomsource = RandomSource.create(pSeed).forkPositional().at(pPos);
            Builder<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> builder = ImmutableMap.builder();
            pAliases.forEach(p_311006_ -> p_311006_.forEachResolved(randomsource, builder::put));
            Map<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> map = builder.build();
            return p_312268_ -> Objects.requireNonNull(map.getOrDefault(p_312268_, p_312268_), () -> "alias " + p_312268_ + " was mapped to null value");
        }
    }
}