/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
