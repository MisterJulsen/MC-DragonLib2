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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.block;

import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.BlockKind;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.render.BlockDecoration;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.render.IBlockDecorator;

public final class HorizontalRuleRule implements IBlockRule, IBlockDecorator {

    public static final BlockKind KIND = BlockKind.register("rule", BlockKind.TRAIT_DECORATION);

    public static final HorizontalRuleRule DEFAULT = new HorizontalRuleRule("-*_", 3);

    public int color = 0xFF505050;
    public int thickness = 1;

    private final String ruleChars;
    private final int minLength;

    public HorizontalRuleRule(String ruleChars, int minLength) {
        this.ruleChars = ruleChars;
        this.minLength = minLength;
    }

    @Override
    public ParsedLine parse(BlockContext context) {
        if (context.isBlank()) {
            return null;
        }
        char marker = context.charAt(context.contentStart());
        if (ruleChars.indexOf(marker) < 0) {
            return null;
        }

        int count = 0;
        for (int i = context.contentStart(); i < context.length(); i++) {
            char c = context.charAt(i);
            if (c == marker) {
                count++;
            } else if (c != ' ') {
                return null;
            }
        }
        if (count < minLength) {
            return null;
        }

        context.emitMarkup(0, context.length());
        return context.build(KIND, 0, false);
    }

    @Override
    public IBlockDecorator decoratorOf(BlockKind kind) {
        return kind == KIND ? this : null;
    }

    @Override
    public void render(BlockDecoration d) {
        d.graphics().graphics().fill(d.contentX(), d.middle(), d.right(), d.middle() + thickness, color);
    }
}
