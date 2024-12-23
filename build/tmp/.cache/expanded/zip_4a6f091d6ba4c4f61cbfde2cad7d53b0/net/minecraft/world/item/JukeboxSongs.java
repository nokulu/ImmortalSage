package net.minecraft.world.item;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public interface JukeboxSongs {
    ResourceKey<JukeboxSong> THIRTEEN = create("13");
    ResourceKey<JukeboxSong> CAT = create("cat");
    ResourceKey<JukeboxSong> BLOCKS = create("blocks");
    ResourceKey<JukeboxSong> CHIRP = create("chirp");
    ResourceKey<JukeboxSong> FAR = create("far");
    ResourceKey<JukeboxSong> MALL = create("mall");
    ResourceKey<JukeboxSong> MELLOHI = create("mellohi");
    ResourceKey<JukeboxSong> STAL = create("stal");
    ResourceKey<JukeboxSong> STRAD = create("strad");
    ResourceKey<JukeboxSong> WARD = create("ward");
    ResourceKey<JukeboxSong> ELEVEN = create("11");
    ResourceKey<JukeboxSong> WAIT = create("wait");
    ResourceKey<JukeboxSong> PIGSTEP = create("pigstep");
    ResourceKey<JukeboxSong> OTHERSIDE = create("otherside");
    ResourceKey<JukeboxSong> FIVE = create("5");
    ResourceKey<JukeboxSong> RELIC = create("relic");
    ResourceKey<JukeboxSong> PRECIPICE = create("precipice");
    ResourceKey<JukeboxSong> CREATOR = create("creator");
    ResourceKey<JukeboxSong> CREATOR_MUSIC_BOX = create("creator_music_box");

    private static ResourceKey<JukeboxSong> create(String pName) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.withDefaultNamespace(pName));
    }

    private static void register(
        BootstrapContext<JukeboxSong> pContext, ResourceKey<JukeboxSong> pKey, Holder.Reference<SoundEvent> pSoundEvent, int pLengthInSeconds, int pComparatorOutput
    ) {
        pContext.register(
            pKey, new JukeboxSong(pSoundEvent, Component.translatable(Util.makeDescriptionId("jukebox_song", pKey.location())), (float)pLengthInSeconds, pComparatorOutput)
        );
    }

    static void bootstrap(BootstrapContext<JukeboxSong> pContext) {
        register(pContext, THIRTEEN, SoundEvents.MUSIC_DISC_13, 178, 1);
        register(pContext, CAT, SoundEvents.MUSIC_DISC_CAT, 185, 2);
        register(pContext, BLOCKS, SoundEvents.MUSIC_DISC_BLOCKS, 345, 3);
        register(pContext, CHIRP, SoundEvents.MUSIC_DISC_CHIRP, 185, 4);
        register(pContext, FAR, SoundEvents.MUSIC_DISC_FAR, 174, 5);
        register(pContext, MALL, SoundEvents.MUSIC_DISC_MALL, 197, 6);
        register(pContext, MELLOHI, SoundEvents.MUSIC_DISC_MELLOHI, 96, 7);
        register(pContext, STAL, SoundEvents.MUSIC_DISC_STAL, 150, 8);
        register(pContext, STRAD, SoundEvents.MUSIC_DISC_STRAD, 188, 9);
        register(pContext, WARD, SoundEvents.MUSIC_DISC_WARD, 251, 10);
        register(pContext, ELEVEN, SoundEvents.MUSIC_DISC_11, 71, 11);
        register(pContext, WAIT, SoundEvents.MUSIC_DISC_WAIT, 238, 12);
        register(pContext, PIGSTEP, SoundEvents.MUSIC_DISC_PIGSTEP, 149, 13);
        register(pContext, OTHERSIDE, SoundEvents.MUSIC_DISC_OTHERSIDE, 195, 14);
        register(pContext, FIVE, SoundEvents.MUSIC_DISC_5, 178, 15);
        register(pContext, RELIC, SoundEvents.MUSIC_DISC_RELIC, 218, 14);
        register(pContext, PRECIPICE, SoundEvents.MUSIC_DISC_PRECIPICE, 299, 13);
        register(pContext, CREATOR, SoundEvents.MUSIC_DISC_CREATOR, 176, 12);
        register(pContext, CREATOR_MUSIC_BOX, SoundEvents.MUSIC_DISC_CREATOR_MUSIC_BOX, 73, 11);
    }
}