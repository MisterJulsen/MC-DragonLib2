package de.mrjulsen.mcdragonlib.net.transport;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Rebuilds split messages for a single connection.
 *
 * <p>An unfinished transfer holds memory on behalf of the remote peer, so a transfer is only
 * started once its announced size passes the {@link TransportPolicy} checks, and it is dropped
 * again when it stalls. The buffer grows with the data that actually arrives rather than with the
 * size the sender claims, so announcing a huge message costs the announcer, not the receiver. One
 * instance belongs to one connection and dies with it.
 */
public final class MessageAssembler {

    private static final int INITIAL_CAPACITY = 64 * 1024;

    private static final class Partial {
        private final String packetName;
        private final int totalSize;
        private final FriendlyByteBuf buffer;
        private long deadline;

        private Partial(String packetName, int totalSize, long deadline) {
            this.packetName = packetName;
            this.totalSize = totalSize;
            this.buffer = new FriendlyByteBuf(Unpooled.buffer(Math.min(totalSize, INITIAL_CAPACITY), totalSize));
            this.deadline = deadline;
        }
    }

    private final TransportPolicy policy;
    private final Map<Integer, Partial> partials = new ConcurrentHashMap<>();
    private int reservedBytes;

    /**
     * Creates an assembler enforcing the given limits.
     *
     * @param policy the limits applied to incoming messages
     */
    public MessageAssembler(TransportPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    /**
     * Feeds one received frame into the assembler and passes the message on once it is complete.
     *
     * @param frame the received frame, positioned at its header and owned by the caller
     * @param sink receives the finished message
     * @throws TransportException if the frame is malformed or violates a limit
     */
    public void accept(FriendlyByteBuf frame, MessageSink sink) {
        FrameHeader header = FrameHeader.read(frame);
        switch (header.type()) {
            case SINGLE -> acceptSingle(header, frame, sink);
            case FIRST -> acceptFirst(header, frame);
            case MIDDLE -> acceptContinuation(header, frame, false, sink);
            case LAST -> acceptContinuation(header, frame, true, sink);
        }
    }

    private void acceptSingle(FrameHeader header, FriendlyByteBuf frame, MessageSink sink) {
        int limit = requireKnownPacket(header.packetName());
        int size = frame.readableBytes();
        if (size > limit) {
            throw new TransportException("Message for packet '" + header.packetName() + "' is " + size + " bytes, which exceeds its limit of " + limit + ".");
        }
        sink.accept(header.packetName(), frame);
    }

    private void acceptFirst(FrameHeader header, FriendlyByteBuf frame) {
        int limit = requireKnownPacket(header.packetName());
        int totalSize = header.totalSize();
        if (totalSize <= 0) {
            throw new TransportException("Announced message size " + totalSize + " is not positive.");
        }
        if (totalSize > limit) {
            throw new TransportException("Announced message size " + totalSize + " for packet " + header.packetName() + " exceeds its limit of " + limit + ".");
        }

        synchronized (this) {
            int budget = policy.getConnectionBudgetBytes();
            if (reservedBytes + totalSize > budget) {
                throw new TransportException("Reassembly budget of " + budget + " bytes is exhausted on this connection.");
            }
            Partial previous = partials.remove(header.transferId());
            if (previous != null) {
                reservedBytes -= previous.totalSize;
                previous.buffer.release();
            }
            Partial partial = new Partial(header.packetName(), totalSize, System.currentTimeMillis() + policy.getReassemblyTimeoutMillis());
            reservedBytes += totalSize;
            partials.put(header.transferId(), partial);
            appendTo(partial, frame);
        }
    }

    private void acceptContinuation(FrameHeader header, FriendlyByteBuf frame, boolean last, MessageSink sink) {
        Partial partial;
        synchronized (this) {
            partial = partials.get(header.transferId());
            if (partial == null) {
                throw new TransportException("Received a continuation frame for unknown transfer " + header.transferId() + ".");
            }
            try {
                appendTo(partial, frame);
            } catch (TransportException e) {
                drop(header.transferId());
                throw e;
            }
            partial.deadline = System.currentTimeMillis() + policy.getReassemblyTimeoutMillis();
            if (!last) {
                return;
            }
            partials.remove(header.transferId());
            reservedBytes -= partial.totalSize;
        }

        try {
            if (partial.buffer.readableBytes() != partial.totalSize) {
                throw new TransportException("Message ended after " + partial.buffer.readableBytes() + " of " + partial.totalSize + " announced bytes.");
            }
            sink.accept(partial.packetName, partial.buffer);
        } finally {
            partial.buffer.release();
        }
    }

    private void appendTo(Partial partial, FriendlyByteBuf frame) {
        int incoming = frame.readableBytes();
        if (partial.buffer.writerIndex() + incoming > partial.totalSize) {
            throw new TransportException("Message carries more data than the announced " + partial.totalSize + " bytes.");
        }
        partial.buffer.writeBytes(frame, incoming);
    }

    private int requireKnownPacket(String packetName) {
        int limit = policy.getMaxMessageBytes(packetName);
        if (limit < 0) {
            throw new TransportException("No packet named '" + packetName + "' is registered.");
        }
        return limit;
    }

    private void drop(int transferId) {
        Partial partial = partials.remove(transferId);
        if (partial != null) {
            reservedBytes -= partial.totalSize;
            partial.buffer.release();
        }
    }

    /**
     * Discards transfers that have not made progress within the configured timeout.
     *
     * @return the number of transfers that were discarded
     */
    public synchronized int sweep() {
        long now = System.currentTimeMillis();
        int dropped = 0;
        Iterator<Map.Entry<Integer, Partial>> it = partials.entrySet().iterator();
        while (it.hasNext()) {
            Partial partial = it.next().getValue();
            if (partial.deadline <= now) {
                it.remove();
                reservedBytes -= partial.totalSize;
                partial.buffer.release();
                dropped++;
            }
        }
        return dropped;
    }

    /**
     * Returns how many bytes all unfinished transfers currently occupy.
     *
     * @return the reserved byte count
     */
    public synchronized int getReservedBytes() {
        return reservedBytes;
    }

    /**
     * Returns how many transfers are currently unfinished.
     *
     * @return the number of pending transfers
     */
    public int getPendingTransferCount() {
        return partials.size();
    }

    /**
     * Discards every unfinished transfer, which is done when the connection goes away.
     */
    public synchronized void close() {
        for (Partial partial : partials.values()) {
            partial.buffer.release();
        }
        partials.clear();
        reservedBytes = 0;
    }
}
