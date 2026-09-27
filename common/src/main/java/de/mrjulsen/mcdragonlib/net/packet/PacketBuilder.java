package de.mrjulsen.mcdragonlib.net.packet;

import java.util.Objects;

import de.mrjulsen.mcdragonlib.net.DLChannel;
import de.mrjulsen.mcdragonlib.net.ExecutionMode;
import de.mrjulsen.mcdragonlib.net.NetworkSettings;
import de.mrjulsen.mcdragonlib.net.PacketDefinition;
import de.mrjulsen.mcdragonlib.net.PacketDirection;

/**
 * Shared configuration for every packet builder.
 *
 * <p>The defaults are deliberately conservative: a packet runs on the game thread and travels to
 * the server unless it says otherwise.
 *
 * @param <T> the concrete builder type, so the setters keep returning it
 */
public abstract class PacketBuilder<T extends PacketBuilder<T>> {

    protected final DLChannel channel;
    protected final String name;

    protected PacketDirection direction = PacketDirection.TO_SERVER;
    protected ExecutionMode handlerExecutionMode = ExecutionMode.MAIN;
    protected ExecutionMode callbackExecutionMode = ExecutionMode.MAIN;
    protected int maxMessageBytes = NetworkSettings.getMaxMessageBytes();

    protected PacketBuilder(DLChannel channel, String name) {
        this.channel = Objects.requireNonNull(channel, "channel");
        this.name = Objects.requireNonNull(name, "name");
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    /**
     * Sets which side starts an exchange of this packet.
     *
     * @param direction the initiating side
     * @return this builder
     */
    public T direction(PacketDirection direction) {
        this.direction = Objects.requireNonNull(direction, "direction");
        return self();
    }

    /**
     * Declares that the client starts the exchange.
     *
     * @return this builder
     */
    public T toServer() {
        return direction(PacketDirection.TO_SERVER);
    }

    /**
     * Declares that the server starts the exchange.
     *
     * @return this builder
     */
    public T toClient() {
        return direction(PacketDirection.TO_CLIENT);
    }

    /**
     * Sets where both the handler and the callback run.
     *
     * <p>Leave this at its default unless the code really does no world access, because anything
     * but {@link ExecutionMode#MAIN} runs off the game thread.
     *
     * @param mode the execution mode for both sides
     * @return this builder
     */
    public T on(ExecutionMode mode) {
        return handlerOn(mode).callbackOn(mode);
    }

    /**
     * Sets where the handler runs, on the side that receives the packet.
     *
     * @param mode the execution mode of the handler
     * @return this builder
     */
    public T handlerOn(ExecutionMode mode) {
        this.handlerExecutionMode = Objects.requireNonNull(mode, "mode");
        return self();
    }

    /**
     * Sets where the result is delivered, on the side that started the exchange. Packets that
     * expect no answer ignore this.
     *
     * @param mode the execution mode of the callback
     * @return this builder
     */
    public T callbackOn(ExecutionMode mode) {
        this.callbackExecutionMode = Objects.requireNonNull(mode, "mode");
        return self();
    }

    /**
     * Sets the largest payload this packet accepts in either direction.
     *
     * @param bytes the message size limit
     * @return this builder
     */
    public T maxSize(int bytes) {
        if (bytes <= 0) {
            throw new IllegalArgumentException("The message size limit must be positive.");
        }
        this.maxMessageBytes = bytes;
        return self();
    }

    protected PacketDefinition.Settings settings() {
        return new PacketDefinition.Settings(channel, name, direction, handlerExecutionMode, callbackExecutionMode, maxMessageBytes);
    }
}
