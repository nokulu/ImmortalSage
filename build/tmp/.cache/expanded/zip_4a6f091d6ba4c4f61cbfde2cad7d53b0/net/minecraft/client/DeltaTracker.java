package net.minecraft.client;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface DeltaTracker {
    DeltaTracker ZERO = new DeltaTracker.DefaultValue(0.0F);
    DeltaTracker ONE = new DeltaTracker.DefaultValue(1.0F);

    float getGameTimeDeltaTicks();

    float getGameTimeDeltaPartialTick(boolean pRunsNormally);

    float getRealtimeDeltaTicks();

    @OnlyIn(Dist.CLIENT)
    public static class DefaultValue implements DeltaTracker {
        private final float value;

        DefaultValue(float pValue) {
            this.value = pValue;
        }

        @Override
        public float getGameTimeDeltaTicks() {
            return this.value;
        }

        @Override
        public float getGameTimeDeltaPartialTick(boolean pRunsNormally) {
            return this.value;
        }

        @Override
        public float getRealtimeDeltaTicks() {
            return this.value;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Timer implements DeltaTracker {
        private float deltaTicks;
        private float deltaTickResidual;
        private float realtimeDeltaTicks;
        private float pausedDeltaTickResidual;
        private long lastMs;
        private long lastUiMs;
        private final float msPerTick;
        private final FloatUnaryOperator targetMsptProvider;
        private boolean paused;
        private boolean frozen;

        public Timer(float pTicksPerSecond, long pTime, FloatUnaryOperator pTargetMsptProvider) {
            this.msPerTick = 1000.0F / pTicksPerSecond;
            this.lastUiMs = this.lastMs = pTime;
            this.targetMsptProvider = pTargetMsptProvider;
        }

        public int advanceTime(long pTime, boolean pAdvanceGameTime) {
            this.advanceRealTime(pTime);
            return pAdvanceGameTime ? this.advanceGameTime(pTime) : 0;
        }

        private int advanceGameTime(long pTime) {
            this.deltaTicks = (float)(pTime - this.lastMs) / this.targetMsptProvider.apply(this.msPerTick);
            this.lastMs = pTime;
            this.deltaTickResidual = this.deltaTickResidual + this.deltaTicks;
            int i = (int)this.deltaTickResidual;
            this.deltaTickResidual -= (float)i;
            return i;
        }

        private void advanceRealTime(long pTime) {
            this.realtimeDeltaTicks = (float)(pTime - this.lastUiMs) / this.msPerTick;
            this.lastUiMs = pTime;
        }

        public void updatePauseState(boolean pPaused) {
            if (pPaused) {
                this.pause();
            } else {
                this.unPause();
            }
        }

        private void pause() {
            if (!this.paused) {
                if (net.minecraftforge.client.ForgeHooksClient.onClientPauseChangePre(true)) return;
                this.pausedDeltaTickResidual = this.deltaTickResidual;
            }

            this.paused = true;
            net.minecraftforge.client.ForgeHooksClient.onClientPauseChangePost(true);
        }

        private void unPause() {
            if (this.paused) {
                if (net.minecraftforge.client.ForgeHooksClient.onClientPauseChangePre(false)) return;
                this.deltaTickResidual = this.pausedDeltaTickResidual;
            }

            this.paused = false;
            net.minecraftforge.client.ForgeHooksClient.onClientPauseChangePost(false);
        }

        public void updateFrozenState(boolean pFrozen) {
            this.frozen = pFrozen;
        }

        @Override
        public float getGameTimeDeltaTicks() {
            return this.deltaTicks;
        }

        @Override
        public float getGameTimeDeltaPartialTick(boolean pRunsNormally) {
            if (!pRunsNormally && this.frozen) {
                return 1.0F;
            } else {
                return this.paused ? this.pausedDeltaTickResidual : this.deltaTickResidual;
            }
        }

        @Override
        public float getRealtimeDeltaTicks() {
            return this.realtimeDeltaTicks > 7.0F ? 0.5F : this.realtimeDeltaTicks;
        }
    }
}
