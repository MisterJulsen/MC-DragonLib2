package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;

/**
 * A trivial layout manager that performs no layout and reports a zero content size.
 *
 * <p>This is a singleton intended for components that manage their own positions or when no layout
 * behavior is desired. It does not alter child positions or sizes.
 */
public class NoLayout implements ILayoutManager {

    /**
     * Singleton instance for reuse.
     */
    public static final NoLayout INSTANCE = new NoLayout();

    /**
     * Private constructor to enforce singleton usage.
     */
    private NoLayout() {}

    /**
     * Arrange components (no-op).
     *
     * @param host the parent component; ignored.
     * @return a {@link LayoutResult} with zero width and height.
     */
    @Override
    public LayoutResult arrangeComponents(DLGuiComponent host) {
        return new LayoutResult(0, 0);
    }    
}
