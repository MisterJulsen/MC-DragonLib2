package de.mrjulsen.mcdragonlib.client.newgui.widgets.render;

import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLScrollBar.ScrollBarState;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiTexture;

public class VanillaContainerScrollBarRenderer implements IStateRenderer<ScrollBarState> {

    public static final VanillaContainerScrollBarRenderer VANILLA_SCROLLBAR = new VanillaContainerScrollBarRenderer();
    
    public VanillaContainerScrollBarRenderer() {
    }

    @Override
    public void renderSprite(Graphics graphics, int x, int y, int w, int h, DLGuiComponent component, ScrollBarState state) {
        switch (state) {
            case BACKGROUND -> GuiTexture.DRAGONLIB_UI.getSprite("scrollbar_background").render(graphics, x, y, w, h);
            case SCROLLER_VERTICAL_DISABLED -> GuiTexture.VANILLA_SCROLLBAR.getSprite("scroller_vertical_disabled").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_VERTICAL_DOWN_SELECTED -> GuiTexture.VANILLA_SCROLLBAR.getSprite("scroller_vertical_down_selected").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_VERTICAL_SELECTED -> GuiTexture.VANILLA_SCROLLBAR.getSprite("scroller_vertical_selected").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_HORIZONTAL_DISABLED -> GuiTexture.VANILLA_SCROLLBAR.getSprite("scroller_horizontal_disabled").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_HORIZONTAL_DOWN_SELECTED -> GuiTexture.VANILLA_SCROLLBAR.getSprite("scroller_horizontal_down_selected").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_HORIZONTAL_SELECTED -> GuiTexture.VANILLA_SCROLLBAR.getSprite("scroller_horizontal_selected").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_HORIZONTAL_NORMAL -> GuiTexture.VANILLA_SCROLLBAR.getSprite("scroller_horizontal_normal").render(graphics, x + 1, y + 1, w - 2, h - 2);
            default -> GuiTexture.VANILLA_SCROLLBAR.getSprite("scroller_vertical_normal").render(graphics, x + 1, y + 1, w - 2, h - 2);
        };
    }
}
