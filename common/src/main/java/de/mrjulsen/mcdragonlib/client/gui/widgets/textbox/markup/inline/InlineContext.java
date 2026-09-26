package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.inline;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;

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
