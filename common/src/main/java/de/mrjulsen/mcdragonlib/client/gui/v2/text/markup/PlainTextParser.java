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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup;

import java.util.Collections;
import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.TextStyle;

public final class PlainTextParser implements ITextFormatParser {

    public static final PlainTextParser INSTANCE = new PlainTextParser();

    private PlainTextParser() {
    }

    @Override
    public ParsedLine parseLine(CharSequence line, int incomingState) {
        List<StyledSpan> spans = line.isEmpty()
                ? Collections.emptyList()
                : List.of(new StyledSpan(0, line.length(), TextStyle.DEFAULT));
        return ParsedLine.paragraph(spans);
    }

    @Override
    public int nextState(CharSequence line, int incomingState) {
        return INITIAL_STATE;
    }

    @Override
    public String name() {
        return "Plain Text";
    }

    @Override
    public boolean isMarkup() {
        return false;
    }
}
