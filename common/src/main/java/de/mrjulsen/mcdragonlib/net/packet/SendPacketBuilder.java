package de.mrjulsen.mcdragonlib.net.packet;

import java.util.Objects;

import de.mrjulsen.mcdragonlib.net.DLChannel;
import de.mrjulsen.mcdragonlib.net.codec.DLStreamCodec;

/**
 * Collects the settings for a {@link SendPacket} and registers it.
 *
 * @param <T> the payload type
 */
public final class SendPacketBuilder<T> extends PacketBuilder<SendPacketBuilder<T>> {

    private final DLStreamCodec<T> codec;
    private SendPacket.Handler<T> handler;

    public SendPacketBuilder(DLChannel channel, String name, DLStreamCodec<T> codec) {
        super(channel, name);
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    /**
     * Sets what happens when a payload arrives.
     *
     * @param handler the receiving side of this packet
     * @return this builder
     */
    public SendPacketBuilder<T> handler(SendPacket.Handler<T> handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
        return this;
    }

    /**
     * Adds the finished packet to its channel.
     *
     * @return the registered packet
     */
    public SendPacket<T> register() {
        if (handler == null) {
            throw new IllegalStateException("Packet '" + name + "' needs a handler.");
        }
        return channel.register(new SendPacket<>(settings(), codec, handler));
    }
}
