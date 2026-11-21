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

/**
 * Mutable variant of Rectangle exposing setters for the corner coordinates.
 *
 * <p>Intended for cases where an existing rectangle needs to be updated frequently.
 */
public class MutableRectangle extends Rectangle {

    protected MutableRectangle(int x1, int y1, int x2, int y2) {
        super(x1, y1, x2, y2);
    }

    public void setX1(int x) {
        this.x1 = x;
    }

    public void setY1(int y) {
        this.y1 = y;
    }

    public void setX2(int x) {
        this.x2 = x;
    }

    public void setY2(int y) {
        this.y2 = y;
    }

    public void setWidth(int w) {
        this.x2 = x1 + w;
    }

    public void setHeight(int h) {
        this.y2 = y1 + h;
    }   
}
