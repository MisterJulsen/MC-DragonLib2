package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.inline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupSnippet;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.StyledSpan;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;

public final class InlineParser {

    private final List<IInlineRule> rules;

    public InlineParser(List<IInlineRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public List<IInlineRule> rules() {
        return rules;
    }

    public InlineParser with(IInlineRule... additional) {
        List<IInlineRule> combined = new ArrayList<>(rules);
        Collections.addAll(combined, additional);
        return new InlineParser(combined);
    }

    public void parse(CharSequence line, int from, int to, TextStyle base, int markupColor,
                      List<StyledSpan> out) {
        InlineContext context = new InlineContext(this, line, markupColor, out);
        context.begin(from, to, base);

        int cursor = from;
        while (cursor < to) {
            int consumed = 0;
            for (IInlineRule rule : rules) {
                consumed = rule.parse(context, cursor);
                if (consumed > 0) {
                    break;
                }
            }
            if (consumed > 0) {
                cursor += consumed;
                context.advance(cursor);
            } else {
                cursor++;
            }
        }
        context.flushTo(to);
    }

    public String delimiterFor(StyleFlag flag) {
        for (IInlineRule rule : rules) {
            String delimiter = rule.delimiterFor(flag);
            if (delimiter != null) {
                return delimiter;
            }
        }
        return null;
    }

    public MarkupSnippet styleBlock(String directives, String content) {
        for (IInlineRule rule : rules) {
            MarkupSnippet snippet = rule.styleBlock(directives, content);
            if (snippet != null) {
                return snippet;
            }
        }
        return null;
    }
}
