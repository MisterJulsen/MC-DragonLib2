package de.mrjulsen.mcdragonlib.client.render;

import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.util.Graphics;

public interface IGuiComponentRenderer {
    void render(DLGuiComponent component, Graphics graphics, int tint);
}
