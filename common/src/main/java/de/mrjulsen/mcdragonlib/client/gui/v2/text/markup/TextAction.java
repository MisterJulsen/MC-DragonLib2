/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.client.gui.v2.text.markup;

import java.util.List;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.input.IKeyStroke;
import de.mrjulsen.mcdragonlib.client.gui.v2.text.style.StyleFlag;
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
