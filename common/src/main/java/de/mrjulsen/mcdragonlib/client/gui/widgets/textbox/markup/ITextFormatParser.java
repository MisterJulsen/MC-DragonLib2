package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.DLTextBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.layout.IBlockLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.IBlockDecorator;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;
import net.minecraft.network.chat.Component;

public interface ITextFormatParser {

    int INITIAL_STATE = 0;

    ParsedLine parseLine(CharSequence line, int incomingState);
    int nextState(CharSequence line, int incomingState);
    String name();

    default boolean isMarkup() {
        return true;
    }

    default String indentUnit() {
        return "  ";
    }

    default String continuationPrefix(ParsedLine line) {
        return "";
    }

    default String inlineDelimiter(StyleFlag flag) {
        return null;
    }

    default MarkupSnippet inlineMarkup(StyleFlag flag, String content) {
        String delimiter = inlineDelimiter(flag);
        return delimiter == null ? null : MarkupSnippet.wrapping(delimiter, content, delimiter);
    }

    default MarkupSnippet styleBlock(String directives, String content) {
        return null;
    }

    default boolean supportsStyleBlocks() {
        return styleBlock("", "") != null;
    }

    default List<TextAction> additionalActions(DLTextBox dlTextBox) {
        return List.of();
    }

    default List<TextAction> contextActions(TextContext context) {
        return List.of();
    }

    default List<Component> contextTooltip(TextContext context) {
        return List.of();
    }

    default IBlockLayout blockLayout(BlockKind kind) {
        return null;
    }

    default IBlockDecorator blockDecorator(BlockKind kind) {
        return null;
    }

    default String reservedCharacters() {
        return "";
    }

    default String lineStartReservedCharacters() {
        return "";
    }

    default char escapeCharacter() {
        return '\\';
    }

    default String escapeLiteral(String text) {
        String inline = reservedCharacters();
        String lineStart = lineStartReservedCharacters();
        if (text == null || text.isEmpty() || (inline.isEmpty() && lineStart.isEmpty())) {
            return text;
        }

        StringBuilder builder = new StringBuilder(text.length());
        boolean atLineStart = true;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                atLineStart = true;
                builder.append(c);
                continue;
            }
            if (inline.indexOf(c) >= 0 || (atLineStart && lineStart.indexOf(c) >= 0)) {
                builder.append(escapeCharacter());
            }
            if (c != ' ' && c != '\t') {
                atLineStart = false;
            }
            builder.append(c);
        }
        return builder.toString();
    }
}
