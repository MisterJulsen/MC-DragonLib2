package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.block;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.BlockKind;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupState;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyles;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;

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
