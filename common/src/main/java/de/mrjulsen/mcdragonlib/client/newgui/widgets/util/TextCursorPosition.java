package de.mrjulsen.mcdragonlib.client.newgui.widgets.util;

import de.mrjulsen.mcdragonlib.core.EAlignment;
import org.jetbrains.annotations.NotNull;

public record TextCursorPosition(int index, int indexInLine, float x, float y, float lineHeight, EAlignment alignment) {
    @Override
    public @NotNull String toString() {
        return String.format("{x=%s,y=%s,lh=%s}", x, y, lineHeight);
    }
}
