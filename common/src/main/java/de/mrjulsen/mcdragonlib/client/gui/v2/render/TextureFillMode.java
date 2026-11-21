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

package de.mrjulsen.mcdragonlib.client.gui.v2.render;

/**
 * Determines how a source region of a texture is mapped onto the destination area.
 */
public enum TextureFillMode {

    /**
     * Scales the source region so that it covers the destination area exactly.
     */
    STRETCH,

    /**
     * Repeats the source region at its original size until the destination area is filled.
     * Partial tiles at the right and bottom edge are cropped. All tiles are emitted into a
     * single draw call, which makes this the cheapest way to fill large areas with a
     * pattern (dirt background, grids, ...).
     */
    TILE
}
