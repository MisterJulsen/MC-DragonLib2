package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.inline;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.ILineAlignment;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupSnippet;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.StyleDirectives;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;

public final class StyleBlockRule implements IInlineRule, ILineAlignment {

    public static final StyleBlockRule DEFAULT = new StyleBlockRule(StyleDirectives.DEFAULT, '{', '}', '|');

    private final StyleDirectives directives;
    private final char opening;
    private final char closing;
    private final char pipe;

    public StyleBlockRule(StyleDirectives directives, char opening, char closing, char pipe) {
        this.directives = directives;
        this.opening = opening;
        this.closing = closing;
        this.pipe = pipe;
    }

    public StyleDirectives directives() {
        return directives;
    }

    @Override
    public int parse(InlineContext context, int at) {
        if (context.charAt(at) != opening) {
            return 0;
        }
        int close = MarkupText.matching(context.line(), at, context.limit(), opening, closing);
        if (close < 0) {
            return 0;
        }
        int separator = MarkupText.indexOfUnescaped(context.line(), at + 1, close, pipe);
        if (separator < 0) {
            return 0;
        }

        String list = context.textBetween(at + 1, separator);
        if (!directives.isRecognized(list)) {
            return 0;
        }
        TextStyle styled = directives.apply(list, context.style());

        context.flushTo(at);
        context.emitMarkup(at, separator + 1);
        context.parseNested(separator + 1, close, styled);
        context.emitMarkup(close, close + 1);
        return close + 1 - at;
    }

    @Override
    public String reservedCharacters() {
        return String.valueOf(opening);
    }

    @Override
    public MarkupSnippet styleBlock(String list, String content) {
        return MarkupSnippet.wrapping(opening + list + pipe, content, String.valueOf(closing));
    }

    public ETextAlignment alignmentOf(CharSequence line, int from, int to) {
        if (from >= to || line.charAt(from) != opening || line.charAt(to - 1) != closing) {
            return null;
        }
        if (MarkupText.matching(line, from, to, opening, closing) != to - 1) {
            return null;
        }
        int separator = MarkupText.indexOfUnescaped(line, from + 1, to - 1, pipe);
        if (separator < 0) {
            return null;
        }
        return directives.alignmentOf(line.subSequence(from + 1, separator).toString());
    }
}
