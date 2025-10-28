package de.mrjulsen.mcdragonlib.client.render;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;

public interface IGuiComponentRenderer {
    void render(DLGuiComponent component, DLGuiGraphics graphics, int tint);
}
