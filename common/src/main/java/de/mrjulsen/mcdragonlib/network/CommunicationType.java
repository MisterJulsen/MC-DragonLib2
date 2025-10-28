package de.mrjulsen.mcdragonlib.network;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum CommunicationType {
    NONE((byte)0x0),
    REQUEST((byte)0x1),
    RESPONSE((byte)0x2);

    private final byte id;
    private static final Map<Byte, CommunicationType> TYPES_BY_ID = Arrays.stream(values()).collect(Collectors.toMap(x -> x.getId(), x -> x));

    private CommunicationType(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public static CommunicationType getById(byte id) {
        if (TYPES_BY_ID.containsKey(id)) {
            return TYPES_BY_ID.get(id);
        }
        return NONE;
    }
}
