package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.block;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.BlockKind;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ParsedLine;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.BlockDecoration;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.IBlockDecorator;

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
