package de.mrjulsen.mcdragonlib.net.stream;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.net.ChannelSession;
import de.mrjulsen.mcdragonlib.net.DLNetwork;
import de.mrjulsen.mcdragonlib.net.NetworkError;
import de.mrjulsen.mcdragonlib.net.NetworkFlow;
import de.mrjulsen.mcdragonlib.net.PacketContext;
import de.mrjulsen.mcdragonlib.net.PacketDefinition;
import de.mrjulsen.mcdragonlib.net.PacketDirection;
import de.mrjulsen.mcdragonlib.net.PacketTarget;
import de.mrjulsen.mcdragonlib.net.SessionResource;
import de.mrjulsen.mcdragonlib.net.codec.DLStreamCodec;
import dev.architectury.utils.Env;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;

/**
 * A packet that transfers an open-ended amount of data.
 *
 * <p>One side asks for a stream and supplies a {@link StreamSink}; the other side answers it with
 * a {@link StreamSource}. Data then flows in chunks that the receiver processes as they arrive,
 * so neither side ever holds the whole payload.
 *
 * <p>Flow control is automatic and invisible: the sender may run ahead by a fixed number of
 * chunks and earns the right to send another one every time the receiver finishes handling one.
 * A slow receiver therefore slows the sender down instead of filling memory, and the connection
 * never has to sit idle waiting for a round trip per chunk.
 *
 * @param <Q> the type describing what is being asked for
 */
public final class StreamPacket<Q> extends PacketDefinition {

    private static final byte KIND_OPEN = 0;
    private static final byte KIND_BEGIN = 1;
    private static final byte KIND_DATA = 2;
    private static final byte KIND_END = 3;
    private static final byte KIND_ERROR = 4;
    private static final byte KIND_CREDIT = 5;
    private static final byte KIND_CANCEL = 6;

    /**
     * Opens the data of a stream in response to a request.
     *
     * @param <Q> the request type
     */
    @FunctionalInterface
    public interface SourceFactory<Q> {

        /**
         * Opens a source for one request.
         *
         * <p>This runs in the packet's execution mode, so a {@code BLOCKING} stream may open files
         * here. Throwing reports the failure to the requesting side.
         *
         * @param request what was asked for
         * @param context what is known about the received message
         * @return the data to stream back
         * @throws Exception if the source could not be opened
         */
        StreamSource open(Q request, PacketContext context) throws Exception;
    }

    private record ResourceKey(PacketDefinition definition, boolean inbound) {}

    private final DLStreamCodec<Q> requestCodec;
    private final SourceFactory<Q> sourceFactory;
    private final int chunkSize;
    private final int window;

    StreamPacket(Settings settings, DLStreamCodec<Q> requestCodec, SourceFactory<Q> sourceFactory, int chunkSize, int window) {
        super(settings);
        this.requestCodec = Objects.requireNonNull(requestCodec, "requestCodec");
        this.sourceFactory = Objects.requireNonNull(sourceFactory, "sourceFactory");
        this.chunkSize = chunkSize;
        this.window = window;
    }

    /**
     * Returns how many bytes one chunk carries.
     *
     * @return the chunk size in bytes
     */
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * Returns how many chunks may be in flight at once.
     *
     * @return the flow control window in chunks
     */
    public int getWindow() {
        return window;
    }

    /**
     * Asks the other side for a stream and feeds it into the given sink.
     *
     * @param target the single provider
     * @param request what is being asked for
     * @param sink where the received data goes
     * @return a handle for progress, completion and cancellation
     */
    public StreamHandle request(PacketTarget target, Q request, StreamSink sink) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(sink, "sink");
        if (target.getFlow() != getDirection().getRequestFlow()) {
            throw new IllegalArgumentException(this + " is requested " + getDirection().getRequestFlow() + " but the target is " + target.getFlow() + ".");
        }

        Connection connection = target.requireSingleConnection();
        ChannelSession session = getChannel().getSession(connection);
        int streamId = session.nextCorrelationId();

        InboundStreams inbound = session.getResource(new ResourceKey(this, true), InboundStreams::new);
        InboundStream stream = new InboundStream(streamId, connection, sink);
        inbound.streams.put(streamId, stream);

        try {
            dispatch(connection, getDirection().getRequestFlow(), buf -> {
                buf.writeByte(KIND_OPEN);
                buf.writeVarInt(streamId);
                buf.writeVarInt(window);
                requestCodec.encode(buf, request);
            });
        } catch (Exception e) {
            inbound.streams.remove(streamId);
            DLNetwork.LOGGER.error("Could not open {}.", this, e);
            stream.handle.fail(NetworkError.of(NetworkError.Reason.MALFORMED, "The stream request could not be sent."));
        }
        return stream.handle;
    }

    @Override
    protected void receive(PacketContext context, FriendlyByteBuf payload) {
        byte kind = payload.readByte();
        int streamId = payload.readVarInt();
        ChannelSession session = getChannel().getSession(context.getConnection());
        switch (kind) {
            case KIND_OPEN -> onOpen(context, session, streamId, payload);
            case KIND_CREDIT -> {
                int granted = payload.readVarInt();
                withOutbound(session, streamId, stream -> stream.addCredit(granted));
            }
            case KIND_CANCEL -> {
                NetworkError reason = NetworkError.CODEC.decode(payload);
                withOutbound(session, streamId, stream -> stream.abort(reason));
                withInbound(session, streamId, stream -> stream.abort(reason));
            }
            case KIND_BEGIN -> {
                long total = payload.readLong();
                withInbound(session, streamId, stream -> stream.handle.setTotal(total));
            }
            case KIND_DATA -> {
                byte[] chunk = payload.readByteArray(getMaxMessageBytes());
                withInbound(session, streamId, stream -> stream.onData(chunk));
            }
            case KIND_END -> withInbound(session, streamId, InboundStream::onEnd);
            case KIND_ERROR -> {
                NetworkError reason = NetworkError.CODEC.decode(payload);
                withInbound(session, streamId, stream -> stream.abort(reason));
            }
            default -> DLNetwork.LOGGER.warn("Received an unknown stream message of kind {} for {}.", kind, this);
        }
    }

    private void withInbound(ChannelSession session, int streamId, Consumer<InboundStream> action) {
        InboundStreams inbound = session.peekResource(new ResourceKey(this, true));
        InboundStream stream = inbound == null ? null : inbound.streams.get(streamId);
        if (stream != null) {
            action.accept(stream);
        }
    }

    private void withOutbound(ChannelSession session, int streamId, Consumer<OutboundStream> action) {
        OutboundStreams outbound = session.peekResource(new ResourceKey(this, false));
        OutboundStream stream = outbound == null ? null : outbound.streams.get(streamId);
        if (stream != null) {
            action.accept(stream);
        }
    }

    private void onOpen(PacketContext context, ChannelSession session, int streamId, FriendlyByteBuf payload) {
        if (context.getFlow() != getDirection().getRequestFlow()) {
            DLNetwork.LOGGER.warn("Ignoring a stream request for {} received from the wrong side.", this);
            return;
        }
        int initialCredit = payload.readVarInt();
        Q request;
        try {
            request = requestCodec.decode(payload);
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not decode a stream request for {}.", this, e);
            sendError(context.getConnection(), streamId, NetworkError.of(NetworkError.Reason.MALFORMED, "The stream request could not be read."));
            return;
        }

        OutboundStreams outbound = session.getResource(new ResourceKey(this, false), OutboundStreams::new);
        OutboundStream stream = new OutboundStream(streamId, context.getConnection(), initialCredit, outbound);
        outbound.streams.put(streamId, stream);

        getHandlerExecutionMode().run(context, () -> {
            StreamSource source;
            try {
                source = Objects.requireNonNull(sourceFactory.open(request, context), "source factory returned null");
            } catch (Exception e) {
                DLNetwork.LOGGER.error("Could not open a source for {}.", this, e);
                outbound.streams.remove(streamId);
                sendError(context.getConnection(), streamId, NetworkError.of(NetworkError.Reason.HANDLER_FAILED));
                return;
            }
            stream.start(source);
        });
    }

    private void sendMessage(Connection connection, NetworkFlow flow, Consumer<FriendlyByteBuf> writer) {
        if (!connection.isConnected()) {
            return;
        }
        try {
            dispatch(connection, flow, writer);
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not send a stream message of {}.", this, e);
        }
    }

    private void sendError(Connection connection, int streamId, NetworkError error) {
        sendMessage(connection, getDirection().getResponseFlow(), buf -> {
            buf.writeByte(KIND_ERROR);
            buf.writeVarInt(streamId);
            NetworkError.CODEC.encode(buf, error);
        });
    }

    private final class OutboundStreams implements SessionResource {
        private final Map<Integer, OutboundStream> streams = new ConcurrentHashMap<>();

        @Override
        public void close(NetworkError.Reason reason) {
            for (OutboundStream stream : streams.values()) {
                stream.abort(NetworkError.of(reason));
            }
            streams.clear();
        }
    }

    private final class OutboundStream {

        private final int streamId;
        private final Connection connection;
        private final OutboundStreams owner;
        private final AtomicInteger credit;
        private final AtomicBoolean pumping = new AtomicBoolean();
        private volatile StreamSource source;
        private final AtomicBoolean closed = new AtomicBoolean();

        private OutboundStream(int streamId, Connection connection, int initialCredit, OutboundStreams owner) {
            this.streamId = streamId;
            this.connection = connection;
            this.owner = owner;
            this.credit = new AtomicInteger(Math.max(1, initialCredit));
        }

        private void start(StreamSource opened) {
            this.source = opened;
            long total = opened.getTotalBytes();
            sendMessage(connection, getDirection().getResponseFlow(), buf -> {
                buf.writeByte(KIND_BEGIN);
                buf.writeVarInt(streamId);
                buf.writeLong(total);
            });
            pump();
        }

        private void addCredit(int amount) {
            credit.addAndGet(amount);
            getHandlerExecutionMode().run(getEnvironmentOfProvider(), this::pump);
        }

        private void pump() {
            if (closed.get() || source == null) {
                return;
            }
            do {
                if (!pumping.compareAndSet(false, true)) {
                    return;
                }
                try {
                    drain();
                } finally {
                    pumping.set(false);
                }
            } while (!closed.get() && credit.get() > 0);
        }

        private void drain() {
            while (!closed.get() && credit.get() > 0) {
                if (!connection.isConnected()) {
                    abort(NetworkError.of(NetworkError.Reason.DISCONNECTED));
                    return;
                }
                byte[] chunk;
                try {
                    chunk = source.next();
                } catch (Exception e) {
                    DLNetwork.LOGGER.error("Could not read from the source of {}.", StreamPacket.this, e);
                    finish(NetworkError.of(NetworkError.Reason.HANDLER_FAILED));
                    return;
                }
                if (chunk == null) {
                    finish(null);
                    return;
                }
                credit.decrementAndGet();
                sendMessage(connection, getDirection().getResponseFlow(), buf -> {
                    buf.writeByte(KIND_DATA);
                    buf.writeVarInt(streamId);
                    buf.writeByteArray(chunk);
                });
            }
        }

        private void finish(NetworkError error) {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            owner.streams.remove(streamId);
            closeSource();
            if (error == null) {
                sendMessage(connection, getDirection().getResponseFlow(), buf -> {
                    buf.writeByte(KIND_END);
                    buf.writeVarInt(streamId);
                });
            } else {
                sendError(connection, streamId, error);
            }
        }

        private void abort(NetworkError reason) {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            owner.streams.remove(streamId);
            closeSource();
        }

        private void closeSource() {
            StreamSource open = source;
            if (open == null) {
                return;
            }
            source = null;
            try {
                open.close();
            } catch (Exception e) {
                DLNetwork.LOGGER.warn("Could not close the source of {}.", StreamPacket.this, e);
            }
        }
    }

    private final class InboundStreams implements SessionResource {
        private final Map<Integer, InboundStream> streams = new ConcurrentHashMap<>();

        @Override
        public void close(NetworkError.Reason reason) {
            for (InboundStream stream : streams.values()) {
                stream.abort(NetworkError.of(reason));
            }
            streams.clear();
        }
    }

    private final class InboundStream {

        private final int streamId;
        private final Connection connection;
        private final StreamSink sink;
        private final StreamHandle handle;
        private final Deque<byte[]> queue = new ArrayDeque<>();
        private final AtomicBoolean draining = new AtomicBoolean();
        private volatile boolean endReceived;
        private final AtomicBoolean closed = new AtomicBoolean();

        private InboundStream(int streamId, Connection connection, StreamSink sink) {
            this.streamId = streamId;
            this.connection = connection;
            this.sink = sink;
            this.handle = new StreamHandle(() -> cancel(NetworkError.of(NetworkError.Reason.CANCELLED)));
        }

        private void onData(byte[] chunk) {
            if (closed.get()) {
                return;
            }
            synchronized (queue) {
                queue.addLast(chunk);
            }
            scheduleDrain();
        }

        private void onEnd() {
            endReceived = true;
            scheduleDrain();
        }

        private void scheduleDrain() {
            getCallbackExecutionMode().run(getEnvironmentOfRequester(), this::drain);
        }

        private void drain() {
            if (closed.get()) {
                return;
            }
            if (!draining.compareAndSet(false, true)) {
                return;
            }
            try {
                while (!closed.get()) {
                    byte[] chunk;
                    synchronized (queue) {
                        chunk = queue.pollFirst();
                    }
                    if (chunk == null) {
                        break;
                    }
                    try {
                        sink.accept(chunk);
                    } catch (Exception e) {
                        DLNetwork.LOGGER.error("Sink of {} failed.", StreamPacket.this, e);
                        cancel(NetworkError.of(NetworkError.Reason.HANDLER_FAILED, "The receiver could not process the data."));
                        return;
                    }
                    handle.advance(chunk.length);
                    grantCredit();
                }
            } finally {
                draining.set(false);
            }
            if (endReceived && isQueueEmpty() && !closed.get()) {
                complete();
            }
        }

        private boolean isQueueEmpty() {
            synchronized (queue) {
                return queue.isEmpty();
            }
        }

        private void grantCredit() {
            sendMessage(connection, getDirection().getRequestFlow(), buf -> {
                buf.writeByte(KIND_CREDIT);
                buf.writeVarInt(streamId);
                buf.writeVarInt(1);
            });
        }

        private void complete() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            remove();
            try {
                sink.finish();
            } catch (Exception e) {
                DLNetwork.LOGGER.error("Sink of {} failed while finishing.", StreamPacket.this, e);
                closeSink();
                handle.fail(NetworkError.of(NetworkError.Reason.HANDLER_FAILED, "The receiver could not finish the data."));
                return;
            }
            closeSink();
            handle.complete();
        }

        private void cancel(NetworkError reason) {
            if (closed.get()) {
                return;
            }
            sendMessage(connection, getDirection().getRequestFlow(), buf -> {
                buf.writeByte(KIND_CANCEL);
                buf.writeVarInt(streamId);
                NetworkError.CODEC.encode(buf, reason);
            });
            abort(reason);
        }

        private void abort(NetworkError reason) {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            remove();
            synchronized (queue) {
                queue.clear();
            }
            sink.abort(reason);
            closeSink();
            handle.fail(reason);
        }

        private void remove() {
            ChannelSession session = getChannel().peekSession(connection);
            InboundStreams inbound = session == null ? null : session.peekResource(new ResourceKey(StreamPacket.this, true));
            if (inbound != null) {
                inbound.streams.remove(streamId);
            }
        }

        private void closeSink() {
            try {
                sink.close();
            } catch (Exception e) {
                DLNetwork.LOGGER.warn("Could not close the sink of {}.", StreamPacket.this, e);
            }
        }
    }

    private Env getEnvironmentOfRequester() {
        return getDirection() == PacketDirection.TO_SERVER ? Env.CLIENT : Env.SERVER;
    }

    private Env getEnvironmentOfProvider() {
        return getDirection() == PacketDirection.TO_SERVER ? Env.SERVER : Env.CLIENT;
    }
}
