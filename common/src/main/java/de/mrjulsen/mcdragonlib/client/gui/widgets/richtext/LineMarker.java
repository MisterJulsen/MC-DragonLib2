package de.mrjulsen.mcdragonlib.client.gui.widgets.richtext;

import de.mrjulsen.mcdragonlib.data.ETextAlignment;

public record LineMarker(int startIndex, int endIndex, float lineWidth, float scale, float lineHeight, float y, ETextAlignment alignment) {
    public boolean isEmpty() {
        return endIndex() <= startIndex();
    }
}
