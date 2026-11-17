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

package de.mrjulsen.mcdragonlib.client.gui.v2;

/**
 * Determines where a component's width or height on one axis comes from. Set per axis via
 * {@link GuiComponent#widthMode} and {@link GuiComponent#heightMode}.
 * <p>Everything except {@link #WEIGHT} is resolved by the component itself while measuring; WEIGHT
 * needs a layout that implements it.</p>
 */
public enum SizeMode {
    /**
     * Keeps the size that was set explicitly, e.g. via setSize. The default.
     */
    FIXED,
    /**
     * Shrinks to fit the content: measureContent plus padding.
     */
    AUTO,
    /**
     * Takes all the space the parent offers, or the AUTO size if that space is unbounded.
     */
    FILL,
    /**
     * Takes a fraction of the offered space, given by widthFactor/heightFactor.
     * Falls back to the AUTO size if that space is unbounded.
     */
    PERCENT,
    /**
     * Takes a share of whatever space is left over after all non-growing siblings have been placed,
     * weighted by widthFactor/heightFactor. Only honoured by layouts that support it, elsewhere it behaves like FILL or AUTO.
     */
    WEIGHT
}
