package de.mrjulsen.mcdragonlib.gui.v2.theme;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;

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
        } else if (component.isPressed()) {
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
