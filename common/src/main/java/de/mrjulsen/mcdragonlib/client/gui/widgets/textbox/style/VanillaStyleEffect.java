package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style;

import java.util.function.Predicate;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

public record VanillaStyleEffect(ChatFormatting format, Predicate<Style> test) implements IStyleEffect {

    @Override
    public ChatFormatting formatting() {
        return format;
    }

    @Override
    public Style applyVanilla(Style style) {
        return style.applyFormat(format);
    }

    @Override
    public boolean matchesVanilla(Style style) {
        return test.test(style);
    }
}
