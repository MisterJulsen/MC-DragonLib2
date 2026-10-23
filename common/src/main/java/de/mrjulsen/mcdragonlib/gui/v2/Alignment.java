package de.mrjulsen.mcdragonlib.gui.v2;

/**
 * Generic alignment of a content block inside a larger space, used by layouts and text rendering.
 * <p>{@link #STRETCH} has no offset of its own; layouts interpret it as "fill the cross axis".</p>
 */
public enum Alignment {
    START,
    CENTER,
    END,
    STRETCH;

    /**
     * Offset at which a content block of the given length has to be placed to be aligned this way.
     *
     * @param available length of the space to align in
     * @param content length of the content to align
     * @return the offset from the start of the space; always 0 for START and STRETCH
     */
    public double offset(double available, double content) {
        return switch (this) {
            case CENTER -> (available - content) / 2.0D;
            case END -> available - content;
            default -> 0.0D;
        };
    }
}
