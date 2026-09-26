package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.inline;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextAction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.IStyleTransform;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;

public final class DelimiterRule implements IInlineRule {

    private final String delimiter;
    private final IStyleTransform transform;
    private final StyleFlag flag;
    private final String actionKey;

    public DelimiterRule(String delimiter, StyleFlag flag) {
        this(delimiter, IStyleTransform.flag(flag), flag, flag.name());
    }

    public DelimiterRule(String delimiter, IStyleTransform transform, String actionKey) {
        this(delimiter, transform, null, actionKey);
    }

    public DelimiterRule(String delimiter, IStyleTransform transform, StyleFlag flag, String actionKey) {
        this.delimiter = delimiter;
        this.transform = transform;
        this.flag = flag;
        this.actionKey = actionKey;
    }

    public String delimiter() {
        return delimiter;
    }

    @Override
    public int parse(InlineContext context, int at) {
        int length = delimiter.length();
        if (!MarkupText.startsWith(context.line(), at, delimiter) || at + length >= context.limit()) {
            return 0;
        }
        int close = MarkupText.indexOfUnescaped(context.line(), at + length, context.limit(), delimiter);
        if (close < 0 || close == at + length) {
            return 0;
        }

        context.flushTo(at);
        context.emitMarkup(at, at + length);
        context.parseNested(at + length, close, transform.apply(context.style()));
        context.emitMarkup(close, close + length);
        return close + length - at;
    }

    @Override
    public String delimiterFor(StyleFlag flag) {
        return this.flag == flag ? delimiter : null;
    }

    @Override
    public String reservedCharacters() {
        return delimiter.substring(0, 1);
    }
}
