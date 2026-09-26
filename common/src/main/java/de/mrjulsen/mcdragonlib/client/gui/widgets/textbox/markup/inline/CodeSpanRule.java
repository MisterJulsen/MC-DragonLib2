package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.inline;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextAction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyles;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;

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
