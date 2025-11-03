package de.mrjulsen.mcdragonlib.client.gui.widgets.render;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton.ButtonState;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.DLColor;

public class FlatButtonRenderer implements IStateRenderer<ButtonState> {

    public static final FlatButtonRenderer INSTANCE = new FlatButtonRenderer();
    
    @Override
    public void renderSprite(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, ButtonState state) {
        switch (state) {
            case SELECTED -> GuiUtils.fill(graphics, x, y, w, h, DLColor.fromInt(0x30000000));
            case DOWN -> GuiUtils.fill(graphics, x, y, w, h, DLColor.fromInt(0x60000000));
            case DOWN_SELECTED -> GuiUtils.fill(graphics, x, y, w, h, DLColor.fromInt(0x60000000));
            default -> {}
        }
    }
}
