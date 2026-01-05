package de.mrjulsen.mcdragonlib.util;

import java.util.Collection;
import java.util.Iterator;

import de.mrjulsen.mcdragonlib.DragonLib;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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
     * @param text The translation key
     * @param args Additional parameters for placeholders
     * @return The translated text component
     */
    public static MutableComponent translate(String text, Object... args) {
        return Component.translatable(text, args);
    }

    /**
     * Creates a new translatable text component.
     * @param text The translation key
     * @return The translated text component
     */
    public static MutableComponent translate(String text) {
        return Component.translatable(text);
    }

    /**
     * Creates a new keybind text component.
     * @param key The keybind key
     * @return The keybind component
     */
    public static MutableComponent keybind(String key) {
        return Component.keybind(key);
    }

    /**
     * Creates an empty text component with {@code ""}.
     * @return An enmpty text component.
     */
    public static MutableComponent empty() {
        return text("");
    }

    

    /**
     * Truncates the given text component and appends an ellipsis {@code ...} to it. The text is truncated so that the remainder, including the ellipsis, does not exceed the maximum bounds.
     * @param font The font to use for this operation
     * @param text The text to truncate
     * @param maxWidth The maximum width
     * @return The truncated text component with the ellipsis {@code ...}.
     */
	public static Component truncateWithEllipsis(Font font, Component text, int maxWidth) {
		int lineWidth = font.width(text);
		return lineWidth < maxWidth ? text : TextUtils.text(font.substrByWidth(text, maxWidth - font.width(TextUtils.text(ELLIPSIS_STRING).withStyle(text.getStyle()))).getString() + ELLIPSIS_STRING).withStyle(text.getStyle());
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
		return lineWidth < maxWidth ? text : font.plainSubstrByWidth(text, maxWidth - font.width(ELLIPSIS_STRING)) + ELLIPSIS_STRING;
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

}
