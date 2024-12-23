package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class ClientboundSoundEntityPacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSoundEntityPacket> STREAM_CODEC = Packet.codec(
        ClientboundSoundEntityPacket::write, ClientboundSoundEntityPacket::new
    );
    private final Holder<SoundEvent> sound;
    private final SoundSource source;
    private final int id;
    private final float volume;
    private final float pitch;
    private final long seed;

    public ClientboundSoundEntityPacket(Holder<SoundEvent> pSound, SoundSource pSource, Entity pEntity, float pVolume, float pPitch, long pSeed) {
        this.sound = pSound;
        this.source = pSource;
        this.id = pEntity.getId();
        this.volume = pVolume;
        this.pitch = pPitch;
        this.seed = pSeed;
    }

    private ClientboundSoundEntityPacket(RegistryFriendlyByteBuf p_329519_) {
        this.sound = SoundEvent.STREAM_CODEC.decode(p_329519_);
        this.source = p_329519_.readEnum(SoundSource.class);
        this.id = p_329519_.readVarInt();
        this.volume = p_329519_.readFloat();
        this.pitch = p_329519_.readFloat();
        this.seed = p_329519_.readLong();
    }

    private void write(RegistryFriendlyByteBuf p_332294_) {
        SoundEvent.STREAM_CODEC.encode(p_332294_, this.sound);
        p_332294_.writeEnum(this.source);
        p_332294_.writeVarInt(this.id);
        p_332294_.writeFloat(this.volume);
        p_332294_.writeFloat(this.pitch);
        p_332294_.writeLong(this.seed);
    }

    @Override
    public PacketType<ClientboundSoundEntityPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SOUND_ENTITY;
    }

    public void handle(ClientGamePacketListener pHandler) {
        pHandler.handleSoundEntityEvent(this);
    }

    public Holder<SoundEvent> getSound() {
        return this.sound;
    }

    public SoundSource getSource() {
        return this.source;
    }

    public int getId() {
        return this.id;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public long getSeed() {
        return this.seed;
    }
}