package de.mrjulsen.mcdragonlib.client.newgui.widgets.util;

import de.mrjulsen.mcdragonlib.core.ETextAlignment;
import org.jetbrains.annotations.NotNull;

public record TextCursorPosition(int index, int indexInLine, float x, float y, float lineHeight, ETextAlignment alignment) {
    @Override
    public @NotNull String toString() {
        return String.format("{x=%s,y=%s,lh=%s}", x, y, lineHeight);
    }
}
