package de.mrjulsen.mcdragonlib.client.gui.widgets.render;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.ScrollBarState;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.DLColor;

public class VanillaListScrollBarRenderer implements IStateRenderer<ScrollBarState> {

    public static final VanillaListScrollBarRenderer VANILLA_SCROLLBAR = new VanillaListScrollBarRenderer();
    
    public VanillaListScrollBarRenderer() {
    }

    @Override
    public void renderSprite(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, ScrollBarState state) {
        switch (state) {
            case BACKGROUND -> {
                GuiUtils.fill(graphics, x, y, w, h, DLColor.BLACK);
            }
            case SCROLLER_VERTICAL_DISABLED -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("simple_scroller_vertical_disabled").render(graphics, x, y, w, h);
            case SCROLLER_VERTICAL_DOWN_SELECTED -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("simple_scroller_vertical_down_selected").render(graphics, x, y, w, h);
            case SCROLLER_VERTICAL_SELECTED -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("simple_scroller_vertical_selected").render(graphics, x, y, w, h);
            case SCROLLER_HORIZONTAL_DISABLED -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("simple_scroller_horizontal_disabled").render(graphics, x, y, w, h);
            case SCROLLER_HORIZONTAL_DOWN_SELECTED -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("simple_scroller_horizontal_down_selected").render(graphics, x, y, w, h);
            case SCROLLER_HORIZONTAL_SELECTED -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("simple_scroller_horizontal_selected").render(graphics, x, y, w, h);
            case SCROLLER_HORIZONTAL_NORMAL -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("simple_scroller_horizontal_normal").render(graphics, x, y, w, h);
            default -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("simple_scroller_vertical_normal").render(graphics, x, y, w, h);
        };
    }
}
