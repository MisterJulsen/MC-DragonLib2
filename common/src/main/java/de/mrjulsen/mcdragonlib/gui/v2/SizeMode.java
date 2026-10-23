package de.mrjulsen.mcdragonlib.gui.v2;

/**
 * Determines where a component's width or height on one axis comes from. Set per axis via
 * {@link GuiComponent#widthMode} and {@link GuiComponent#heightMode}.
 * <p>Everything except {@link #WEIGHT} is resolved by the component itself while measuring; WEIGHT
 * needs a layout that implements it.</p>
 */
public enum SizeMode {
    /**
     * Keeps the size that was set explicitly, e.g. via setSize. The default.
     */
    FIXED,
    /**
     * Shrinks to fit the content: measureContent plus padding.
     */
    AUTO,
    /**
     * Takes all the space the parent offers, or the AUTO size if that space is unbounded.
     */
    FILL,
    /**
     * Takes a fraction of the offered space, given by widthFactor/heightFactor.
     * Falls back to the AUTO size if that space is unbounded.
     */
    PERCENT,
    /**
     * Takes a share of whatever space is left over after all non-growing siblings have been placed,
     * weighted by widthFactor/heightFactor. Only honoured by layouts that support it, elsewhere it behaves like FILL or AUTO.
     */
    WEIGHT
}
