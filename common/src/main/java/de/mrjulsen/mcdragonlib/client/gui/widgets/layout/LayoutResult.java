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
