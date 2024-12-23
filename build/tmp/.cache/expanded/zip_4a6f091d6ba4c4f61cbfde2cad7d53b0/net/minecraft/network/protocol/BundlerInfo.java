package net.minecraft.network.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.PacketListener;

public interface BundlerInfo {
    int BUNDLE_SIZE_LIMIT = 4096;

    static <T extends PacketListener, P extends BundlePacket<? super T>> BundlerInfo createForPacket(
        final PacketType<P> pType, final Function<Iterable<Packet<? super T>>, P> pBundler, final BundleDelimiterPacket<? super T> pPacket
    ) {
        return new BundlerInfo() {
            @Override
            public void unbundlePacket(Packet<?> p_265538_, Consumer<Packet<?>> p_265064_) {
                if (p_265538_.type() == pType) {
                    P p = (P)p_265538_;
                    p_265064_.accept(pPacket);
                    p.subPackets().forEach(p_265064_);
                    p_265064_.accept(pPacket);
                } else {
                    p_265064_.accept(p_265538_);
                }
            }

            @Nullable
            @Override
            public BundlerInfo.Bundler startPacketBundling(Packet<?> p_265749_) {
                return p_265749_ == pPacket ? new BundlerInfo.Bundler() {
                    private final List<Packet<? super T>> bundlePackets = new ArrayList<>();

                    @Nullable
                    @Override
                    public Packet<?> addPacket(Packet<?> p_336207_) {
                        if (p_336207_ == pPacket) {
                            return pBundler.apply(this.bundlePackets);
                        } else if (this.bundlePackets.size() >= 4096) {
                            throw new IllegalStateException("Too many packets in a bundle");
                        } else {
                            this.bundlePackets.add((Packet<? super T>)p_336207_);
                            return null;
                        }
                    }
                } : null;
            }
        };
    }

    void unbundlePacket(Packet<?> pPacket, Consumer<Packet<?>> pConsumer);

    @Nullable
    BundlerInfo.Bundler startPacketBundling(Packet<?> pPacket);

    public interface Bundler {
        @Nullable
        Packet<?> addPacket(Packet<?> pPacket);
    }
}