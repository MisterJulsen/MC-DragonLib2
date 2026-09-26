package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.inline;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupText;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextAction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextContext;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyles;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.Component;

public final class LinkRule implements IInlineRule {

    public static final LinkRule LINK = new LinkRule(false);
    public static final LinkRule IMAGE = new LinkRule(true);

    public int linkColor = 0xFF5C9DFF;
    public int imageColor = 0xFFA0A0A0;

    private final boolean image;

    public LinkRule(boolean image) {
        this.image = image;
    }

    @Override
    public String reservedCharacters() {
        return image ? "!" : "[";
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
        int limit = context.limit();
        int textStart;
        if (image) {
            if (context.charAt(at) != '!' || at + 1 >= limit || context.charAt(at + 1) != '[') {
                return 0;
            }
            textStart = at + 2;
        } else {
            if (context.charAt(at) != '[') {
                return 0;
            }
            textStart = at + 1;
        }

        int closeBracket = MarkupText.indexOfUnescaped(context.line(), textStart, limit, ']');
        if (closeBracket < 0 || closeBracket + 1 >= limit || context.charAt(closeBracket + 1) != '(') {
            return 0;
        }
        int closeParen = MarkupText.indexOfUnescaped(context.line(), closeBracket + 2, limit, ')');
        if (closeParen < 0) {
            return 0;
        }

        String target = context.textBetween(closeBracket + 2, closeParen);
        TextStyle style = image
                ? context.style().withColor(imageColor).with(TextStyles.ITALIC, true)
                : context.style().withLink(target).withColor(linkColor).with(TextStyles.UNDERLINED, true);

        context.flushTo(at);
        context.emitMarkup(at, textStart);
        context.parseNested(textStart, closeBracket, style);
        context.emitMarkup(closeBracket, closeParen + 1);
        return closeParen + 1 - at;
    }
}
