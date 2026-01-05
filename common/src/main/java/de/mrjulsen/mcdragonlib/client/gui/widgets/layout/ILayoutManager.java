package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;

/**
 * Contract for layout managers that arrange child components of a host.
 *
 * <p>Implementations should set child positions and sizes using the host's DLGuiComponent API and
 * return a {@link LayoutResult} describing the content dimensions after layout. Implementations
 * should avoid side effects beyond modifying child component geometry.
 */
public interface ILayoutManager {
    /**
     * Arrange the direct children of the given host component.
     *
     * @param host the parent container whose children will be laid out; the implementation may
     *             query host width/height and should set child positions and sizes accordingly.
     * @return a {@link LayoutResult} describing the bounding content size produced by the layout.
     */
    LayoutResult arrangeComponents(DLGuiComponent host);
}
