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

import de.mrjulsen.mcdragonlib.client.gui.v2.text.render.SpanDecoration;
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
