package net.minecraft.world.level.chunk.storage;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.world.level.ChunkPos;

public interface ChunkIOErrorReporter {
    void reportChunkLoadFailure(Throwable pThrowable, RegionStorageInfo pRegionStorageInfo, ChunkPos pChunkPos);

    void reportChunkSaveFailure(Throwable pThrowable, RegionStorageInfo pRegionStorageInfo, ChunkPos pChunkPos);

    static ReportedException createMisplacedChunkReport(ChunkPos pPos, ChunkPos pExpectedPos) {
        CrashReport crashreport = CrashReport.forThrowable(
            new IllegalStateException("Retrieved chunk position " + pPos + " does not match requested " + pExpectedPos), "Chunk found in invalid location"
        );
        CrashReportCategory crashreportcategory = crashreport.addCategory("Misplaced Chunk");
        crashreportcategory.setDetail("Stored Position", pPos::toString);
        return new ReportedException(crashreport);
    }

    default void reportMisplacedChunk(ChunkPos pPos, ChunkPos pExpectedPos, RegionStorageInfo pRegionStorageInfo) {
        this.reportChunkLoadFailure(createMisplacedChunkReport(pPos, pExpectedPos), pRegionStorageInfo, pExpectedPos);
    }
}