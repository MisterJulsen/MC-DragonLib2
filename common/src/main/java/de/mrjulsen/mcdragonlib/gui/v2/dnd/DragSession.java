package de.mrjulsen.mcdragonlib.gui.v2.dnd;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.gui.v2.render.LayerRegistry;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;

/**
 * One running drag and drop operation: where the payload came from, what it is, and which target it
 * is currently over.
 * <p>Created and driven by the input router, which also renders the ghost. There is at most one
 * session at a time, see InputRouter#dragSession.</p>
 */
public final class DragSession {

    private final DragSource source;
    private final GuiComponent sourceComponent;
    private final Object payload;
    private final int button;

    private DropTarget target;
    private GuiComponent targetComponent;
    private DropPosition position = DropPosition.NONE;
    private double screenX;
    private double screenY;

    public DragSession(DragSource source, GuiComponent sourceComponent, Object payload, int button, double screenX, double screenY) {
        this.source = source;
        this.sourceComponent = sourceComponent;
        this.payload = payload;
        this.button = button;
        this.screenX = screenX;
        this.screenY = screenY;
    }

    public DragSource source() {
        return source;
    }

    public GuiComponent sourceComponent() {
        return sourceComponent;
    }

    public Object payload() {
        return payload;
    }

    public int button() {
        return button;
    }

    /**
     * The target under the mouse that accepted the payload, or null if there is none.
     */
    public DropTarget target() {
        return target;
    }

    public GuiComponent targetComponent() {
        return targetComponent;
    }

    /**
     * Where the payload would land right now, as reported by the current target.
     */
    public DropPosition position() {
        return position;
    }

    public double screenX() {
        return screenX;
    }

    public double screenY() {
        return screenY;
    }

    /**
     * Moves the session to a new position and target, firing the enter and leave callbacks on the way.
     * A candidate that refuses the payload is treated as no target at all.
     *
     * @param candidate the innermost drop target under the mouse, or null
     */
    public void update(GuiComponent component, DropTarget candidate, double screenX, double screenY) {
        this.screenX = screenX;
        this.screenY = screenY;

        if (candidate != null && !candidate.acceptsDrop(payload)) {
            candidate = null;
            component = null;
        }
        if (target != candidate) {
            if (target != null) {
                target.onDragLeave(payload);
            }
            target = candidate;
            targetComponent = component;
            position = DropPosition.NONE;
            if (target != null) {
                target.onDragEnter(payload);
            }
        }
        if (target != null) {
            position = target.dropPositionAt(payload, screenX, screenY);
            target.onDragOver(payload, position);
        }
    }

    /**
     * Completes the drag: hands the payload to the current target if the position is valid and notifies
     * both sides.
     *
     * @return whether the payload was accepted
     */
    public boolean finish() {
        boolean accepted = false;
        if (target != null && position.isValid()) {
            target.performDrop(payload, position, source);
            accepted = true;
            if (targetComponent != null) {
                targetComponent.invokeEvent(targetComponent, new GuiEvents.DropEvent(payload, position, source));
            }
        }
        if (target != null) {
            target.onDragLeave(payload);
        }
        source.onDragFinished(payload, accepted);
        target = null;
        targetComponent = null;
        position = DropPosition.NONE;
        return accepted;
    }

    /**
     * Aborts the drag without dropping anything, e.g. because the source component disappeared.
     */
    public void cancel() {
        if (target != null) {
            target.onDragLeave(payload);
            target = null;
            targetComponent = null;
        }
        position = DropPosition.NONE;
        source.onDragFinished(payload, false);
    }

    /**
     * Queues the source's drag ghost on the drag layer, above everything else.
     */
    public void renderGhost(DLGuiGraphics g) {
        g.defer(LayerRegistry.DRAG, () -> source.renderDragGhost(g, payload, screenX, screenY));
    }
}
