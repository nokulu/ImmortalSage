package net.minecraft.client.sounds;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import java.io.IOException;
import java.nio.ByteBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface FloatSampleSource extends FiniteAudioStream {
    int EXPECTED_MAX_FRAME_SIZE = 8192;

    boolean readChunk(FloatConsumer pOutput) throws IOException;

    @Override
    default ByteBuffer read(int pSize) throws IOException {
        ChunkedSampleByteBuf chunkedsamplebytebuf = new ChunkedSampleByteBuf(pSize + 8192);

        while (this.readChunk(chunkedsamplebytebuf) && chunkedsamplebytebuf.size() < pSize) {
        }

        return chunkedsamplebytebuf.get();
    }

    @Override
    default ByteBuffer readAll() throws IOException {
        ChunkedSampleByteBuf chunkedsamplebytebuf = new ChunkedSampleByteBuf(16384);

        while (this.readChunk(chunkedsamplebytebuf)) {
        }

        return chunkedsamplebytebuf.get();
    }
}