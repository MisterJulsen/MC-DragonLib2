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

package de.mrjulsen.mcdragonlib.client.model.mesh;

public enum Rotation {
    R0(0, 0),
    R90(90, 1),
    R180(180, 2),
    R270(270, 3);

    private final int degrees;
    private final int iterations;

    private Rotation(int degrees, int iterations) {
        this.degrees = degrees;
        this.iterations = iterations;
    }

    public int getDegrees() {
        return degrees;
    }

    public int getIterations() {
        return iterations;
    }
}
