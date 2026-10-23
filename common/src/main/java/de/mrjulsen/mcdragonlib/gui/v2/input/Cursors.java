package de.mrjulsen.mcdragonlib.gui.v2.input;

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
