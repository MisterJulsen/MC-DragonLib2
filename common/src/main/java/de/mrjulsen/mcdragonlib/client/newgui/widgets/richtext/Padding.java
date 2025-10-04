package de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext;

/**
 * Represents padding values for top, right, bottom, and left.
 */
public record Padding(int top, int right, int bottom, int left) {

    public static final Padding ZERO = new Padding(0);
    /**
     * Creates a Padding instance with the same value for all sides.
     * @param all The padding value for all sides.
     */
    public Padding(int all) {
        this(all, all, all, all);
    }

    public int width() {
        return right() - left();
    }

    public int height() {
        return bottom() - top();
    }

}
