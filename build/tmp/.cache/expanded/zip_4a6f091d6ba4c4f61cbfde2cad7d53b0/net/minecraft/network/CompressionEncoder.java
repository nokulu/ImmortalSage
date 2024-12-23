package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;

/**
 * Handles compression of network traffic.
 * 
 * @see Connection#setupCompression
 */
public class CompressionEncoder extends MessageToByteEncoder<ByteBuf> {
    private static final boolean DISABLE_PACKET_DEBUG = Boolean.parseBoolean(System.getProperty("forge.disablePacketCompressionDebug", "false"));
    private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();
    private final byte[] encodeBuf = new byte[8192];
    private final Deflater deflater;
    private int threshold;

    public CompressionEncoder(int pThreshold) {
        this.threshold = pThreshold;
        this.deflater = new Deflater();
    }

    protected void encode(ChannelHandlerContext pContext, ByteBuf pEncodingByteBuf, ByteBuf pByteBuf) {
        int i = pEncodingByteBuf.readableBytes();
        if (i > 8388608) {
            throw new IllegalArgumentException("Packet too big (is " + i + ", should be less than 8388608)");
        } else {
            if (i < this.threshold) {
                VarInt.write(pByteBuf, 0);
                pByteBuf.writeBytes(pEncodingByteBuf);
            } else {
                if (!DISABLE_PACKET_DEBUG && i > net.minecraft.network.CompressionDecoder.MAXIMUM_UNCOMPRESSED_LENGTH) {
                     pEncodingByteBuf.markReaderIndex();
                     LOGGER.error("Attempted to send packet over maximum protocol size: {} > {}\nData:\n{}", i, net.minecraft.network.CompressionDecoder.MAXIMUM_UNCOMPRESSED_LENGTH,
                            net.minecraftforge.common.util.HexDumper.dump(pEncodingByteBuf));
                     pEncodingByteBuf.resetReaderIndex();
                }
                byte[] abyte = new byte[i];
                pEncodingByteBuf.readBytes(abyte);
                VarInt.write(pByteBuf, abyte.length);
                this.deflater.setInput(abyte, 0, i);
                this.deflater.finish();

                while (!this.deflater.finished()) {
                    int j = this.deflater.deflate(this.encodeBuf);
                    pByteBuf.writeBytes(this.encodeBuf, 0, j);
                }

                this.deflater.reset();
            }
        }
    }

    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(int pThreshold) {
        this.threshold = pThreshold;
    }
}
