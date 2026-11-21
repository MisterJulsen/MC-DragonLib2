/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
