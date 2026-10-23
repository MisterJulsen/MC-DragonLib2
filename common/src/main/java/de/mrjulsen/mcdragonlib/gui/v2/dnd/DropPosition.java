package de.mrjulsen.mcdragonlib.gui.v2.dnd;

import de.mrjulsen.mcdragonlib.util.math.Rectangle;

/**
 * Where a dragged payload would land: between two entries or on one of them, plus the index and an
 * optional rectangle the target can render as a marker.
 */
public record DropPosition(Kind kind, int index, Rectangle highlight) {

    /**
     * Whether the payload lands between entries, on an entry, or nowhere.
     */
    public enum Kind {
        NONE,
        INSERT,
        ON
    }

    public static final DropPosition NONE = new DropPosition(Kind.NONE, -1, null);

    /**
     * Insertion at the given index, i.e. the payload ends up before the entry that is there now.
     */
    public static DropPosition insert(int index) {
        return new DropPosition(Kind.INSERT, index, null);
    }

    /**
     * Insertion with a rectangle to render as the insert line, in screen coordinates.
     */
    public static DropPosition insert(int index, Rectangle marker) {
        return new DropPosition(Kind.INSERT, index, marker);
    }

    /**
     * A drop on the target as a whole, without an index.
     */
    public static DropPosition onto() {
        return new DropPosition(Kind.ON, -1, null);
    }

    public static DropPosition onto(Rectangle highlight) {
        return new DropPosition(Kind.ON, -1, highlight);
    }

    /**
     * A drop on one entry, e.g. to move a file into a folder.
     */
    public static DropPosition onto(int index, Rectangle highlight) {
        return new DropPosition(Kind.ON, index, highlight);
    }

    /**
     * Alias for {@link #insert(int)}.
     */
    public static DropPosition at(int index) {
        return insert(index);
    }

    public static DropPosition at(int index, Rectangle marker) {
        return insert(index, marker);
    }

    /**
     * Whether the payload can actually be dropped here.
     */
    public boolean isValid() {
        return kind != Kind.NONE;
    }

    public boolean isInsert() {
        return kind == Kind.INSERT;
    }

    public boolean isOn() {
        return kind == Kind.ON;
    }

    /**
     * The rectangle to render, or null. Same as the highlight component.
     */
    public Rectangle marker() {
        return highlight;
    }
}
