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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block;

import de.mrjulsen.mcdragonlib.data.ETextAlignment;

public final class TableAlignment {

    public static final char LEFT = 'l';
    public static final char CENTER = 'c';
    public static final char RIGHT = 'r';

    private TableAlignment() {
    }

    public static char code(ETextAlignment alignment) {
        return switch (alignment) {
            case CENTER -> CENTER;
            case RIGHT -> RIGHT;
            default -> LEFT;
        };
    }

    public static ETextAlignment of(char code) {
        return switch (code) {
            case CENTER -> ETextAlignment.CENTER;
            case RIGHT -> ETextAlignment.RIGHT;
            default -> ETextAlignment.LEFT;
        };
    }

    public static ETextAlignment[] parse(String codes) {
        if (codes == null || codes.isEmpty()) {
            return null;
        }
        ETextAlignment[] alignments = new ETextAlignment[codes.length()];
        for (int i = 0; i < codes.length(); i++) {
            alignments[i] = of(codes.charAt(i));
        }
        return alignments;
    }
}
