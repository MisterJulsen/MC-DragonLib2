package de.mrjulsen.mcdragonlib.gui.v2.event;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;

/**
 * An event that belongs to one specific component but is also delivered to its ancestors.
 */
public abstract class TargetedEvent extends GuiEvent {

    private final GuiComponent target;

    protected TargetedEvent(GuiComponent target) {
        this.target = target;
    }

    /**
     * The component the event originally happened on, not necessarily the one currently handling it.
     */
    public GuiComponent target() {
        return target;
    }

    /**
     * Whether the given component is the original target rather than just an ancestor of it.
     */
    public boolean isDirect(GuiComponent component) {
        return target == component;
    }
}
