package de.mrjulsen.mcdragonlib.util;

import java.util.Collection;
import java.util.Iterator;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TextUtils {

    public static MutableComponent text(String text) {
        return Component.literal(text);
    }

    public static MutableComponent translate(String text, Object... args) {
        return Component.translatable(text, args);
    }

    public static MutableComponent translate(String text) {
        return Component.translatable(text);
    }

    public static MutableComponent keybind(String key) {
        return Component.keybind(key);
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
