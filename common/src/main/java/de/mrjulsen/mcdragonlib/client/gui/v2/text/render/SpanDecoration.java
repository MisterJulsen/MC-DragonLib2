package de.mrjulsen.mcdragonlib.client.gui.v2.text.render;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.layout.TextMeasurer;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.TextBoxStyle;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.TextStyle;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.DLGuiGraphics;

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
