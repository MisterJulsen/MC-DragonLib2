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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.inline;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.TextStyle;

public final class InlineContext {

    private final InlineParser parser;
    private final CharSequence line;
    private final int markupColor;
    private final List<StyledSpan> out;

    private int limit;
    private int pending;
    private TextStyle style;

    InlineContext(InlineParser parser, CharSequence line, int markupColor, List<StyledSpan> out) {
        this.parser = parser;
        this.line = line;
        this.markupColor = markupColor;
        this.out = out;
    }

    public CharSequence line() {
        return line;
    }

    public char charAt(int index) {
        return line.charAt(index);
    }

    public int limit() {
        return limit;
    }

    public TextStyle style() {
        return style;
    }

    public int markupColor() {
        return markupColor;
    }

    public String textBetween(int from, int to) {
        return line.subSequence(from, to).toString();
    }

    public void flushTo(int column) {
        if (pending < column) {
            out.add(new StyledSpan(pending, column, style));
        }
        pending = column;
    }

    public void emit(StyledSpan span) {
        if (!span.isEmpty()) {
            out.add(span);
        }
    }

    public void emitMarkup(int from, int to) {
        emit(MarkupText.markup(from, to, markupColor));
    }

    public void emitStyled(int from, int to, TextStyle style) {
        emit(new StyledSpan(from, to, style));
    }

    public void parseNested(int from, int to, TextStyle nested) {
        parser.parse(line, from, to, nested, markupColor, out);
    }

    void begin(int from, int to, TextStyle style) {
        this.pending = from;
        this.limit = to;
        this.style = style;
    }

    void advance(int column) {
        this.pending = column;
    }

    int pending() {
        return pending;
    }
}
