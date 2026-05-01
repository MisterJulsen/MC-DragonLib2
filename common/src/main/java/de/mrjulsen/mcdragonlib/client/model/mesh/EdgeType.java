package de.mrjulsen.mcdragonlib.client.model.mesh;

import java.util.Arrays;

public enum EdgeType {
    LEFT((byte)0),
    BOTTOM((byte)1),
    RIGHT((byte)2),
    TOP((byte)3);

    private final byte index;

    private EdgeType(byte index) {
        this.index = index;
    }

    public int index() {
        return index;
    }

    public static EdgeType getByIndex(int index) {
        return Arrays.stream(values()).filter(x -> x.index() == index).findFirst().orElse(LEFT);
    }
}
