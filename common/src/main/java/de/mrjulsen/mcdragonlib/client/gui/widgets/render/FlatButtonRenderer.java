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
