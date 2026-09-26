package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.markup;

import java.util.List;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.input.IKeyStroke;
import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style.StyleFlag;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.Component;

public record TextAction(String id, Component label, Consumer<ITextActionTarget> action, List<TextAction> children, IKeyStroke shortcut) {

    public static final String MENU_PREFIX = "gui." + DragonLib.MODID + ".textbox.";
    public static final String FORMAT_PREFIX = MENU_PREFIX + "format.";
    public static final TextAction SEPARATOR = new TextAction("", TextUtils.empty(), null, List.of(), null);

    public TextAction(String id, Component label, Consumer<ITextActionTarget> action, List<TextAction> children) {
        this(id, label, action, children, null);
    }

    public static TextAction of(String id, Component label, Consumer<ITextActionTarget> action) {
        return new TextAction(id, label, action, List.of(), null);
    }

    public static TextAction format(String key, Consumer<ITextActionTarget> action) {
        return of(key, formatLabel(key), action);
    }

    public static TextAction menu(String key, Consumer<ITextActionTarget> action) {
        return of(key, menuLabel(key), action);
    }

    public static TextAction submenu(String key, List<TextAction> children) {
        return new TextAction(key, formatLabel(key), null, List.copyOf(children), null);
    }

    public static TextAction inlineStyle(StyleFlag flag) {
        String key = flag.name();
        return format(key, target -> target.toggleInlineStyle(flag));
    }

    public static TextAction linePrefix(String key, String prefix) {
        return format(key, target -> target.toggleLinePrefix(prefix));
    }

    public static Component formatLabel(String key) {
        return TextUtils.translate(FORMAT_PREFIX + key);
    }

    public static Component menuLabel(String key) {
        return TextUtils.translate(MENU_PREFIX + key);
    }

    public TextAction withShortcut(IKeyStroke shortcut) {
        return shortcut == this.shortcut ? this : new TextAction(id, label, action, children, shortcut);
    }

    public boolean hasShortcut() {
        return shortcut != null && action != null;
    }

    public boolean isSeparator() {
        return this == SEPARATOR;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }
}
