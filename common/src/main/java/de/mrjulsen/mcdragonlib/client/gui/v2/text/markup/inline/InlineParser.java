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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.MarkupSnippet;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.StyleFlag;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.TextStyle;

public final class InlineParser {

    private final List<IInlineRule> rules;

    public InlineParser(List<IInlineRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public List<IInlineRule> rules() {
        return rules;
    }

    public InlineParser with(IInlineRule... additional) {
        List<IInlineRule> combined = new ArrayList<>(rules);
        Collections.addAll(combined, additional);
        return new InlineParser(combined);
    }

    public void parse(CharSequence line, int from, int to, TextStyle base, int markupColor,
                      List<StyledSpan> out) {
        InlineContext context = new InlineContext(this, line, markupColor, out);
        context.begin(from, to, base);

        int cursor = from;
        while (cursor < to) {
            int consumed = 0;
            for (IInlineRule rule : rules) {
                consumed = rule.parse(context, cursor);
                if (consumed > 0) {
                    break;
                }
            }
            if (consumed > 0) {
                cursor += consumed;
                context.advance(cursor);
            } else {
                cursor++;
            }
        }
        context.flushTo(to);
    }

    public String delimiterFor(StyleFlag flag) {
        for (IInlineRule rule : rules) {
            String delimiter = rule.delimiterFor(flag);
            if (delimiter != null) {
                return delimiter;
            }
        }
        return null;
    }

    public MarkupSnippet styleBlock(String directives, String content) {
        for (IInlineRule rule : rules) {
            MarkupSnippet snippet = rule.styleBlock(directives, content);
            if (snippet != null) {
                return snippet;
            }
        }
        return null;
    }
}
