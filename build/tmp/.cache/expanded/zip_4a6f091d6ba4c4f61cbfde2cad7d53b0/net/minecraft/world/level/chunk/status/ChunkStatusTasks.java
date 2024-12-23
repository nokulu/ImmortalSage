package net.minecraft.world.level.chunk.status;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;

public class ChunkStatusTasks {
    private static boolean isLighted(ChunkAccess pChunk) {
        return pChunk.getPersistedStatus().isOrAfter(ChunkStatus.LIGHT) && pChunk.isLightCorrect();
    }

    static CompletableFuture<ChunkAccess> passThrough(
        WorldGenContext pWorldGenContext, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk
    ) {
        return CompletableFuture.completedFuture(pChunk);
    }

    static CompletableFuture<ChunkAccess> generateStructureStarts(
        WorldGenContext pWorldGenContext, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk
    ) {
        ServerLevel serverlevel = pWorldGenContext.level();
        if (serverlevel.getServer().getWorldData().worldGenOptions().generateStructures()) {
            pWorldGenContext.generator()
                .createStructures(serverlevel.registryAccess(), serverlevel.getChunkSource().getGeneratorState(), serverlevel.structureManager(), pChunk, pWorldGenContext.structureManager());
        }

        serverlevel.onStructureStartsAvailable(pChunk);
        return CompletableFuture.completedFuture(pChunk);
    }

    static CompletableFuture<ChunkAccess> loadStructureStarts(
        WorldGenContext pWorldGenContext, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk
    ) {
        pWorldGenContext.level().onStructureStartsAvailable(pChunk);
        return CompletableFuture.completedFuture(pChunk);
    }

    static CompletableFuture<ChunkAccess> generateStructureReferences(
        WorldGenContext pWorldGenContext, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk
    ) {
        ServerLevel serverlevel = pWorldGenContext.level();
        WorldGenRegion worldgenregion = new WorldGenRegion(serverlevel, pCache, pStep, pChunk);
        pWorldGenContext.generator().createReferences(worldgenregion, serverlevel.structureManager().forWorldGenRegion(worldgenregion), pChunk);
        return CompletableFuture.completedFuture(pChunk);
    }

    static CompletableFuture<ChunkAccess> generateBiomes(
        WorldGenContext pWorldGenContext, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk
    ) {
        ServerLevel serverlevel = pWorldGenContext.level();
        WorldGenRegion worldgenregion = new WorldGenRegion(serverlevel, pCache, pStep, pChunk);
        return pWorldGenContext.generator()
            .createBiomes(serverlevel.getChunkSource().randomState(), Blender.of(worldgenregion), serverlevel.structureManager().forWorldGenRegion(worldgenregion), pChunk);
    }

    static CompletableFuture<ChunkAccess> generateNoise(
        WorldGenContext pWorldGenContext, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk
    ) {
        ServerLevel serverlevel = pWorldGenContext.level();
        WorldGenRegion worldgenregion = new WorldGenRegion(serverlevel, pCache, pStep, pChunk);
        return pWorldGenContext.generator()
            .fillFromNoise(Blender.of(worldgenregion), serverlevel.getChunkSource().randomState(), serverlevel.structureManager().forWorldGenRegion(worldgenregion), pChunk)
            .thenApply(p_328030_ -> {
                if (p_328030_ instanceof ProtoChunk protochunk) {
                    BelowZeroRetrogen belowzeroretrogen = protochunk.getBelowZeroRetrogen();
                    if (belowzeroretrogen != null) {
                        BelowZeroRetrogen.replaceOldBedrock(protochunk);
                        if (belowzeroretrogen.hasBedrockHoles()) {
                            belowzeroretrogen.applyBedrockMask(protochunk);
                        }
                    }
                }

                return (ChunkAccess)p_328030_;
            });
    }

    static CompletableFuture<ChunkAccess> generateSurface(
        WorldGenContext pWorldGenContext, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk
    ) {
        ServerLevel serverlevel = pWorldGenContext.level();
        WorldGenRegion worldgenregion = new WorldGenRegion(serverlevel, pCache, pStep, pChunk);
        pWorldGenContext.generator().buildSurface(worldgenregion, serverlevel.structureManager().forWorldGenRegion(worldgenregion), serverlevel.getChunkSource().randomState(), pChunk);
        return CompletableFuture.completedFuture(pChunk);
    }

    static CompletableFuture<ChunkAccess> generateCarvers(
        WorldGenContext pWorldGenContext, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk
    ) {
        ServerLevel serverlevel = pWorldGenContext.level();
        WorldGenRegion worldgenregion = new WorldGenRegion(serverlevel, pCache, pStep, pChunk);
        if (pChunk instanceof ProtoChunk protochunk) {
            Blender.addAroundOldChunksCarvingMaskFilter(worldgenregion, protochunk);
        }

        pWorldGenContext.generator()
            .applyCarvers(
                worldgenregion,
                serverlevel.getSeed(),
                serverlevel.getChunkSource().randomState(),
                serverlevel.getBiomeManager(),
                serverlevel.structureManager().forWorldGenRegion(worldgenregion),
                pChunk,
                GenerationStep.Carving.AIR
            );
        return CompletableFuture.completedFuture(pChunk);
    }

    static CompletableFuture<ChunkAccess> generateFeatures(
        WorldGenContext pWorldGenContext, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk
    ) {
        ServerLevel serverlevel = pWorldGenContext.level();
        Heightmap.primeHeightmaps(
            pChunk,
            EnumSet.of(Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE)
        );
        WorldGenRegion worldgenregion = new WorldGenRegion(serverlevel, pCache, pStep, pChunk);
        pWorldGenContext.generator().applyBiomeDecoration(worldgenregion, pChunk, serverlevel.structureManager().forWorldGenRegion(worldgenregion));
        Blender.generateBorderTicks(worldgenregion, pChunk);
        return CompletableFuture.completedFuture(pChunk);
    }

    static CompletableFuture<ChunkAccess> initializeLight(
        WorldGenContext pWorldGenContext, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk
    ) {
        ThreadedLevelLightEngine threadedlevellightengine = pWorldGenContext.lightEngine();
        pChunk.initializeLightSources();
        ((ProtoChunk)pChunk).setLightEngine(threadedlevellightengine);
        boolean flag = isLighted(pChunk);
        return threadedlevellightengine.initializeLight(pChunk, flag);
    }

    static CompletableFuture<ChunkAccess> light(
        WorldGenContext pWorldGenContext, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk
    ) {
        boolean flag = isLighted(pChunk);
        return pWorldGenContext.lightEngine().lightChunk(pChunk, flag);
    }

    static CompletableFuture<ChunkAccess> generateSpawn(
        WorldGenContext pWorldGenContext, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk
    ) {
        if (!pChunk.isUpgrading()) {
            pWorldGenContext.generator().spawnOriginalMobs(new WorldGenRegion(pWorldGenContext.level(), pCache, pStep, pChunk));
        }

        return CompletableFuture.completedFuture(pChunk);
    }

    static CompletableFuture<ChunkAccess> full(
        WorldGenContext pWorldGenContext, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache, ChunkAccess pChunk
    ) {
        ChunkPos chunkpos = pChunk.getPos();
        GenerationChunkHolder generationchunkholder = pCache.get(chunkpos.x, chunkpos.z);
        return CompletableFuture.supplyAsync(() -> {
            ProtoChunk protochunk = (ProtoChunk)pChunk;
            ServerLevel serverlevel = pWorldGenContext.level();
            LevelChunk levelchunk;
            if (protochunk instanceof ImposterProtoChunk) {
                levelchunk = ((ImposterProtoChunk)protochunk).getWrapped();
            } else {
                levelchunk = new LevelChunk(serverlevel, protochunk, p_341875_ -> postLoadProtoChunk(serverlevel, protochunk.getEntities()));
                generationchunkholder.replaceProtoChunk(new ImposterProtoChunk(levelchunk, false));
            }

            levelchunk.setFullStatus(generationchunkholder::getFullStatus);
            levelchunk.runPostLoad();
            levelchunk.setLoaded(true);
            try {
                generationchunkholder.currentlyLoading = levelchunk; // Forge - bypass the future chain when getChunk is called, this prevents deadlocks.
            levelchunk.registerAllBlockEntitiesAfterLevelLoad();
            levelchunk.registerTickContainerInLevel(serverlevel);
                net.minecraftforge.event.ForgeEventFactory.onChunkLoad(levelchunk, !(protochunk instanceof ImposterProtoChunk));
            } finally {
                generationchunkholder.currentlyLoading = null; // Forge - Stop bypassing the future chain.
            }
            return levelchunk;
        }, p_341879_ -> pWorldGenContext.mainThreadMailBox().tell(ChunkTaskPriorityQueueSorter.message(p_341879_, chunkpos.toLong(), generationchunkholder::getTicketLevel)));
    }

    private static void postLoadProtoChunk(ServerLevel pLevel, List<CompoundTag> pEntityTags) {
        if (!pEntityTags.isEmpty()) {
            pLevel.addWorldGenChunkEntities(EntityType.loadEntitiesRecursive(pEntityTags, pLevel));
        }
    }
}
