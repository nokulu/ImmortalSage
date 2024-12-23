package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VertexFormat implements net.minecraftforge.client.extensions.IForgeVertexFormat {
    public static final int UNKNOWN_ELEMENT = -1;
    private final List<VertexFormatElement> elements;
    private final List<String> names;
    private final int vertexSize;
    private final int elementsMask;
    private final int[] offsetsByElement = new int[32];
    @Nullable
    private VertexBuffer immediateDrawVertexBuffer;
    private final com.google.common.collect.ImmutableMap<String, VertexFormatElement> elementMapping;

    VertexFormat(List<VertexFormatElement> pElements, List<String> pNames, IntList pOffsets, int pVertexSize) {
        this.elements = pElements;
        this.names = pNames;
        this.vertexSize = pVertexSize;
        this.elementsMask = pElements.stream().mapToInt(VertexFormatElement::mask).reduce(0, (p_344142_, p_345074_) -> p_344142_ | p_345074_);

        for (int i = 0; i < this.offsetsByElement.length; i++) {
            VertexFormatElement vertexformatelement = VertexFormatElement.byId(i);
            int j = vertexformatelement != null ? pElements.indexOf(vertexformatelement) : -1;
            this.offsetsByElement[i] = j != -1 ? pOffsets.getInt(j) : -1;
        }

        ImmutableMap.Builder<String, VertexFormatElement> elements = ImmutableMap.builder();
        for (int i = 0; i < pElements.size(); i++)
            elements.put(pNames.get(i), pElements.get(i));
        this.elementMapping = elements.buildOrThrow();
    }

    public static VertexFormat.Builder builder() {
        return new VertexFormat.Builder();
    }

    @Override
    public String toString() {
        StringBuilder stringbuilder = new StringBuilder("Vertex format (").append(this.vertexSize).append(" bytes):\n");

        for (int i = 0; i < this.elements.size(); i++) {
            VertexFormatElement vertexformatelement = this.elements.get(i);
            stringbuilder.append(i)
                .append(". ")
                .append(this.names.get(i))
                .append(": ")
                .append(vertexformatelement)
                .append(" @ ")
                .append(this.getOffset(vertexformatelement))
                .append('\n');
        }

        return stringbuilder.toString();
    }

    public int getVertexSize() {
        return this.vertexSize;
    }

    public List<VertexFormatElement> getElements() {
        return this.elements;
    }

    public List<String> getElementAttributeNames() {
        return this.names;
    }

    public int[] getOffsetsByElement() {
        return this.offsetsByElement;
    }

    public int getOffset(VertexFormatElement pElement) {
        return this.offsetsByElement[pElement.id()];
    }

    public boolean contains(VertexFormatElement pElement) {
        return (this.elementsMask & pElement.mask()) != 0;
    }

    public int getElementsMask() {
        return this.elementsMask;
    }

    public String getElementName(VertexFormatElement pElement) {
        int i = this.elements.indexOf(pElement);
        if (i == -1) {
            throw new IllegalArgumentException(pElement + " is not contained in format");
        } else {
            return this.names.get(i);
        }
    }

    @Override
    public boolean equals(Object pOther) {
        if (this == pOther) {
            return true;
        } else {
            if (pOther instanceof VertexFormat vertexformat
                && this.elementsMask == vertexformat.elementsMask
                && this.vertexSize == vertexformat.vertexSize
                && this.names.equals(vertexformat.names)
                && Arrays.equals(this.offsetsByElement, vertexformat.offsetsByElement)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.elementsMask * 31 + Arrays.hashCode(this.offsetsByElement);
    }

    public void setupBufferState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::_setupBufferState);
        } else {
            this._setupBufferState();
        }
    }

    private void _setupBufferState() {
        int i = this.getVertexSize();

        for (int j = 0; j < this.elements.size(); j++) {
            GlStateManager._enableVertexAttribArray(j);
            VertexFormatElement vertexformatelement = this.elements.get(j);
            vertexformatelement.setupBufferState(j, (long)this.getOffset(vertexformatelement), i);
        }
    }

    public void clearBufferState() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::_clearBufferState);
        } else {
            this._clearBufferState();
        }
    }

    private void _clearBufferState() {
        for (int i = 0; i < this.elements.size(); i++) {
            GlStateManager._disableVertexAttribArray(i);
        }
    }

    public VertexBuffer getImmediateDrawVertexBuffer() {
        VertexBuffer vertexbuffer = this.immediateDrawVertexBuffer;
        if (vertexbuffer == null) {
            this.immediateDrawVertexBuffer = vertexbuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        }

        return vertexbuffer;
    }

    public ImmutableMap<String, VertexFormatElement> getElementMapping() { return elementMapping; }
    public int getOffset(int index) { return offsetsByElement[index]; }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final ImmutableMap.Builder<String, VertexFormatElement> elements = ImmutableMap.builder();
        private final IntList offsets = new IntArrayList();
        private int offset;

        Builder() {
        }

        public VertexFormat.Builder add(String pName, VertexFormatElement pElement) {
            this.elements.put(pName, pElement);
            this.offsets.add(this.offset);
            this.offset = this.offset + pElement.byteSize();
            return this;
        }

        public VertexFormat.Builder padding(int pPadding) {
            this.offset += pPadding;
            return this;
        }

        public VertexFormat build() {
            ImmutableMap<String, VertexFormatElement> immutablemap = this.elements.buildOrThrow();
            ImmutableList<VertexFormatElement> immutablelist = immutablemap.values().asList();
            ImmutableList<String> immutablelist1 = immutablemap.keySet().asList();
            return new VertexFormat(immutablelist, immutablelist1, this.offsets, this.offset);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum IndexType {
        SHORT(5123, 2),
        INT(5125, 4);

        public final int asGLType;
        public final int bytes;

        private IndexType(final int pAsGLType, final int pBytes) {
            this.asGLType = pAsGLType;
            this.bytes = pBytes;
        }

        public static VertexFormat.IndexType least(int pIndexCount) {
            return (pIndexCount & -65536) != 0 ? INT : SHORT;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Mode {
        LINES(4, 2, 2, false),
        LINE_STRIP(5, 2, 1, true),
        DEBUG_LINES(1, 2, 2, false),
        DEBUG_LINE_STRIP(3, 2, 1, true),
        TRIANGLES(4, 3, 3, false),
        TRIANGLE_STRIP(5, 3, 1, true),
        TRIANGLE_FAN(6, 3, 1, true),
        QUADS(4, 4, 4, false);

        public final int asGLMode;
        public final int primitiveLength;
        public final int primitiveStride;
        public final boolean connectedPrimitives;

        private Mode(final int pAsGLMode, final int pPrimitiveLength, final int pPrimitiveStride, final boolean pConnectedPrimitives) {
            this.asGLMode = pAsGLMode;
            this.primitiveLength = pPrimitiveLength;
            this.primitiveStride = pPrimitiveStride;
            this.connectedPrimitives = pConnectedPrimitives;
        }

        public int indexCount(int pVertices) {
            return switch (this) {
                case LINES, QUADS -> pVertices / 4 * 6;
                case LINE_STRIP, DEBUG_LINES, DEBUG_LINE_STRIP, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN -> pVertices;
                default -> 0;
            };
        }
    }
}
