package de.mrjulsen.mcdragonlib.util;

import java.util.Collection;
import java.util.Iterator;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public final class TextUtils {

    public static MutableComponent text(String text) {
        return new TextComponent(text);
    }

    public static MutableComponent translate(String text, Object... args) {
        return new TranslatableComponent(text, args);
    }

    public static MutableComponent translate(String text) {
        return new TranslatableComponent(text);
    }

    public static MutableComponent keybind(String key) {
        return new KeybindComponent(key);
    }

    public static MutableComponent empty() {
        return text("");
    }
    
    private static final Component TEXT_CONCAT = text("     ***     ");

    public static MutableComponent concatWithStarChars(Component... components) {
        return concat(TEXT_CONCAT, components);
    }

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

    public static MutableComponent concat(Collection<Component> components) {
        return concat(TEXT_CONCAT, components);
    }

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
