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

package de.mrjulsen.mcdragonlib.client.gui.widgets.util;

public enum RenderLayer {
    BACK(500, false),
    MAIN(0, false),
    FRONT(-1000, false),
    SCREEN_SPACE(-1500, true),
    OVERLAY(-2000, true);

    private final int z;
    private final boolean special;

    private RenderLayer(int z, boolean special) {
        this.z = z;
        this.special = special;
    }

    public int z() {
        return z;
    }

    public boolean isSpecial() {
        return special;
    }

}
