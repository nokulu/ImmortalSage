package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.slf4j.Logger;

public interface ListOperation {
    MapCodec<ListOperation> UNLIMITED_CODEC = codec(Integer.MAX_VALUE);

    static MapCodec<ListOperation> codec(int pMaxSize) {
        return ListOperation.Type.CODEC
            .<ListOperation>dispatchMap("mode", ListOperation::mode, p_328481_ -> p_328481_.mapCodec)
            .validate(p_336261_ -> {
                if (p_336261_ instanceof ListOperation.ReplaceSection listoperation$replacesection && listoperation$replacesection.size().isPresent()) {
                    int i = listoperation$replacesection.size().get();
                    if (i > pMaxSize) {
                        return DataResult.error(() -> "Size value too large: " + i + ", max size is " + pMaxSize);
                    }
                }

                return DataResult.success(p_336261_);
            });
    }

    ListOperation.Type mode();

    default <T> List<T> apply(List<T> pCurrentValue, List<T> pOperand) {
        return this.apply(pCurrentValue, pOperand, Integer.MAX_VALUE);
    }

    <T> List<T> apply(List<T> pCurrentValue, List<T> pOperand, int pMaxSize);

    public static class Append implements ListOperation {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final ListOperation.Append INSTANCE = new ListOperation.Append();
        public static final MapCodec<ListOperation.Append> MAP_CODEC = MapCodec.unit(() -> INSTANCE);

        private Append() {
        }

        @Override
        public ListOperation.Type mode() {
            return ListOperation.Type.APPEND;
        }

        @Override
        public <T> List<T> apply(List<T> p_330728_, List<T> p_331859_, int p_335288_) {
            if (p_330728_.size() + p_331859_.size() > p_335288_) {
                LOGGER.error("Contents overflow in section append");
                return p_330728_;
            } else {
                return Stream.concat(p_330728_.stream(), p_331859_.stream()).toList();
            }
        }
    }

    public static record Insert(int offset) implements ListOperation {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<ListOperation.Insert> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_329650_ -> p_329650_.group(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("offset", 0).forGetter(ListOperation.Insert::offset))
                    .apply(p_329650_, ListOperation.Insert::new)
        );

        @Override
        public ListOperation.Type mode() {
            return ListOperation.Type.INSERT;
        }

        @Override
        public <T> List<T> apply(List<T> pCurrentValue, List<T> pOperand, int pMaxSize) {
            int i = pCurrentValue.size();
            if (this.offset > i) {
                LOGGER.error("Cannot insert when offset is out of bounds");
                return pCurrentValue;
            } else if (i + pOperand.size() > pMaxSize) {
                LOGGER.error("Contents overflow in section insertion");
                return pCurrentValue;
            } else {
                Builder<T> builder = ImmutableList.builder();
                builder.addAll(pCurrentValue.subList(0, this.offset));
                builder.addAll(pOperand);
                builder.addAll(pCurrentValue.subList(this.offset, i));
                return builder.build();
            }
        }
    }

    public static class ReplaceAll implements ListOperation {
        public static final ListOperation.ReplaceAll INSTANCE = new ListOperation.ReplaceAll();
        public static final MapCodec<ListOperation.ReplaceAll> MAP_CODEC = MapCodec.unit(() -> INSTANCE);

        private ReplaceAll() {
        }

        @Override
        public ListOperation.Type mode() {
            return ListOperation.Type.REPLACE_ALL;
        }

        @Override
        public <T> List<T> apply(List<T> p_333557_, List<T> p_331455_, int p_335044_) {
            return p_331455_;
        }
    }

    public static record ReplaceSection(int offset, Optional<Integer> size) implements ListOperation {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<ListOperation.ReplaceSection> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_332380_ -> p_332380_.group(
                        ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("offset", 0).forGetter(ListOperation.ReplaceSection::offset),
                        ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("size").forGetter(ListOperation.ReplaceSection::size)
                    )
                    .apply(p_332380_, ListOperation.ReplaceSection::new)
        );

        public ReplaceSection(int pOffset) {
            this(pOffset, Optional.empty());
        }

        @Override
        public ListOperation.Type mode() {
            return ListOperation.Type.REPLACE_SECTION;
        }

        @Override
        public <T> List<T> apply(List<T> pCurrentValue, List<T> pOperand, int pMaxSize) {
            int i = pCurrentValue.size();
            if (this.offset > i) {
                LOGGER.error("Cannot replace when offset is out of bounds");
                return pCurrentValue;
            } else {
                Builder<T> builder = ImmutableList.builder();
                builder.addAll(pCurrentValue.subList(0, this.offset));
                builder.addAll(pOperand);
                int j = this.offset + this.size.orElse(pOperand.size());
                if (j < i) {
                    builder.addAll(pCurrentValue.subList(j, i));
                }

                List<T> list = builder.build();
                if (list.size() > pMaxSize) {
                    LOGGER.error("Contents overflow in section replacement");
                    return pCurrentValue;
                } else {
                    return list;
                }
            }
        }
    }

    public static record StandAlone<T>(List<T> value, ListOperation operation) {
        public static <T> Codec<ListOperation.StandAlone<T>> codec(Codec<T> pElementCodec, int pMaxSize) {
            return RecordCodecBuilder.create(
                p_334562_ -> p_334562_.group(
                            pElementCodec.sizeLimitedListOf(pMaxSize).fieldOf("values").forGetter(p_331378_ -> p_331378_.value),
                            ListOperation.codec(pMaxSize).forGetter(p_330703_ -> p_330703_.operation)
                        )
                        .apply(p_334562_, ListOperation.StandAlone::new)
            );
        }

        public List<T> apply(List<T> pList) {
            return this.operation.apply(pList, this.value);
        }
    }

    public static enum Type implements StringRepresentable {
        REPLACE_ALL("replace_all", ListOperation.ReplaceAll.MAP_CODEC),
        REPLACE_SECTION("replace_section", ListOperation.ReplaceSection.MAP_CODEC),
        INSERT("insert", ListOperation.Insert.MAP_CODEC),
        APPEND("append", ListOperation.Append.MAP_CODEC);

        public static final Codec<ListOperation.Type> CODEC = StringRepresentable.fromEnum(ListOperation.Type::values);
        private final String id;
        final MapCodec<? extends ListOperation> mapCodec;

        private Type(final String pId, final MapCodec<? extends ListOperation> pMapCodec) {
            this.id = pId;
            this.mapCodec = pMapCodec;
        }

        public MapCodec<? extends ListOperation> mapCodec() {
            return this.mapCodec;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }
}