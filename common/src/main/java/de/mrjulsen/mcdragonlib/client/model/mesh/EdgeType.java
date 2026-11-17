package de.mrjulsen.mcdragonlib.client.model.mesh;


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

    private static final EdgeType[] VALUES = values();

    public static EdgeType getByIndex(int index) {
        for (EdgeType type : VALUES) {
            if (type.index() == index) return type;
        }
        return LEFT;
    }
}
