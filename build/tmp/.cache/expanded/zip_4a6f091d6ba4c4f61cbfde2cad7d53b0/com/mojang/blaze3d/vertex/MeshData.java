package com.mojang.blaze3d.vertex;

import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableLong;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class MeshData implements AutoCloseable {
    private final ByteBufferBuilder.Result vertexBuffer;
    @Nullable
    private ByteBufferBuilder.Result indexBuffer;
    private final MeshData.DrawState drawState;

    public MeshData(ByteBufferBuilder.Result pVertexBuffer, MeshData.DrawState pDrawState) {
        this.vertexBuffer = pVertexBuffer;
        this.drawState = pDrawState;
    }

    private static Vector3f[] unpackQuadCentroids(ByteBuffer pByteBuffer, int pVertexCount, VertexFormat pFormat) {
        int i = pFormat.getOffset(VertexFormatElement.POSITION);
        if (i == -1) {
            throw new IllegalArgumentException("Cannot identify quad centers with no position element");
        } else {
            FloatBuffer floatbuffer = pByteBuffer.asFloatBuffer();
            int j = pFormat.getVertexSize() / 4;
            int k = j * 4;
            int l = pVertexCount / 4;
            Vector3f[] avector3f = new Vector3f[l];

            for (int i1 = 0; i1 < l; i1++) {
                int j1 = i1 * k + i;
                int k1 = j1 + j * 2;
                float f = floatbuffer.get(j1 + 0);
                float f1 = floatbuffer.get(j1 + 1);
                float f2 = floatbuffer.get(j1 + 2);
                float f3 = floatbuffer.get(k1 + 0);
                float f4 = floatbuffer.get(k1 + 1);
                float f5 = floatbuffer.get(k1 + 2);
                avector3f[i1] = new Vector3f((f + f3) / 2.0F, (f1 + f4) / 2.0F, (f2 + f5) / 2.0F);
            }

            return avector3f;
        }
    }

    public ByteBuffer vertexBuffer() {
        return this.vertexBuffer.byteBuffer();
    }

    @Nullable
    public ByteBuffer indexBuffer() {
        return this.indexBuffer != null ? this.indexBuffer.byteBuffer() : null;
    }

    public MeshData.DrawState drawState() {
        return this.drawState;
    }

    @Nullable
    public MeshData.SortState sortQuads(ByteBufferBuilder pBufferBuilder, VertexSorting pSorting) {
        if (this.drawState.mode() != VertexFormat.Mode.QUADS) {
            return null;
        } else {
            Vector3f[] avector3f = unpackQuadCentroids(this.vertexBuffer.byteBuffer(), this.drawState.vertexCount(), this.drawState.format());
            MeshData.SortState meshdata$sortstate = new MeshData.SortState(avector3f, this.drawState.indexType());
            this.indexBuffer = meshdata$sortstate.buildSortedIndexBuffer(pBufferBuilder, pSorting);
            return meshdata$sortstate;
        }
    }

    @Override
    public void close() {
        this.vertexBuffer.close();
        if (this.indexBuffer != null) {
            this.indexBuffer.close();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record DrawState(VertexFormat format, int vertexCount, int indexCount, VertexFormat.Mode mode, VertexFormat.IndexType indexType) {
    }

    @OnlyIn(Dist.CLIENT)
    public static record SortState(Vector3f[] centroids, VertexFormat.IndexType indexType) {
        @Nullable
        public ByteBufferBuilder.Result buildSortedIndexBuffer(ByteBufferBuilder pBufferBuilder, VertexSorting pSorting) {
            int[] aint = pSorting.sort(this.centroids);
            long i = pBufferBuilder.reserve(aint.length * 6 * this.indexType.bytes);
            IntConsumer intconsumer = this.indexWriter(i, this.indexType);

            for (int j : aint) {
                intconsumer.accept(j * 4 + 0);
                intconsumer.accept(j * 4 + 1);
                intconsumer.accept(j * 4 + 2);
                intconsumer.accept(j * 4 + 2);
                intconsumer.accept(j * 4 + 3);
                intconsumer.accept(j * 4 + 0);
            }

            return pBufferBuilder.build();
        }

        private IntConsumer indexWriter(long pIndex, VertexFormat.IndexType pType) {
            MutableLong mutablelong = new MutableLong(pIndex);

            return switch (pType) {
                case SHORT -> p_344551_ -> MemoryUtil.memPutShort(mutablelong.getAndAdd(2L), (short)p_344551_);
                case INT -> p_342795_ -> MemoryUtil.memPutInt(mutablelong.getAndAdd(4L), p_342795_);
            };
        }
    }
}