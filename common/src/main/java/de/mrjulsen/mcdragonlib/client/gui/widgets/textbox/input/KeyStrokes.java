package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.input;

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
