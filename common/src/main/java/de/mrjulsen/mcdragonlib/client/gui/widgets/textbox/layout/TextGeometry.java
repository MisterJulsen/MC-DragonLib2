package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.StyledSpan;

public final class TextGeometry {

    private TextGeometry() {
    }

    public static float xOfColumn(TextMeasurer measurer, VisualRow row, String lineText, int column) {
        int target = Math.max(row.startColumn(), Math.min(column, row.endColumn()));
        float x = row.indentX();
        float flow = 0.0F;

        List<StyledSpan> spans = row.spans();
        for (int i = 0; i < spans.size(); i++) {
            StyledSpan span = spans.get(i);
            float origin = row.offsetOf(i, flow);
            if (span.start() >= target) {
                break;
            }
            int end = Math.min(span.end(), target);
            float consumed = measurer.width(lineText, span.start(), end, span.style());
            x = row.indentX() + origin + consumed;
            flow = origin + measurer.width(lineText, span.start(), span.end(), span.style());
        }
        return x;
    }

    public static int columnAtX(TextMeasurer measurer, VisualRow row, String lineText, float targetX) {
        float x = targetX - row.indentX();
        if (x <= 0.0F) {
            return row.startColumn();
        }

        float flow = 0.0F;
        List<StyledSpan> spans = row.spans();
        for (int i = 0; i < spans.size(); i++) {
            StyledSpan span = spans.get(i);
            int end = Math.min(span.end(), lineText.length());
            if (span.start() >= end) {
                continue;
            }
            float origin = row.offsetOf(i, flow);
            if (origin >= x) {
                return span.start();
            }

            String runText = lineText.substring(span.start(), end);
            float localX = x - origin;
            if (measurer.fitCharacters(runText, span.style(), localX) < runText.length()) {
                return span.start() + measurer.indexAtWidth(runText, span.style(), localX);
            }
            flow = origin + measurer.width(runText, span.style());
        }
        return row.endColumn();
    }

    public static StyledSpan spanAtColumn(VisualRow row, int column) {
        for (StyledSpan span : row.spans()) {
            if (column >= span.start() && column < span.end()) {
                return span;
            }
        }
        return null;
    }
}
