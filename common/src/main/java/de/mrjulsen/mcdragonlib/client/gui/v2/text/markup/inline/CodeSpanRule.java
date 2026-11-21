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

import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.StyleFlag;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.TextStyles;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.TextStyle;

public final class CodeSpanRule implements IInlineRule {

    public static final CodeSpanRule BACKTICK = new CodeSpanRule('`');

    public int textColor = 0xFFD7BA7D;
    public int backgroundColor = 0x60303030;

    private final char delimiter;

    public CodeSpanRule(char delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public int parse(InlineContext context, int at) {
        if (context.charAt(at) != delimiter) {
            return 0;
        }
        int close = MarkupText.indexOfUnescaped(context.line(), at + 1, context.limit(), delimiter);
        if (close < 0) {
            return 0;
        }

        TextStyle code = context.style()
                .withColor(textColor)
                .withBackgroundColor(backgroundColor)
                .with(TextStyles.CODE, true);

        context.flushTo(at);
        context.emitMarkup(at, at + 1);
        context.emitStyled(at + 1, close, code);
        context.emitMarkup(close, close + 1);
        return close + 1 - at;
    }

    @Override
    public String delimiterFor(StyleFlag flag) {
        return flag == TextStyles.CODE ? String.valueOf(delimiter) : null;
    }

    @Override
    public String reservedCharacters() {
        return String.valueOf(delimiter);
    }
}
