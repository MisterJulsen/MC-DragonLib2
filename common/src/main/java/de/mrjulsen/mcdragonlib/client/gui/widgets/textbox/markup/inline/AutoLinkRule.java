package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.inline;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextAction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextContext;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyles;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.Component;

public final class AutoLinkRule implements IInlineRule {

    public static final AutoLinkRule WEB = new AutoLinkRule(List.of("http://", "https://", "www."));

    private static final String TRAILING_PUNCTUATION = ".,)]!?;:";

    public int color = 0xFF5C9DFF;

    private final List<String> prefixes;

    public AutoLinkRule(List<String> prefixes) {
        this.prefixes = List.copyOf(prefixes);
    }

    @Override
    public List<TextAction> contextActions(TextContext context) {
        String link = context.link();
        if (link == null) {
            return List.of();
        }
        return List.of(
                TextAction.menu("open_link", target -> target.openLink(link)),
                TextAction.menu("copy_link", target -> target.copyToClipboard(link))
        );
    }

    @Override
    public List<Component> contextTooltip(TextContext context) {
        String link = context == null ? null : context.link();
        return link == null ? List.of() : List.of(TextUtils.text(link));
    }

    @Override
    public int parse(InlineContext context, int at) {
        if (at > 0 && Character.isLetterOrDigit(context.charAt(at - 1))) {
            return 0;
        }
        if (!startsWithPrefix(context, at)) {
            return 0;
        }

        int end = urlEnd(context, at);
        if (end <= at) {
            return 0;
        }

        String url = context.textBetween(at, end);
        context.flushTo(at);
        context.emitStyled(at, end, context.style()
                .withLink(url)
                .withColor(color)
                .with(TextStyles.UNDERLINED, true));
        return end - at;
    }

    private boolean startsWithPrefix(InlineContext context, int at) {
        for (String prefix : prefixes) {
            if (MarkupText.startsWith(context.line(), at, prefix)) {
                return true;
            }
        }
        return false;
    }

    private static int urlEnd(InlineContext context, int from) {
        int limit = context.limit();
        int i = from;
        while (i < limit) {
            char c = context.charAt(i);
            if (Character.isWhitespace(c) || c == '<' || c == '>') {
                break;
            }
            i++;
        }
        while (i > from && TRAILING_PUNCTUATION.indexOf(context.charAt(i - 1)) >= 0) {
            i--;
        }
        return i;
    }
}
