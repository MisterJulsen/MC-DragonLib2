package de.mrjulsen.mcdragonlib.gui.v2.behaviour;

import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;

/**
 * A behaviour that wants to decide the mouse cursor. Asked before the component's own cursor
 * property, see GuiComponent#getCurrentCursor.
 */
public interface CursorSource {
    /**
     * The cursor to show, or null to leave the decision to others.
     */
    CursorType cursor();
}
