package com.mojang.blaze3d.vertex;

import java.nio.ByteOrder;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class BufferBuilder implements VertexConsumer {
    private static final long NOT_BUILDING = -1L;
    private static final long UNKNOWN_ELEMENT = -1L;
    private static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
    private final ByteBufferBuilder buffer;
    private long vertexPointer = -1L;
    private int vertices;
    private final VertexFormat format;
    private final VertexFormat.Mode mode;
    private final boolean fastFormat;
    private final boolean fullFormat;
    private final int vertexSize;
    private final int initialElementsToFill;
    private final int[] offsetsByElement;
    private int elementsToFill;
    private boolean building = true;

    public BufferBuilder(ByteBufferBuilder pBuffer, VertexFormat.Mode pMode, VertexFormat pFormat) {
        if (!pFormat.contains(VertexFormatElement.POSITION)) {
            throw new IllegalArgumentException("Cannot build mesh with no position element");
        } else {
            this.buffer = pBuffer;
            this.mode = pMode;
            this.format = pFormat;
            this.vertexSize = pFormat.getVertexSize();
            this.initialElementsToFill = pFormat.getElementsMask() & ~VertexFormatElement.POSITION.mask();
            this.offsetsByElement = pFormat.getOffsetsByElement();
            boolean flag = pFormat == DefaultVertexFormat.NEW_ENTITY;
            boolean flag1 = pFormat == DefaultVertexFormat.BLOCK;
            this.fastFormat = flag || flag1;
            this.fullFormat = flag;
        }
    }

    @Nullable
    public MeshData build() {
        this.ensureBuilding();
        this.endLastVertex();
        MeshData meshdata = this.storeMesh();
        this.building = false;
        this.vertexPointer = -1L;
        return meshdata;
    }

    public MeshData buildOrThrow() {
        MeshData meshdata = this.build();
        if (meshdata == null) {
            throw new IllegalStateException("BufferBuilder was empty");
        } else {
            return meshdata;
        }
    }

    private void ensureBuilding() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        }
    }

    @Nullable
    private MeshData storeMesh() {
        if (this.vertices == 0) {
            return null;
        } else {
            ByteBufferBuilder.Result bytebufferbuilder$result = this.buffer.build();
            if (bytebufferbuilder$result == null) {
                return null;
            } else {
                int i = this.mode.indexCount(this.vertices);
                VertexFormat.IndexType vertexformat$indextype = VertexFormat.IndexType.least(this.vertices);
                return new MeshData(bytebufferbuilder$result, new MeshData.DrawState(this.format, this.vertices, i, this.mode, vertexformat$indextype));
            }
        }
    }

    private long beginVertex() {
        this.ensureBuilding();
        this.endLastVertex();
        this.vertices++;
        long i = this.buffer.reserve(this.vertexSize);
        this.vertexPointer = i;
        return i;
    }

    private long beginElement(VertexFormatElement pElement) {
        int i = this.elementsToFill;
        int j = i & ~pElement.mask();
        if (j == i) {
            return -1L;
        } else {
            this.elementsToFill = j;
            long k = this.vertexPointer;
            if (k == -1L) {
                throw new IllegalArgumentException("Not currently building vertex");
            } else {
                return k + (long)this.offsetsByElement[pElement.id()];
            }
        }
    }

    private void endLastVertex() {
        if (this.vertices != 0) {
            if (this.elementsToFill != 0) {
                String s = VertexFormatElement.elementsFromMask(this.elementsToFill).map(this.format::getElementName).collect(Collectors.joining(", "));
                throw new IllegalStateException("Missing elements in vertex: " + s);
            } else {
                if (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP) {
                    long i = this.buffer.reserve(this.vertexSize);
                    MemoryUtil.memCopy(i - (long)this.vertexSize, i, (long)this.vertexSize);
                    this.vertices++;
                }
            }
        }
    }

    private static void putRgba(long pPointer, int pColor) {
        int i = FastColor.ABGR32.fromArgb32(pColor);
        MemoryUtil.memPutInt(pPointer, IS_LITTLE_ENDIAN ? i : Integer.reverseBytes(i));
    }

    private static void putPackedUv(long pPointer, int pPackedUv) {
        if (IS_LITTLE_ENDIAN) {
            MemoryUtil.memPutInt(pPointer, pPackedUv);
        } else {
            MemoryUtil.memPutShort(pPointer, (short)(pPackedUv & 65535));
            MemoryUtil.memPutShort(pPointer + 2L, (short)(pPackedUv >> 16 & 65535));
        }
    }

    @Override
    public VertexConsumer addVertex(float pX, float pY, float pZ) {
        long i = this.beginVertex() + (long)this.offsetsByElement[VertexFormatElement.POSITION.id()];
        this.elementsToFill = this.initialElementsToFill;
        MemoryUtil.memPutFloat(i, pX);
        MemoryUtil.memPutFloat(i + 4L, pY);
        MemoryUtil.memPutFloat(i + 8L, pZ);
        return this;
    }

    @Override
    public VertexConsumer setColor(int pRed, int pGreen, int pBlue, int pAlpha) {
        long i = this.beginElement(VertexFormatElement.COLOR);
        if (i != -1L) {
            MemoryUtil.memPutByte(i, (byte)pRed);
            MemoryUtil.memPutByte(i + 1L, (byte)pGreen);
            MemoryUtil.memPutByte(i + 2L, (byte)pBlue);
            MemoryUtil.memPutByte(i + 3L, (byte)pAlpha);
        }

        return this;
    }

    @Override
    public VertexConsumer setColor(int pColor) {
        long i = this.beginElement(VertexFormatElement.COLOR);
        if (i != -1L) {
            putRgba(i, pColor);
        }

        return this;
    }

    @Override
    public VertexConsumer setUv(float pU, float pV) {
        long i = this.beginElement(VertexFormatElement.UV0);
        if (i != -1L) {
            MemoryUtil.memPutFloat(i, pU);
            MemoryUtil.memPutFloat(i + 4L, pV);
        }

        return this;
    }

    @Override
    public VertexConsumer setUv1(int pU, int pV) {
        return this.uvShort((short)pU, (short)pV, VertexFormatElement.UV1);
    }

    @Override
    public VertexConsumer setOverlay(int pPackedOverlay) {
        long i = this.beginElement(VertexFormatElement.UV1);
        if (i != -1L) {
            putPackedUv(i, pPackedOverlay);
        }

        return this;
    }

    @Override
    public VertexConsumer setUv2(int pU, int pV) {
        return this.uvShort((short)pU, (short)pV, VertexFormatElement.UV2);
    }

    @Override
    public VertexConsumer setLight(int pPackedLight) {
        long i = this.beginElement(VertexFormatElement.UV2);
        if (i != -1L) {
            putPackedUv(i, pPackedLight);
        }

        return this;
    }

    private VertexConsumer uvShort(short pU, short pV, VertexFormatElement pElement) {
        long i = this.beginElement(pElement);
        if (i != -1L) {
            MemoryUtil.memPutShort(i, pU);
            MemoryUtil.memPutShort(i + 2L, pV);
        }

        return this;
    }

    @Override
    public VertexConsumer setNormal(float pNormalX, float pNormalY, float pNormalZ) {
        long i = this.beginElement(VertexFormatElement.NORMAL);
        if (i != -1L) {
            MemoryUtil.memPutByte(i, normalIntValue(pNormalX));
            MemoryUtil.memPutByte(i + 1L, normalIntValue(pNormalY));
            MemoryUtil.memPutByte(i + 2L, normalIntValue(pNormalZ));
        }

        return this;
    }

    private static byte normalIntValue(float pValue) {
        return (byte)((int)(Mth.clamp(pValue, -1.0F, 1.0F) * 127.0F) & 0xFF);
    }

    @Override
    public void addVertex(
        float pX,
        float pY,
        float pZ,
        int pColor,
        float pU,
        float pV,
        int pPackedOverlay,
        int pPackedLight,
        float pNormalX,
        float pNormalY,
        float pNormalZ
    ) {
        if (this.fastFormat) {
            long i = this.beginVertex();
            MemoryUtil.memPutFloat(i + 0L, pX);
            MemoryUtil.memPutFloat(i + 4L, pY);
            MemoryUtil.memPutFloat(i + 8L, pZ);
            putRgba(i + 12L, pColor);
            MemoryUtil.memPutFloat(i + 16L, pU);
            MemoryUtil.memPutFloat(i + 20L, pV);
            long j;
            if (this.fullFormat) {
                putPackedUv(i + 24L, pPackedOverlay);
                j = i + 28L;
            } else {
                j = i + 24L;
            }

            putPackedUv(j + 0L, pPackedLight);
            MemoryUtil.memPutByte(j + 4L, normalIntValue(pNormalX));
            MemoryUtil.memPutByte(j + 5L, normalIntValue(pNormalY));
            MemoryUtil.memPutByte(j + 6L, normalIntValue(pNormalZ));
        } else {
            VertexConsumer.super.addVertex(
                pX, pY, pZ, pColor, pU, pV, pPackedOverlay, pPackedLight, pNormalX, pNormalY, pNormalZ
            );
        }
    }
}