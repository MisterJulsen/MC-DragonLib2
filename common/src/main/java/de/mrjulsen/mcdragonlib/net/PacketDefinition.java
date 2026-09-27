package de.mrjulsen.mcdragonlib.net;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;

/**
 * A packet registered on a {@link DLChannel}.
 *
 * <p>A definition owns everything about one exchange: how its payload is laid out, which side
 * starts it, where its handler runs and how large its messages may get. Splitting a message into
 * frames and putting them back together is handled below this class, so a definition always sees
 * one complete payload.
 *
 * <p>Subclassing this is the supported way to add an exchange pattern the built-in ones do not
 * cover. A subclass only has to decide what its payload means in {@link #receive}.
 */
public abstract class PacketDefinition {

    private final DLChannel channel;
    private final String name;
    private final PacketDirection direction;
    private final ExecutionMode handlerExecutionMode;
    private final ExecutionMode callbackExecutionMode;
    private final int maxMessageBytes;


    /**
     * Creates a definition from the settings collected by a builder.
     *
     * @param settings the properties shared by every packet
     */
    protected PacketDefinition(Settings settings) {
        this.channel = Objects.requireNonNull(settings.channel(), "channel");
        this.name = Objects.requireNonNull(settings.name(), "name");
        this.direction = Objects.requireNonNull(settings.direction(), "direction");
        this.handlerExecutionMode = Objects.requireNonNull(settings.handlerExecutionMode(), "handlerExecutionMode");
        this.callbackExecutionMode = Objects.requireNonNull(settings.callbackExecutionMode(), "callbackExecutionMode");
        this.maxMessageBytes = settings.maxMessageBytes();
    }

    /**
     * The properties every packet has, independent of its exchange pattern.
     *
     * @param channel the channel the packet belongs to
     * @param name the packet id, unique within its channel
     * @param direction which side starts the exchange
     * @param handlerExecutionMode where the receiving handler runs
     * @param callbackExecutionMode where the result is delivered on the side that asked
     * @param maxMessageBytes the largest payload accepted in either direction
     */
    public record Settings(DLChannel channel, String name, PacketDirection direction, ExecutionMode handlerExecutionMode, ExecutionMode callbackExecutionMode, int maxMessageBytes) {}

    /**
     * Returns the channel this packet is registered on.
     *
     * @return the owning channel
     */
    public final DLChannel getChannel() {
        return channel;
    }

    /**
     * Returns the packet id, which is also what identifies this packet on the wire.
     *
     * @return the packet name
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns which side starts an exchange of this packet.
     *
     * @return the packet direction
     */
    public final PacketDirection getDirection() {
        return direction;
    }

    /**
     * Returns where the handler of this packet runs, on the side that receives it.
     *
     * @return the execution mode of the handler
     */
    public final ExecutionMode getHandlerExecutionMode() {
        return handlerExecutionMode;
    }

    /**
     * Returns where a result is delivered, on the side that started the exchange.
     *
     * @return the execution mode of the callback
     */
    public final ExecutionMode getCallbackExecutionMode() {
        return callbackExecutionMode;
    }

    /**
     * Returns the largest payload this packet accepts.
     *
     * @return the message size limit in bytes
     */
    public final int getMaxMessageBytes() {
        return maxMessageBytes;
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + channel.getId() + "/" + name + "]";
    }

    /**
     * Handles one complete received payload.
     *
     * <p>This runs on the netty event loop, so the payload must be decoded before any work is
     * handed to another thread. The buffer is released as soon as this method returns.
     *
     * @param context what is known about the received message
     * @param payload the complete payload of the message
     */
    protected abstract void receive(PacketContext context, FriendlyByteBuf payload);

    /**
     * Encodes and sends a message of this packet to every connection the target covers.
     *
     * @param target where the message goes
     * @param writer writes the payload
     */
    protected final void dispatch(PacketTarget target, Consumer<FriendlyByteBuf> writer) {
        Collection<Connection> connections = target.getConnections();
        if (connections.isEmpty()) {
            return;
        }
        FriendlyByteBuf payload = encode(writer);
        try {
            channel.transmit(this, target.getFlow(), connections, payload);
        } finally {
            payload.release();
        }
    }

    /**
     * Encodes and sends a message of this packet to one connection.
     *
     * @param connection the recipient
     * @param flow the direction the message travels in
     * @param writer writes the payload
     */
    protected final void dispatch(Connection connection, NetworkFlow flow, Consumer<FriendlyByteBuf> writer) {
        FriendlyByteBuf payload = encode(writer);
        try {
            channel.transmit(this, flow, List.of(connection), payload);
        } finally {
            payload.release();
        }
    }

    private FriendlyByteBuf encode(Consumer<FriendlyByteBuf> writer) {
        FriendlyByteBuf payload = new FriendlyByteBuf(Unpooled.buffer());
        try {
            writer.accept(payload);
        } catch (Throwable t) {
            payload.release();
            throw t;
        }
        if (payload.readableBytes() > maxMessageBytes) {
            int size = payload.readableBytes();
            payload.release();
            throw new IllegalStateException("Payload of " + this + " is " + size + " bytes, which exceeds its limit of " + maxMessageBytes + ". Raise the limit or use a stream packet.");
        }
        return payload;
    }
}
