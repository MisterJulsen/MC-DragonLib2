package de.mrjulsen.mcdragonlib.util;

import java.util.*;

import de.mrjulsen.mcdragonlib.DragonLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/**
 * This class contains some useful utilities for text components. It uses its
 * own methods to create text components and forms a unified wrapper around the
 * Minecraft methods.
 */
public final class TextUtils {
    
    /** 🐉 */
    public static final Component TEXT_DRAGON = TextUtils.translate("text." + DragonLib.MODID + ".dragon");
    public static final Component TEXT_NEXT = TextUtils.translate("text." + DragonLib.MODID + ".next");
    public static final Component TEXT_PREVIOUS = TextUtils.translate("text." + DragonLib.MODID + ".previous");
    public static final Component TEXT_GO_BACK = TextUtils.translate("text." + DragonLib.MODID + ".go_back");
    public static final Component TEXT_GO_FORTH = TextUtils.translate("text." + DragonLib.MODID + ".go_forth");    
    public static final Component TEXT_GO_UP = TextUtils.translate("text." + DragonLib.MODID + ".go_down");
    public static final Component TEXT_GO_DOWN = TextUtils.translate("text." + DragonLib.MODID + ".go_up");
    public static final Component TEXT_GO_RIGHT= TextUtils.translate("text." + DragonLib.MODID + ".go_right");
    public static final Component TEXT_GO_LEFT = TextUtils.translate("text." + DragonLib.MODID + ".go_left");
    public static final Component TEXT_GO_TO_TOP = TextUtils.translate("text." + DragonLib.MODID + ".go_to_top");
    public static final Component TEXT_GO_TO_BOTTOM = TextUtils.translate("text." + DragonLib.MODID + ".go_to_bottom");
    public static final Component TEXT_RESET_DEFAULTS = TextUtils.translate("text." + DragonLib.MODID + ".reset_defaults");
    public static final Component TEXT_EXPAND = TextUtils.translate("text." + DragonLib.MODID + ".expand");
    public static final Component TEXT_COLLAPSE = TextUtils.translate("text." + DragonLib.MODID + ".collapse");
    public static final Component TEXT_COUNT = TextUtils.translate("text." + DragonLib.MODID + ".count");
    public static final Component TEXT_TRUE = TextUtils.translate("text." + DragonLib.MODID + ".true");
    public static final Component TEXT_FALSE = TextUtils.translate("text." + DragonLib.MODID + ".false");
    public static final Component TEXT_CLOSE = TextUtils.translate("text." + DragonLib.MODID + ".close");
    public static final Component TEXT_SHOW = TextUtils.translate("text." + DragonLib.MODID + ".show");
    public static final Component TEXT_HIDE = TextUtils.translate("text." + DragonLib.MODID + ".hide");
    public static final Component TEXT_SEARCH = TextUtils.translate("text." + DragonLib.MODID + ".search");
    public static final Component TEXT_REFRESH = TextUtils.translate("text." + DragonLib.MODID + ".refresh");
    public static final Component TEXT_RELOAD = TextUtils.translate("text." + DragonLib.MODID + ".reload");

    /**
     * a predefined an immutable empty text component. Similar to {@link TextUtils#empty()}
     */
    public static final Component EMPTY = empty();
    private static final Component TEXT_CONCAT = text("     ***     ");
    public static final String ELLIPSIS_STRING = "...";
    public static final Component ELLIPSIS_COMPONENT = TextUtils.text(ELLIPSIS_STRING);
    
    /**
     * Creates a new text component with the specified text.
     * @param text The literal text of the component.
     * @return A new text component.
     */
    public static MutableComponent text(String text) {
        return Component.literal(text);
    }
    
    /**
     * Creates a new translatable text component.
     * @param key The translation key
     * @param args Additional parameters for placeholders
     * @return The translated text component
     */
    public static MutableComponent translate(String key, Object... args) {
        return Component.translatable(key, args);
    }

    /**
     * Creates a new translatable text component.
     * @param key The translation key
     * @return The translated text component
     */
    public static MutableComponent translate(String key) {
        return Component.translatable(key);
    }

    /**
     * Creates a new keybind text component.
     * @param key The keybind key
     * @return The keybind component
     */
    public static MutableComponent keybind(String key) {
        return Component.keybind(key);
    }

    public static MutableComponent score(String name, String objective) {
        return Component.score(name, objective);
    }

    /**
     * Creates an empty text component with {@code ""}.
     * @return An enmpty text component.
     */
    public static MutableComponent empty() {
        return text("");
    }





    /**
     * @return the client's default font
     */
    public static Font font() {
        return Minecraft.getInstance().font;
    }

    /**
     * @return the width of the text in pixels
     */
    public static int textWidth(Font font, String value) {
        return font.width(value);
    }

    /**
     * @return the width of the text in pixels
     */
    public static int textWidth(Font font, FormattedText value) {
        return font.width(value);
    }

    /**
     * @return the height of one text line in pixels
     */
    public static int lineHeight(Font font) {
        return font.lineHeight;
    }

    /**
     * @return the y offset that vertically centers one line of text within the given height
     */
    public static double centerTextY(Font font, double availableHeight) {
        return (availableHeight - font.lineHeight) / 2D;
    }

    /**
     * Converts any {@link FormattedText} into a text component without flattening it, so the style of every part of the text is preserved.
     * @param text The text to convert
     * @return A text component holding one styled sibling per part of the given text.
     */
    public static MutableComponent toComponent(FormattedText text) {
        MutableComponent result = empty();
        if (text != null) {
            text.visit((style, content) -> {
                result.append(text(content).withStyle(style));
                return Optional.empty();
            }, Style.EMPTY);
        }
        return result;
    }

    private static Style lastStyleOf(Component text) {
        List<Component> siblings = text.getSiblings();
        return siblings.isEmpty() ? text.getStyle() : siblings.get(siblings.size() - 1).getStyle();
    }

    /**
     * Truncates the given text component and appends an ellipsis {@code ...} to it. The text is truncated so that the remainder, including the ellipsis, does not exceed the maximum bounds.
     * @param font The font to use for this operation
     * @param text The text to truncate
     * @param maxWidth The maximum width
     * @return The truncated text component with the ellipsis {@code ...}.
     */
	public static Component truncateWithEllipsis(Font font, Component text, int maxWidth) {
        if (maxWidth <= 0) {
            return empty();
        }
		int lineWidth = font.width(text);
        if (lineWidth <= maxWidth) {
            return text;
        }
        int ellipsisWidth = textWidth(font, TextUtils.text(ELLIPSIS_STRING).withStyle(text.getStyle()));
		return TextUtils.text(font.substrByWidth(text, maxWidth - ellipsisWidth).getString() + ELLIPSIS_STRING).withStyle(text.getStyle());
	}

    /**
     * Truncates the given text and appends an ellipsis {@code ...} to it. The text is truncated so that the remainder, including the ellipsis, does not exceed the maximum bounds.
     * Unlike {@link #truncateWithEllipsis(Font, Component, int)} this keeps the individual style of every part of the text, and the ellipsis adopts the style of the last remaining part.
     * @param font The font to use for this operation
     * @param text The text to truncate
     * @param maxWidth The maximum width
     * @return The truncated text component with the ellipsis {@code ...}.
     */
    public static Component truncateWithEllipsis(Font font, FormattedText text, int maxWidth) {
        return truncateWithEllipsis(font, toComponent(text), maxWidth);
    }

    /**
     * Truncates the given text and appends an ellipsis {@code ...} to it. The text is truncated so that the remainder, including the ellipsis, does not exceed the maximum bounds.
     * @param font The font to use for this operation
     * @param text The text to truncate
     * @param maxWidth The maximum width
     * @return The truncated text with the ellipsis {@code ...}.
     */
    public static String truncateWithEllipsis(Font font, String text, int maxWidth) {
        int lineWidth = font.width(text);
        if (lineWidth > maxWidth) {
            return text;
        }
        int ellipsisWidth = font.width(ELLIPSIS_STRING);
        return font.plainSubstrByWidth(text, maxWidth - ellipsisWidth) + ELLIPSIS_STRING;
    }

    /**
     * Appends an ellipsis {@code ...} to the given text, no matter whether the text would fit into the maximum bounds on its own. The text is truncated so that the remainder, including the ellipsis, does not exceed the maximum bounds.
     * The individual style of every part of the text is kept, and the ellipsis adopts the style of the last remaining part.
     * @param font The font to use for this operation
     * @param text The text to append the ellipsis to
     * @param maxWidth The maximum width
     * @return The text component with the ellipsis {@code ...}.
     */
    public static Component appendEllipsis(Font font, FormattedText text, int maxWidth) {
        Component txt = toComponent(text);
        Style style = lastStyleOf(txt);
        Component ellipsis = text(ELLIPSIS_STRING).withStyle(style);
        int ellipsisWidth = textWidth(font, ellipsis);
        if (maxWidth - ellipsisWidth <= 0) {
            return ellipsis;
        }
        return TextUtils.empty().append(toComponent(splitText(font, List.of(txt), maxWidth - ellipsisWidth).get(0))).append(ellipsis);
    }
    

    /**
     * Connects the different text components with {@code *** }.
     * @param components The text components to combine
     * @return The combined text component.
     */
    public static MutableComponent concatSimple(Component... components) {
        return concat(TEXT_CONCAT, components);
    }

    /**
     * Connects the different text components with the specified string.
     * @param concatString The connection text
     * @param components The text components to combine
     * @return The combined text component.
     */
    public static MutableComponent concat(Component concatString, Component... components) {
        if (components.length <= 0) {
            return empty();
        }

        MutableComponent c = components[0].copy();
        for (int i = 1; i < components.length; i++) {
            c.append(concatString);
            c.append(components[i]);
        }
        return c;
    }

    /**
     * Connects the different text components with {@code *** }.
     * @param components The text components to combine
     * @return The combined text component.
     */
    public static MutableComponent concat(Collection<Component> components) {
        return concat(TEXT_CONCAT, components);
    }

    /**
     * Connects the different text components with the specified string.
     * @param concatString The connection text
     * @param components The text components to combine
     * @return The combined text component.
     */
    public static MutableComponent concat(Component concatString, Collection<Component> components) {
        if (components == null || components.isEmpty()) {
            return empty();
        }

        Iterator<Component> com = components.iterator();
        MutableComponent c = empty();

        if (!com.hasNext()) {
            return empty();
        }
        c.append(com.next());

        while (com.hasNext()) {
            c.append(concatString);
            c.append(com.next());
        }
        return c;
    }

    public static FormattedCharSequence toFormattedCharSequence(FormattedText text) {
        return text instanceof Component ? ((Component)text).getVisualOrderText() : Language.getInstance().getVisualOrder(text);
    }

    public static <T extends FormattedText> List<FormattedCharSequence> splitToFormattedCharSequences(Font font, Collection<T> components, int maxWidth) {
        if (maxWidth <= 0) {
            return List.of();
        }
        List<FormattedCharSequence> lines = new ArrayList<>(components.size());
        for (T c : components) {
            lines.addAll(font.split(c, maxWidth));
        }
        return lines;
    }

    public static <T extends FormattedText> List<FormattedText> splitText(Font font, Collection<T> components, int maxWidth) {
        if (maxWidth <= 0) {
            return List.of();
        }
        List<FormattedText> lines = new ArrayList<>(components.size());
        for (T c : components) {
            lines.addAll(font.getSplitter().splitLines(c, maxWidth, Style.EMPTY));
        }
        return lines;
    }

    /**
     * Splits the given texts into lines that each fit into the maximum width. If they do not all fit into the maximum height, the result is cut off after the last line that still fits and that line ends with an ellipsis {@code ...}.
     * @param font The font to use for this operation
     * @param components The texts to split
     * @param maxWidth The maximum width of one line
     * @param maxHeight The maximum height of all lines together
     * @param lineHeight The height of one line
     * @return At most {@code maxHeight / lineHeight} lines, all of them within the given bounds.
     */
    public static <T extends FormattedText> List<FormattedText> splitText(Font font, Collection<T> components, int maxWidth, int maxHeight, int lineHeight) {
        if (maxWidth <= 0 || maxHeight <= 0) {
            return List.of();
        }
        int max = maxHeight / lineHeight;
        if (max <= 0) {
            return List.of();
        }

        List<FormattedText> lines = new ArrayList<>(max);
        boolean truncated = false;
        iteration: for (T c : components) {
            for (FormattedText subLine : splitText(font, List.of(c), maxWidth)) {
                if (lines.size() >= max) {
                    truncated = true;
                    break iteration;
                }
                lines.add(subLine);
            }
        }

        if (truncated) {
            lines.set(max - 1, appendEllipsis(font, lines.get(max - 1), maxWidth));
        }

        return lines;
    }
}
