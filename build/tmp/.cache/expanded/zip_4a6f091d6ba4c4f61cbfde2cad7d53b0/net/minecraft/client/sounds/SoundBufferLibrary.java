package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.audio.SoundBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The {@linkplain SoundBufferLibrary} class provides a cache containing instances of {@linkplain SoundBuffer} and
 * {@linkplain AudioStream} for use in Minecraft sound handling.
 */
@OnlyIn(Dist.CLIENT)
public class SoundBufferLibrary {
    private final ResourceProvider resourceManager;
    private final Map<ResourceLocation, CompletableFuture<SoundBuffer>> cache = Maps.newHashMap();

    public SoundBufferLibrary(ResourceProvider pResourceManager) {
        this.resourceManager = pResourceManager;
    }

    public CompletableFuture<SoundBuffer> getCompleteBuffer(ResourceLocation pSoundID) {
        return this.cache.computeIfAbsent(pSoundID, p_325581_ -> CompletableFuture.supplyAsync(() -> {
                try {
                    SoundBuffer soundbuffer;
                    try (
                        InputStream inputstream = this.resourceManager.open(p_325581_);
                        FiniteAudioStream finiteaudiostream = new JOrbisAudioStream(inputstream);
                    ) {
                        ByteBuffer bytebuffer = finiteaudiostream.readAll();
                        soundbuffer = new SoundBuffer(bytebuffer, finiteaudiostream.getFormat());
                    }

                    return soundbuffer;
                } catch (IOException ioexception) {
                    throw new CompletionException(ioexception);
                }
            }, Util.nonCriticalIoPool()));
    }

    public CompletableFuture<AudioStream> getStream(ResourceLocation pResourceLocation, boolean pIsWrapper) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InputStream inputstream = this.resourceManager.open(pResourceLocation);
                return (AudioStream)(pIsWrapper ? new LoopingAudioStream(JOrbisAudioStream::new, inputstream) : new JOrbisAudioStream(inputstream));
            } catch (IOException ioexception) {
                throw new CompletionException(ioexception);
            }
        }, Util.nonCriticalIoPool());
    }

    public void clear() {
        this.cache.values().forEach(p_120201_ -> p_120201_.thenAccept(SoundBuffer::discardAlBuffer));
        this.cache.clear();
    }

    public CompletableFuture<?> preload(Collection<Sound> pSounds) {
        return CompletableFuture.allOf(pSounds.stream().map(p_120197_ -> this.getCompleteBuffer(p_120197_.getPath())).toArray(CompletableFuture[]::new));
    }
}