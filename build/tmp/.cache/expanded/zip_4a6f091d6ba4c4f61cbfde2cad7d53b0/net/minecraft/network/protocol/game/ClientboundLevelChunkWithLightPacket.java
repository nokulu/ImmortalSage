package net.minecraft.network.protocol.game;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLevelChunkWithLightPacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLevelChunkWithLightPacket> STREAM_CODEC = Packet.codec(
        ClientboundLevelChunkWithLightPacket::write, ClientboundLevelChunkWithLightPacket::new
    );
    private final int x;
    private final int z;
    private final ClientboundLevelChunkPacketData chunkData;
    private final ClientboundLightUpdatePacketData lightData;

    public ClientboundLevelChunkWithLightPacket(LevelChunk pChunk, LevelLightEngine pLightEngine, @Nullable BitSet pSkyLight, @Nullable BitSet pBlockLight) {
        ChunkPos chunkpos = pChunk.getPos();
        this.x = chunkpos.x;
        this.z = chunkpos.z;
        this.chunkData = new ClientboundLevelChunkPacketData(pChunk);
        this.lightData = new ClientboundLightUpdatePacketData(chunkpos, pLightEngine, pSkyLight, pBlockLight);
    }

    private ClientboundLevelChunkWithLightPacket(RegistryFriendlyByteBuf p_331782_) {
        this.x = p_331782_.readInt();
        this.z = p_331782_.readInt();
        this.chunkData = new ClientboundLevelChunkPacketData(p_331782_, this.x, this.z);
        this.lightData = new ClientboundLightUpdatePacketData(p_331782_, this.x, this.z);
    }

    private void write(RegistryFriendlyByteBuf p_327690_) {
        p_327690_.writeInt(this.x);
        p_327690_.writeInt(this.z);
        this.chunkData.write(p_327690_);
        this.lightData.write(p_327690_);
    }

    @Override
    public PacketType<ClientboundLevelChunkWithLightPacket> type() {
        return GamePacketTypes.CLIENTBOUND_LEVEL_CHUNK_WITH_LIGHT;
    }

    public void handle(ClientGamePacketListener pHandler) {
        pHandler.handleLevelChunkWithLight(this);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public ClientboundLevelChunkPacketData getChunkData() {
        return this.chunkData;
    }

    public ClientboundLightUpdatePacketData getLightData() {
        return this.lightData;
    }
}