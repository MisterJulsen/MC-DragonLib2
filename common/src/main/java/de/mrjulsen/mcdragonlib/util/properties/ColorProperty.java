package de.mrjulsen.mcdragonlib.util.properties;

import de.mrjulsen.mcdragonlib.util.DLColor;

/**
 * Property wrapper for a {@link DLColor} value that supports a non-undefined fallback
 * color and inheritance-aware resolution.
 *
 * <p>The property stores a local color value which may be undefined. When resolving
 * the effective value {@link #get()}, this class considers inheritance and a
 * fallback color: if the local value is undefined (or inheritance overrides local),
 * an inherited value is used when present and defined; otherwise the configured
 * fallback color is returned.</p>
 */
public class ColorProperty extends Property<DLColor> {

    private final DLColor fallbackColor;

    /**
     * Create a new ColorProperty.
     *
     * @param defaultValue the default local color value for this property; may be undefined
     * @param fallback a fallback color that must not be undefined and will be used
     *                 when no defined local or inherited color is available
     * @throws IllegalArgumentException if {@code fallback} is undefined
     */
    public ColorProperty(DLColor defaultValue, DLColor fallback) {
        super(defaultValue);
        if (fallback.isUndefined()) {
            throw new IllegalArgumentException("The fallback color must not be undefined!");
        }
        this.fallbackColor = fallback;
    }

    /**
     * Resolve and return the effective color for this property.
     *
     * <p>If the locally stored color is undefined or inheritance is configured to
     * override local values, this method attempts to use an inherited value (if present
     * and defined). If neither a defined inherited nor a defined local color is available,
     * the configured fallback color is returned.</p>
     *
     * @return the resolved {@link DLColor} (never undefined due to fallback)
     */
    @Override
    public DLColor get() {
        DLColor color = super.get();
        return color.isUndefined() ? fallbackColor : color;
    }
}
