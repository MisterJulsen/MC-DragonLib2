package de.mrjulsen.mcdragonlib.client.gui.widgets.render;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.ScrollBarState;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;

public class VanillaContainerScrollBarRenderer implements IStateRenderer<ScrollBarState> {

    public static final VanillaContainerScrollBarRenderer VANILLA_SCROLLBAR = new VanillaContainerScrollBarRenderer();
    
    public VanillaContainerScrollBarRenderer() {
    }

    @Override
    public void renderSprite(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, ScrollBarState state) {
        switch (state) {
            case BACKGROUND -> DLTextureSheet.DRAGONLIB_UI.getSprite("scrollbar_background").render(graphics, x, y, w, h);
            case SCROLLER_VERTICAL_DISABLED -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("scroller_vertical_disabled").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_VERTICAL_DOWN_SELECTED -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("scroller_vertical_down_selected").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_VERTICAL_SELECTED -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("scroller_vertical_selected").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_HORIZONTAL_DISABLED -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("scroller_horizontal_disabled").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_HORIZONTAL_DOWN_SELECTED -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("scroller_horizontal_down_selected").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_HORIZONTAL_SELECTED -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("scroller_horizontal_selected").render(graphics, x + 1, y + 1, w - 2, h - 2);
            case SCROLLER_HORIZONTAL_NORMAL -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("scroller_horizontal_normal").render(graphics, x + 1, y + 1, w - 2, h - 2);
            default -> DLTextureSheet.VANILLA_SCROLLBAR.getSprite("scroller_vertical_normal").render(graphics, x + 1, y + 1, w - 2, h - 2);
        };
    }
}
