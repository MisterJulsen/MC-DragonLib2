package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style;

import de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.render.SpanDecoration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

public interface IStyleEffect {

    IStyleEffect NONE = new IStyleEffect() {
    };

    default ChatFormatting formatting() {
        return null;
    }

    default Style applyVanilla(Style style) {
        return style;
    }

    default boolean matchesVanilla(Style style) {
        return false;
    }

    default boolean shadow() {
        return false;
    }

    default int color(int color, TextBoxStyle style) {
        return color;
    }

    default void render(SpanDecoration decoration) {
    }
}
