package de.mrjulsen.mcdragonlib.client.newgui.widgets.render;

import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.util.Graphics;

@FunctionalInterface
public interface IStateRenderer<E extends Enum<E>> {
    void renderSprite(Graphics graphics, int x, int y, int w, int h, DLGuiComponent component, E state);
}
