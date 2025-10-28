package de.mrjulsen.mcdragonlib.client.gui.widgets.richtext;

@FunctionalInterface
public interface WidthProvider {
    float getWidth(TextStyle style, int character);
}
