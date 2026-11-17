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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.search;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.document.TextRange;

public record SearchHit(int start, int end, String[] groups) {

    public static SearchHit of(int start, int end) {
        return new SearchHit(start, end, null);
    }

    public int length() {
        return end - start;
    }

    public boolean isEmpty() {
        return end <= start;
    }

    public TextRange range() {
        return new TextRange(start, end);
    }

    public String group(int index) {
        if (groups == null || index < 0 || index >= groups.length) {
            return null;
        }
        return groups[index];
    }
}
