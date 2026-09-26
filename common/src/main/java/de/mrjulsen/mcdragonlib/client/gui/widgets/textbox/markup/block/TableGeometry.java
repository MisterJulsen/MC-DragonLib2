package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.block;

import de.mrjulsen.mcdragonlib.data.ETextAlignment;

public record TableGeometry(
        float[] columnWidths,
        ETextAlignment[] alignments,
        int rowIndex,
        int rowCount,
        boolean header,
        boolean delimiter
) {
    public float totalWidth() {
        float total = 0.0F;
        for (float width : columnWidths) {
            total += width;
        }
        return total;
    }

    public int columnCount() {
        return columnWidths.length;
    }

    public float columnStart(int column) {
        float x = 0.0F;
        for (int i = 0; i < column && i < columnWidths.length; i++) {
            x += columnWidths[i];
        }
        return x;
    }

    public float columnWidth(int column) {
        return column >= 0 && column < columnWidths.length ? columnWidths[column] : 0.0F;
    }

    public ETextAlignment alignment(int column) {
        return alignments != null && column >= 0 && column < alignments.length
                ? alignments[column]
                : ETextAlignment.LEFT;
    }

    public boolean last() {
        return rowIndex == rowCount - 1;
    }

    public boolean striped() {
        return !header && !delimiter && (rowIndex % 2) == 1;
    }
}
