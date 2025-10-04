package de.mrjulsen.mcdragonlib.client.model.mesh;

import java.util.Arrays;

public enum CornerType {
    TOP_LEFT    ((byte)0, new float[] { 0, 0 }),
    BOTTOM_LEFT ((byte)1, new float[] { 0, 1 }),
    BOTTOM_RIGHT((byte)2, new float[] { 1, 1 }),
    TOP_RIGHT   ((byte)3, new float[] { 1, 0 });

    private final byte index;
    private final float[] uv;

    private CornerType(byte index, float[] uv) {
        this.index = index;
        this.uv = uv;
    }

    public int index() {
        return index;
    }

    public float[] uv() {
        return uv;
    }

    public static CornerType getByIndex(int index) {
        return Arrays.stream(values()).filter(x -> x.index() == index).findFirst().orElse(TOP_LEFT);
    }
}
