package de.mrjulsen.mcdragonlib.gui.v2;

/**
 * Inner or outer spacing of a component, see {@link GuiComponent#padding} and
 * {@link GuiComponent#margin}.
 */
public record Insets(double top, double right, double bottom, double left) {

    public static final Insets ZERO = new Insets(0);

    /**
     * The same spacing on all four sides.
     */
    public Insets(double all) {
        this(all, all, all, all);
    }

    /**
     * Spacing in CSS shorthand order.
     *
     * @param vertical top and bottom
     * @param horizontal left and right
     */
    public Insets(double vertical, double horizontal) {
        this(vertical, horizontal, vertical, horizontal);
    }

    public static Insets of(double all) {
        return all == 0 ? ZERO : new Insets(all);
    }

    public static Insets of(double vertical, double horizontal) {
        return new Insets(vertical, horizontal);
    }

    /**
     * Combined left and right spacing, i.e. the total width this takes away.
     */
    public double horizontal() {
        return left + right;
    }

    /**
     * Combined top and bottom spacing, i.e. the total height this takes away.
     */
    public double vertical() {
        return top + bottom;
    }

    /**
     * Sums both insets side by side.
     */
    public Insets add(Insets other) {
        return new Insets(top + other.top, right + other.right, bottom + other.bottom, left + other.left);
    }
}
