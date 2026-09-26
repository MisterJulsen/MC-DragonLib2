package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.inline;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextAction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.MarkupSnippet;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup.TextContext;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;
import net.minecraft.network.chat.Component;

public interface IInlineRule {

    int parse(InlineContext context, int at);

    default String delimiterFor(StyleFlag flag) {
        return null;
    }

    default MarkupSnippet styleBlock(String directives, String content) {
        return null;
    }

    default List<TextAction> formatActions() {
        return List.of();
    }

    default List<TextAction> contextActions(TextContext context) {
        return List.of();
    }

    default List<Component> contextTooltip(TextContext context) {
        return List.of();
    }

    default String reservedCharacters() {
        return "";
    }
}
