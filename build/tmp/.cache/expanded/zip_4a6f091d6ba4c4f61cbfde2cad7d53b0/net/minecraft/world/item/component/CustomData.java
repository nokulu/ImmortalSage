package net.minecraft.world.item.component;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

public final class CustomData {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CustomData EMPTY = new CustomData(new CompoundTag());
    public static final Codec<CustomData> CODEC = Codec.withAlternative(CompoundTag.CODEC, TagParser.AS_CODEC)
        .xmap(CustomData::new, p_327962_ -> p_327962_.tag);
    public static final Codec<CustomData> CODEC_WITH_ID = CODEC.validate(
        p_332921_ -> p_332921_.getUnsafe().contains("id", 8)
                ? DataResult.success(p_332921_)
                : DataResult.error(() -> "Missing id for entity in: " + p_332921_)
    );
    @Deprecated
    public static final StreamCodec<ByteBuf, CustomData> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(CustomData::new, p_329964_ -> p_329964_.tag);
    private final CompoundTag tag;

    private CustomData(CompoundTag p_331981_) {
        this.tag = p_331981_;
    }

    public static CustomData of(CompoundTag pTag) {
        return new CustomData(pTag.copy());
    }

    public static Predicate<ItemStack> itemMatcher(DataComponentType<CustomData> pComponentType, CompoundTag pTag) {
        return p_334391_ -> {
            CustomData customdata = p_334391_.getOrDefault(pComponentType, EMPTY);
            return customdata.matchedBy(pTag);
        };
    }

    public boolean matchedBy(CompoundTag pTag) {
        return NbtUtils.compareNbt(pTag, this.tag, true);
    }

    public static void update(DataComponentType<CustomData> pComponentType, ItemStack pStack, Consumer<CompoundTag> pUpdater) {
        CustomData customdata = pStack.getOrDefault(pComponentType, EMPTY).update(pUpdater);
        if (customdata.tag.isEmpty()) {
            pStack.remove(pComponentType);
        } else {
            pStack.set(pComponentType, customdata);
        }
    }

    public static void set(DataComponentType<CustomData> pComponentType, ItemStack pStack, CompoundTag pTag) {
        if (!pTag.isEmpty()) {
            pStack.set(pComponentType, of(pTag));
        } else {
            pStack.remove(pComponentType);
        }
    }

    public CustomData update(Consumer<CompoundTag> pUpdater) {
        CompoundTag compoundtag = this.tag.copy();
        pUpdater.accept(compoundtag);
        return new CustomData(compoundtag);
    }

    public void loadInto(Entity pEntity) {
        CompoundTag compoundtag = pEntity.saveWithoutId(new CompoundTag());
        UUID uuid = pEntity.getUUID();
        compoundtag.merge(this.tag);
        pEntity.load(compoundtag);
        pEntity.setUUID(uuid);
    }

    public boolean loadInto(BlockEntity pBlockEntity, HolderLookup.Provider pLevelRegistry) {
        CompoundTag compoundtag = pBlockEntity.saveCustomOnly(pLevelRegistry);
        CompoundTag compoundtag1 = compoundtag.copy();
        compoundtag.merge(this.tag);
        if (!compoundtag.equals(compoundtag1)) {
            try {
                pBlockEntity.loadCustomOnly(compoundtag, pLevelRegistry);
                pBlockEntity.setChanged();
                return true;
            } catch (Exception exception1) {
                LOGGER.warn("Failed to apply custom data to block entity at {}", pBlockEntity.getBlockPos(), exception1);

                try {
                    pBlockEntity.loadCustomOnly(compoundtag1, pLevelRegistry);
                } catch (Exception exception) {
                    LOGGER.warn("Failed to rollback block entity at {} after failure", pBlockEntity.getBlockPos(), exception);
                }
            }
        }

        return false;
    }

    public <T> DataResult<CustomData> update(DynamicOps<Tag> pOps, MapEncoder<T> pEncoder, T pValue) {
        return pEncoder.encode(pValue, pOps, pOps.mapBuilder()).build(this.tag).map(p_327948_ -> new CustomData((CompoundTag)p_327948_));
    }

    public <T> DataResult<T> read(MapDecoder<T> pDecoder) {
        return this.read(NbtOps.INSTANCE, pDecoder);
    }

    public <T> DataResult<T> read(DynamicOps<Tag> pOps, MapDecoder<T> pDecoder) {
        MapLike<Tag> maplike = pOps.getMap(this.tag).getOrThrow();
        return pDecoder.decode(pOps, maplike);
    }

    public int size() {
        return this.tag.size();
    }

    public boolean isEmpty() {
        return this.tag.isEmpty();
    }

    public CompoundTag copyTag() {
        return this.tag.copy();
    }

    public boolean contains(String pKey) {
        return this.tag.contains(pKey);
    }

    @Override
    public boolean equals(Object pOther) {
        if (pOther == this) {
            return true;
        } else {
            return pOther instanceof CustomData customdata ? this.tag.equals(customdata.tag) : false;
        }
    }

    @Override
    public int hashCode() {
        return this.tag.hashCode();
    }

    @Override
    public String toString() {
        return this.tag.toString();
    }

    @Deprecated
    public CompoundTag getUnsafe() {
        return this.tag;
    }
}