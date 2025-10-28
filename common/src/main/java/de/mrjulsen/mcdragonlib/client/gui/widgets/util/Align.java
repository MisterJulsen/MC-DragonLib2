package de.mrjulsen.mcdragonlib.client.gui.widgets.util;

import java.util.Arrays;

public enum Align {
    TOP_LEFT("top_left", (byte)0, CursorType.TLBRRESIZE, AffectedTransformtion.POSITION),
    TOP("top", (byte)1, CursorType.VRESIZE, AffectedTransformtion.POSITION),
    TOP_RIGHT("top_right", (byte)2, CursorType.TRBLRESIZE, AffectedTransformtion.POSITION),
    LEFT("left", (byte)3, CursorType.HRESIZE, AffectedTransformtion.POSITION),
    CENTER("center", (byte)4, null, AffectedTransformtion.NONE),
    RIGHT("right", (byte)5, CursorType.HRESIZE, AffectedTransformtion.SIZE),
    BOTTOM_LEFT("bottom_left", (byte)6, CursorType.TRBLRESIZE, AffectedTransformtion.SIZE),
    BOTTOM("bottom", (byte)7, CursorType.VRESIZE, AffectedTransformtion.SIZE),
    BOTTOM_RIGHT("bottom_right", (byte)8, CursorType.TLBRRESIZE, AffectedTransformtion.SIZE);

    private final String name;
    private  final byte id;
    private final CursorType cursor;
    private final AffectedTransformtion transform;

    private Align(String name, byte id, CursorType cursor, AffectedTransformtion transform) {
        this.name = name;
        this.id = id;
        this.cursor = cursor;
        this.transform = transform;
    }

    public String getName() {
        return name;
    }

    public byte getId() {
        return id;
    }

    public CursorType getCursor() {
        return cursor;
    }

    public AffectedTransformtion getAffectedTransformation() {
        return transform;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Align getById(int id) {
        return Arrays.stream(values()).filter(x -> x.getId() == id).findFirst().orElse(CENTER);
    }


    public boolean isRightAlign() {
        return this == BOTTOM_RIGHT || this == RIGHT || this == TOP_RIGHT;
    }

    public boolean isLeftAlign() {
        return this == BOTTOM_LEFT || this == LEFT || this == TOP_LEFT;
    }

    public boolean isTopAlign() {
        return this == TOP_LEFT || this == TOP || this == TOP_RIGHT;
    }

    public boolean isBottomAlign() {
        return this == BOTTOM_LEFT || this == BOTTOM || this == BOTTOM_RIGHT;
    }

    public boolean isVerticalCentered() {
        return this == TOP || this == CENTER || this == BOTTOM;
    }

    public boolean isHorizontalCentered() {
        return this == LEFT || this == CENTER || this == RIGHT;
    }

    public static enum AffectedTransformtion {
        NONE,
        POSITION,
        SIZE;
    }
}
