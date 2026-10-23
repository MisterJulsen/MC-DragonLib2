package de.mrjulsen.mcdragonlib.gui.v2.event;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;

/**
 * An event with a mouse position. The position is stored in screen space, because the same event is
 * delivered to several components; use the conversion methods to get it in the space you need.
 */
public abstract class PointerEvent extends TargetedEvent {

    private final double screenX;
    private final double screenY;

    protected PointerEvent(GuiComponent target, double screenX, double screenY) {
        super(target);
        this.screenX = screenX;
        this.screenY = screenY;
    }

    public double screenX() {
        return screenX;
    }

    public double screenY() {
        return screenY;
    }

    /**
     * Mouse x relative to the given component's top left corner, ignoring its scroll offset.
     */
    public double localX(GuiComponent component) {
        return component.getTransform().toLocalX(screenX);
    }

    /**
     * Mouse y relative to the given component's top left corner, ignoring its scroll offset.
     */
    public double localY(GuiComponent component) {
        return component.getTransform().toLocalY(screenY);
    }

    /**
     * Mouse x in the given component's content space, i.e. including its scroll offset.
     */
    public double contentX(GuiComponent component) {
        return component.getTransform().screenToContentX(screenX);
    }

    /**
     * Mouse y in the given component's content space, i.e. including its scroll offset.
     */
    public double contentY(GuiComponent component) {
        return component.getTransform().screenToContentY(screenY);
    }
}
