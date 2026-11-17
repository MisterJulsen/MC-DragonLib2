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

package de.mrjulsen.mcdragonlib.client.gui.v2.input;

import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import net.minecraft.client.Minecraft;

/**
 * Holds the mouse cursor currently set, so the shape is only handed to GLFW when it really changes.
 * Driven by the input router from the hovered components, see GuiComponent#getCurrentCursor.
 */
public final class Cursors {

    private static CursorType current;

    private Cursors() {
    }

    public static CursorType current() {
        return current;
    }

    /**
     * Sets the cursor shape, or restores the default with null. Does nothing if that shape is already
     * active.
     */
    public static void set(CursorType type) {
        if (current == type) {
            return;
        }
        current = type;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            return;
        }
        CursorType.set(type);
    }
}
