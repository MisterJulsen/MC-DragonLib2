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

package de.mrjulsen.mcdragonlib.client.gui.widgets.util;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

public enum CursorType {
    ARROW(GLFW.GLFW_ARROW_CURSOR),
    IBEAM(GLFW.GLFW_IBEAM_CURSOR),
    CROSSHAIR(GLFW.GLFW_CROSSHAIR_CURSOR),
    HAND(GLFW.GLFW_HAND_CURSOR),
    HRESIZE(GLFW.GLFW_HRESIZE_CURSOR),
    VRESIZE(GLFW.GLFW_VRESIZE_CURSOR),
    TRBLRESIZE(GLFW.GLFW_RESIZE_NESW_CURSOR),
    TLBRRESIZE(GLFW.GLFW_RESIZE_NWSE_CURSOR),
    ALLRESIZE(GLFW.GLFW_RESIZE_ALL_CURSOR),
    NOT_ALLOWED(GLFW.GLFW_NOT_ALLOWED_CURSOR);

    private final int shape;
    private long cursor = 0L;

    private static long currentCursor = 0L;

    CursorType(int c) {
        shape = c;
    }

    public static void set(CursorType type) {
        var window = Minecraft.getInstance().getWindow().getWindow();

        if (type == null) {
            GLFW.glfwSetCursor(window, MemoryUtil.NULL);
            currentCursor = 0L;
            return;
        }

        if (type.cursor == 0L) {
            type.cursor = GLFW.glfwCreateStandardCursor(type.shape);
        }

        if (currentCursor != type.cursor) {
            GLFW.glfwSetCursor(window, type.cursor);
            currentCursor = type.cursor;
        }
    }
}