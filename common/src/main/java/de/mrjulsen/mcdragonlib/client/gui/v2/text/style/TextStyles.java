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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.style;

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
