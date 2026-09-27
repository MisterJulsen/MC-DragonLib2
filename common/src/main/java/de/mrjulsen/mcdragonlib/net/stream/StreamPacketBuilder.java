package de.mrjulsen.mcdragonlib.net.stream;

import java.util.Objects;

import de.mrjulsen.mcdragonlib.net.DLChannel;
import de.mrjulsen.mcdragonlib.net.DLNetwork;
import de.mrjulsen.mcdragonlib.net.NetworkFlow;
import de.mrjulsen.mcdragonlib.net.NetworkSettings;
import de.mrjulsen.mcdragonlib.net.codec.DLStreamCodec;
import de.mrjulsen.mcdragonlib.net.packet.PacketBuilder;
import de.mrjulsen.mcdragonlib.net.transport.OutboundMessage;

/**
 * Collects the settings for a {@link StreamPacket} and registers it.
 *
 * @param <Q> the type describing what is being asked for
 */
public final class StreamPacketBuilder<Q> extends PacketBuilder<StreamPacketBuilder<Q>> {

    private final DLStreamCodec<Q> requestCodec;
    private StreamPacket.SourceFactory<Q> sourceFactory;
    private int chunkSize = NetworkSettings.getStreamChunkBytes();
    private int window = NetworkSettings.getStreamWindow();

    public StreamPacketBuilder(DLChannel channel, String name, DLStreamCodec<Q> requestCodec) {
        super(channel, name);
        this.requestCodec = Objects.requireNonNull(requestCodec, "requestCodec");
    }

    /**
     * Sets how much data one chunk carries.
     *
     * <p>A chunk always fits into a single packet, so this is capped at what the direction allows.
     * Larger chunks reduce overhead, smaller ones give smoother progress and use less memory.
     *
     * @param bytes the chunk size
     * @return this builder
     */
    public StreamPacketBuilder<Q> chunkSize(int bytes) {
        if (bytes <= 0) {
            throw new IllegalArgumentException("The chunk size must be positive.");
        }
        this.chunkSize = bytes;
        return this;
    }

    /**
     * Sets how many chunks the sender may have in flight before it waits for the receiver.
     *
     * <p>A window of one reproduces a strict back and forth, which wastes a round trip per chunk.
     * The default trades a little memory for a connection that stays busy.
     *
     * @param chunks the number of chunks that may be outstanding
     * @return this builder
     */
    public StreamPacketBuilder<Q> window(int chunks) {
        if (chunks <= 0) {
            throw new IllegalArgumentException("The window must be at least one chunk.");
        }
        this.window = chunks;
        return this;
    }

    /**
     * Sets where the data comes from when a request arrives.
     *
     * @param factory opens a source for one request
     * @return this builder
     */
    public StreamPacketBuilder<Q> source(StreamPacket.SourceFactory<Q> factory) {
        this.sourceFactory = Objects.requireNonNull(factory, "factory");
        return this;
    }

    /**
     * Adds the finished packet to its channel.
     *
     * @return the registered packet
     */
    public StreamPacket<Q> register() {
        if (sourceFactory == null) {
            throw new IllegalStateException("Stream packet '" + name + "' needs a source.");
        }
        NetworkFlow dataFlow = direction.getResponseFlow();
        int frameLimit = OutboundMessage.getFramePayloadLimit(dataFlow, name) - 32;
        int effectiveChunkSize = Math.min(chunkSize, frameLimit);
        if (effectiveChunkSize < chunkSize) {
            DLNetwork.LOGGER.info("Chunk size of stream packet '{}' reduced from {} to {} bytes so that a chunk fits into one {} packet.", name, chunkSize, effectiveChunkSize, dataFlow);
        }
        maxSize(Math.max(maxMessageBytes, effectiveChunkSize + 64));
        return channel.register(new StreamPacket<>(settings(), requestCodec, sourceFactory, effectiveChunkSize, window));
    }
}
