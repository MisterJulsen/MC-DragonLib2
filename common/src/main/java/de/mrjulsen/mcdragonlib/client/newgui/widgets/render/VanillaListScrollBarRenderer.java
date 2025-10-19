package de.mrjulsen.mcdragonlib.client.newgui.widgets.render;

import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.components.DLScrollBar.ScrollBarState;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.Color;

public class VanillaListScrollBarRenderer implements IStateRenderer<ScrollBarState> {

    public static final VanillaListScrollBarRenderer VANILLA_SCROLLBAR = new VanillaListScrollBarRenderer();
    
    public VanillaListScrollBarRenderer() {
    }

    @Override
    public void renderSprite(Graphics graphics, int x, int y, int w, int h, DLGuiComponent component, ScrollBarState state) {
        switch (state) {
            case BACKGROUND -> {
                GuiUtils.fill(graphics, x, y, w, h, Color.BLACK);
            }
            case SCROLLER_VERTICAL_DISABLED -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("simple_scroller_vertical_disabled").render(graphics, x, y, w, h);
            case SCROLLER_VERTICAL_DOWN_SELECTED -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("simple_scroller_vertical_down_selected").render(graphics, x, y, w, h);
            case SCROLLER_VERTICAL_SELECTED -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("simple_scroller_vertical_selected").render(graphics, x, y, w, h);
            case SCROLLER_HORIZONTAL_DISABLED -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("simple_scroller_horizontal_disabled").render(graphics, x, y, w, h);
            case SCROLLER_HORIZONTAL_DOWN_SELECTED -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("simple_scroller_horizontal_down_selected").render(graphics, x, y, w, h);
            case SCROLLER_HORIZONTAL_SELECTED -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("simple_scroller_horizontal_selected").render(graphics, x, y, w, h);
            case SCROLLER_HORIZONTAL_NORMAL -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("simple_scroller_horizontal_normal").render(graphics, x, y, w, h);
            default -> DefaultGuiTextures.VANILLA_SCROLLBAR.getSprite("simple_scroller_vertical_normal").render(graphics, x, y, w, h);
        };
    }
}
