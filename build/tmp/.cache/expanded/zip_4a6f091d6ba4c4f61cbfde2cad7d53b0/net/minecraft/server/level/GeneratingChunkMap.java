package net.minecraft.server.level;

import java.util.concurrent.CompletableFuture;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;

public interface GeneratingChunkMap {
    GenerationChunkHolder acquireGeneration(long pChunkPos);

    void releaseGeneration(GenerationChunkHolder pChunk);

    CompletableFuture<ChunkAccess> applyStep(GenerationChunkHolder pChunk, ChunkStep pStep, StaticCache2D<GenerationChunkHolder> pCache);

    ChunkGenerationTask scheduleGenerationTask(ChunkStatus pTargetStatus, ChunkPos pPos);

    void runGenerationTasks();
}