package de.mrjulsen.mcdragonlib.net.transport;

/**
 * The role a frame plays within a logical message.
 *
 * <p>Messages small enough for one vanilla packet are sent as a single {@link #SINGLE} frame.
 * Larger ones are split into a {@link #FIRST}, any number of {@link #MIDDLE} and one {@link #LAST}
 * frame, which the receiver reassembles before the payload is handed on.
 */
public enum FrameType {

    /** A complete message in one frame. */
    SINGLE((byte) 0),
    /** The opening frame of a split message, carrying the packet id and the total size. */
    FIRST((byte) 1),
    /** A continuation frame of a split message. */
    MIDDLE((byte) 2),
    /** The closing frame of a split message. */
    LAST((byte) 3);

    private static final FrameType[] BY_ID = new FrameType[4];

    static {
        for (FrameType type : values()) {
            BY_ID[type.id] = type;
        }
    }

    private final byte id;

    private FrameType(byte id) {
        this.id = id;
    }

    /**
     * Returns the byte written to the wire for this type.
     *
     * @return the wire id
     */
    public byte getId() {
        return id;
    }

    /**
     * Resolves a wire id back to a frame type.
     *
     * @param id the byte read from the wire
     * @return the matching frame type
     * @throws IllegalArgumentException if the id belongs to no known type
     */
    public static FrameType byId(byte id) {
        if (id < 0 || id >= BY_ID.length) {
            throw new IllegalArgumentException("Unknown frame type id " + id + ".");
        }
        return BY_ID[id];
    }
}
