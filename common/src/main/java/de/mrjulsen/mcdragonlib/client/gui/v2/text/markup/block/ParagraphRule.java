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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.BlockKind;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.TextStyle;

public final class ParagraphRule implements IBlockRule {

    public static final ParagraphRule DEFAULT = new ParagraphRule();

    @Override
    public ParsedLine parse(BlockContext context) {
        if (context.isBlank()) {
            if (context.length() > 0) {
                context.emit(new StyledSpan(0, context.length(), TextStyle.DEFAULT));
            }
            return context.buildWithIndent(BlockKind.PARAGRAPH, 0, 0, false);
        }

        context.emitMarkup(0, context.contentStart());
        context.parseInline(context.contentStart(), context.length(), TextStyle.DEFAULT);
        return context.buildWithIndent(BlockKind.PARAGRAPH, 0, 0, false);
    }
}
