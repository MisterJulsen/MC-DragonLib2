package de.mrjulsen.mcdragonlib.client.newgui.widgets.render;

import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLScrollBar.ScrollBarState;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.Graphics;

public class VanillaContainerScrollBarRenderer implements IStateRenderer<ScrollBarState> {

    public static final VanillaContainerScrollBarRenderer VANILLA_SCROLLBAR = new VanillaContainerScrollBarRenderer();
    
    public VanillaContainerScrollBarRenderer() {
    }

    @Override
    public void renderSprite(Graphics graphics, int x, int y, int w, int h, DLGuiComponent component, ScrollBarState state) {
        switch (state) {
            case BACKGROUND -> DefaultGuiTextures.DRAGONLIB_UI.getSprite("scrollbar_background").render(graphics, x, y, w, h);
            case SCROLLER_VERTICAL_DISABLED -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("scroller_vertical_disabled").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_VERTICAL_DOWN_SELECTED -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("scroller_vertical_down_selected").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_VERTICAL_SELECTED -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("scroller_vertical_selected").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_HORIZONTAL_DISABLED -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("scroller_horizontal_disabled").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_HORIZONTAL_DOWN_SELECTED -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("scroller_horizontal_down_selected").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_HORIZONTAL_SELECTED -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("scroller_horizontal_selected").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_HORIZONTAL_NORMAL -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("scroller_horizontal_normal").render(graphics, x + 1, y + 1, w - 2, h - 2);
            default -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("scroller_vertical_normal").render(graphics, x + 1, y + 1, w - 2, h - 2);
        };
    }
}
