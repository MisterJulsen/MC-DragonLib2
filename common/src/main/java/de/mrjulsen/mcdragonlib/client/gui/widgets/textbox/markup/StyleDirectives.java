package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyles;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.TextStyle;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;

public class StyleDirectives {

    @FunctionalInterface
    public interface IDirective {
        TextStyle apply(StyleDirectives owner, String directive, TextStyle style);
    }

    public static final StyleDirectives DEFAULT = createDefault();

    private final Map<String, UnaryOperator<TextStyle>> keywords = new LinkedHashMap<>();
    private final Map<String, Integer> colors = new LinkedHashMap<>();
    private final Map<String, ETextAlignment> alignments = new LinkedHashMap<>();
    private final List<IDirective> dynamic = new ArrayList<>();

    public char separator = ';';
    public float minScale = 0.25F;
    public float maxScale = 8.0F;

    public static StyleDirectives createDefault() {
        StyleDirectives directives = new StyleDirectives();

        directives.registerColor("black", 0xFF000000)
                .registerColor("dark_blue", 0xFF0000AA)
                .registerColor("dark_green", 0xFF00AA00)
                .registerColor("dark_aqua", 0xFF00AAAA)
                .registerColor("dark_red", 0xFFAA0000)
                .registerColor("dark_purple", 0xFFAA00AA)
                .registerColor("gold", 0xFFFFAA00)
                .registerColor("gray", 0xFFAAAAAA)
                .registerColor("dark_gray", 0xFF555555)
                .registerColor("blue", 0xFF5555FF)
                .registerColor("green", 0xFF55FF55)
                .registerColor("aqua", 0xFF55FFFF)
                .registerColor("red", 0xFFFF5555)
                .registerColor("purple", 0xFFFF55FF)
                .registerColor("yellow", 0xFFFFFF55)
                .registerColor("white", 0xFFFFFFFF);

        directives.registerFlag("b", TextStyles.BOLD).registerFlag("bold", TextStyles.BOLD)
                .registerFlag("i", TextStyles.ITALIC).registerFlag("italic", TextStyles.ITALIC)
                .registerFlag("u", TextStyles.UNDERLINED).registerFlag("underline", TextStyles.UNDERLINED)
                .registerFlag("s", TextStyles.STRIKETHROUGH).registerFlag("strike", TextStyles.STRIKETHROUGH)
                .registerFlag("o", TextStyles.OBFUSCATED).registerFlag("obfuscated", TextStyles.OBFUSCATED)
                .registerFlag("sh", TextStyles.SHADOW).registerFlag("shadow", TextStyles.SHADOW);

        directives.registerAlignment("left", ETextAlignment.LEFT)
                .registerAlignment("center", ETextAlignment.CENTER)
                .registerAlignment("centre", ETextAlignment.CENTER)
                .registerAlignment("middle", ETextAlignment.CENTER)
                .registerAlignment("right", ETextAlignment.RIGHT);

        directives.registerDirective((owner, directive, style) -> {
            if (!directive.startsWith("bg:")) {
                return null;
            }
            Integer color = owner.parseColor(directive.substring(3));
            return color == null ? null : style.withBackgroundColor(color);
        });
        directives.registerDirective((owner, directive, style) -> {
            if (!directive.endsWith("x")) {
                return null;
            }
            Float scale = owner.parseScale(directive.substring(0, directive.length() - 1));
            return scale == null ? null : style.withScale(scale);
        });
        directives.registerDirective((owner, directive, style) -> {
            Integer color = owner.parseColor(directive);
            return color == null ? null : style.withColor(color);
        });
        return directives;
    }

    public StyleDirectives registerKeyword(String keyword, UnaryOperator<TextStyle> action) {
        keywords.put(keyword.toLowerCase(), action);
        return this;
    }

    public StyleDirectives registerFlag(String keyword, StyleFlag flag) {
        return registerKeyword(keyword, style -> style.with(flag, true));
    }

    public StyleDirectives registerColor(String name, int argb) {
        colors.put(name.toLowerCase(), argb);
        return this;
    }

    public StyleDirectives registerAlignment(String keyword, ETextAlignment alignment) {
        alignments.put(keyword.toLowerCase(), alignment);
        return this;
    }

    public StyleDirectives registerDirective(IDirective directive) {
        dynamic.add(directive);
        return this;
    }

    public StyleDirectives copy() {
        StyleDirectives copy = new StyleDirectives();
        copy.keywords.putAll(keywords);
        copy.colors.putAll(colors);
        copy.alignments.putAll(alignments);
        copy.dynamic.addAll(dynamic);
        copy.separator = separator;
        copy.minScale = minScale;
        copy.maxScale = maxScale;
        return copy;
    }

    public TextStyle apply(String directives, TextStyle base) {
        TextStyle style = base;
        for (String raw : split(directives)) {
            style = applyOne(raw, style);
        }
        return style;
    }

    public ETextAlignment alignmentOf(String directives) {
        ETextAlignment alignment = null;
        for (String raw : split(directives)) {
            ETextAlignment found = alignments.get(raw);
            if (found != null) {
                alignment = found;
            }
        }
        return alignment;
    }

    public boolean isRecognized(String directives) {
        if (alignmentOf(directives) != null) {
            return true;
        }
        return !apply(directives, TextStyle.DEFAULT).equals(TextStyle.DEFAULT);
    }

    public Integer parseColor(String text) {
        String value = text.trim().toLowerCase();
        if (value.isEmpty()) {
            return null;
        }
        if (value.charAt(0) != '#') {
            return colors.get(value);
        }

        String digits = value.substring(1);
        try {
            return switch (digits.length()) {
                case 3 -> 0xFF000000
                        | expandNibble(digits.charAt(0)) << 16
                        | expandNibble(digits.charAt(1)) << 8
                        | expandNibble(digits.charAt(2));
                case 6 -> 0xFF000000 | Integer.parseInt(digits, 16);
                case 8 -> (int) Long.parseLong(digits, 16);
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Float parseScale(String text) {
        try {
            float value = Float.parseFloat(text.trim());
            return Math.max(minScale, Math.min(value, maxScale));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private TextStyle applyOne(String directive, TextStyle style) {
        if (alignments.containsKey(directive)) {
            return style;
        }
        UnaryOperator<TextStyle> keyword = keywords.get(directive);
        if (keyword != null) {
            return keyword.apply(style);
        }
        for (IDirective handler : dynamic) {
            TextStyle result = handler.apply(this, directive, style);
            if (result != null) {
                return result;
            }
        }
        return style;
    }

    private List<String> split(String directives) {
        List<String> parts = new ArrayList<>(2);
        for (String raw : directives.split(Pattern.quote(String.valueOf(separator)))) {
            String trimmed = raw.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                parts.add(trimmed);
            }
        }
        return parts;
    }

    private static int expandNibble(char c) {
        int value = Character.digit(c, 16);
        if (value < 0) {
            throw new NumberFormatException("Not a hex digit: " + c);
        }
        return value * 17;
    }
}
