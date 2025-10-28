package de.mrjulsen.mcdragonlib.client.gui.widgets.render;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;

@FunctionalInterface
public interface IStateRenderer<E extends Enum<E>> {
    void renderSprite(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, E state);
}
