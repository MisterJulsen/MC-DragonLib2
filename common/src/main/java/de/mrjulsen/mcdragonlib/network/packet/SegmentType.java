package de.mrjulsen.mcdragonlib.network.packet;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum SegmentType {
    SINGLE((byte)0x0),
    START((byte)0x1),
    PART((byte)0x2),
    END((byte)0x3);

    private final byte id;
    private static final Map<Byte, SegmentType> TYPES_BY_ID = Arrays.stream(values()).collect(Collectors.toMap(x -> x.getId(), x -> x));

    private SegmentType(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public static SegmentType getById(byte id) {
        if (TYPES_BY_ID.containsKey(id)) {
            return TYPES_BY_ID.get(id);
        }
        return SINGLE;
    }
}
