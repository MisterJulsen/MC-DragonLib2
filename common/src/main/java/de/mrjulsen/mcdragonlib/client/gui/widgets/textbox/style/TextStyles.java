package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.style;

import java.util.function.Predicate;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

public final class TextStyles {

    public static final StyleFlag BOLD = vanilla("bold", ChatFormatting.BOLD, Style::isBold);
    public static final StyleFlag ITALIC = vanilla("italic", ChatFormatting.ITALIC, Style::isItalic);
    public static final StyleFlag UNDERLINED = vanilla("underlined", ChatFormatting.UNDERLINE, Style::isUnderlined);
    public static final StyleFlag STRIKETHROUGH = vanilla("strikethrough", ChatFormatting.STRIKETHROUGH, Style::isStrikethrough);
    public static final StyleFlag OBFUSCATED = vanilla("obfuscated", ChatFormatting.OBFUSCATED, Style::isObfuscated);

    public static final StyleFlag SHADOW = StyleFlag.register("shadow", new IStyleEffect() {
        @Override
        public boolean shadow() {
            return true;
        }
    });

    public static final StyleFlag CODE = StyleFlag.register("code");
    public static final StyleFlag NO_WRAP = StyleFlag.register("no_wrap");

    private TextStyles() {
    }

    private static StyleFlag vanilla(String name, ChatFormatting formatting, Predicate<Style> test) {
        return StyleFlag.register(name, new VanillaStyleEffect(formatting, test));
    }
}
