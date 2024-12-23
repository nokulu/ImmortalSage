package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record StructuresDebugPayload(ResourceKey<Level> dimension, BoundingBox mainBB, List<StructuresDebugPayload.PieceInfo> pieces)
    implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, StructuresDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
        StructuresDebugPayload::write, StructuresDebugPayload::new
    );
    public static final CustomPacketPayload.Type<StructuresDebugPayload> TYPE = CustomPacketPayload.createType("debug/structures");

    private StructuresDebugPayload(FriendlyByteBuf p_301247_) {
        this(p_301247_.readResourceKey(Registries.DIMENSION), readBoundingBox(p_301247_), p_301247_.readList(StructuresDebugPayload.PieceInfo::new));
    }

    private void write(FriendlyByteBuf p_300362_) {
        p_300362_.writeResourceKey(this.dimension);
        writeBoundingBox(p_300362_, this.mainBB);
        p_300362_.writeCollection(this.pieces, (p_300337_, p_299834_) -> p_299834_.write(p_300362_));
    }

    @Override
    public CustomPacketPayload.Type<StructuresDebugPayload> type() {
        return TYPE;
    }

    static BoundingBox readBoundingBox(FriendlyByteBuf pBuffer) {
        return new BoundingBox(pBuffer.readInt(), pBuffer.readInt(), pBuffer.readInt(), pBuffer.readInt(), pBuffer.readInt(), pBuffer.readInt());
    }

    static void writeBoundingBox(FriendlyByteBuf pBuffer, BoundingBox pBoundingBox) {
        pBuffer.writeInt(pBoundingBox.minX());
        pBuffer.writeInt(pBoundingBox.minY());
        pBuffer.writeInt(pBoundingBox.minZ());
        pBuffer.writeInt(pBoundingBox.maxX());
        pBuffer.writeInt(pBoundingBox.maxY());
        pBuffer.writeInt(pBoundingBox.maxZ());
    }

    public static record PieceInfo(BoundingBox boundingBox, boolean isStart) {
        public PieceInfo(FriendlyByteBuf pBuffer) {
            this(StructuresDebugPayload.readBoundingBox(pBuffer), pBuffer.readBoolean());
        }

        public void write(FriendlyByteBuf pBuffer) {
            StructuresDebugPayload.writeBoundingBox(pBuffer, this.boundingBox);
            pBuffer.writeBoolean(this.isStart);
        }
    }
}