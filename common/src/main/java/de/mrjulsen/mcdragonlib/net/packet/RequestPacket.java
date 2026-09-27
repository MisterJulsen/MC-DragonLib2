package de.mrjulsen.mcdragonlib.net.packet;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.net.ChannelSession;
import de.mrjulsen.mcdragonlib.net.DLNetwork;
import de.mrjulsen.mcdragonlib.net.ExecutionMode;
import de.mrjulsen.mcdragonlib.net.NetworkError;
import de.mrjulsen.mcdragonlib.net.PacketContext;
import de.mrjulsen.mcdragonlib.net.PacketDefinition;
import de.mrjulsen.mcdragonlib.net.PacketDirection;
import de.mrjulsen.mcdragonlib.net.PacketTarget;
import de.mrjulsen.mcdragonlib.net.PendingRequest;
import de.mrjulsen.mcdragonlib.net.Response;
import de.mrjulsen.mcdragonlib.net.codec.DLStreamCodec;
import dev.architectury.utils.Env;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;

/**
 * A packet that answers every message with a result.
 *
 * <p>One call sends the request and delivers the outcome, so there is no need to register a
 * second packet for the way back. The outcome is a {@link Response}, which is either the result
 * or a {@link NetworkError} and never silently both.
 *
 * @param <Q> the request type
 * @param <R> the response type
 */
public final class RequestPacket<Q, R> extends PacketDefinition {

    private static final byte KIND_REQUEST = 0;
    private static final byte KIND_RESPONSE = 1;
    private static final byte KIND_ERROR = 2;

    /**
     * Turns a request into a response.
     *
     * @param <Q> the request type
     * @param <R> the response type
     */
    @FunctionalInterface
    public interface Handler<Q, R> {

        /**
         * Handles one request.
         *
         * <p>Throwing from here is a normal way to report failure: the exception is logged
         * locally and the other side is told that the handler failed, without the message.
         *
         * @param request the decoded request
         * @param context what is known about the received message
         * @return the value sent back
         */
        R handle(Q request, PacketContext context);
    }

    private final DLStreamCodec<Q> requestCodec;
    private final DLStreamCodec<R> responseCodec;
    private final Handler<Q, R> handler;
    private final long timeoutMillis;

    RequestPacket(Settings settings, DLStreamCodec<Q> requestCodec, DLStreamCodec<R> responseCodec, Handler<Q, R> handler, long timeoutMillis) {
        super(settings);
        this.requestCodec = Objects.requireNonNull(requestCodec, "requestCodec");
        this.responseCodec = Objects.requireNonNull(responseCodec, "responseCodec");
        this.handler = Objects.requireNonNull(handler, "handler");
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Sends a request and hands the outcome to a callback.
     *
     * <p>The callback runs in this packet's {@link ExecutionMode} and is called exactly once,
     * including when the exchange times out or the connection drops.
     *
     * @param target the single recipient
     * @param request the value to send
     * @param callback receives the outcome
     */
    public void request(PacketTarget target, Q request, Consumer<Response<R>> callback) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(callback, "callback");
        if (target.getFlow() != getDirection().getRequestFlow()) {
            throw new IllegalArgumentException(this + " travels " + getDirection().getRequestFlow() + " but the target is " + target.getFlow() + ".");
        }

        Env requester = getDirection() == PacketDirection.TO_SERVER ? Env.CLIENT : Env.SERVER;
        Connection connection;
        try {
            connection = target.requireSingleConnection();
        } catch (IllegalStateException e) {
            deliver(requester, callback, Response.failure(NetworkError.of(NetworkError.Reason.DISCONNECTED, e.getMessage())));
            return;
        }

        ChannelSession session = getChannel().getSession(connection);
        int correlationId = session.nextCorrelationId();
        Pending pending = new Pending(callback, System.currentTimeMillis() + timeoutMillis, requester);
        session.addPending(correlationId, pending);

        try {
            dispatch(connection, getDirection().getRequestFlow(), buf -> {
                buf.writeByte(KIND_REQUEST);
                buf.writeVarInt(correlationId);
                requestCodec.encode(buf, request);
            });
        } catch (Exception e) {
            session.removePending(correlationId);
            DLNetwork.LOGGER.error("Could not send {}.", this, e);
            pending.settle(Response.failure(NetworkError.of(NetworkError.Reason.MALFORMED, "The request could not be sent.")));
        }
    }

    /**
     * Sends a request and completes a future with the outcome.
     *
     * @param target the single recipient
     * @param request the value to send
     * @return a future completed with the outcome, never completed exceptionally
     */
    public CompletableFuture<Response<R>> request(PacketTarget target, Q request) {
        CompletableFuture<Response<R>> future = new CompletableFuture<>();
        request(target, request, future::complete);
        return future;
    }

    @Override
    protected void receive(PacketContext context, FriendlyByteBuf payload) {
        byte kind = payload.readByte();
        int correlationId = payload.readVarInt();
        if (kind == KIND_REQUEST) {
            receiveRequest(context, correlationId, payload);
        } else {
            receiveResponse(context, correlationId, kind == KIND_RESPONSE, payload);
        }
    }

    private void receiveRequest(PacketContext context, int correlationId, FriendlyByteBuf payload) {
        if (context.getFlow() != getDirection().getRequestFlow()) {
            DLNetwork.LOGGER.warn("Ignoring a request for {} received from the wrong side.", this);
            return;
        }
        Q request;
        try {
            request = requestCodec.decode(payload);
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not decode a request for {}.", this, e);
            respondError(context, correlationId, NetworkError.of(NetworkError.Reason.MALFORMED, "The request could not be read."));
            return;
        }

        getHandlerExecutionMode().run(context, () -> {
            R result;
            try {
                result = handler.handle(request, context);
            } catch (Exception e) {
                DLNetwork.LOGGER.error("Handler of {} failed.", this, e);
                respondError(context, correlationId, NetworkError.of(NetworkError.Reason.HANDLER_FAILED));
                return;
            }
            try {
                dispatch(context.reply(), buf -> {
                    buf.writeByte(KIND_RESPONSE);
                    buf.writeVarInt(correlationId);
                    responseCodec.encode(buf, result);
                });
            } catch (Exception e) {
                DLNetwork.LOGGER.error("Could not send the response of {}.", this, e);
                respondError(context, correlationId, NetworkError.of(NetworkError.Reason.MALFORMED, "The response could not be written."));
            }
        });
    }

    private void receiveResponse(PacketContext context, int correlationId, boolean success, FriendlyByteBuf payload) {
        ChannelSession session = getChannel().peekSession(context.getConnection());
        PendingRequest waiting = session == null ? null : session.removePending(correlationId);
        if (waiting == null) {
            DLNetwork.LOGGER.debug("Discarding a late or unexpected response for {}.", this);
            return;
        }
        if (waiting.getDefinition() != this) {
            DLNetwork.LOGGER.warn("Correlation id {} of {} was answered by {}.", correlationId, waiting.getDefinition(), this);
            waiting.fail(NetworkError.of(NetworkError.Reason.MALFORMED, "The answer did not match the request."));
            return;
        }

        Response<R> response;
        try {
            response = success
                ? Response.success(responseCodec.decode(payload))
                : Response.failure(NetworkError.CODEC.decode(payload));
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not decode the response of {}.", this, e);
            response = Response.failure(NetworkError.of(NetworkError.Reason.MALFORMED, "The response could not be read."));
        }

        @SuppressWarnings("unchecked")
        Pending pending = (Pending) waiting;
        pending.settle(response);
    }

    private void respondError(PacketContext context, int correlationId, NetworkError error) {
        try {
            dispatch(context.reply(), buf -> {
                buf.writeByte(KIND_ERROR);
                buf.writeVarInt(correlationId);
                NetworkError.CODEC.encode(buf, error);
            });
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not report a failure of {} back to the sender.", this, e);
        }
    }

    private void deliver(Env environment, Consumer<Response<R>> callback, Response<R> response) {
        getCallbackExecutionMode().run(environment, () -> {
            try {
                callback.accept(response);
            } catch (Exception e) {
                DLNetwork.LOGGER.error("Callback of {} failed.", this, e);
            }
        });
    }

    private final class Pending implements PendingRequest {

        private final Consumer<Response<R>> callback;
        private final long deadline;
        private final Env requester;
        private final AtomicBoolean settled = new AtomicBoolean();

        private Pending(Consumer<Response<R>> callback, long deadline, Env requester) {
            this.callback = callback;
            this.deadline = deadline;
            this.requester = requester;
        }

        @Override
        public PacketDefinition getDefinition() {
            return RequestPacket.this;
        }

        @Override
        public long getDeadline() {
            return deadline;
        }

        @Override
        public void fail(NetworkError error) {
            settle(Response.failure(error));
        }

        private void settle(Response<R> response) {
            if (settled.compareAndSet(false, true)) {
                deliver(requester, callback, response);
            }
        }
    }
}
