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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.inline;

public final class EscapeRule implements IInlineRule {

    public static final EscapeRule BACKSLASH = new EscapeRule('\\');

    private final char escape;

    public EscapeRule(char escape) {
        this.escape = escape;
    }

    @Override
    public int parse(InlineContext context, int at) {
        if (context.charAt(at) != escape || at + 1 >= context.limit()) {
            return 0;
        }
        context.flushTo(at);
        context.emitMarkup(at, at + 1);
        context.emitStyled(at + 1, at + 2, context.style());
        return 2;
    }

    @Override
    public String reservedCharacters() {
        return String.valueOf(escape);
    }
}
