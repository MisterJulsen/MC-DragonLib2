package de.mrjulsen.mcdragonlib.gui.v2.dnd;

import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;

/**
 * A component that something can be dragged out of. Implement this on the component itself.
 * <p>The router only starts a drag and drop session if the component has no drag listeners of its
 * own, so a component cannot be moved and dragged out of at the same time.</p>
 */
public interface DragSource {

    /**
     * Builds the object being dragged, from the position the drag started at, e.g. the list entry under
     * the mouse.
     *
     * @return the payload, or null if there is nothing to drag here
     */
    Object createDragPayload(double screenX, double screenY);

    /**
     * Called once the drag really started, e.g. to grey out the dragged entry.
     */
    default void onDragStarted(Object payload) {
    }

    /**
     * Called when the drag is over, no matter how.
     *
     * @param accepted whether a target took the payload; false for a cancelled drag, which is when a
     *                 move has to be undone
     */
    default void onDragFinished(Object payload, boolean accepted) {
    }

    /**
     * Draws what hangs off the cursor while dragging. Rendered on its own layer above everything else,
     * so screen coordinates are the ones to use.
     */
    default void renderDragGhost(DLGuiGraphics g, Object payload, double screenX, double screenY) {
    }
}
