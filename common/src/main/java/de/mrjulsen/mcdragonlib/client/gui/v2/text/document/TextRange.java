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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.document;

public record TextRange(int start, int end) {

    public static final TextRange EMPTY = new TextRange(0, 0);

    public TextRange {
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
    }

    public static TextRange of(int a, int b) {
        return new TextRange(a, b);
    }

    public static TextRange at(int offset) {
        return new TextRange(offset, offset);
    }

    public int length() {
        return end - start;
    }

    public boolean isEmpty() {
        return start == end;
    }

    public boolean contains(int offset) {
        return offset >= start && offset < end;
    }

    public boolean intersects(TextRange other) {
        return start < other.end && other.start < end;
    }

    public TextRange intersection(TextRange other) {
        int s = Math.max(start, other.start);
        int e = Math.min(end, other.end);
        return s < e ? new TextRange(s, e) : TextRange.at(start);
    }

    public TextRange shift(int delta) {
        return new TextRange(start + delta, end + delta);
    }

    public TextRange clamp(int limit) {
        return new TextRange(Math.max(0, Math.min(start, limit)), Math.max(0, Math.min(end, limit)));
    }
}
