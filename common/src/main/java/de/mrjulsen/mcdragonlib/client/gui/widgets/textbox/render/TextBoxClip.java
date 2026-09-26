package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render;

import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

public final class TextBoxClip {

    private TextBoxClip() {
    }

    public static Rectangle clampToScreen(Rectangle bounds) {
        return Rectangle.intersection(bounds, Rectangle.withSize(0, 0, GuiUtils.getScreenWidth(), GuiUtils.getScreenHeight()));
    }

    public static boolean isEmpty(Rectangle bounds) {
        return bounds == null || bounds.width() <= 0 || bounds.height() <= 0;
    }
}
