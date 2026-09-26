package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.block;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.BlockKind;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextAction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyles;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;

public final class HeadingRule implements IBlockRule {

    public static final BlockKind KIND = BlockKind.register("heading", BlockKind.TRAIT_NONE);

    public static final HeadingRule DEFAULT = new HeadingRule('#', 6);

    public int color = 0xFFFFFFFF;
    public float[] scales = { 2.0F, 1.6F, 1.35F, 1.2F, 1.1F, 1.0F };

    private final char marker;
    private final int maxLevel;

    public HeadingRule(char marker, int maxLevel) {
        this.marker = marker;
        this.maxLevel = maxLevel;
    }

    @Override
    public String reservedCharacters() {
        return String.valueOf(marker);
    }

    public float scaleOf(int level) {
        int index = Math.max(1, Math.min(level, scales.length)) - 1;
        return scales[index];
    }

    @Override
    public ParsedLine parse(BlockContext context) {
        if (context.isBlank()) {
            return null;
        }
        int start = context.contentStart();
        int level = MarkupText.runLength(context.line(), start, marker);
        if (level < 1 || level > maxLevel || start + level >= context.length() || context.charAt(start + level) != ' ') {
            return null;
        }

        int contentStart = start + level + 1;
        context.emitMarkup(0, contentStart);
        context.parseInline(contentStart, context.length(), TextStyle.DEFAULT
                .withColor(color)
                .withScale(scaleOf(level))
                .with(TextStyles.BOLD, true));
        return context.build(KIND, level, false);
    }
}
