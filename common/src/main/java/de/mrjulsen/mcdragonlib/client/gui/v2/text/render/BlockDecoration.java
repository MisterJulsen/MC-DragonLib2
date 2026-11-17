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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.render;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.layout.LineLayout;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.layout.TextMeasurer;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.layout.VisualRow;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.TextBoxStyle;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.DLGuiGraphics;

public record BlockDecoration(
        DLGuiGraphics graphics,
        TextBoxRenderContext context,
        LineLayout layout,
        VisualRow row,
        int rowIndex,
        int top,
        int bottom,
        int contentX,
        int lineX,
        int markerX,
        TextBoxStyle boxStyle,
        TextMeasurer measurer
) {

    public boolean firstRow() {
        return rowIndex == 0;
    }

    public int textTop() {
        return top + Math.round(row.baseline() - measurer.baseLineHeight());
    }

    public int middle() {
        return (top + bottom) / 2;
    }

    public int right() {
        return context.textLeft() + context.textWidth();
    }
}
