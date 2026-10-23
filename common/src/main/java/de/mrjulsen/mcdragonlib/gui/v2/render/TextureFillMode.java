package de.mrjulsen.mcdragonlib.gui.v2.render;

/**
 * Determines how a source region of a texture is mapped onto the destination area.
 */
public enum TextureFillMode {

    /**
     * Scales the source region so that it covers the destination area exactly.
     */
    STRETCH,

    /**
     * Repeats the source region at its original size until the destination area is filled.
     * Partial tiles at the right and bottom edge are cropped. All tiles are emitted into a
     * single draw call, which makes this the cheapest way to fill large areas with a
     * pattern (dirt background, grids, ...).
     */
    TILE
}
