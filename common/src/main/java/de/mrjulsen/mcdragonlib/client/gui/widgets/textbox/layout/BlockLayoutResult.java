package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout;

import java.util.List;

public record BlockLayoutResult(List<VisualRow> rows, Object attachment) {

    public static BlockLayoutResult of(List<VisualRow> rows) {
        return new BlockLayoutResult(rows, null);
    }

    public static BlockLayoutResult of(List<VisualRow> rows, Object attachment) {
        return new BlockLayoutResult(rows, attachment);
    }

    public static BlockLayoutResult of(VisualRow row) {
        return new BlockLayoutResult(List.of(row), null);
    }

    public static BlockLayoutResult of(VisualRow row, Object attachment) {
        return new BlockLayoutResult(List.of(row), attachment);
    }

    public boolean isEmpty() {
        return rows == null || rows.isEmpty();
    }
}
