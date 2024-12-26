package com.example.sagecraft;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import net.minecraft.network.FriendlyByteBuf;

public class CustomPacket {
    private final int someData; // Example data field

    public CustomPacket(int someData) {
        this.someData = someData; // Initialize with data
    }

    public static void encode(CustomPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.someData); // Serialize packet data to buffer
    }

    public CustomPacket(FriendlyByteBuf buffer) {
        this.someData = buffer.readInt(); // Deserialize packet data from buffer
    }

    public static CustomPacket decode(FriendlyByteBuf buffer) {
        return new CustomPacket(buffer); // Create a new packet from buffer
    }

    public static void handle(CustomPacket packet, MessagePassingQueue.Supplier<Object> contextSupplier) {
        Object context = contextSupplier.get();
        // Handle the packet on the main thread
        // Example: process the packet data
    }
}
