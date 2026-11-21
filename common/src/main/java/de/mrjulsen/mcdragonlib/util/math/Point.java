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

package de.mrjulsen.mcdragonlib.util.math;

import java.util.Objects;

/**
 * Simple immutable 2D point with utility helpers.
 */
public class Point {
    
    protected double x;
    protected double y;

    protected Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Point of(double x, double y) {
        return new Point(x, y);
    }

    /**
     * Euclidean distance between two points.
     */
    public static double distance(Point a, Point b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        double dX = Math.abs(a.x() - b.x());
        double dY = Math.abs(a.y() - b.y());
        return Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
    }

    /**
     * Compute centroid/center of the provided points.
     *
     * @throws NullPointerException when points is null
     */
    public static Point center(Point... points) {
        Objects.requireNonNull(points);
        double sumX = 0;
        double sumY = 0;
        for (Point p : points) {
            sumX += p.x;
            sumY += p.y;
        }
        double mX = sumX / points.length;
        double mY = sumY / points.length;
        return new Point(mX, mY);
    }

    public Point add(Point p) {
        return new Point(x + p.x, y + p.y);
    }

    public double x() { return x; }
    public double y() { return y; }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point o) {
            return x == o.x && y == o.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return String.format("Point[x=%s,y=%s]", x, y);
    }
}
