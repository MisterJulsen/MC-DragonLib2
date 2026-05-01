package de.mrjulsen.mcdragonlib.client.newgui.widgets.richtext;

@FunctionalInterface
public interface WidthProvider {
    float getWidth(TextStyle style, int character);
}
