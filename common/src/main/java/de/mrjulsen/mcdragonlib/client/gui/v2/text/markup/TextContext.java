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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup;

public record TextContext(double localX, double localY, int line, int lineStart, String lineText, StyledSpan span, ParsedLine parsed) {

    public static final TextContext NONE = new TextContext(Double.NaN, Double.NaN, -1, 0, "", null, null);

    public boolean hasLine() {
        return line >= 0 && parsed != null;
    }

    public boolean hasSpan() {
        return span != null;
    }

    public BlockKind kind() {
        return parsed == null ? null : parsed.kind();
    }

    public boolean isKind(BlockKind kind) {
        return parsed != null && parsed.kind() == kind;
    }

    public String link() {
        return hasSpan() && span.style().hasLink() ? span.style().link() : null;
    }

    public boolean hasLink() {
        return link() != null;
    }

    public String tooltip() {
        return hasSpan() && span.style().hasTooltip() ? span.style().tooltip() : null;
    }

    public String meta() {
        return parsed == null ? null : parsed.meta();
    }

    public int offsetOf(int column) {
        return lineStart + Math.max(0, column);
    }
}
