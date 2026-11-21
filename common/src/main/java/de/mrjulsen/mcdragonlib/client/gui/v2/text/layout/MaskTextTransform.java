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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.layout;

import java.util.Arrays;

public final class MaskTextTransform implements ITextTransform {

    public static final char BULLET = '●';
    public static final char ASTERISK = '*';

    private final char mask;

    public MaskTextTransform(char mask) {
        this.mask = mask;
    }

    public char mask() {
        return mask;
    }

    @Override
    public String apply(int line, String text) {
        if (text.isEmpty()) {
            return text;
        }
        char[] masked = new char[text.length()];
        Arrays.fill(masked, mask);
        return new String(masked);
    }
}
