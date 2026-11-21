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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.TextStyle;

public final class ComponentJsonParser implements ITextFormatParser {

    public static final ComponentJsonParser INSTANCE = new ComponentJsonParser();

    private static final String PUNCTUATION = "{}[],:";
    private static final String KEYWORDS_START = "tfn";
    private static final String[] KEYWORDS = { "true", "false", "null" };
    private static final int ESCAPE_LENGTH = 2;

    public int keyColor = 0xFF9CDCFE;
    public int stringColor = 0xFFCE9178;
    public int numberColor = 0xFFB5CEA8;
    public int keywordColor = 0xFF569CD6;
    public int punctuationColor = 0xFFD4D4D4;
    public int textColor = 0xFFE0E0E0;

    private ComponentJsonParser() {
    }

    @Override
    public String name() {
        return "Text Component (JSON)";
    }

    @Override
    public boolean isMarkup() {
        return false;
    }

    @Override
    public int nextState(CharSequence line, int incomingState) {
        return INITIAL_STATE;
    }

    @Override
    public ParsedLine parseLine(CharSequence line, int incomingState) {
        List<StyledSpan> spans = new ArrayList<>(8);
        int length = line.length();
        int cursor = 0;

        while (cursor < length) {
            char c = line.charAt(cursor);

            if (c == '"') {
                int end = stringEnd(line, cursor, length);
                spans.add(new StyledSpan(cursor, end, TextStyle.DEFAULT
                        .withColor(isKey(line, end, length) ? keyColor : stringColor)));
                cursor = end;
                continue;
            }
            if (PUNCTUATION.indexOf(c) >= 0) {
                spans.add(new StyledSpan(cursor, cursor + 1, TextStyle.DEFAULT.withColor(punctuationColor)));
                cursor++;
                continue;
            }
            if (c == '-' || Character.isDigit(c)) {
                int end = numberEnd(line, cursor, length);
                spans.add(new StyledSpan(cursor, end, TextStyle.DEFAULT.withColor(numberColor)));
                cursor = end;
                continue;
            }
            if (KEYWORDS_START.indexOf(c) >= 0) {
                int end = keywordEnd(line, cursor, length);
                if (end > cursor) {
                    spans.add(new StyledSpan(cursor, end, TextStyle.DEFAULT.withColor(keywordColor)));
                    cursor = end;
                    continue;
                }
            }

            int end = plainEnd(line, cursor, length);
            spans.add(new StyledSpan(cursor, end, TextStyle.DEFAULT.withColor(textColor)));
            cursor = end;
        }
        return ParsedLine.paragraph(spans);
    }

    private static int stringEnd(CharSequence line, int from, int limit) {
        int i = from + 1;
        while (i < limit) {
            char c = line.charAt(i);
            if (c == '\\') {
                i += ESCAPE_LENGTH;
                continue;
            }
            i++;
            if (c == '"') {
                return i;
            }
        }
        return limit;
    }

    private static boolean isKey(CharSequence line, int stringEnd, int limit) {
        for (int i = stringEnd; i < limit; i++) {
            char c = line.charAt(i);
            if (c == ':') {
                return true;
            }
            if (c != ' ' && c != '\t') {
                return false;
            }
        }
        return false;
    }

    private static int numberEnd(CharSequence line, int from, int limit) {
        int i = from + 1;
        while (i < limit) {
            char c = line.charAt(i);
            if (!Character.isDigit(c) && c != '.' && c != 'e' && c != 'E' && c != '+' && c != '-') {
                break;
            }
            i++;
        }
        return i;
    }

    private static int keywordEnd(CharSequence line, int from, int limit) {
        for (String keyword : KEYWORDS) {
            if (MarkupText.startsWith(line, from, keyword)) {
                return from + keyword.length();
            }
        }
        return from;
    }

    private static int plainEnd(CharSequence line, int from, int limit) {
        int i = from + 1;
        while (i < limit) {
            char c = line.charAt(i);
            if (c == '"' || PUNCTUATION.indexOf(c) >= 0 || Character.isDigit(c) || KEYWORDS_START.indexOf(c) >= 0) {
                break;
            }
            i++;
        }
        return i;
    }
}
