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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block;

import de.mrjulsen.mcdragonlib.data.ETextAlignment;

public record TableGeometry(
        float[] columnWidths,
        ETextAlignment[] alignments,
        int rowIndex,
        int rowCount,
        boolean header,
        boolean delimiter
) {
    public float totalWidth() {
        float total = 0.0F;
        for (float width : columnWidths) {
            total += width;
        }
        return total;
    }

    public int columnCount() {
        return columnWidths.length;
    }

    public float columnStart(int column) {
        float x = 0.0F;
        for (int i = 0; i < column && i < columnWidths.length; i++) {
            x += columnWidths[i];
        }
        return x;
    }

    public float columnWidth(int column) {
        return column >= 0 && column < columnWidths.length ? columnWidths[column] : 0.0F;
    }

    public ETextAlignment alignment(int column) {
        return alignments != null && column >= 0 && column < alignments.length
                ? alignments[column]
                : ETextAlignment.LEFT;
    }

    public boolean last() {
        return rowIndex == rowCount - 1;
    }

    public boolean striped() {
        return !header && !delimiter && (rowIndex % 2) == 1;
    }
}
