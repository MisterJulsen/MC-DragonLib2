package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.StyledSpan;

public record VisualRow(
        int startColumn,
        int endColumn,
        float indentX,
        float width,
        float height,
        float baseline,
        List<StyledSpan> spans,
        float[] spanOffsets
) {
    public static VisualRow flowing(int startColumn, int endColumn, float indentX, float width, float height, List<StyledSpan> spans) {
        return new VisualRow(startColumn, endColumn, indentX, width, height, height, spans, null);
    }

    public boolean isPositioned() {
        return spanOffsets != null;
    }

    public VisualRow withIndentX(float indent) {
        return indent == indentX ? this : new VisualRow(startColumn, endColumn, indent, width, height, baseline, spans, spanOffsets);
    }

    public float offsetOf(int spanIndex, float flowX) {
        return spanOffsets != null && spanIndex < spanOffsets.length ? spanOffsets[spanIndex] : flowX;
    }

    public int length() {
        return endColumn - startColumn;
    }

    public boolean containsColumn(int column) {
        return column >= startColumn && column < endColumn;
    }
}
