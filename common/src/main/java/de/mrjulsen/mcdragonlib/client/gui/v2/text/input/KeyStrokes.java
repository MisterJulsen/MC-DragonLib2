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

package de.mrjulsen.mcdragonlib.client.gui.v2.text.input;

import org.lwjgl.glfw.GLFW;

public final class KeyStrokes {
    private KeyStrokes() {}

    public static IKeyStroke of(int keyCode) {
        return event -> event.keyCode() == keyCode;
    }

    public static IKeyStroke plain(int keyCode) {
        return event -> event.keyCode() == keyCode && !event.control() && !event.shift() && !event.alt();
    }

    public static IKeyStroke shift(int keyCode) {
        return event -> event.keyCode() == keyCode && event.shift() && !event.control() && !event.alt();
    }

    public static IKeyStroke control(int keyCode) {
        return event -> event.keyCode() == keyCode && event.control() && !event.shift() && !event.alt();
    }

    public static IKeyStroke controlShift(int keyCode) {
        return event -> event.keyCode() == keyCode && event.control() && event.shift() && !event.alt();
    }

    public static IKeyStroke controlLabelled(char label, int fallback) {
        return event -> event.control() && !event.shift() && isLabelled(event, label, fallback);
    }

    public static IKeyStroke controlShiftLabelled(char label, int fallback) {
        return event -> event.control() && event.shift() && isLabelled(event, label, fallback);
    }

    public static boolean isLabelled(KeyEvent event, char label, int fallback) {
        String name;
        try {
            name = GLFW.glfwGetKeyName(GLFW.GLFW_KEY_UNKNOWN, event.scanCode());
        } catch (Exception ignored) {
            name = null;
        }
        if (name != null && name.length() == 1) {
            return Character.toLowerCase(name.charAt(0)) == label;
        }
        return event.keyCode() == fallback;
    }
}
