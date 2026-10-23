package de.mrjulsen.mcdragonlib.gui.v2.input;

import com.google.common.util.concurrent.AtomicDouble;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.behaviour.AbstractBehaviour;
import de.mrjulsen.mcdragonlib.gui.v2.behaviour.CursorSource;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvent;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;

/**
 * Lets the user drag a component around. The component the user grabs and the one that moves may be
 * different, which is how a window is dragged by its title bar.
 * <p>Works on the raw drag events and consumes them, so a move does not turn into a drag and drop at
 * the same time.</p>
 */
public final class MoveHandle extends AbstractBehaviour implements CursorSource {

    /**
     * Default listener priority. Below normal, so a component's own drag handling comes first.
     */
    public static final int PRIORITY = GuiEvent.PRIORITY_LOW;

    private final GuiComponent explicitTarget;
    private final int priority;

    private boolean moving;
    private double startX;
    private double startY;

    /**
     * Whether dragging is possible at all. Turned off e.g. while a window is fullscreen.
     */
    public final BooleanProperty enabled = new BooleanProperty(true);
    /**
     * Whether the component has to stay inside its parent's content area.
     */
    public final BooleanProperty constrainToParent = new BooleanProperty(false);

    public MoveHandle() {
        this(null, PRIORITY);
    }

    public MoveHandle(GuiComponent target) {
        this(target, PRIORITY);
    }

    /**
     * @param target the component that moves, or null for the one this behaviour is attached to
     * @param priority listener priority
     */
    public MoveHandle(GuiComponent target, int priority) {
        this.explicitTarget = target;
        this.priority = priority;
    }

    /**
     * Makes a component draggable by itself.
     *
     * @return the behaviour, for further configuration
     */
    public static MoveHandle attach(GuiComponent target) {
        return target.addBehaviour(new MoveHandle());
    }

    /**
     * Makes one component drag another, e.g. a title bar moving its window. The behaviour is attached to
     * the handle, not to the target.
     *
     * @return the behaviour, for further configuration
     */
    public static MoveHandle attach(GuiComponent handle, GuiComponent target) {
        return handle.addBehaviour(new MoveHandle(target));
    }

    @Override
    protected void onAttached(GuiComponent host) {
        listen(GuiEvents.DragBeginEvent.class, this::onDragBegin, priority);
        listen(GuiEvents.DragEvent.class, this::onDrag, priority);
        listen(GuiEvents.DragEndEvent.class, this::onDragEnd, priority);
    }

    @Override
    protected void onDetached(GuiComponent host) {
        moving = false;
    }

    /**
     * The component the user grabs.
     */
    public GuiComponent handle() {
        return host();
    }

    /**
     * The component that actually moves, which is the handle itself unless another target was given.
     */
    public GuiComponent target() {
        return explicitTarget != null ? explicitTarget : host();
    }

    public boolean isMoving() {
        return moving;
    }

    @Override
    public CursorType cursor() {
        return moving ? CursorType.ALLRESIZE : null;
    }

    private void onDragBegin(GuiComponent src, GuiEvents.DragBeginEvent event) {
        GuiComponent target = target();
        if (!enabled.get() || target == null) {
            return;
        }
        moving = true;
        startX = target.dX();
        startY = target.dY();
        target.invokeEvent(target, new GuiEvents.MoveBeginEvent(target, event.screenX(), event.screenY()));
        event.consume();
    }

    private void onDrag(GuiComponent src, GuiEvents.DragEvent event) {
        GuiComponent target = target();
        if (!enabled.get() || !moving || target == null) {
            return;
        }
        event.consume();
        double scale = target.getTransform().outerScale();
        if (scale == 0) {
            return;
        }
        AtomicDouble x = new AtomicDouble(startX + event.totalDeltaX() / scale);
        AtomicDouble y = new AtomicDouble(startY + event.totalDeltaY() / scale);
        if (constrainToParent.get()) {
            target.getParent().ifPresent(p -> {
                Rectangle area = p.contentArea();
                x.set(Math.min(Math.max(x.get(), area.x()), Math.max(area.x(), area.x() + area.width() - target.dWidth())));
                y.set(Math.min(Math.max(y.get(), area.y()), Math.max(area.y(), area.y() + area.height() - target.dHeight())));
            });
        }
        target.setPosition(x.get(), y.get());
        target.invokeEvent(target, new GuiEvents.MoveEvent(target.dX(), target.dY()));
    }

    private void onDragEnd(GuiComponent src, GuiEvents.DragEndEvent event) {
        if (!moving) {
            return;
        }
        moving = false;
        GuiComponent target = target();
        if (target != null) {
            target.invokeEvent(target, new GuiEvents.MoveEndEvent(target.dX(), target.dY()));
        }
        event.consume();
    }
}
