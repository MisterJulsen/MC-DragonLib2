package de.mrjulsen.mcdragonlib.net.packet;

import java.util.Objects;

import de.mrjulsen.mcdragonlib.net.DLNetwork;
import de.mrjulsen.mcdragonlib.net.PacketContext;
import de.mrjulsen.mcdragonlib.net.PacketDefinition;
import de.mrjulsen.mcdragonlib.net.PacketTarget;
import de.mrjulsen.mcdragonlib.net.codec.DLStreamCodec;
import net.minecraft.network.FriendlyByteBuf;

/**
 * A packet that is sent and forgotten.
 *
 * <p>Nothing travels back, so the sender learns nothing about whether the handler succeeded. Use
 * a {@link RequestPacket} when the outcome matters.
 *
 * @param <T> the payload type
 */
public final class SendPacket<T> extends PacketDefinition {

    /**
     * Handles a received payload.
     *
     * @param <T> the payload type
     */
    @FunctionalInterface
    public interface Handler<T> {

        /**
         * Handles one received payload.
         *
         * @param payload the decoded payload
         * @param context what is known about the received message
         */
        void handle(T payload, PacketContext context);
    }

    private final DLStreamCodec<T> codec;
    private final Handler<T> handler;

    SendPacket(Settings settings, DLStreamCodec<T> codec, Handler<T> handler) {
        super(settings);
        this.codec = Objects.requireNonNull(codec, "codec");
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    /**
     * Sends a payload to every recipient the target covers.
     *
     * @param target where the payload goes
     * @param payload the value to send
     */
    public void send(PacketTarget target, T payload) {
        Objects.requireNonNull(target, "target");
        if (target.getFlow() != getDirection().getRequestFlow()) {
            throw new IllegalArgumentException(this + " travels " + getDirection().getRequestFlow() + " but the target is " + target.getFlow() + ".");
        }
        dispatch(target, buf -> codec.encode(buf, payload));
    }

    @Override
    protected void receive(PacketContext context, FriendlyByteBuf payload) {
        if (context.getFlow() != getDirection().getRequestFlow()) {
            DLNetwork.LOGGER.warn("Ignoring {} received from the wrong side.", this);
            return;
        }
        T value;
        try {
            value = codec.decode(payload);
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not decode {}.", this, e);
            return;
        }
        getHandlerExecutionMode().run(context, () -> {
            try {
                handler.handle(value, context);
            } catch (Exception e) {
                DLNetwork.LOGGER.error("Handler of {} failed.", this, e);
            }
        });
    }
}
