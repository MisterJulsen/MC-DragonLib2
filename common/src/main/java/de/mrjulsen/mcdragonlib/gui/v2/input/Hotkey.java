package de.mrjulsen.mcdragonlib.gui.v2.input;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

/**
 * A key combination: a GLFW key code plus modifiers. Only Shift, Ctrl, Alt and Super are kept, so
 * stray modifier bits like Caps Lock do not break the comparison.
 */
public record Hotkey(int keyCode, int modifiers) {

    public static final int RELEVANT_MODIFIERS = GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT | GLFW.GLFW_MOD_SUPER;

    public Hotkey(int keyCode, int modifiers) {
        this.keyCode = keyCode;
        this.modifiers = modifiers & RELEVANT_MODIFIERS;
    }

    public static Hotkey of(int keyCode) {
        return new Hotkey(keyCode, 0);
    }

    public static Hotkey of(int keyCode, int modifiers) {
        return new Hotkey(keyCode, modifiers);
    }

    public static Hotkey ctrl(int keyCode) {
        return new Hotkey(keyCode, GLFW.GLFW_MOD_CONTROL);
    }

    public static Hotkey shift(int keyCode) {
        return new Hotkey(keyCode, GLFW.GLFW_MOD_SHIFT);
    }

    public static Hotkey alt(int keyCode) {
        return new Hotkey(keyCode, GLFW.GLFW_MOD_ALT);
    }

    public static Hotkey ctrlShift(int keyCode) {
        return new Hotkey(keyCode, GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT);
    }

    /**
     * Whether this combination is exactly the one pressed. The modifiers have to match as well, so
     * Ctrl+S does not trigger on Ctrl+Shift+S.
     */
    public boolean matches(int keyCode, int modifiers) {
        return this.keyCode == keyCode && this.modifiers == (modifiers & RELEVANT_MODIFIERS);
    }

    public boolean hasShift() {
        return (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
    }

    public boolean hasControl() {
        return (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
    }

    public boolean hasAlt() {
        return (modifiers & GLFW.GLFW_MOD_ALT) != 0;
    }

    public boolean hasSuper() {
        return (modifiers & GLFW.GLFW_MOD_SUPER) != 0;
    }

    /**
     * The combination as readable text, e.g. "Ctrl+Shift+S". Used by menu entries.
     */
    public String displayName() {

        StringBuilder builder = new StringBuilder();
        if (hasControl()) {
            builder.append("Ctrl+");
        }
        if (hasShift()) {
            builder.append("Shift+");
        }
        if (hasAlt()) {
            builder.append("Alt+");
        }
        if (hasSuper()) {
            builder.append("Super+");
        }
        builder.append(keyName());
        return builder.toString();
    }

    private String keyName() {
        String value = InputConstants.Type.KEYSYM.getOrCreate(keyCode).getDisplayName().getString();
        return value.length() == 1 ? value.toUpperCase() : value;
    }

    @Override
    public String toString() {
        return displayName();
    }
}
