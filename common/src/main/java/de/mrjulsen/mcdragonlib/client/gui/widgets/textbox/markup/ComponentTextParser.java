package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.DLTextBox;
import org.lwjgl.glfw.GLFW;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.input.IKeyStroke;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.input.KeyStrokes;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyles;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class ComponentTextParser implements ITextFormatParser {

    @FunctionalInterface
    public interface IRunVisitor {
        void visit(int from, int to, TextStyle style, boolean code);
    }

    public static final char SECTION_SIGN = '§';
    public static final char AMPERSAND_SIGN = '&';
    public static final char HEX_MARKER = 'x';
    public static final char SHORT_HEX_MARKER = '#';
    public static final int HEX_DIGITS = 6;

    public static final ComponentTextParser SECTION = new ComponentTextParser(SECTION_SIGN);
    public static final ComponentTextParser AMPERSAND = new ComponentTextParser(AMPERSAND_SIGN);

    public int markupColor = 0xFFA0A0A0;
    public int markupAlpha = 0x80;

    private final char prefix;

    public ComponentTextParser(char prefix) {
        this.prefix = prefix;
    }

    public char prefix() {
        return prefix;
    }

    @Override
    public String name() {
        return prefix == SECTION_SIGN ? "Minecraft Text" : "Minecraft Text (" + prefix + ")";
    }

    @Override
    public ParsedLine parseLine(CharSequence line, int incomingState) {
        List<StyledSpan> spans = new ArrayList<>(4);
        int markup = MarkupText.dimmed(markupColor, markupAlpha);
        scan(line, (from, to, style, code) -> spans.add(code ? MarkupText.markup(from, to, markup) : new StyledSpan(from, to, style)));
        return ParsedLine.paragraph(spans);
    }

    @Override
    public int nextState(CharSequence line, int incomingState) {
        return INITIAL_STATE;
    }

    @Override
    public MarkupSnippet inlineMarkup(StyleFlag flag, String content) {
        ChatFormatting formatting = formattingOf(flag);
        if (formatting == null) {
            return null;
        }
        return MarkupSnippet.wrapping(code(formatting.getChar()), content, code(ChatFormatting.RESET.getChar()));
    }

    @Override
    public MarkupSnippet styleBlock(String directives, String content) {
        Integer color = StyleDirectives.DEFAULT.parseColor(directives);
        if (color == null) {
            return null;
        }
        return MarkupSnippet.wrapping(colorCode(color), content, code(ChatFormatting.RESET.getChar()));
    }

    @Override
    public List<TextAction> additionalActions(DLTextBox dlTextBox) {
        List<TextAction> actions = new ArrayList<>();
        for (StyleFlag flag : StyleFlag.all()) {
            if (formattingOf(flag) != null) {
                actions.add(TextAction.inlineStyle(flag).withShortcut(shortcutOf(flag)));
            }
        }

        List<TextAction> colors = new ArrayList<>();
        for (ChatFormatting formatting : ChatFormatting.values()) {
            if (formatting.isColor()) {
                colors.add(TextAction.of("color." + formatting.getName(),
                        Component.literal(formatting.getName()).withStyle(formatting),
                        target -> target.toggleInlineMarkup(code(formatting.getChar()),
                                code(ChatFormatting.RESET.getChar()))));
            }
        }

        actions.add(TextAction.SEPARATOR);
        actions.add(TextAction.submenu("color", colors));
        actions.add(TextAction.format("reset", target -> target.insertText(code(ChatFormatting.RESET.getChar()))));
        return List.copyOf(actions);
    }

    @Override
    public String escapeLiteral(String text) {
        if (text == null || text.indexOf(prefix) < 0) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length());
        scan(text, (from, to, style, code) -> {
            if (!code) {
                builder.append(text, from, to);
            }
        });
        return builder.toString();
    }

    public String code(char formatting) {
        return String.valueOf(prefix) + formatting;
    }

    public String colorCode(int argb) {
        ChatFormatting named = namedColorOf(argb);
        if (named != null) {
            return code(named.getChar());
        }
        return String.valueOf(prefix) + SHORT_HEX_MARKER + String.format("%06X", argb & 0xFFFFFF);
    }

    public void scan(CharSequence line, IRunVisitor visitor) {
        int length = line.length();
        TextStyle style = TextStyle.DEFAULT;
        int plainStart = 0;
        int cursor = 0;

        while (cursor < length) {
            if (line.charAt(cursor) != prefix || cursor + 1 >= length) {
                cursor++;
                continue;
            }

            int consumed = 0;
            TextStyle next = null;

            Integer longHex = readLongHex(line, cursor, length);
            Integer shortHex = longHex == null ? readShortHex(line, cursor, length) : null;
            if (longHex != null) {
                next = colored(longHex);
                consumed = 2 + HEX_DIGITS * 2;
            } else if (shortHex != null) {
                next = colored(shortHex);
                consumed = 2 + HEX_DIGITS;
            } else {
                ChatFormatting formatting = formattingOf(line.charAt(cursor + 1));
                if (formatting != null) {
                    next = apply(formatting, style);
                    consumed = 2;
                }
            }

            if (consumed == 0) {
                cursor++;
                continue;
            }

            if (plainStart < cursor) {
                visitor.visit(plainStart, cursor, style, false);
            }
            visitor.visit(cursor, cursor + consumed, style, true);
            style = next;
            cursor += consumed;
            plainStart = cursor;
        }

        if (plainStart < length) {
            visitor.visit(plainStart, length, style, false);
        }
    }

    private Integer readLongHex(CharSequence line, int at, int limit) {
        if (Character.toLowerCase(line.charAt(at + 1)) != HEX_MARKER || at + 2 + HEX_DIGITS * 2 > limit) {
            return null;
        }
        int color = 0;
        for (int i = 0; i < HEX_DIGITS; i++) {
            int index = at + 2 + i * 2;
            if (line.charAt(index) != prefix) {
                return null;
            }
            int digit = Character.digit(line.charAt(index + 1), 16);
            if (digit < 0) {
                return null;
            }
            color = color << 4 | digit;
        }
        return color;
    }

    private Integer readShortHex(CharSequence line, int at, int limit) {
        if (line.charAt(at + 1) != SHORT_HEX_MARKER || at + 2 + HEX_DIGITS > limit) {
            return null;
        }
        int color = 0;
        for (int i = 0; i < HEX_DIGITS; i++) {
            int digit = Character.digit(line.charAt(at + 2 + i), 16);
            if (digit < 0) {
                return null;
            }
            color = color << 4 | digit;
        }
        return color;
    }

    private static TextStyle colored(int rgb) {
        return TextStyle.DEFAULT.withColor(0xFF000000 | rgb);
    }

    public static TextStyle apply(ChatFormatting formatting, TextStyle style) {
        if (formatting == ChatFormatting.RESET) {
            return TextStyle.DEFAULT;
        }
        if (formatting.isColor()) {
            Integer color = formatting.getColor();
            return colored(color == null ? TextStyle.DEFAULT.color() : color);
        }
        StyleFlag flag = flagOf(formatting);
        return flag == null ? style : style.with(flag, true);
    }

    public static ChatFormatting formattingOf(char code) {
        return ChatFormatting.getByCode(Character.toLowerCase(code));
    }

    public static StyleFlag flagOf(ChatFormatting formatting) {
        return StyleFlag.byFormatting(formatting);
    }

    public static ChatFormatting formattingOf(StyleFlag flag) {
        return flag == null ? null : flag.effect().formatting();
    }

    private static IKeyStroke shortcutOf(StyleFlag flag) {
        if (flag == TextStyles.BOLD) {
            return KeyStrokes.control(GLFW.GLFW_KEY_B);
        }
        if (flag == TextStyles.ITALIC) {
            return KeyStrokes.control(GLFW.GLFW_KEY_I);
        }
        if (flag == TextStyles.UNDERLINED) {
            return KeyStrokes.control(GLFW.GLFW_KEY_U);
        }
        if (flag == TextStyles.STRIKETHROUGH) {
            return KeyStrokes.controlShift(GLFW.GLFW_KEY_X);
        }
        return null;
    }

    public static ChatFormatting namedColorOf(int argb) {
        int rgb = argb & 0xFFFFFF;
        for (ChatFormatting formatting : ChatFormatting.values()) {
            Integer color = formatting.getColor();
            if (formatting.isColor() && color != null && color == rgb) {
                return formatting;
            }
        }
        return null;
    }
}
