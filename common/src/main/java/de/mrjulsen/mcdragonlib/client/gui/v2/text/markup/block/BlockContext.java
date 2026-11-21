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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.BlockKind;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.inline.InlineParser;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.TextStyle;

public final class BlockContext {

    private final CharSequence line;
    private final int length;
    private final int state;
    private final int markupColor;
    private final InlineParser inline;
    private final int contentStart;
    private final int indent;

    private List<StyledSpan> spans = new ArrayList<>(4);

    public BlockContext(CharSequence line, int state, InlineParser inline, int spacesPerIndent, int markupColor) {
        this.line = line;
        this.length = line.length();
        this.state = state;
        this.markupColor = markupColor;
        this.inline = inline;
        this.contentStart = MarkupText.indentOf(line);
        this.indent = this.contentStart / Math.max(1, spacesPerIndent);
    }

    public CharSequence line() {
        return line;
    }

    public char charAt(int index) {
        return line.charAt(index);
    }

    public int length() {
        return length;
    }

    public int state() {
        return state;
    }

    public int markupColor() {
        return markupColor;
    }

    public InlineParser inline() {
        return inline;
    }

    public int contentStart() {
        return contentStart;
    }

    public int indent() {
        return indent;
    }

    public boolean isBlank() {
        return contentStart >= length;
    }

    public String textBetween(int from, int to) {
        return line.subSequence(from, to).toString();
    }

    public List<StyledSpan> spans() {
        return spans;
    }

    public void emitMarkup(int from, int to) {
        if (from < to) {
            spans.add(MarkupText.markup(from, to, markupColor));
        }
    }

    public void emit(StyledSpan span) {
        spans.add(span);
    }

    public void parseInline(int from, int to, TextStyle style) {
        inline.parse(line, from, to, style, markupColor, spans);
    }

    public ParsedLine build(BlockKind kind, int level, boolean checked) {
        return build(kind, level, checked, null, null);
    }

    public ParsedLine build(BlockKind kind, int level, boolean checked, String meta, Object data) {
        return buildWithIndent(kind, indent, level, checked, meta, data);
    }

    public ParsedLine buildWithIndent(BlockKind kind, int indent, int level, boolean checked) {
        return buildWithIndent(kind, indent, level, checked, null, null);
    }

    public ParsedLine buildWithIndent(BlockKind kind, int indent, int level, boolean checked, String meta, Object data) {
        return new ParsedLine(kind, indent, level, checked, spans, meta, data);
    }

    public void reset() {
        if (!spans.isEmpty()) {
            spans = new ArrayList<>(4);
        }
    }
}
