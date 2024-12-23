package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.core.RegistryAccess;

public class RegistryFriendlyByteBuf extends FriendlyByteBuf {
    private final RegistryAccess registryAccess;

    public RegistryFriendlyByteBuf(ByteBuf pSource, RegistryAccess pRegistryAccess) {
        super(pSource);
        this.registryAccess = pRegistryAccess;
    }

    public RegistryAccess registryAccess() {
        return this.registryAccess;
    }

    @Override
    public RegistryFriendlyByteBuf wrap(ByteBuf data) {
        return new RegistryFriendlyByteBuf(data, this.registryAccess);
    }

    public static Function<ByteBuf, RegistryFriendlyByteBuf> decorator(RegistryAccess pRegistry) {
        return p_328649_ -> new RegistryFriendlyByteBuf(p_328649_, pRegistry);
    }
}
