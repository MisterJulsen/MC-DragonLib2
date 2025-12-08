package de.mrjulsen.mcdragonlib.client.gui.widgets.richtext;

/**
 * Represents padding values for top, right, bottom, and left.
 */
public record PaddingF(float top, float right, float bottom, float left) {

    public static final PaddingF ZERO = new PaddingF(0);
    /**
     * Creates a Padding instance with the same value for all sides.
     * @param all The padding value for all sides.
     */
    public PaddingF(float all) {
        this(all, all, all, all);
    }

    public float width() {
        return right() - left();
    }

    public float height() {
        return bottom() - top();
    }

}
