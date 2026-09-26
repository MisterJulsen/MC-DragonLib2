package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

import java.util.Optional;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.document.TextDocument;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class ComponentText {

    private ComponentText() {
    }

    public static String fromComponent(FormattedText text) {
        return fromComponent(text, ComponentTextParser.SECTION);
    }

    public static String fromComponent(FormattedText text, ComponentTextParser parser) {
        StringBuilder builder = new StringBuilder();
        Style[] previous = { null };

        text.visit((style, content) -> {
            if (!style.equals(previous[0])) {
                appendStyle(builder, style, parser, previous[0] == null);
                previous[0] = style;
            }
            builder.append(content);
            return Optional.empty();
        }, Style.EMPTY);

        return builder.toString();
    }

    public static MutableComponent toComponent(String text) {
        return toComponent(text, ComponentTextParser.SECTION);
    }

    public static MutableComponent toComponent(String text, ComponentTextParser parser) {
        MutableComponent root = Component.empty();
        String[] lines = TextDocument.normalizeLineEndings(text).split("\n", -1);

        for (int line = 0; line < lines.length; line++) {
            if (line > 0) {
                root.append(Component.literal("\n"));
            }
            appendLine(root, lines[line], parser);
        }
        return root;
    }

    private static void appendLine(MutableComponent root, String line, ComponentTextParser parser) {
        parser.scan(line, (from, to, style, code) -> {
            if (code || from >= to) {
                return;
            }
            root.append(Component.literal(line.substring(from, to)).setStyle(toVanillaStyle(style)));
        });
    }

    public static Style toVanillaStyle(TextStyle style) {
        Style vanilla = Style.EMPTY;
        if (style.color() != TextStyle.DEFAULT.color()) {
            vanilla = vanilla.withColor(TextColor.fromRgb(style.color() & 0xFFFFFF));
        }
        return style.applyTo(vanilla);
    }

    private static void appendStyle(StringBuilder builder, Style style, ComponentTextParser parser, boolean first) {
        if (!first) {
            builder.append(parser.code(ChatFormatting.RESET.getChar()));
        }

        TextColor color = style.getColor();
        if (color != null) {
            builder.append(parser.colorCode(color.getValue()));
        }
        for (StyleFlag flag : StyleFlag.all()) {
            ChatFormatting formatting = flag.effect().formatting();
            if (formatting != null && flag.effect().matchesVanilla(style)) {
                builder.append(parser.code(formatting.getChar()));
            }
        }
    }

    public static boolean isPlain(Style style) {
        if (style.getColor() != null) {
            return false;
        }
        for (StyleFlag flag : StyleFlag.all()) {
            if (flag.effect().matchesVanilla(style)) {
                return false;
            }
        }
        return true;
    }
}
