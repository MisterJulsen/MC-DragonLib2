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

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.TextAction;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.MarkupSnippet;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.TextContext;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.StyleFlag;
import net.minecraft.network.chat.Component;

public interface IInlineRule {

    int parse(InlineContext context, int at);

    default String delimiterFor(StyleFlag flag) {
        return null;
    }

    default MarkupSnippet styleBlock(String directives, String content) {
        return null;
    }

    default List<TextAction> formatActions() {
        return List.of();
    }

    default List<Component> contextTooltip(TextContext context) {
        return List.of();
    }

    default String reservedCharacters() {
        return "";
    }
}
