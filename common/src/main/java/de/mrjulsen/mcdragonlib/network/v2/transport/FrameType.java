/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.network.v2.transport;

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
