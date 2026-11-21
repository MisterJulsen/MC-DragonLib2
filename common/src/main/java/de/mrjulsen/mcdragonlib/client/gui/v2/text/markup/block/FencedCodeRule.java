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
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.MarkupState;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.TextStyles;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.TextStyle;

public final class FencedCodeRule implements IBlockRule {

    public static final BlockKind KIND = BlockKind.register("code", BlockKind.TRAIT_NO_ALIGNMENT);

    private static final int STATE_FENCE = MarkupState.allocateFlag();

    public static final FencedCodeRule DEFAULT = new FencedCodeRule("`~", 3);

    public int textColor = 0xFFD7BA7D;
    public int backgroundColor = 0x60303030;

    private final String fenceChars;
    private final int minFenceLength;

    public FencedCodeRule(String fenceChars, int minFenceLength) {
        this.fenceChars = fenceChars;
        this.minFenceLength = minFenceLength;
    }

    public boolean isFence(CharSequence line, int start) {
        if (start >= line.length()) {
            return false;
        }
        for (int i = 0; i < fenceChars.length(); i++) {
            if (MarkupText.runLength(line, start, fenceChars.charAt(i)) >= minFenceLength) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ParsedLine parse(BlockContext context) {
        boolean inside = MarkupState.isSet(context.state(), STATE_FENCE);
        if (!inside && !isFence(context.line(), context.contentStart())) {
            return null;
        }

        TextStyle style = TextStyle.DEFAULT
                .withColor(textColor)
                .withBackgroundColor(backgroundColor)
                .with(TextStyles.CODE, true);
        if (context.length() > 0) {
            context.emit(new StyledSpan(0, context.length(), style));
        }
        return context.buildWithIndent(KIND, 0, 0, false);
    }

    @Override
    public int nextState(CharSequence line, int contentStart, int incomingState) {
        if (isFence(line, contentStart)) {
            return MarkupState.toggle(incomingState, STATE_FENCE);
        }
        return MarkupState.isSet(incomingState, STATE_FENCE) ? incomingState : STATE_UNCHANGED;
    }
}
