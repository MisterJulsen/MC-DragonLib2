/*
 * DragonLib 3 - 1.21.1
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
