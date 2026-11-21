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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.layout;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.ParsedLine;

public record LineLayout(
        int lineIndex,
        long generation,
        int wrapWidth,
        ParsedLine parsed,
        List<VisualRow> rows,
        float height,
        float width,
        Object attachment
) {
    public LineLayout(int lineIndex, long generation, int wrapWidth, ParsedLine parsed, List<VisualRow> rows, float height, float width) {
        this(lineIndex, generation, wrapWidth, parsed, rows, height, width, null);
    }

    public <T> T attachmentAs(Class<T> type) {
        return type.isInstance(attachment) ? type.cast(attachment) : null;
    }

    public int rowCount() {
        return rows.size();
    }

    public VisualRow row(int index) {
        return rows.get(Math.max(0, Math.min(index, rows.size() - 1)));
    }

    public int rowOfColumn(int column) {
        for (int i = 0; i < rows.size(); i++) {
            if (column < rows.get(i).endColumn()) {
                return i;
            }
        }
        return rows.size() - 1;
    }

    public float rowTop(int index) {
        float y = 0.0F;
        for (int i = 0; i < index && i < rows.size(); i++) {
            y += rows.get(i).height();
        }
        return y;
    }

    public boolean isValid(long generation, int wrapWidth) {
        return this.generation == generation && this.wrapWidth == wrapWidth;
    }
}
