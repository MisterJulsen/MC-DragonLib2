package de.mrjulsen.mcdragonlib.net.transport;

import java.nio.charset.StandardCharsets;

import net.minecraft.network.FriendlyByteBuf;

/**
 * The fixed part every DragonLib frame starts with.
 *
 * <p>Only the fields a given {@link FrameType} needs are written, so a follow-up frame costs three
 * bytes. The opening frame carries the packet name, which makes a message self describing: adding
 * or removing packets never shifts what an existing one is called, and an unknown packet shows up
 * in the log by name instead of as a number. The transfer id distinguishes messages that overlap
 * on the same connection; the total size lets the receiver reject an oversized message before
 * allocating anything for it.
 *
 * @param protocolVersion the wire revision the sender used
 * @param type the role this frame plays within its message
 * @param transferId identifies the message on its connection, unused for {@link FrameType#SINGLE}
 * @param packetName the registered packet this message belongs to, only present on the opening frame
 * @param totalSize the payload size of the whole message, only present on {@link FrameType#FIRST}
 */
public record FrameHeader(byte protocolVersion, FrameType type, int transferId, String packetName, int totalSize) {

    /** The wire revision this build speaks. */
    public static final byte PROTOCOL_VERSION = 1;

    /** The longest a packet name may be, in characters. */
    public static final int MAX_PACKET_NAME_LENGTH = 128;

    private static final int UNUSED = -1;
    private static final String NO_NAME = "";

    /**
     * Returns the largest number of bytes a header for the given packet can occupy.
     *
     * @param packetName the packet the message belongs to
     * @return the worst case header size in bytes
     */
    public static int maxHeaderBytes(String packetName) {
        int nameBytes = packetName.getBytes(StandardCharsets.UTF_8).length;
        return 1 + 1 + 5 + 5 + 5 + nameBytes;
    }

    /**
     * Creates a header for a message that fits into one frame.
     *
     * @param packetName the registered packet the message belongs to
     * @return a header for a single-frame message
     */
    public static FrameHeader single(String packetName) {
        return new FrameHeader(PROTOCOL_VERSION, FrameType.SINGLE, UNUSED, packetName, UNUSED);
    }

    /**
     * Creates the opening header of a split message.
     *
     * @param transferId identifies the message on its connection
     * @param packetName the registered packet the message belongs to
     * @param totalSize the payload size of the whole message
     * @return a header for the first frame
     */
    public static FrameHeader first(int transferId, String packetName, int totalSize) {
        return new FrameHeader(PROTOCOL_VERSION, FrameType.FIRST, transferId, packetName, totalSize);
    }

    /**
     * Creates a continuation or closing header of a split message.
     *
     * @param type either {@link FrameType#MIDDLE} or {@link FrameType#LAST}
     * @param transferId identifies the message on its connection
     * @return a header for a follow-up frame
     */
    public static FrameHeader continuation(FrameType type, int transferId) {
        return new FrameHeader(PROTOCOL_VERSION, type, transferId, NO_NAME, UNUSED);
    }

    /**
     * Writes this header to the start of a frame buffer.
     *
     * @param buf the buffer to write to
     */
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(protocolVersion);
        buf.writeByte(type.getId());
        switch (type) {
            case SINGLE -> buf.writeUtf(packetName, MAX_PACKET_NAME_LENGTH);
            case FIRST -> {
                buf.writeVarInt(transferId);
                buf.writeUtf(packetName, MAX_PACKET_NAME_LENGTH);
                buf.writeVarInt(totalSize);
            }
            case MIDDLE, LAST -> buf.writeVarInt(transferId);
        }
    }

    /**
     * Reads a header from the start of a received frame, leaving the reader positioned at the payload.
     *
     * @param buf the received frame
     * @return the decoded header
     * @throws TransportException if the frame is malformed or uses an unsupported wire revision
     */
    public static FrameHeader read(FriendlyByteBuf buf) {
        if (buf.readableBytes() < 2) {
            throw new TransportException("Frame is too short to contain a header.");
        }
        byte version = buf.readByte();
        if (version != PROTOCOL_VERSION) {
            throw new TransportException("Unsupported protocol version " + version + ", expected " + PROTOCOL_VERSION + ".");
        }
        FrameType type;
        try {
            type = FrameType.byId(buf.readByte());
        } catch (IllegalArgumentException e) {
            throw new TransportException(e.getMessage());
        }
        return switch (type) {
            case SINGLE -> new FrameHeader(version, type, UNUSED, readName(buf), UNUSED);
            case FIRST -> {
                int transferId = buf.readVarInt();
                String packetName = readName(buf);
                int totalSize = buf.readVarInt();
                yield new FrameHeader(version, type, transferId, packetName, totalSize);
            }
            case MIDDLE, LAST -> new FrameHeader(version, type, buf.readVarInt(), NO_NAME, UNUSED);
        };
    }

    private static String readName(FriendlyByteBuf buf) {
        try {
            return buf.readUtf(MAX_PACKET_NAME_LENGTH);
        } catch (Exception e) {
            throw new TransportException("Frame carries no readable packet name.", e);
        }
    }
}
