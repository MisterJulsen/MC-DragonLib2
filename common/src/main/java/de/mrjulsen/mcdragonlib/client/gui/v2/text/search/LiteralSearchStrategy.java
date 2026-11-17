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

public final class LiteralSearchStrategy implements ISearchStrategy {

    public static final LiteralSearchStrategy INSTANCE = new LiteralSearchStrategy();

    @Override
    public boolean supports(SearchQuery query) {
        return !query.isEmpty() && !query.regex();
    }

    @Override
    public SearchHit find(CharSequence text, SearchQuery query, int from, int to) {
        String pattern = query.pattern();
        int length = pattern.length();
        int limit = Math.min(to, text.length()) - length;

        for (int start = Math.max(0, from); start <= limit; start++) {
            if (!matchesAt(text, pattern, start, query.caseSensitive())) {
                continue;
            }
            if (query.wholeWord() && !isWholeWord(text, start, start + length)) {
                continue;
            }
            return SearchHit.of(start, start + length);
        }
        return null;
    }

    @Override
    public String name() {
        return "Literal";
    }

    private static boolean matchesAt(CharSequence text, String pattern, int at, boolean caseSensitive) {
        for (int i = 0; i < pattern.length(); i++) {
            char a = text.charAt(at + i);
            char b = pattern.charAt(i);
            if (a == b) {
                continue;
            }
            if (caseSensitive || Character.toLowerCase(a) != Character.toLowerCase(b)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWholeWord(CharSequence text, int start, int end) {
        boolean beforeIsWord = start > 0 && isWordCharacter(text.charAt(start - 1));
        boolean afterIsWord = end < text.length() && isWordCharacter(text.charAt(end));
        return !beforeIsWord && !afterIsWord;
    }

    public static boolean isWordCharacter(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
