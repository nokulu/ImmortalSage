package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public record BeeDebugPayload(BeeDebugPayload.BeeInfo beeInfo) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, BeeDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
        BeeDebugPayload::write, BeeDebugPayload::new
    );
    public static final CustomPacketPayload.Type<BeeDebugPayload> TYPE = CustomPacketPayload.createType("debug/bee");

    private BeeDebugPayload(FriendlyByteBuf p_297658_) {
        this(new BeeDebugPayload.BeeInfo(p_297658_));
    }

    private void write(FriendlyByteBuf p_300191_) {
        this.beeInfo.write(p_300191_);
    }

    @Override
    public CustomPacketPayload.Type<BeeDebugPayload> type() {
        return TYPE;
    }

    public static record BeeInfo(
        UUID uuid,
        int id,
        Vec3 pos,
        @Nullable Path path,
        @Nullable BlockPos hivePos,
        @Nullable BlockPos flowerPos,
        int travelTicks,
        Set<String> goals,
        List<BlockPos> blacklistedHives
    ) {
        public BeeInfo(FriendlyByteBuf pBuffer) {
            this(
                pBuffer.readUUID(),
                pBuffer.readInt(),
                pBuffer.readVec3(),
                pBuffer.readNullable(Path::createFromStream),
                pBuffer.readNullable(BlockPos.STREAM_CODEC),
                pBuffer.readNullable(BlockPos.STREAM_CODEC),
                pBuffer.readInt(),
                pBuffer.readCollection(HashSet::new, FriendlyByteBuf::readUtf),
                pBuffer.readList(BlockPos.STREAM_CODEC)
            );
        }

        public void write(FriendlyByteBuf pBuffer) {
            pBuffer.writeUUID(this.uuid);
            pBuffer.writeInt(this.id);
            pBuffer.writeVec3(this.pos);
            pBuffer.writeNullable(this.path, (p_297580_, p_297572_) -> p_297572_.writeToStream(p_297580_));
            pBuffer.writeNullable(this.hivePos, BlockPos.STREAM_CODEC);
            pBuffer.writeNullable(this.flowerPos, BlockPos.STREAM_CODEC);
            pBuffer.writeInt(this.travelTicks);
            pBuffer.writeCollection(this.goals, FriendlyByteBuf::writeUtf);
            pBuffer.writeCollection(this.blacklistedHives, BlockPos.STREAM_CODEC);
        }

        public boolean hasHive(BlockPos pPos) {
            return Objects.equals(pPos, this.hivePos);
        }

        public String generateName() {
            return DebugEntityNameGenerator.getEntityName(this.uuid);
        }

        @Override
        public String toString() {
            return this.generateName();
        }
    }
}