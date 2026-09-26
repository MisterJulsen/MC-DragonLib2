package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.TextMeasurer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextBoxStyle;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;

public record SpanDecoration(
        DLGuiGraphics graphics,
        TextStyle style,
        String text,
        float x,
        float y,
        float width,
        float height,
        int color,
        TextBoxStyle boxStyle,
        TextMeasurer measurer
) {
    public float right() {
        return x + width;
    }

    public float bottom() {
        return y + height;
    }
}
