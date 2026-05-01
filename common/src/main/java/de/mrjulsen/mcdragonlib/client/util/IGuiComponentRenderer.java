package de.mrjulsen.mcdragonlib.client.util;

import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;

public interface IGuiComponentRenderer {
    void render(DLGuiComponent component, Graphics graphics, int tint);
}
