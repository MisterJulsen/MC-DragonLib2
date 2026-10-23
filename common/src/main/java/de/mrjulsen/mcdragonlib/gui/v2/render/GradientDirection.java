package de.mrjulsen.mcdragonlib.gui.v2.render;

/**
 * Direction in which a two-color gradient runs across a rectangle.
 */
public enum GradientDirection {

    TOP_TO_BOTTOM(0.0F, 0.0F, 1.0F, 1.0F),
    BOTTOM_TO_TOP(1.0F, 1.0F, 0.0F, 0.0F),
    LEFT_TO_RIGHT(0.0F, 1.0F, 1.0F, 0.0F),
    RIGHT_TO_LEFT(1.0F, 0.0F, 0.0F, 1.0F),
    TOP_LEFT_TO_BOTTOM_RIGHT(0.0F, 0.5F, 1.0F, 0.5F),
    TOP_RIGHT_TO_BOTTOM_LEFT(0.5F, 0.0F, 0.5F, 1.0F),
    BOTTOM_RIGHT_TO_TOP_LEFT(1.0F, 0.5F, 0.0F, 0.5F),
    BOTTOM_LEFT_TO_TOP_RIGHT(0.5F, 1.0F, 0.5F, 0.0F);

    private final float topLeft;
    private final float topRight;
    private final float bottomRight;
    private final float bottomLeft;

    private GradientDirection(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
        this.bottomLeft = bottomLeft;
    }

    public float topLeft() {
        return topLeft;
    }

    public float topRight() {
        return topRight;
    }

    public float bottomRight() {
        return bottomRight;
    }

    public float bottomLeft() {
        return bottomLeft;
    }

    /**
     * @return the direction describing the same axis with start and end color swapped
     */
    public GradientDirection inverted() {
        return switch (this) {
            case TOP_TO_BOTTOM -> BOTTOM_TO_TOP;
            case BOTTOM_TO_TOP -> TOP_TO_BOTTOM;
            case LEFT_TO_RIGHT -> RIGHT_TO_LEFT;
            case RIGHT_TO_LEFT -> LEFT_TO_RIGHT;
            case TOP_LEFT_TO_BOTTOM_RIGHT -> BOTTOM_RIGHT_TO_TOP_LEFT;
            case BOTTOM_RIGHT_TO_TOP_LEFT -> TOP_LEFT_TO_BOTTOM_RIGHT;
            case TOP_RIGHT_TO_BOTTOM_LEFT -> BOTTOM_LEFT_TO_TOP_RIGHT;
            case BOTTOM_LEFT_TO_TOP_RIGHT -> TOP_RIGHT_TO_BOTTOM_LEFT;
        };
    }
}
