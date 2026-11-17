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

package de.mrjulsen.mcdragonlib.util.math;

import java.util.Objects;

/**
 * Immutable two-dimensional size (width, height) with convenience factories and common constants.
 *
 * <p>Instances validate that width and height are non-negative. Protected constructor allows subclassing.
 */
public class Size {

    /** Zero size constant. */
    public static final Size EMPTY = new Size(0, 0);
    /** Represents an infinite size (positive infinity components). */
    public static final Size INFINITY = new Size(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    
    protected double w;
    protected double h;

    protected Size(double w, double h) {
        this.w = w;
        this.h = h;
    }

    /**
     * Create a Size with the given width and height.
     *
     * @param w width (>= 0)
     * @param h height (>= 0)
     * @return new Size
     * @throws IllegalArgumentException when negative values are supplied
     */
    public static Size of(double w, double h) {
        if (w < 0 || h < 0) {
            throw new IllegalArgumentException(String.format("width and height must not be negative! width=%s, height=%s", w, h));
        }
        return new Size(w, h);
    }

    /**
     * Width (may be fractional).
     */
    public double w() {
        return w;
    }

    /**
     * Height (may be fractional).
     */
    public double h() {
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Size o) {
            return w == o.w && h == o.h;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(w, h);
    }

    @Override
    public String toString() {
        return String.format("Size[xw=%s,h=%s]", w, h);
    }
}
