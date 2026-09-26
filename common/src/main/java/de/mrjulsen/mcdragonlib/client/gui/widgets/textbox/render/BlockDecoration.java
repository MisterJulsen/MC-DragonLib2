package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.LineLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.TextMeasurer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.VisualRow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextBoxStyle;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;

public record BlockDecoration(
        DLGuiGraphics graphics,
        TextBoxRenderContext context,
        LineLayout layout,
        VisualRow row,
        int rowIndex,
        int top,
        int bottom,
        int contentX,
        int lineX,
        int markerX,
        TextBoxStyle boxStyle,
        TextMeasurer measurer
) {

    public boolean firstRow() {
        return rowIndex == 0;
    }

    public int textTop() {
        return top + Math.round(row.baseline() - measurer.baseLineHeight());
    }

    public int middle() {
        return (top + bottom) / 2;
    }

    public int right() {
        return context.textLeft() + context.textWidth();
    }
}
