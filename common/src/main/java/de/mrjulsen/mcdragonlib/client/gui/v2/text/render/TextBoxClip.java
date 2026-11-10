package de.mrjulsen.mcdragonlib.client.gui.v2.text.render;

import de.mrjulsen.mcdragonlib.util.math.Rectangle;

public final class TextBoxClip {

    private TextBoxClip() {
    }

    public static boolean isEmpty(Rectangle bounds) {
        return bounds == null || bounds.width() <= 0 || bounds.height() <= 0;
    }
}
