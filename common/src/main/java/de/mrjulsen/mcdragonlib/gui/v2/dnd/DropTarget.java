package de.mrjulsen.mcdragonlib.gui.v2.dnd;

/**
 * A component that can receive a drag and drop payload. Implement this on the component itself.
 * <p>While dragging, the innermost target under the mouse is asked whether it takes the payload, and
 * if so, where exactly it would land.</p>
 */
public interface DropTarget {

    /**
     * Whether this target can do anything with the payload. Check the type here; a no means the drag
     * passes over as if the target were not there.
     */
    boolean acceptsDrop(Object payload);

    /**
     * Where the payload would land at this position, e.g. between two list entries.
     *
     * @return the position, or {@link DropPosition#NONE} if it cannot be dropped right here
     */
    DropPosition dropPositionAt(Object payload, double screenX, double screenY);

    /**
     * Takes the payload. Only called for a valid position.
     *
     * @param source where the payload came from, which is what tells a move from a copy
     */
    void performDrop(Object payload, DropPosition position, DragSource source);

    /**
     * Called when an accepted payload reaches this target.
     */
    default void onDragEnter(Object payload) {
    }

    /**
     * Called on every mouse movement over this target, with the current drop position. The place to
     * update an insert marker.
     */
    default void onDragOver(Object payload, DropPosition position) {
    }

    /**
     * Called when the payload leaves the target, and also after a drop. Time to drop any markers.
     */
    default void onDragLeave(Object payload) {
    }
}
