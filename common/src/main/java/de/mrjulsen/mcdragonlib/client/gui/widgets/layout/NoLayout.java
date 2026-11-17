/*
 * DragonLib 3 - 1.20.1
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
