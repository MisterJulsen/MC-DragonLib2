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

package de.mrjulsen.mcdragonlib.client.gui.v2.theme;

import de.mrjulsen.mcdragonlib.client.gui.v2.GuiComponent;
import org.lwjgl.glfw.GLFW;

/**
 * The visual state of a widget, which is what a {@link WidgetRenderer} picks its look from.
 */
public enum WidgetState {
    NORMAL,
    HOVERED,
    PRESSED,
    FOCUSED,
    DISABLED_HOVERED,
    DISABLED;

    /**
     * The state of a component, by precedence: disabled, pressed, hovered, focused, normal.
     */
    public static WidgetState of(GuiComponent component) {
        if (!component.isEnabled() && component.isHovered()) {
            return DISABLED_HOVERED;
        } else if (!component.isEnabled()) {
            return DISABLED;
        } else if (component.isPressed() && component.getPressedButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return PRESSED;
        } else if (component.isHovered()) {
            return HOVERED;
        } else if (component.isFocused()) {
            return FOCUSED;
        }
        return NORMAL;
    }

    /**
     * The state of a component with pressed and hovered given explicitly. For widgets whose parts have
     * their own state, like the thumb of a scrollbar.
     */
    public static WidgetState of(GuiComponent component, boolean pressed, boolean hovered) {
        if (!component.isEnabled()) {
            return DISABLED;
        }
        if (pressed) {
            return PRESSED;
        }
        if (hovered) {
            return HOVERED;
        }
        if (component.isFocused()) {
            return FOCUSED;
        }
        return NORMAL;
    }
}
