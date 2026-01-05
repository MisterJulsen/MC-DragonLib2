package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

/**
 * Immutable description of the content area produced by a layout manager.
 *
 * <p>Fields:
 * <ul>
 * <li>contentWidth: the width in pixels required to contain the laid-out children (does not include
 *                 any external host margin but usually includes layout padding right).</li>
 * <li>contentHeight: the height in pixels required similarly.</li>
 * </ul>
 *
 * <p>Callers can use the provided helper methods to test whether the computed content will overflow
 * a given host size.
 *
 * @param contentWidth total content width in pixels
 * @param contentHeight total content height in pixels
 */
public record LayoutResult(int contentWidth, int contentHeight) {
    
    /**
     * Constant representing an empty layout result (zero width and height).
     */
    public static final LayoutResult EMPTY = new LayoutResult(0, 0);

    /**
     * Check whether the computed content width exceeds the supplied host width.
     *
     * @param hostWidth host width in pixels to compare against.
     * @return true if contentWidth > hostWidth.
     */
    public boolean causesOverflowX(int hostWidth) {
        return contentWidth > hostWidth;
    }
    
    /**
     * Check whether the computed content height exceeds the supplied host height.
     *
     * @param hostHeight host height in pixels to compare against.
     * @return true if contentHeight > hostHeight.
     */
    public boolean causesOverflowY(int hostHeight) {
        return contentHeight > hostHeight;
    }
}
