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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.render;

import java.util.function.IntPredicate;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.document.TextRange;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

public record TextBoxRenderContext(
        int textLeft,
        int textTop,
        int textWidth,
        int textHeight,
        double scrollX,
        double scrollY,
        int caretOffset,
        TextRange selection,
        boolean focused,
        boolean enabled,
        boolean caretVisible,
        boolean highlightCurrentLine,
        String hoveredLink,
        IntPredicate lineHighlight,
        Rectangle gutterArea,
        IHighlightSource highlights,
        Rectangle textClip,
        int maxRows,
        String ellipsis
) {

    public TextBoxRenderContext(int textLeft, int textTop, int textWidth, int textHeight, double scrollX,
                                double scrollY, int caretOffset, TextRange selection, boolean focused,
                                boolean enabled, boolean caretVisible, boolean highlightCurrentLine,
                                String hoveredLink, IntPredicate lineHighlight, Rectangle gutterArea,
                                IHighlightSource highlights, Rectangle textClip) {
        this(textLeft, textTop, textWidth, textHeight, scrollX, scrollY, caretOffset, selection, focused, enabled,
                caretVisible, highlightCurrentLine, hoveredLink, lineHighlight, gutterArea, highlights,
                textClip, 0, null);
    }

    public boolean hasRowLimit() {
        return maxRows > 0;
    }

    public boolean hasEllipsis() {
        return ellipsis != null && !ellipsis.isEmpty();
    }

    public boolean hasGutter() {
        return gutterArea != null;
    }

    public boolean hasHighlights() {
        return highlights != null;
    }

    public boolean hasTextClip() {
        return textClip != null && textClip.width() > 0 && textClip.height() > 0;
    }
}
