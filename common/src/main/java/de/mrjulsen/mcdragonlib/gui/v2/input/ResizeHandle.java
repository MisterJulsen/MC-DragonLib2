package de.mrjulsen.mcdragonlib.gui.v2.input;

import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.behaviour.AbstractBehaviour;
import de.mrjulsen.mcdragonlib.gui.v2.behaviour.CursorSource;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;

/**
 * Lets the user resize a component by dragging its edges and corners.
 * <p>The grab zones sit inside the component's own bounds, see {@link #borderSize}, so no extra
 * components are needed. Min and max size are respected, and the cursor changes to match the edge
 * under the mouse.</p>
 */
public final class ResizeHandle extends AbstractBehaviour implements CursorSource {

    /**
     * Default listener priority. Just above the move handle, so grabbing an edge resizes instead of
     * moving.
     */
    public static final int PRIORITY = MoveHandle.PRIORITY + 1;

    /**
     * The edge or corner being dragged, each with the cursor it shows.
     */
    public enum Edge {
        NONE(null),
        TOP(CursorType.VRESIZE),
        BOTTOM(CursorType.VRESIZE),
        LEFT(CursorType.HRESIZE),
        RIGHT(CursorType.HRESIZE),
        TOP_LEFT(CursorType.TLBRRESIZE),
        TOP_RIGHT(CursorType.TRBLRESIZE),
        BOTTOM_LEFT(CursorType.TRBLRESIZE),
        BOTTOM_RIGHT(CursorType.TLBRRESIZE);

        private final CursorType cursor;

        Edge(CursorType cursor) {
            this.cursor = cursor;
        }

        public CursorType cursor() {
            return cursor;
        }

        public boolean movesLeft() {
            return this == LEFT || this == TOP_LEFT || this == BOTTOM_LEFT;
        }

        public boolean movesRight() {
            return this == RIGHT || this == TOP_RIGHT || this == BOTTOM_RIGHT;
        }

        public boolean movesTop() {
            return this == TOP || this == TOP_LEFT || this == TOP_RIGHT;
        }

        public boolean movesBottom() {
            return this == BOTTOM || this == BOTTOM_LEFT || this == BOTTOM_RIGHT;
        }
    }

    private final int priority;

    private Edge hovered = Edge.NONE;
    private Edge active = Edge.NONE;
    private double startX;
    private double startY;
    private double startWidth;
    private double startHeight;

    /**
     * Whether resizing is possible at all. Turned off e.g. while a window is fullscreen.
     */
    public final BooleanProperty enabled = new BooleanProperty(true);
    /**
     * How many pixels inwards from an edge still count as a grab zone.
     */
    public final NumberProperty<Double> borderSize = new NumberProperty<>(5D, 1D, 64D);
    /**
     * How long the corner zones are, where two edges can be dragged at once. Should be bigger than
     * {@link #borderSize}.
     */
    public final NumberProperty<Double> cornerSize = new NumberProperty<>(14D, 1D, 128D);

    public ResizeHandle() {
        this(PRIORITY);
    }

    public ResizeHandle(int priority) {
        this.priority = priority;
    }

    /**
     * Makes a component resizable at its edges.
     *
     * @return the behaviour, for further configuration
     */
    public static ResizeHandle attach(GuiComponent target) {
        return target.addBehaviour(new ResizeHandle());
    }

    @Override
    protected void onAttached(GuiComponent host) {
        listen(GuiEvents.MouseEnterEvent.class, (src, e) -> updateHovered(e.localX(host), e.localY(host)), priority);
        listen(GuiEvents.MouseMoveEvent.class, (src, e) -> updateHovered(e.localX(host), e.localY(host)), priority);
        listen(GuiEvents.MouseLeaveEvent.class, (src, e) -> hovered = Edge.NONE, priority);
        listen(GuiEvents.DragThresholdBeginEvent.class, this::onDragBegin, priority);
        listen(GuiEvents.DragEvent.class, this::onDrag, priority);
        listen(GuiEvents.DragEndEvent.class, this::onDragEnd, priority);
    }

    @Override
    protected void onDetached(GuiComponent host) {
        active = Edge.NONE;
        hovered = Edge.NONE;
    }

    public GuiComponent target() {
        return host();
    }

    /**
     * The grab zone the mouse is in, or {@link Edge#NONE}.
     */
    public Edge hoveredEdge() {
        return hovered;
    }

    /**
     * The edge currently being dragged, or {@link Edge#NONE}.
     */
    public Edge activeEdge() {
        return active;
    }

    public boolean isResizing() {
        return active != Edge.NONE;
    }

    @Override
    public CursorType cursor() {
        if (!enabled.get()) {
            return null;
        }
        return active != Edge.NONE ? active.cursor() : hovered.cursor();
    }

    private void updateHovered(double localX, double localY) {
        if (enabled.get() && active == Edge.NONE) {
            hovered = edgeAt(localX, localY);
        }
    }

    /**
     * Which grab zone a local position falls into. Useful to render resize hints.
     *
     * @return the edge, or {@link Edge#NONE} if the position is not in a grab zone
     */
    public Edge edgeAt(double localX, double localY) {
        GuiComponent target = target();
        if (target == null) {
            return Edge.NONE;
        }
        double border = borderSize.get();
        double corner = Math.min(cornerSize.get(), Math.min(target.dWidth(), target.dHeight()) / 2.0D);

        boolean left = localX >= -border && localX <= border;
        boolean right = localX >= target.dWidth() - border && localX <= target.dWidth() + border;
        boolean top = localY >= -border && localY <= border;
        boolean bottom = localY >= target.dHeight() - border && localY <= target.dHeight() + border;

        boolean nearLeft = localX <= corner;
        boolean nearRight = localX >= target.dWidth() - corner;
        boolean nearTop = localY <= corner;
        boolean nearBottom = localY >= target.dHeight() - corner;

        if (top && nearLeft || left && nearTop) {
            return Edge.TOP_LEFT;
        }
        if (top && nearRight || right && nearTop) {
            return Edge.TOP_RIGHT;
        }
        if (bottom && nearLeft || left && nearBottom) {
            return Edge.BOTTOM_LEFT;
        }
        if (bottom && nearRight || right && nearBottom) {
            return Edge.BOTTOM_RIGHT;
        }
        if (top) {
            return Edge.TOP;
        }
        if (bottom) {
            return Edge.BOTTOM;
        }
        if (left) {
            return Edge.LEFT;
        }
        if (right) {
            return Edge.RIGHT;
        }
        return Edge.NONE;
    }

    private void onDragBegin(GuiComponent src, GuiEvents.DragBeginEvent event) {
        GuiComponent target = target();
        if (!enabled.get() || target == null) {
            return;
        }
        active = edgeAt(event.localX(target), event.localY(target));
        if (active == Edge.NONE) {
            return;
        }
        startX = target.dX();
        startY = target.dY();
        startWidth = target.dWidth();
        startHeight = target.dHeight();
        target.invokeEvent(target, new GuiEvents.ResizeBeginEvent(target, event.screenX(), event.screenY()));
        event.consume();
    }

    private void onDrag(GuiComponent src, GuiEvents.DragEvent event) {
        if (!enabled.get() || active == Edge.NONE) {
            return;
        }
        apply(event.totalDeltaX(), event.totalDeltaY());
        event.consume();
    }

    private void onDragEnd(GuiComponent src, GuiEvents.DragEndEvent event) {
        GuiComponent target = target();
        if (active == Edge.NONE || target == null) {
            return;
        }
        active = Edge.NONE;
        target.invokeEvent(target, new GuiEvents.ResizeEndEvent(target.dX(), target.dY(), target.dWidth(), target.dHeight()));
        event.consume();
    }

    private void apply(double screenDeltaX, double screenDeltaY) {
        GuiComponent target = target();
        if (target == null) {
            return;
        }
        double scale = target.getTransform().outerScale();
        if (scale == 0) {
            return;
        }
        double dx = screenDeltaX / scale;
        double dy = screenDeltaY / scale;

        double minWidth = target.minSize.get().w();
        double maxWidth = target.maxSize.get().w();
        double minHeight = target.minSize.get().h();
        double maxHeight = target.maxSize.get().h();

        double x = startX;
        double y = startY;
        double width = startWidth;
        double height = startHeight;

        if (active.movesRight()) {
            width = MathUtils.clamp(startWidth + dx, minWidth, maxWidth);
        } else if (active.movesLeft()) {
            width = MathUtils.clamp(startWidth - dx, minWidth, maxWidth);
            x = startX + (startWidth - width);
        }
        if (active.movesBottom()) {
            height = MathUtils.clamp(startHeight + dy, minHeight, maxHeight);
        } else if (active.movesTop()) {
            height = MathUtils.clamp(startHeight - dy, minHeight, maxHeight);
            y = startY + (startHeight - height);
        }
        target.setBounds(x, y, width, height);
        target.invokeEvent(target, new GuiEvents.ResizeEvent(target.dX(), target.dY(), target.dWidth(), target.dHeight()));
    }
}
