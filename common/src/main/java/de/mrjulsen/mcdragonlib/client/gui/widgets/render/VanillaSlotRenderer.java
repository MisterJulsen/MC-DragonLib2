/*
 * DragonLib 3 - 1.20.1
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

import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData.AbstractSprite;
import de.mrjulsen.mcdragonlib.client.gui.container.DLSlot.SlotState;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.DLColor;

public class VanillaSlotRenderer implements ILayeredStateRenderer<SlotState> {

    public static final VanillaSlotRenderer VANILLA_SLOT = new VanillaSlotRenderer();
    
    @Override
    public void renderSprite(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, SlotState state) {
        AbstractSprite sprite = switch (state) {
            case SELECTED -> DLTextureSheet.DRAGONLIB_UI.getSprite("slot");
            case DISABLED -> DLTextureSheet.DRAGONLIB_UI.getSprite("slot");
            case DISABLED_SELECTED -> DLTextureSheet.DRAGONLIB_UI.getSprite("slot");
            default -> DLTextureSheet.DRAGONLIB_UI.getSprite("slot");
        };
        sprite.render(graphics, x, y, w, h);
    }

    @Override
    public void renderSpritePost(DLGuiGraphics graphics, int x, int y, int w, int h, DLGuiComponent component, SlotState state) {
        switch (state) {
            case SELECTED -> GuiUtils.fill(graphics, 1, 1, 16, 16, DLColor.fromInt(-2130706433));
            default -> {}
        };
    }
}
