package de.mrjulsen.mcdragonlib.net.transport;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Receives a fully reassembled message from the transport.
 *
 * <p>The payload buffer is only valid for the duration of the call and is released by the
 * transport afterwards, so anything needed later must be decoded or copied before returning.
 */
@FunctionalInterface
public interface MessageSink {

    /**
     * Handles one complete message.
     *
     * @param packetName the registered packet the message belongs to
     * @param payload the complete payload, valid only during this call
     */
    void accept(String packetName, FriendlyByteBuf payload);
}
