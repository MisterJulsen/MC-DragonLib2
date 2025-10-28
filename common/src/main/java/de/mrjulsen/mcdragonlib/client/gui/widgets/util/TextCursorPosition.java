package de.mrjulsen.mcdragonlib.client.gui.widgets.util;

import org.jetbrains.annotations.NotNull;

import de.mrjulsen.mcdragonlib.data.ETextAlignment;

public record TextCursorPosition(int index, int indexInLine, float x, float y, float lineHeight, ETextAlignment alignment) {
    @Override
    public @NotNull String toString() {
        return String.format("{x=%s,y=%s,lh=%s}", x, y, lineHeight);
    }
}
