package de.mrjulsen.mcdragonlib.gui.v2.behaviour;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;

/**
 * A piece of reusable behaviour that can be attached to any component, instead of baking it into a
 * subclass. Moving, resizing, scrolling, tooltips and hotkeys all work this way.
 * <p>Only one behaviour per class can be attached to a component; attaching another one replaces it.
 * Implementations should extend {@link AbstractBehaviour}, which already handles the host reference
 * and cleans up event listeners.</p>
 */
public interface Behaviour {
    /**
     * Called when this behaviour was attached to a component.
     */
    void onAttach(GuiComponent host);
    /**
     * Called when it was removed again, also when the component is disposed of.
     */
    void onDetach(GuiComponent host);
}
