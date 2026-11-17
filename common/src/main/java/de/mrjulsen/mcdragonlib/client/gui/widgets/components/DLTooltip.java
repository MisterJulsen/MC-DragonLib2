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

package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import net.minecraft.network.chat.FormattedText;

public record DLTooltip(List<? extends FormattedText> lines, int maxWidth) {

    public static final DLTooltip EMPTY = new DLTooltip(List.of(), 100);

    public void render(DLGuiGraphics graphics, int x, int y) {
        GuiUtils.drawTooltip(graphics, graphics.defaultFont(), x, y, lines(), maxWidth());
    }    
}
