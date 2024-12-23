package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderRegionCache {
    private final Long2ObjectMap<RenderRegionCache.ChunkInfo> chunkInfoCache = new Long2ObjectOpenHashMap<>();

    @Nullable
    public RenderChunkRegion createRegion(Level pLevel, SectionPos pSectionPos) {
        RenderRegionCache.ChunkInfo renderregioncache$chunkinfo = this.getChunkInfo(pLevel, pSectionPos.x(), pSectionPos.z());
        if (renderregioncache$chunkinfo.chunk().isSectionEmpty(pSectionPos.y())) {
            return null;
        } else {
            int i = pSectionPos.x() - 1;
            int j = pSectionPos.z() - 1;
            int k = pSectionPos.x() + 1;
            int l = pSectionPos.z() + 1;
            RenderChunk[] arenderchunk = new RenderChunk[9];

            for (int i1 = j; i1 <= l; i1++) {
                for (int j1 = i; j1 <= k; j1++) {
                    int k1 = RenderChunkRegion.index(i, j, j1, i1);
                    RenderRegionCache.ChunkInfo renderregioncache$chunkinfo1 = j1 == pSectionPos.x() && i1 == pSectionPos.z()
                        ? renderregioncache$chunkinfo
                        : this.getChunkInfo(pLevel, j1, i1);
                    arenderchunk[k1] = renderregioncache$chunkinfo1.renderChunk();
                }
            }

            return new RenderChunkRegion(pLevel, i, j, arenderchunk);
        }
    }

    private RenderRegionCache.ChunkInfo getChunkInfo(Level pLevel, int pX, int pZ) {
        return this.chunkInfoCache
            .computeIfAbsent(
                ChunkPos.asLong(pX, pZ),
                p_200464_ -> new RenderRegionCache.ChunkInfo(pLevel.getChunk(ChunkPos.getX(p_200464_), ChunkPos.getZ(p_200464_)))
            );
    }

    @OnlyIn(Dist.CLIENT)
    static final class ChunkInfo {
        private final LevelChunk chunk;
        @Nullable
        private RenderChunk renderChunk;

        ChunkInfo(LevelChunk pChunk) {
            this.chunk = pChunk;
        }

        public LevelChunk chunk() {
            return this.chunk;
        }

        public RenderChunk renderChunk() {
            if (this.renderChunk == null) {
                this.renderChunk = new RenderChunk(this.chunk);
            }

            return this.renderChunk;
        }
    }
}