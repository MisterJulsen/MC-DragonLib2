package de.mrjulsen.mcdragonlib.client.gui.widgets.render;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;

public interface ILayeredStateRenderer<E extends Enum<E>> extends IStateRenderer<E> {
    void renderSpritePost(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, E state);
}
