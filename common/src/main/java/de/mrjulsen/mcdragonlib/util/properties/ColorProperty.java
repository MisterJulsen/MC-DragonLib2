package de.mrjulsen.mcdragonlib.util.properties;

import de.mrjulsen.mcdragonlib.util.DLColor;

public class ColorProperty extends Property<DLColor> {

    private final DLColor fallbackColor;

    public ColorProperty(DLColor defaultValue, DLColor fallback) {
        super(defaultValue);
        if (fallback.isUndefined()) {
            throw new IllegalArgumentException("The fallback color must not be undefined!");
        }
        this.fallbackColor = fallback;
    }

    @Override
    public DLColor get() {
        if (getValue().isUndefined() || shouldOverrideLocal()) {
            return getInheritedValue().map(x -> x.isUndefined() ? null : x).orElse(getColor());
        }
        return getColor();
    }

    private DLColor getColor() {
        return getValue().isUndefined() ? fallbackColor : getValue();
    }
}
