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

package de.mrjulsen.mcdragonlib.client.model.mesh;


public enum EdgeType {
    LEFT((byte)0),
    BOTTOM((byte)1),
    RIGHT((byte)2),
    TOP((byte)3);

    private final byte index;

    private EdgeType(byte index) {
        this.index = index;
    }

    public int index() {
        return index;
    }

    private static final EdgeType[] VALUES = values();

    public static EdgeType getByIndex(int index) {
        for (EdgeType type : VALUES) {
            if (type.index() == index) return type;
        }
        return LEFT;
    }
}
