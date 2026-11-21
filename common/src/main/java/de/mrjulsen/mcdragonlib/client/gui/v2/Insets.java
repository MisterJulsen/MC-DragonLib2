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

package de.mrjulsen.mcdragonlib.client.gui.v2;

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
