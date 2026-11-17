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

public record SearchQuery(String pattern, boolean caseSensitive, boolean wholeWord, boolean regex) {

    public static final SearchQuery EMPTY = new SearchQuery("", false, false, false);

    public static SearchQuery of(String pattern) {
        return new SearchQuery(pattern == null ? "" : pattern, false, false, false);
    }

    public boolean isEmpty() {
        return pattern == null || pattern.isEmpty();
    }

    public SearchQuery withPattern(String pattern) {
        return new SearchQuery(pattern == null ? "" : pattern, caseSensitive, wholeWord, regex);
    }

    public SearchQuery withCaseSensitive(boolean caseSensitive) {
        return new SearchQuery(pattern, caseSensitive, wholeWord, regex);
    }

    public SearchQuery withWholeWord(boolean wholeWord) {
        return new SearchQuery(pattern, caseSensitive, wholeWord, regex);
    }

    public SearchQuery withRegex(boolean regex) {
        return new SearchQuery(pattern, caseSensitive, wholeWord, regex);
    }
}
