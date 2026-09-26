package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout;

import java.util.List;
import java.util.Map;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextBoxStyle;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;

public final class BlockLayoutContext {

    private final LayoutEngine engine;
    private final int line;
    private final String text;
    private final ParsedLine parsed;
    private final List<StyledSpan> spans;
    private final float indent;

    BlockLayoutContext(LayoutEngine engine, int line, String text, ParsedLine parsed, List<StyledSpan> spans,
                       float indent) {
        this.engine = engine;
        this.line = line;
        this.text = text;
        this.parsed = parsed;
        this.spans = spans;
        this.indent = indent;
    }

    public int line() {
        return line;
    }

    public String text() {
        return text;
    }

    public ParsedLine parsed() {
        return parsed;
    }

    public List<StyledSpan> spans() {
        return spans;
    }

    public float indent() {
        return indent;
    }

    public TextStyle baseStyle() {
        return engine.baseStyle();
    }

    public TextBoxStyle style() {
        return engine.style();
    }

    public int indentWidth() {
        return engine.indentWidth();
    }

    public TextMeasurer measurer() {
        return engine.measurer();
    }

    public boolean showsMarkup() {
        return engine.showsMarkup();
    }

    public boolean isWrapping() {
        return engine.isWrapping();
    }

    public float wrapWidth() {
        return engine.wrapWidth();
    }

    public float viewportWidth() {
        return engine.viewportWidth();
    }

    public int lineCount() {
        return engine.lineCount();
    }

    public String textAt(int line) {
        return engine.textOf(line);
    }

    public ParsedLine parsedAt(int line) {
        return engine.parsedLineAt(line);
    }

    public Map<Object, Object> cache() {
        return engine.blockCache();
    }

    public List<VisualRow> textRows() {
        return textRows(indent, 0.0F);
    }

    public List<VisualRow> textRows(float indent, float rightInset) {
        return engine.textRows(text, spans, parsed, indent, rightInset);
    }

    public VisualRow textRow(float indent, int from, int to) {
        return engine.singleRow(text, spans, indent, from, to);
    }

    public VisualRow row(float indent, float width, float height, List<StyledSpan> spans, float[] offsets) {
        return new VisualRow(0, text.length(), indent, width, height, height, spans, offsets);
    }

    public List<StyledSpan> clipSpans(int from, int to) {
        return LayoutEngine.clipSpans(spans, from, to);
    }

    public VisualRow withLeading(VisualRow row) {
        return engine.withLeading(row);
    }

    public VisualRow padded(VisualRow row, float above, float below) {
        return LayoutEngine.padded(row, above, below);
    }

    public float width(int from, int to, TextStyle style) {
        return engine.measurer().width(text, from, to, style);
    }
}
