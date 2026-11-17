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

package de.mrjulsen.mcdragonlib.client.gui.widgets.richtext;

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
