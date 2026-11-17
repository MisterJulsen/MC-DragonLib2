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


public enum CornerType {
    TOP_LEFT    ((byte)0, new float[] { 0, 0 }),
    BOTTOM_LEFT ((byte)1, new float[] { 0, 1 }),
    BOTTOM_RIGHT((byte)2, new float[] { 1, 1 }),
    TOP_RIGHT   ((byte)3, new float[] { 1, 0 });

    private final byte index;
    private final float[] uv;

    private CornerType(byte index, float[] uv) {
        this.index = index;
        this.uv = uv;
    }

    public int index() {
        return index;
    }

    public float[] uv() {
        return uv;
    }

    private static final CornerType[] VALUES = values();

    public static CornerType getByIndex(int index) {
        for (CornerType type : VALUES) {
            if (type.index() == index) return type;
        }
        return TOP_LEFT;
    }
}
