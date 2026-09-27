package de.mrjulsen.mcdragonlib.net.transport;

import java.util.Objects;

import de.mrjulsen.mcdragonlib.net.NetworkFlow;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

/**
 * One logical message on its way out, handed over one frame at a time.
 *
 * <p>Frames are produced on demand rather than all at once so that a large message does not have
 * to exist twice in memory and so the sender can pause between frames while the connection is
 * congested. Each returned frame is owned by the caller and is normally consumed by the
 * networking stack.
 *
 * <p>A frame is filled right up to the limit vanilla accepts. Both loaders hand the buffer to the
 * vanilla custom payload constructor unchanged, and that constructor compares exactly these bytes
 * against its own maximum, so no slack is needed. The header size is a worst case, which leaves a
 * few unused bytes per frame anyway.
 */
public final class OutboundMessage {

    private final String packetName;
    private final int transferId;
    private final int totalBytes;
    private final int framePayloadLimit;
    private final int frameCount;
    private final FriendlyByteBuf payload;

    private int emittedFrames;
    private int sentBytes;
    private boolean released;

    private OutboundMessage(String packetName, int transferId, FriendlyByteBuf payload, int framePayloadLimit) {
        this.packetName = packetName;
        this.transferId = transferId;
        this.payload = payload;
        this.totalBytes = payload.readableBytes();
        this.framePayloadLimit = framePayloadLimit;
        this.frameCount = totalBytes <= framePayloadLimit ? 1 : (totalBytes + framePayloadLimit - 1) / framePayloadLimit;
    }

    /**
     * Returns how many payload bytes fit into one frame in the given direction.
     *
     * @param flow the direction the message travels in
     * @param packetName the packet the message belongs to, whose name sits in the opening frame
     * @return the usable payload size per frame
     */
    public static int getFramePayloadLimit(NetworkFlow flow, String packetName) {
        return flow.getMaxPayloadBytes() - FrameHeader.maxHeaderBytes(packetName);
    }

    /**
     * Prepares a message for sending. The given payload becomes owned by the returned message and
     * is released once all frames have been taken or {@link #release()} is called.
     *
     * @param flow the direction the message travels in
     * @param transferId identifies the message on its connection
     * @param packetName the registered packet the message belongs to
     * @param payload the complete encoded payload
     * @return a message ready to emit its frames
     */
    public static OutboundMessage of(NetworkFlow flow, int transferId, String packetName, FriendlyByteBuf payload) {
        Objects.requireNonNull(flow, "flow");
        Objects.requireNonNull(payload, "payload");
        return new OutboundMessage(packetName, transferId, payload, getFramePayloadLimit(flow, packetName));
    }

    /**
     * Returns the registered packet this message belongs to.
     *
     * @return the packet name
     */
    public String getPacketName() {
        return packetName;
    }

    /**
     * Returns how many frames this message will produce in total.
     *
     * @return the frame count
     */
    public int getFrameCount() {
        return frameCount;
    }

    /**
     * Returns the encoded size of the whole message.
     *
     * @return the payload size in bytes
     */
    public int getTotalBytes() {
        return totalBytes;
    }

    /**
     * Returns how many bytes have already been handed out as frames.
     *
     * @return the number of payload bytes emitted so far
     */
    public int getSentBytes() {
        return sentBytes;
    }

    /**
     * Returns whether another frame is waiting to be sent.
     *
     * @return {@code true} while frames remain
     */
    public boolean hasNextFrame() {
        return !released && emittedFrames < frameCount;
    }

    /**
     * Builds the next frame and advances past the payload it carries.
     *
     * @return a buffer holding one complete frame, owned by the caller
     * @throws IllegalStateException if no frame is left
     */
    public FriendlyByteBuf nextFrame() {
        if (!hasNextFrame()) {
            throw new IllegalStateException("No frame left to send.");
        }
        int index = emittedFrames++;
        int chunk = Math.min(payload.readableBytes(), framePayloadLimit);

        FrameHeader header;
        if (frameCount == 1) {
            header = FrameHeader.single(packetName);
        } else if (index == 0) {
            header = FrameHeader.first(transferId, packetName, totalBytes);
        } else if (index == frameCount - 1) {
            header = FrameHeader.continuation(FrameType.LAST, transferId);
        } else {
            header = FrameHeader.continuation(FrameType.MIDDLE, transferId);
        }

        FriendlyByteBuf frame = new FriendlyByteBuf(Unpooled.buffer(FrameHeader.maxHeaderBytes(packetName) + chunk));
        header.write(frame);
        frame.writeBytes(payload, chunk);
        sentBytes += chunk;

        if (!hasNextFrame()) {
            release();
        }
        return frame;
    }

    /**
     * Discards the remaining payload. Calling this more than once has no effect.
     */
    public void release() {
        if (released) {
            return;
        }
        released = true;
        payload.release();
    }
}
