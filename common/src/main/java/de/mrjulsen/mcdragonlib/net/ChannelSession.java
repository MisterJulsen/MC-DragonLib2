package de.mrjulsen.mcdragonlib.net;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.mixin.ConnectionAccessor;
import de.mrjulsen.mcdragonlib.net.transport.MessageAssembler;
import de.mrjulsen.mcdragonlib.net.transport.OutboundMessage;
import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

/**
 * Everything one channel keeps for one connection.
 *
 * <p>Transfer ids, correlation ids, half-received messages and unanswered exchanges are all
 * scoped to a single connection, so two players can never interfere with each other and
 * everything can be released at once when that connection closes.
 */
public final class ChannelSession {

    private record Queued(OutboundMessage message, NetworkFlow flow) {}

    private final DLChannel channel;
    private final Connection connection;
    private final MessageAssembler assembler;

    private final AtomicInteger transferIds = new AtomicInteger();
    private final AtomicInteger correlationIds = new AtomicInteger();
    private final Map<Integer, PendingRequest> pending = new ConcurrentHashMap<>();
    private final Map<Object, SessionResource> resources = new ConcurrentHashMap<>();
    private final Deque<Queued> outbound = new ArrayDeque<>();

    private volatile boolean closed;

    ChannelSession(DLChannel channel, Connection connection) {
        this.channel = channel;
        this.connection = connection;
        this.assembler = new MessageAssembler(channel.getTransportPolicy());
    }

    /**
     * Returns the channel this session belongs to.
     *
     * @return the owning channel
     */
    public DLChannel getChannel() {
        return channel;
    }

    /**
     * Returns the connection this session belongs to.
     *
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Returns the reassembler for messages arriving on this connection.
     *
     * @return the message assembler
     */
    public MessageAssembler getAssembler() {
        return assembler;
    }

    /**
     * Reserves the next transfer id for an outgoing split message.
     *
     * @return a transfer id unique among the transfers currently in flight
     */
    public int nextTransferId() {
        return transferIds.incrementAndGet();
    }

    /**
     * Reserves the next correlation id for an outgoing exchange.
     *
     * @return a correlation id unique among the exchanges currently in flight
     */
    public int nextCorrelationId() {
        return correlationIds.incrementAndGet();
    }

    /**
     * Remembers an exchange so its answer can be matched to it.
     *
     * @param correlationId the id sent along with the request
     * @param request the exchange waiting for an answer
     */
    public void addPending(int correlationId, PendingRequest request) {
        if (closed) {
            request.fail(NetworkError.of(NetworkError.Reason.DISCONNECTED));
            return;
        }
        pending.put(correlationId, request);
    }

    /**
     * Takes an exchange out of the waiting list.
     *
     * @param correlationId the id the answer arrived with
     * @return the waiting exchange, or {@code null} if it already finished or timed out
     */
    public PendingRequest removePending(int correlationId) {
        return pending.remove(correlationId);
    }

    /**
     * Returns how many exchanges are currently waiting for an answer.
     *
     * @return the number of unanswered exchanges
     */
    public int getPendingRequestCount() {
        return pending.size();
    }

    /**
     * Returns the state a packet keeps for this connection, creating it on first use.
     *
     * @param key identifies the resource, normally the packet definition itself
     * @param factory creates the resource when it does not exist yet
     * @param <T> the resource type
     * @return the resource belonging to that key
     */
    @SuppressWarnings("unchecked")
    public <T extends SessionResource> T getResource(Object key, Supplier<T> factory) {
        return (T) resources.computeIfAbsent(key, ignored -> factory.get());
    }

    /**
     * Returns the state a packet keeps for this connection only if it already exists.
     *
     * @param key identifies the resource
     * @param <T> the resource type
     * @return the resource, or {@code null} if nothing is attached under that key
     */
    @SuppressWarnings("unchecked")
    public <T extends SessionResource> T peekResource(Object key) {
        return (T) resources.get(key);
    }

    /**
     * Queues a message for sending and pushes out as many frames as the connection accepts.
     *
     * @param message the message to send
     * @param flow the direction the message travels in
     */
    public void enqueue(OutboundMessage message, NetworkFlow flow) {
        if (closed || !connection.isConnected()) {
            message.release();
            return;
        }
        synchronized (outbound) {
            if (outbound.isEmpty() && message.getFrameCount() == 1) {
                sendFrame(message, flow);
                return;
            }
            outbound.addLast(new Queued(message, flow));
        }
        pump();
    }

    /**
     * Sends further queued frames while the connection has room for them.
     */
    public void pump() {
        if (closed) {
            return;
        }
        synchronized (outbound) {
            while (!outbound.isEmpty()) {
                if (!connection.isConnected()) {
                    discardOutbound();
                    return;
                }
                Queued head = outbound.peekFirst();
                while (head.message().hasNextFrame()) {
                    if (!isWritable()) {
                        return;
                    }
                    sendFrame(head.message(), head.flow());
                }
                outbound.pollFirst();
            }
        }
    }

    private void sendFrame(OutboundMessage message, NetworkFlow flow) {
        Packet<?> packet = DLNetwork.toPacket(channel.getId(), flow, message.nextFrame());
        connection.send(packet);
    }

    private boolean isWritable() {
        Channel nettyChannel = ((ConnectionAccessor) connection).dragonlib$getChannel();
        return nettyChannel == null || nettyChannel.isWritable();
    }

    private void discardOutbound() {
        for (Queued queued : outbound) {
            queued.message().release();
        }
        outbound.clear();
    }

    /**
     * Drops stalled transfers and gives up on exchanges whose deadline has passed.
     *
     * @param now the current wall-clock time in milliseconds
     */
    public void tick(long now) {
        assembler.sweep();
        for (SessionResource resource : resources.values()) {
            resource.tick(now);
        }
        Iterator<Map.Entry<Integer, PendingRequest>> it = pending.entrySet().iterator();
        while (it.hasNext()) {
            PendingRequest request = it.next().getValue();
            if (request.getDeadline() <= now) {
                it.remove();
                request.fail(NetworkError.of(NetworkError.Reason.TIMEOUT, "No response within the configured time."));
            }
        }
        pump();
    }

    /**
     * Releases everything this session holds and completes every outstanding exchange with the
     * given reason.
     *
     * @param reason why the session is going away
     */
    public void close(NetworkError.Reason reason) {
        if (closed) {
            return;
        }
        closed = true;
        synchronized (outbound) {
            discardOutbound();
        }
        assembler.close();
        for (SessionResource resource : resources.values()) {
            resource.close(reason);
        }
        resources.clear();
        Iterator<Map.Entry<Integer, PendingRequest>> it = pending.entrySet().iterator();
        while (it.hasNext()) {
            PendingRequest request = it.next().getValue();
            it.remove();
            request.fail(NetworkError.of(reason));
        }
    }
}
