package de.mrjulsen.mcdragonlib.client.newgui.widgets.util;

import de.mrjulsen.mcdragonlib.util.Color;

public class ColorProperty extends Property<Color> {

    private final Color fallbackColor;

    public ColorProperty(Color defaultValue, Color fallback) {
        super(defaultValue);
        if (fallback.isUndefined()) {
            throw new IllegalArgumentException("The fallback color must not be undefined!");
        }
        this.fallbackColor = fallback;
    }

    @Override
    public Color get() {
        if (getValue().isUndefined() || shouldOverrideLocal()) {
            return getInheritedValue().map(x -> x.isUndefined() ? null : x).orElse(getColor());
        }
        return getColor();
    }

    private Color getColor() {
        return getValue().isUndefined() ? fallbackColor : getValue();
    }
}
