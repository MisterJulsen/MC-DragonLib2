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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.layout;

import java.util.List;

public record BlockLayoutResult(List<VisualRow> rows, Object attachment) {

    public static BlockLayoutResult of(List<VisualRow> rows) {
        return new BlockLayoutResult(rows, null);
    }

    public static BlockLayoutResult of(List<VisualRow> rows, Object attachment) {
        return new BlockLayoutResult(rows, attachment);
    }

    public static BlockLayoutResult of(VisualRow row) {
        return new BlockLayoutResult(List.of(row), null);
    }

    public static BlockLayoutResult of(VisualRow row, Object attachment) {
        return new BlockLayoutResult(List.of(row), attachment);
    }

    public boolean isEmpty() {
        return rows == null || rows.isEmpty();
    }
}
