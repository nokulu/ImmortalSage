package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class JukeboxSongPlayer {
    public static final int PLAY_EVENT_INTERVAL_TICKS = 20;
    private long ticksSinceSongStarted;
    @Nullable
    private Holder<JukeboxSong> song;
    private final BlockPos blockPos;
    private final JukeboxSongPlayer.OnSongChanged onSongChanged;

    public JukeboxSongPlayer(JukeboxSongPlayer.OnSongChanged pOnSongChanged, BlockPos pBlockPos) {
        this.onSongChanged = pOnSongChanged;
        this.blockPos = pBlockPos;
    }

    public boolean isPlaying() {
        return this.song != null;
    }

    @Nullable
    public JukeboxSong getSong() {
        return this.song == null ? null : this.song.value();
    }

    public long getTicksSinceSongStarted() {
        return this.ticksSinceSongStarted;
    }

    public void setSongWithoutPlaying(Holder<JukeboxSong> pSong, long pTicksSinceSongStarted) {
        if (!pSong.value().hasFinished(pTicksSinceSongStarted)) {
            this.song = pSong;
            this.ticksSinceSongStarted = pTicksSinceSongStarted;
        }
    }

    public void play(LevelAccessor pLevel, Holder<JukeboxSong> pSong) {
        this.song = pSong;
        this.ticksSinceSongStarted = 0L;
        int i = pLevel.registryAccess().registryOrThrow(Registries.JUKEBOX_SONG).getId(this.song.value());
        pLevel.levelEvent(null, 1010, this.blockPos, i);
        this.onSongChanged.notifyChange();
    }

    public void stop(LevelAccessor pLevel, @Nullable BlockState pState) {
        if (this.song != null) {
            this.song = null;
            this.ticksSinceSongStarted = 0L;
            pLevel.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.blockPos, GameEvent.Context.of(pState));
            pLevel.levelEvent(1011, this.blockPos, 0);
            this.onSongChanged.notifyChange();
        }
    }

    public void tick(LevelAccessor pLevel, @Nullable BlockState pState) {
        if (this.song != null) {
            if (this.song.value().hasFinished(this.ticksSinceSongStarted)) {
                this.stop(pLevel, pState);
            } else {
                if (this.shouldEmitJukeboxPlayingEvent()) {
                    pLevel.gameEvent(GameEvent.JUKEBOX_PLAY, this.blockPos, GameEvent.Context.of(pState));
                    spawnMusicParticles(pLevel, this.blockPos);
                }

                this.ticksSinceSongStarted++;
            }
        }
    }

    private boolean shouldEmitJukeboxPlayingEvent() {
        return this.ticksSinceSongStarted % 20L == 0L;
    }

    private static void spawnMusicParticles(LevelAccessor pLevel, BlockPos pPos) {
        if (pLevel instanceof ServerLevel serverlevel) {
            Vec3 vec3 = Vec3.atBottomCenterOf(pPos).add(0.0, 1.2F, 0.0);
            float f = (float)pLevel.getRandom().nextInt(4) / 24.0F;
            serverlevel.sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, (double)f, 0.0, 0.0, 1.0);
        }
    }

    @FunctionalInterface
    public interface OnSongChanged {
        void notifyChange();
    }
}