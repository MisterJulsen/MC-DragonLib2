package de.mrjulsen.mcdragonlib.network.packet;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum PacketType {
    EMPTY((byte)0x0),
    SEND((byte)0x1),
    RECEIVE((byte)0x2),
    SEND_AND_RECEIVE((byte)0x3),
    STREAM((byte)0x4);

    private final byte id;
    private static final Map<Byte, PacketType> TYPES_BY_ID = Arrays.stream(values()).collect(Collectors.toMap(x -> x.getId(), x -> x));

    private PacketType(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public static PacketType getById(byte id) {
        if (TYPES_BY_ID.containsKey(id)) {
            return TYPES_BY_ID.get(id);
        }
        return EMPTY;
    }
}
