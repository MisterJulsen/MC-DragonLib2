package de.mrjulsen.mcdragonlib.network.internal;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.net.ChannelSession;
import de.mrjulsen.mcdragonlib.net.DLNetwork;
import de.mrjulsen.mcdragonlib.net.NetworkError;
import de.mrjulsen.mcdragonlib.net.NetworkSettings;
import de.mrjulsen.mcdragonlib.net.PacketContext;
import de.mrjulsen.mcdragonlib.net.PacketDefinition;
import de.mrjulsen.mcdragonlib.net.PendingRequest;
import de.mrjulsen.mcdragonlib.net.codec.DLStreamCodec;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Carries a deprecated request and response packet over the current transport.
 *
 * <p>The old contract is kept exactly: a handler that throws still answers with a payload whose
 * status carries the error, and only a timeout or a lost connection completes the waiting future
 * exceptionally.
 *
 * @param <I> the request type
 * @param <O> the response type
 */
@Deprecated
public final class LegacyRequestPacket<I extends NetworkPacketData, O extends NetworkPacketData> extends PacketDefinition {

    private static final byte KIND_REQUEST = 0;
    private static final byte KIND_RESPONSE = 1;

    private static final Map<Integer, PendingRequest> FALLBACK = new ConcurrentHashMap<>();

    private final DLStreamCodec<I> inputCodec;
    private final DLStreamCodec<O> outputCodec;
    private final Function<DLStatus, O> outputFactory;
    private final BiFunction<I, NetworkPacketContext, O> handler;

    public LegacyRequestPacket(Settings settings, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory, BiFunction<I, NetworkPacketContext, O> handler) {
        super(settings);
        this.inputCodec = LegacyBridge.dataCodec(inputFactory);
        this.outputCodec = LegacyBridge.dataCodec(outputFactory);
        this.outputFactory = Objects.requireNonNull(outputFactory, "outputFactory");
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    /**
     * Returns how many exchanges of connection-less senders are still waiting.
     *
     * @return the number of pending fallback exchanges
     */
    public static int getFallbackCount() {
        return FALLBACK.size();
    }

    /**
     * Sends a request and completes the given future with the answer.
     *
     * @param sender where the request goes
     * @param data the value to send
     * @param future completed with the response, or exceptionally if none arrives
     */
    public void send(NetworkDirection sender, I data, CompletableFuture<O> future) {
        long deadline = System.currentTimeMillis() + NetworkSettings.getResponseTimeoutMillis();
        Pending pending = new Pending(future, deadline);

        int correlationId;
        Connection connection = sender.getConnection();
        if (connection != null) {
            ChannelSession session = getChannel().getSession(connection);
            correlationId = session.nextCorrelationId();
            session.addPending(correlationId, pending);
        } else {
            sweepFallback();
            correlationId = LegacyBridge.nextFallbackId();
            FALLBACK.put(correlationId, pending);
        }

        try {
            LegacyBridge.send(getChannel(), this, sender, buf -> {
                buf.writeByte(KIND_REQUEST);
                buf.writeVarInt(correlationId);
                inputCodec.encode(buf, data);
            });
        } catch (Exception e) {
            removePending(connection, correlationId);
            DLNetwork.LOGGER.error("Could not send {}.", this, e);
            pending.fail(NetworkError.of(NetworkError.Reason.MALFORMED, "The request could not be sent."));
        }
    }

    @Override
    protected void receive(PacketContext context, FriendlyByteBuf payload) {
        byte kind = payload.readByte();
        int correlationId = payload.readVarInt();
        if (kind == KIND_REQUEST) {
            receiveRequest(context, correlationId, payload);
        } else {
            receiveResponse(context, correlationId, payload);
        }
    }

    private void receiveRequest(PacketContext context, int correlationId, FriendlyByteBuf payload) {
        I request;
        try {
            request = inputCodec.decode(payload);
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not decode a request for {}.", this, e);
            respond(context, correlationId, outputFactory.apply(DLStatus.error(e)));
            return;
        }

        NetworkPacketContext legacyContext = LegacyBridge.adapt(context);
        getHandlerExecutionMode().run(context, () -> {
            O result;
            try {
                result = handler.apply(request, legacyContext);
            } catch (Exception e) {
                DLNetwork.LOGGER.error("Handler of {} failed.", this, e);
                result = outputFactory.apply(DLStatus.error(e));
            }
            respond(context, correlationId, result);
        });
    }

    private void respond(PacketContext context, int correlationId, O result) {
        try {
            dispatch(context.reply(), buf -> {
                buf.writeByte(KIND_RESPONSE);
                buf.writeVarInt(correlationId);
                outputCodec.encode(buf, result);
            });
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not send the response of {}.", this, e);
        }
    }

    private void receiveResponse(PacketContext context, int correlationId, FriendlyByteBuf payload) {
        PendingRequest waiting = takePending(context.getConnection(), correlationId);
        if (waiting == null) {
            DLNetwork.LOGGER.debug("Discarding a late or unexpected response for {}.", this);
            return;
        }
        if (waiting.getDefinition() != this) {
            waiting.fail(NetworkError.of(NetworkError.Reason.MALFORMED, "The answer did not match the request."));
            return;
        }

        O result;
        try {
            result = outputCodec.decode(payload);
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not decode the response of {}.", this, e);
            waiting.fail(NetworkError.of(NetworkError.Reason.MALFORMED, "The response could not be read."));
            return;
        }
        ((Pending) waiting).deliver(result);
    }

    private PendingRequest takePending(Connection connection, int correlationId) {
        if (correlationId < 0) {
            return FALLBACK.remove(correlationId);
        }
        ChannelSession session = getChannel().peekSession(connection);
        return session == null ? null : session.removePending(correlationId);
    }

    private void removePending(Connection connection, int correlationId) {
        if (correlationId < 0) {
            FALLBACK.remove(correlationId);
            return;
        }
        ChannelSession session = getChannel().peekSession(connection);
        if (session != null) {
            session.removePending(correlationId);
        }
    }

    private static void sweepFallback() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, PendingRequest>> it = FALLBACK.entrySet().iterator();
        while (it.hasNext()) {
            PendingRequest request = it.next().getValue();
            if (request.getDeadline() <= now) {
                it.remove();
                request.fail(NetworkError.of(NetworkError.Reason.TIMEOUT));
            }
        }
    }

    private final class Pending implements PendingRequest {

        private final CompletableFuture<O> future;
        private final long deadline;

        private Pending(CompletableFuture<O> future, long deadline) {
            this.future = future;
            this.deadline = deadline;
        }

        @Override
        public PacketDefinition getDefinition() {
            return LegacyRequestPacket.this;
        }

        @Override
        public long getDeadline() {
            return deadline;
        }

        @Override
        public void fail(NetworkError error) {
            if (error.reason() == NetworkError.Reason.TIMEOUT) {
                future.completeExceptionally(new TimeoutException(error.toString()));
            } else {
                future.completeExceptionally(new IllegalStateException(error.toString()));
            }
        }

        private void deliver(O result) {
            future.complete(result);
        }
    }
}
