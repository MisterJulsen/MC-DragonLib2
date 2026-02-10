package de.mrjulsen.mcdragonlib.client.gui.widgets.base;

import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.container.IMenuGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.ILayoutManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.LayoutResult;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.NoLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.Align;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.HitResult;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.HitResult.ComponentHitContext;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.HitResult.ComponentSelectionState;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.events.EventListenerWrapper;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.events.IEventDispatcher;
import de.mrjulsen.mcdragonlib.events.IEvent.Phase;
import de.mrjulsen.mcdragonlib.util.Cache;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Point;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.math.Size;
import de.mrjulsen.mcdragonlib.util.properties.BitflagProperty;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Base class for DragonLib GUI components.
 *
 * <p>This abstract component implements common GUI functionality such as:
 * event dispatching, child component management, rendering scaffolding, layout
 * integration and basic interaction handling (mouse, drag, resize, focus).</p>
 *
 * <p>Concrete widgets should extend this class and override rendering and
 * interaction hook methods (e.g. renderBackLayer, renderMainLayer, tick, ...).</p>
 */
@SupportsEvents({
        DLGuiStandardEvents.RenderPreEvent.class,
        DLGuiStandardEvents.RenderEvent.class,
        DLGuiStandardEvents.RenderPostEvent.class,
        DLGuiStandardEvents.RenderOnScreenEvent.class,
        DLGuiStandardEvents.ClickEvent.class,
        DLGuiStandardEvents.RightClickEvent.class,
        DLGuiStandardEvents.MultiClickEvent.class,
        DLGuiStandardEvents.MousePressedEvent.class,
        DLGuiStandardEvents.FocusChangedEvent.class,
        DLGuiStandardEvents.MouseDownEvent.class,
        DLGuiStandardEvents.MouseHoldDownEvent.class,
        DLGuiStandardEvents.MouseReleaseEvent.class,
        DLGuiStandardEvents.MouseEnterEvent.class,
        DLGuiStandardEvents.MouseLeaveEvent.class,
        DLGuiStandardEvents.MouseMoveEvent.class,
        DLGuiStandardEvents.ScrollEvent.class,
        DLGuiStandardEvents.ComponentAddedEvent.class,
        DLGuiStandardEvents.ComponentRemovedEvent.class,
        DLGuiStandardEvents.TickEvent.class,
        DLGuiStandardEvents.DragBeginEvent.class,
        DLGuiStandardEvents.DragEvent.class,
        DLGuiStandardEvents.DragEndEvent.class,
        DLGuiStandardEvents.KeyPressEvent.class,
        DLGuiStandardEvents.KeyReleaseEvent.class,
        DLGuiStandardEvents.CharTypeEvent.class,
        DLGuiStandardEvents.ResizeBeginEvent.class,
        DLGuiStandardEvents.ResizeEvent.class,
        DLGuiStandardEvents.ResizeEndEvent.class,
        DLGuiStandardEvents.ComponentPosAndSizeChanged.class,
        DLGuiStandardEvents.DragComponentBeginEvent.class,
        DLGuiStandardEvents.DragComponentEvent.class,
        DLGuiStandardEvents.DragComponentEndEvent.class,
        DLGuiStandardEvents.DragComponentOverBeginEvent.class,
        DLGuiStandardEvents.DragComponentOverEndEvent.class,
        DLGuiStandardEvents.DragComponentOverEvent.class,
        DLGuiStandardEvents.DraggingOverEvent.class,
        DLGuiStandardEvents.VisibilityChangedEvent.class,
        DLGuiStandardEvents.EnabledChangedEvent.class,
        DLGuiStandardEvents.ResizableChangedEvent.class,
        DLGuiStandardEvents.MovableChangedEvent.class,
        DLGuiStandardEvents.ParentChangedEvent.class,
        DLGuiStandardEvents.ComponentsClearEvent.class,
        DLGuiStandardEvents.ScreenLayoutUpdatedEvent.class,
        DLGuiStandardEvents.ComponentLayoutUpdatedEvent.class,
        DLGuiStandardEvents.DropComponentEvent.class,
        DLGuiStandardEvents.DragAndDropFilesEvent.class,
        DLGuiStandardEvents.WindowManagerChangeEvent.class,
        DLGuiStandardEvents.CloseEvent.class,
})
public abstract class DLGuiComponent implements IEventDispatcher<DLGuiComponent>, AutoCloseable {

    private final Map<Class<? extends IEvent>, PriorityQueue<EventListenerWrapper<?>>> eventListeners = new HashMap<>();

    @Override
    public Map<Class<? extends IEvent>, PriorityQueue<EventListenerWrapper<?>>> getEventListeners() {
        return eventListeners;
    }

    /**
     * Enumeration of input consumption contexts used to determine whether a
     * component should consume a certain interaction (click, drag, scroll, ...).
     */
    public enum ConsumptionType {
        /** Standard click events. */
        CLICK,
        /** Mouse movement events. */
        MOUSE_MOVE,
        /** Drag operations. */
        DRAG,
        /** Scroll wheel operations. */
        SCROLL,
        /** Drag-and-drop semantics. */
        DRAG_AND_DROP;
    }

    /**
     * Return the size in pixels used to detect a resize border area, scaled by current GUI scale.
     *
     * @return the number of pixels for the resize border at current GUI scale
     */
    public static final int getResizeBorderSize() {
        return (int) (10.0D / Minecraft.getInstance().getWindow().getGuiScale());
    }

    /**
     * Return the size in pixels used to detect resize corner areas.
     *
     * @return the number of pixels for resize corners based on {@link #getResizeBorderSize()}
     */
    public static final int getResizeCornerSize() {
        return getResizeBorderSize() * 8;
    }

    /**
     * Mouse drag threshold in pixels before a drag operation is considered started.
     */
    public static final int MOUSE_DRAG_THRESHOLD = 5;
    /**
     * Number of consecutive clicks that constitute a double/multi-click as configured.
     */
    public static final byte DOUBLE_CLICK_COUNT = 2;
    /**
     * Maximum interval (ms) between clicks to count as a multi-click.
     */
    public static final int MULTI_CLICK_SPEED_MS = 500;
    /**
     * Initial delay (ticks) before repeating mouse-hold events begin.
     */
    public static final int MOUSE_DOWN_INITIAL_DELAY = 10;

    // Container
    private final ConcurrentLinkedDeque<DLGuiComponent> components = new ConcurrentLinkedDeque<>();

    private final Cache<List<DLGuiComponent>> cachedComponents;

    // Widget
    private DLWindowManager windowManager = null;
    private DLGuiComponent parent;
    private double x;
    private double y;
    private double width;
    private double height;

    protected double scrollOffsetX;
    protected double scrollOffsetY;

    private boolean mouseSelected;
    private boolean mouseDown;
    private boolean focused;
    private boolean dragging;

    private boolean mouseInMoveArea = false;
    private Align resizeArea = Align.CENTER;
    protected Rectangle newBounds = Rectangle.EMPTY;

    private byte multiClickCount;
    private long multiClickLastMs = Long.MIN_VALUE;

    private int ticksHoldingDown;
    private double mouseDownX;
    private double mouseDownY;
    private int mouseDownButton;


    /**
     * The property that controls whether this component is enabled.
     * <p>This property fires {@link DLGuiStandardEvents.EnabledChangedEvent}.</p>
     */
    public final BooleanProperty enabled = new BooleanProperty(true)
            .withAfterPropertyChangedCallback(this::onEnabledChanged);

    /**
     * The property that controls whether this component is visible.
     * <p>This property fires {@link DLGuiStandardEvents.VisibilityChangedEvent}.</p>
     */
    public final BooleanProperty visible = new BooleanProperty(true)
            .withAfterPropertyChangedCallback(this::onVisibilityChanged);

    /**
     * Whether the component can be resized by the user.
     */
    public final BooleanProperty resizable = new BooleanProperty(false)
            .withAfterPropertyChangedCallback((o, v) -> invokeEvent(this, new DLGuiStandardEvents.ResizableChangedEvent(v), true));

    /**
     * Whether the component can be moved by the user.
     */
    public final BooleanProperty movable = new BooleanProperty(false)
            .withAfterPropertyChangedCallback((o, v) -> invokeEvent(this, new DLGuiStandardEvents.MovableChangedEvent(v), true));

    /**
     * Number of clicks required for multi-click behaviour.
     */
    public final NumberProperty<Byte> multiClickable = new NumberProperty<>((byte) 1, (byte) 1, Byte.MAX_VALUE);

    /**
     * Optional cursor override for this component. When null, the cursor is determined by area (resize/move/default).
     */
    public final Property<CursorType> cursor = new Property<>(null);

    /**
     * Policy that decides which types of input this component consumes.
     */
    public final Property<Predicate<ConsumptionType>> inputConsumptionPolicy = new Property<>(
            (context) -> context != ConsumptionType.SCROLL);

    /**
     * Anchor flags used for child positioning logic.
     */
    public final BitflagProperty<EAlign> anchor = new BitflagProperty<>(EAlign.class, EAlign.LEFT, EAlign.TOP);

    /**
     * Minimum allowed size for this component.
     */
    public final Property<Size> minSize = new Property<Size>(Size.of(5, 5));

    /**
     * Maximum allowed size for this component.
     */
    public final Property<Size> maxSize = new Property<>(Size.INFINITY);

    /**
     * Local scale factor for this component; inherited by children when computing global scale.
     */
    public final NumberProperty<Double> scale = new NumberProperty<>(1D, 0.01D, 10D);

    /**
     * Optional tooltip shown when this component is selected.
     */
    public final Property<DLTooltip> tooltip = new Property<DLTooltip>(DLTooltip.EMPTY);

    /**
     * Layout manager used to arrange child components.
     * <p>Changing the layout triggers arrangeComponents on the new manager.</p>
     */
    public final Property<ILayoutManager> layout = new Property<ILayoutManager>(NoLayout.INSTANCE)
        .withAfterPropertyChangedCallback((o, v) -> v.arrangeComponents(this));

    /**
     * Optional constraint object passed to the layout manager.
     */
    public final Property<Object> layoutContraint = new Property<>(null);

    /**
     * Arbitrary custom data associated with this component.
     */
    public final Property<Object> customData = new Property<>(null);

    /**
     * If true, will scroll this component into view when it receives focus.
     */
    public final BooleanProperty scrollToFocus = new BooleanProperty(true);


    protected final Cache<Double> globalX = new Cache<>(() -> getParent().map(p -> p.getXOnScreen()).orElse(0D) + (dX() * getParent().map(p -> p.scale.get()).orElse(1D)));
    protected final Cache<Double> globalY = new Cache<>(() -> getParent().map(p -> p.getYOnScreen()).orElse(0D) + (dY() * getParent().map(p -> p.scale.get()).orElse(1D)));

    private boolean layoutLoopFix = false;
    private boolean applyingLayout = false;
    private boolean doLayout = true;

    /**
     * Construct a new component with the given local position and size.
     *
     * @param x initial x position (local coordinates)
     * @param y initial y position (local coordinates)
     * @param w initial width
     * @param h initial height
     */
    public DLGuiComponent(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.cachedComponents = new Cache<>(() -> ImmutableList.copyOf(components));

        addEventListener(DLGuiStandardEvents.RenderEvent.class, (src, e) -> {
            if (src.width() <= 0 || src.height() <= 0)
                return true;
            switch (e.layer()) {
                case BACK -> renderBackLayer(e.graphics(), e.mouseX(), e.mouseY(), e.renderBounds());
                case MAIN -> renderMainLayer(e.graphics(), e.mouseX(), e.mouseY(), e.renderBounds());
                case FRONT -> renderFrontLayer(e.graphics(), e.mouseX(), e.mouseY(), e.renderBounds());
                case OVERLAY -> renderSpecialOverlay(e.graphics(), e.mouseX(), e.mouseY(), e.renderBounds());
                default -> {}
            }
            return false;
        });

        addEventListener(DLGuiStandardEvents.RenderOnScreenEvent.class, (src, e) -> {                
            if (isSelected() && tooltip.get() != DLTooltip.EMPTY) {
                tooltip.get().render(e.graphics(), (int)e.mouseX(), (int)e.mouseY());
            }
            renderOnScreen(e.graphics(), e.mouseX(), e.mouseY());
            return false;
        });

        addEventListener(DLGuiStandardEvents.ComponentPosAndSizeChanged.class, (s, e) -> {
            if (layoutLoopFix) return false;
            layoutLoopFix = true;
            getParent().ifPresent(p -> p.applyLayout());
            applyLayout();
            layoutLoopFix = false;
            return false;
        });
        addEventListener(DLGuiStandardEvents.ParentChangedEvent.class, (s, e) -> {
            applyLayout();
            return false;
        });
        addEventListener(DLGuiStandardEvents.ComponentAddedEvent.class, (s, e) -> {
            applyLayout();
            return false;
        });
        addEventListener(DLGuiStandardEvents.ComponentRemovedEvent.class, (s, e) -> {
            applyLayout();
            return false;
        });
        addEventListener(DLGuiStandardEvents.ScreenLayoutUpdatedEvent.class, (s, e) -> {
            applyLayout();
            return false;
        });        
        addEventListener(DLGuiStandardEvents.ComponentsClearEvent.class, (s, e) -> {
            applyLayout();
            return false;
        });
    }

    public void suspendLayout() {
        doLayout = false;
    }

    public void resumeLayout() {
        doLayout = true;
        applyLayout();
    }

    /**
     * Request the layout manager to arrange child components and notify listeners.
     * <p>This method guards against re-entrance using an internal flag.</p>
     */
    protected final void applyLayout() {
        if (applyingLayout || !doLayout) return;
        applyingLayout = true;
        LayoutResult result = this.layout.get().arrangeComponents(this);
        invokeEvent(this, new DLGuiStandardEvents.ComponentLayoutUpdatedEvent(result));
        applyingLayout = false;
    }

    /**
     * Hook invoked when the overall screen layout (e.g. screen size) changes.
     * <p>Subclasses may override to update internal state dependent on screen size.</p>
     */
    protected void updateScreenLayout() {
    }

    /**
     * Close this component and all children, emitting a CloseEvent.
     *
     * @throws Exception if any child's close throws; exceptions are propagated
     */
    @Override
    public void close() throws Exception {
        invokeEvent(this, new DLGuiStandardEvents.CloseEvent(), true);
        for (DLGuiComponent child : getComponents()) {
            child.close();
        }
    }

    /**
     * Return whether the component is currently in a dragging operation.
     *
     * @return true when dragging
     */
    public boolean isDragged() {
        return dragging;
    }

    /**
     * Return whether the mouse is currently hovering/selected on this component.
     *
     * @return true when selected by mouse
     */
    public boolean isSelected() {
        return mouseSelected;
    }

    /**
     * Return whether this component currently has keyboard focus.
     *
     * @return true if focused
     */
    public boolean isFocused() {
        return focused;
    }

    /**
     * Return whether the mouse button is currently held down over this component.
     *
     * @return true when mouse is down
     */
    public boolean isMouseDown() {
        return mouseDown;
    }

    /**
     * Return whether another component is currently being dragged over this component.
     *
     * @return true if a component is dragged over this component
     */
    public boolean isComponentDraggedOver() {
        return isComponentDraggedOver;
    }

    /**
     * Get the integer height of this component.
     *
     * @return height as int
     */
    public int height() {
        return (int) this.height;
    }

    /**
     * Get the integer width of this component.
     *
     * @return width as int
     */
    public int width() {
        return (int) this.width;
    }

    /**
     * Get the integer x position (local).
     *
     * @return x coordinate as int
     */
    public int x() {
        return (int) this.x;
    }

    /**
     * Get the integer y position (local).
     *
     * @return y coordinate as int
     */
    public int y() {
        return (int) this.y;
    }

    /**
     * Get the actual stored height as double.
     *
     * @return height as double
     */
    public double dHeight() {
        return this.height;
    }

    /**
     * Get the actual stored width as double.
     *
     * @return width as double
     */
    public double dWidth() {
        return this.width;
    }

    /**
     * Get the actual stored x as double.
     *
     * @return x as double
     */
    public double dX() {
        return this.x;
    }

    /**
     * Get the actual stored y as double.
     *
     * @return y as double
     */
    public double dY() {
        return this.y;
    }

    /**
     * Compute the component's X position on screen (including parents).
     *
     * @return the X coordinate in screen space
     */
    public double getXOnScreen() {
        return globalX.get();
    }

    /**
     * Compute the component's Y position on screen (including parents).
     *
     * @return the Y coordinate in screen space
     */
    public double getYOnScreen() {
        return globalY.get();
    }

    /**
     * Compute the top-left point of this component in screen coordinates.
     *
     * @return a Point representing the screen coordinates of this component
     */
    public Point toScreenCoordinates() {
        Point local = Point.of(
            dX() - getScrollOffsetX(),
            dY() - getScrollOffsetY()
        );

        return getParent()
            .map(parent -> parent.toScreenCoordinates().add(local))
            .orElse(local);
    }


    /**
     * Compute the global scale by combining parent scales with this component's scale.
     *
     * @return global scale factor
     */
    public double getGlobalScale() {
        return getParent().map(x -> x.getGlobalScale()).orElse(1D) * scale.get();
    }

    /**
     * Invalidate cached global coordinates and propagate to children.
     *
     * @param x whether to invalidate X cache
     * @param y whether to invalidate Y cache
     */
    protected void invalidateGlobalCoordinates(boolean x, boolean y) {
        if (x)
            globalX.clear();
        if (y)
            globalY.clear();
        forEachComponentMatching(t -> true, c -> c.invalidateGlobalCoordinates(x, y));
    }

    /**
     * Set the X position for this component (local coordinates) and notify listeners.
     *
     * @param x new x position
     */
    public void setX(double x) {
        double oldX = x;
        this.x = x;
        invalidateGlobalCoordinates(true, false);
        invokeEvent(this, new DLGuiStandardEvents.ComponentPosAndSizeChanged((int)oldX, (int)x, (int)y, (int)y, (int)width, (int)width, (int)height, (int)height));
    }

    /**
     * Set the Y position for this component (local coordinates) and notify listeners.
     *
     * @param y new y position
     */
    public void setY(double y) {
        double oldY = x;
        this.y = y;
        invalidateGlobalCoordinates(false, true);
        invokeEvent(this, new DLGuiStandardEvents.ComponentPosAndSizeChanged((int)x, (int)x, (int)oldY, (int)y, (int)width, (int)width, (int)height, (int)height));
    }

    /**
     * Move the left side to the given x coordinate and adjust width accordingly.
     *
     * @param x new left position
     */
    public void setLeft(double x) {
        double diff = dX() - x;
        setX(x);
        setWidth(dWidth() + diff);
    }

    /**
     * Move the top side to the given y coordinate and adjust height accordingly.
     *
     * @param y new top position
     */
    public void setTop(double y) {
        double diff = y - dY();
        setY(y);
        setHeight(dHeight() + diff);
    }

    /**
     * Set the width of this component, adjusting anchored children as needed.
     *
     * @param width new width in local coordinates
     */
    public void setWidth(double width) {
        double oldWidth = dWidth();
        this.width = width;
        double diff = width - oldWidth;

        for (DLGuiComponent child : getComponents()) {
            double k = child.dWidth() / 2D;
            if (child.anchor.has(EAlign.RIGHT)) {
                if (child.anchor.has(EAlign.LEFT)) {
                    child.setWidth(child.dWidth() + diff);
                } else {
                    child.setX(dWidth() - child.dWidth());
                }
            }
            if (child.anchor.hasNone(EAlign.LEFT, EAlign.RIGHT)) {
                if (oldWidth > 0 && width() > 0) {
                    double p = Math.max(1D / oldWidth * (child.dX() + k), 0);
                    child.setX(p * dWidth() - k);
                }
            }
        }
        invokeEvent(this, new DLGuiStandardEvents.ComponentPosAndSizeChanged((int)x, (int)x, (int)y, (int)y, (int)oldWidth, (int)width, (int)height, (int)height));
    }

    /**
     * Set the height of this component, adjusting anchored children as needed.
     *
     * @param height new height in local coordinates
     */
    public void setHeight(double height) {
        double oldHeight = dHeight();
        this.height = height;
        double diff = height - oldHeight;

        for (DLGuiComponent child : getComponents()) {
            double k = child.dHeight() / 2D;
            if (child.anchor.has(EAlign.BOTTOM)) {
                if (child.anchor.has(EAlign.TOP)) {
                    child.setHeight(child.dHeight() + diff);
                } else {
                    child.setY(dHeight() - child.dHeight());
                }
            }
            if (child.anchor.hasNone(EAlign.TOP, EAlign.BOTTOM)) {
                if (oldHeight > 0 && height() > 0) {
                    double p = Math.max(1D / oldHeight * (child.dY() + k), 0);
                    child.setY(p * dHeight() - k);
                }
            }
        }

        invokeEvent(this, new DLGuiStandardEvents.ComponentPosAndSizeChanged((int)x, (int)x, (int)y, (int)y, (int)width, (int)width, (int)oldHeight, (int)height));
    }

    /**
     * Set both local position coordinates.
     *
     * @param x target x
     * @param y target y
     */
    public void setPosition(double x, double y) {
        setX(x);
        setY(y);
    }

    /**
     * Set both size dimensions.
     *
     * @param width new width
     * @param height new height
     */
    public void setSize(double width, double height) {
        setWidth(width);
        setHeight(height);
    }

    /**
     * Bring the given child component to the front of the z-order.
     *
     * @param component child to bring to front
     * @return true if the component was present and moved
     */
    public boolean bringToFront(DLGuiComponent component) {
        if (components.contains(component)) {
            components.remove(component);
            components.addLast(component);
            cachedComponents.clear();
            return true;
        }
        return false;
    }    

    /**
     * Send the given child component to the back of the z-order.
     *
     * @param component child to send to back
     * @return true if the component was present and moved
     */
    public boolean sendToBack(DLGuiComponent component) {
        if (components.contains(component)) {
            components.remove(component);
            components.addFirst(component);
            cachedComponents.clear();
            return true;
        }
        return false;
    }

    /**
     * Set the horizontal scroll offset (clamped to non-negative).
     *
     * @param scrollOffsetX scroll offset in local pixels
     */
    public void setScrollOffsetX(double scrollOffsetX) {
        this.scrollOffsetX = MathUtils.clamp(scrollOffsetX, 0, Integer.MAX_VALUE);
    }

    /**
     * Set the vertical scroll offset (clamped to non-negative).
     *
     * @param scrollOffsetY scroll offset in local pixels
     */
    public void setScrollOffsetY(double scrollOffsetY) {
        this.scrollOffsetY = MathUtils.clamp(scrollOffsetY, 0, Integer.MAX_VALUE);
    }

    /**
     * Get the current horizontal scroll offset.
     *
     * @return scroll offset X
     */
    public double getScrollOffsetX() {
        return scrollOffsetX;
    }

    /**
     * Get the current vertical scroll offset.
     *
     * @return scroll offset Y
     */
    public double getScrollOffsetY() {
        return scrollOffsetY;
    }

    /**
     * Ensure the provided child is visible by adjusting scroll offsets if necessary.
     *
     * @param child the child component to bring into view
     */
    public void scrollIntoView(DLGuiComponent child) {
        if (!components.contains(child)) {
            return;
        }

        if (child.x() + child.width() > getScrollOffsetX() + width()) {
            this.setScrollOffsetX(child.x() + child.width() - width());
        } else if (child.x() < getScrollOffsetX()) {
            setScrollOffsetX(child.x());
        }

        if (child.y() + child.height() > getScrollOffsetY() + height()) {
            this.setScrollOffsetY(child.y() + child.height() - height());
        } else if (child.y() < getScrollOffsetY()) {
            setScrollOffsetY(child.y());
        }
    }

    /**
     * The bounds in which all child components can be interacted with.
     *
     * @return the rectangle describing child interaction bounds (local coords)
     */
    public Rectangle getChildInteractionBounds() {
        return Rectangle.withSize(0, 0, Math.max(width(), 0), Math.max(height(), 0));
    }

    /**
     * The bounds in which this and all child components can be interacted with.
     *
     * @return the rectangle describing interaction bounds (local coords)
     */
    public Rectangle getInteractionBounds() {
        return Rectangle.withSize(0, 0, Math.max(width(), 0), Math.max(height(), 0));
    }

    /**
     * The bounds in which this component and all children are rendered. Anything outside is clipped.
     *
     * @return the rectangle describing render bounds (local coords)
     */
    public Rectangle getRenderBounds() {
        return Rectangle.withSize(0, 0, Math.max(width(), 0), Math.max(height(), 0));
    }

    /**
     * The child render bounds used to clip children; cannot exceed {@link #getRenderBounds()}.
     *
     * @return the child render bounds rectangle
     */
    public Rectangle getChildRenderBounds() {
        return Rectangle.withSize(0, 0, Math.max(width(), 0), Math.max(height(), 0));
    }

    /**
     * Return the position box containing this component at its local coordinates.
     *
     * @return a rectangle that covers this component at its position
     */
    public Rectangle getPositionBox() {
        return Rectangle.withSize(x(), y(), Math.max(width(), 0), Math.max(height(), 0));
    }

    /**
     * Return a bounding collision box that surrounds this component and all nested children.
     *
     * @return the surrounding collision rectangle in local coordinates
     */
    public Rectangle getSurroundingCollisionBox() {
        return Rectangle.surroundingBase(getInteractionBounds(),
                getComponents().stream().map(DLGuiComponent::getSurroundingCollisionBox).toArray(Rectangle[]::new));
    }

    /**
     * Get the mouse X coordinate relative to this component.
     *
     * @return local mouse X coordinate
     */
    public double getLocalMouseX() {
        if (getWindowManager() == null)
            return 0;
        return getWindowManager().mouseXOnScreen() - getXOnScreen();
    }

    /**
     * Get the mouse Y coordinate relative to this component.
     *
     * @return local mouse Y coordinate
     */
    public double getLocalMouseY() {
        if (getWindowManager() == null)
            return 0;
        return getWindowManager().mouseYOnScreen() - getYOnScreen();
    }

    private void onEnabledChanged(boolean oldState, boolean newState) {
        invokeEvent(this, new DLGuiStandardEvents.EnabledChangedEvent(newState), false);
    }

    private void onVisibilityChanged(boolean oldState, boolean newState) {
        invokeEvent(this, new DLGuiStandardEvents.VisibilityChangedEvent(newState), false);
    }

    /**
     * Add a child component to this container and initialize its parent/window manager.
     *
     * @param component the component to add
     * @param <T> type of the component
     * @return the added component
     * @throws IllegalArgumentException if the component is a window or invalid for menus
     */
    public <T extends DLGuiComponent> T addComponent(T component) {
        addComponentInternal(component);
        invokeEvent(this, new DLGuiStandardEvents.ComponentAddedEvent(List.of(component)), true);
        return component;
    }

    public void addComponents(List<DLGuiComponent> components) {
        for (var comp : components) {
            this.addComponent(comp);
        }
        invokeEvent(this, new DLGuiStandardEvents.ComponentAddedEvent(components), true);
    }

    private <T extends DLGuiComponent> T addComponentInternal(T component) {
        Objects.requireNonNull(component);

        if (component instanceof DLWindow) {
            throw new IllegalArgumentException("Cannot add windows as components.");
        }
        if (component instanceof IMenuGuiComponent) {
            if (!windowManager.supportsMenus()) {
                throw new IllegalArgumentException("The window manager doesn't support menus.");
            }
            if (getNextParentMatching(c -> c instanceof DLMenuWindow).isEmpty()) {
                throw new IllegalArgumentException("Cannot add menu components to non-menu windows.");
            }
        }

        this.components.add(component);
        component.setParent(this);
        component.setWindowManager(windowManager);
        component.invalidateGlobalCoordinates(true, true);
        cachedComponents.clear();
        return component;
    }

    /**
     * Remove a child component.
     *
     * @param component the component to remove
     * @param <T> type of the component
     * @return true if removed
     */
    public <T extends DLGuiComponent> boolean removeComponent(T component) {
        boolean b = removeComponentInternal(component);
        if (b) {
            invokeEvent(this, new DLGuiStandardEvents.ComponentRemovedEvent(List.of(component)), true);
        }
        return b;
    }

    public boolean removeComponents(List<DLGuiComponent> components) {
        boolean b = false;
        for (var comp : components) {
            b |= removeComponentInternal(comp);
        }
        if (b) {
            invokeEvent(this, new DLGuiStandardEvents.ComponentRemovedEvent(components), true);
        }
        return b;
    }

    private <T extends DLGuiComponent> boolean removeComponentInternal(T component) {
        Objects.requireNonNull(component);
        boolean b = this.components.remove(component);
        cachedComponents.clear();
        if (b) {
            component.setParent(null);
            component.setWindowManager(null);
            component.invalidateGlobalCoordinates(true, true);
        }
        return b;
    }

    /**
     * Test whether the given mouse coordinates are within this component's interaction bounds.
     *
     * @param mouseX mouse X in local coordinates
     * @param mouseY mouse Y in local coordinates
     * @return true when the mouse is over this component
     */
    public boolean isMouseOver(double mouseX, double mouseY) {
        return getInteractionBounds().collision(mouseX, mouseY);
    }

    /**
     * Determine the cursor that should be displayed for this component depending on area (resize/move/default).
     *
     * @return the CursorType to set
     */
    public CursorType getCursor() {
        return resizeArea == Align.CENTER ? (mouseInMoveArea ? CursorType.ALLRESIZE : cursor.get())
                : resizeArea.getCursor();
    }

    /**
     * Return the number of direct child components.
     *
     * @return child count
     */
    public int componentsCount() {
        return this.components.size();
    }

    /**
     * Return whether this component has any children.
     *
     * @return true when children are present
     */
    public boolean hasComponents() {
        return !this.components.isEmpty();
    }

    /**
     * Return an Optional containing this component's parent if present.
     *
     * @return optional parent component
     */
    public Optional<DLGuiComponent> getParent() {
        return Optional.ofNullable(this.parent);
    }

    /**
     * Find the next parent (up the hierarchy) that matches the given predicate.
     *
     * @param condition predicate to match a parent
     * @return optional matching parent
     */
    public Optional<DLGuiComponent> getNextParentMatching(Predicate<DLGuiComponent> condition) {
        if (!getParent().isPresent()) {
            return Optional.empty();
        }
        DLGuiComponent parent = getParent().get();
        if (condition.test(parent)) {
            return Optional.of(parent);
        }
        return parent.getNextParentMatching(condition);
    }

    /**
     * Execute an action for each child that satisfies the test predicate.
     *
     * @param test predicate to select children
     * @param action action to perform on selected children
     */
    public void forEachComponentMatching(Predicate<DLGuiComponent> test, Consumer<DLGuiComponent> action) {
        for (DLGuiComponent component : getComponents()) {
            if (test.test(component))
                action.accept(component);
        }
    }

    /**
     * Execute an action for each child of the exact provided class type (no subtypes).
     *
     * @param type class to match
     * @param test predicate applied to matched children
     * @param action action to perform
     * @param <T> component type
     */
    @SuppressWarnings("unchecked")
    public <T extends DLGuiComponent> void forEachComponentMatching(Class<T> type, Predicate<T> test,
            Consumer<T> action) {
        for (DLGuiComponent component : getComponents()) {
            if (component.getClass().equals(type)) {
                T t = (T) component;
                if (test.test(t))
                    action.accept(t);
            }
        }
    }

    /**
     * Get the window manager this component is assigned to. May be null if not attached.
     *
     * @return the DLWindowManager or null
     */
    public DLWindowManager getWindowManager() {
        return windowManager;
    }

    /**
     * Assign a window manager to this component and propagate to children.
     *
     * @param windowManager the manager to set, may be null to detach
     */
    public void setWindowManager(DLWindowManager windowManager) {
        DLWindowManager oldManager = this.windowManager;
        this.windowManager = windowManager;
        invokeEvent(this, new DLGuiStandardEvents.WindowManagerChangeEvent(oldManager, windowManager), true);
        for (DLGuiComponent child : getComponents()) {
            child.setWindowManager(windowManager);
        }
    }

    /**
     * Set the parent for this component (package-private). Fires parent-changed event.
     *
     * @param parent new parent component or null
     * @param <T> parent type
     */
    <T extends DLGuiComponent> void setParent(T parent) {
        Optional<DLGuiComponent> oldParent = getParent();
        this.parent = parent;
        invokeEvent(this, new DLGuiStandardEvents.ParentChangedEvent(oldParent, Optional.ofNullable(parent)), true);
    }

    /**
     * Return an immutable copy of the direct children list.
     *
     * @return list of children
     */
    public List<DLGuiComponent> getComponents() {
        return cachedComponents.get();
    }

    /**
     * Return a list of children matching the provided predicate.
     *
     * @param predicate selection predicate
     * @return list of matching children
     */
    public List<DLGuiComponent> getComponentsMatching(Predicate<DLGuiComponent> predicate) {
        return getComponents().stream().filter(predicate::test).toList();
    }

    /**
     * Return children of the provided type. Can include subtypes when requested.
     *
     * @param type target class
     * @param includeSubtypes whether to include subclasses
     * @param <T> component type
     * @return list of matching components
     */
    public <T extends DLGuiComponent> List<T> getComponentsOfType(Class<T> type, boolean includeSubtypes) {
        List<T> result = new ArrayList<>(componentsCount());
        for (DLGuiComponent obj : getComponents()) {
            if (includeSubtypes) {
                if (type.isInstance(obj)) {
                    result.add(type.cast(obj));
                }
            } else {
                if (obj.getClass().equals(type)) {
                    result.add(type.cast(obj));
                }
            }
        }
        return result;
    }

    /**
     * Remove all child components.
     */
    public void clearComponents() {
        clearComponents(c -> true);
    }

    /**
     * Remove child components matching the predicate. Fires pre/post clear events.
     *
     * @param predicate predicate to select components for removal
     */
    public void clearComponents(Predicate<DLGuiComponent> predicate) {
        MutableBoolean bool = new MutableBoolean();
        invokeEvent(this, new DLGuiStandardEvents.ComponentsClearEvent(Phase.PRE, bool), true);
        if (bool.isTrue())
            return;
        Iterator<DLGuiComponent> iterator = components.iterator();
        while (iterator.hasNext()) {
            DLGuiComponent component = iterator.next();
            if (predicate.test(component)) {
                iterator.remove();
            }
        }
        cachedComponents.clear();
        invokeEvent(this, new DLGuiStandardEvents.ComponentsClearEvent(Phase.POST, bool), true);
    }

    /**
     * Set whether this component is selected (mouse entered/left) and fire enter/leave/move events.
     *
     * @param b selection state
     * @param mouseX mouse X local coordinate
     * @param mouseY mouse Y local coordinate
     * @return true if selection state changed
     */
    public boolean setSelected(boolean b, double mouseX, double mouseY) {
        boolean hasChanged = mouseSelected != b;
        if (hasChanged) {
            if (b) {
                CursorType.set(getCursor());
                invokeEvent(this, new DLGuiStandardEvents.MouseEnterEvent(mouseX, mouseY), true);
            } else {
                CursorType.set(null);
                invokeEvent(this, new DLGuiStandardEvents.MouseLeaveEvent(mouseX, mouseY), true);
            }
            this.mouseSelected = b;
        } else if (b) {
            CursorType.set(getCursor());
            invokeEvent(this, new DLGuiStandardEvents.MouseMoveEvent(mouseX, mouseY), true);
        }

        if (b) {
            updateResizeArea(mouseX, mouseY);
            updateMoveArea(mouseX, mouseY);
        }

        return hasChanged;
    }

    /**
     * Update whether the mouse is inside the move area for dragging the component.
     *
     * @param mouseX mouse X local coordinate
     * @param mouseY mouse Y local coordinate
     */
    public void updateMoveArea(double mouseX, double mouseY) {
        if (!this.movable.get() || getResizeArea() != Align.CENTER) {
            setInMoveArea(false);
            return;
        }
        int dX = (int) mouseX, dY = (int) mouseY;
        setInMoveArea(dX <= getResizeBorderSize() || dX >= width() - getResizeBorderSize() || dY <= getResizeBorderSize() || dY >= height() - getResizeBorderSize());
    }

    /**
     * Update the resize area (which border/corner is hovered) based on mouse position.
     *
     * @param mouseX local mouse X
     * @param mouseY local mouse Y
     */
    public void updateResizeArea(double mouseX, double mouseY) {
        if (!this.resizable.get()) {
            setResizeArea(Align.CENTER);
            return;
        }
        int dX = (int) mouseX, dY = (int) mouseY;
        int cornerWidthX = Math.min(getResizeCornerSize(), width() / 2),
                cornerHeightY = Math.min(getResizeCornerSize(), height() / 2);
        if (dX > getResizeBorderSize() && dX < width() - getResizeBorderSize() && dY > getResizeBorderSize()
                && dY < height() - getResizeBorderSize()) {
            setResizeArea(Align.CENTER);
            return;
        }
        if (dY < cornerHeightY) { // Top
            boolean xCondition = !this.movable.get() || (width() >= getResizeCornerSize() * 3 + 2
                    && dX > width() / 2 - getResizeCornerSize() / 2 && dX < width() / 2 + getResizeCornerSize() / 2);
            if (dX < cornerWidthX)
                setResizeArea(Align.TOP_LEFT);
            else if (dX > width() - cornerWidthX)
                setResizeArea(Align.TOP_RIGHT);
            else if (xCondition)
                setResizeArea(Align.TOP);
            else
                setResizeArea(Align.CENTER);
            return;
        }
        if (dY > height() - cornerHeightY) { // Bottom
            boolean xCondition = !this.movable.get() || (width() >= getResizeCornerSize() * 3 + 2
                    && dX > width() / 2 - getResizeCornerSize() / 2 && dX < width() / 2 + getResizeCornerSize() / 2);
            if (dX < cornerWidthX)
                setResizeArea(Align.BOTTOM_LEFT);
            else if (dX > width() - cornerWidthX)
                setResizeArea(Align.BOTTOM_RIGHT);
            else if (xCondition)
                setResizeArea(Align.BOTTOM);
            else
                setResizeArea(Align.CENTER);
            return;
        }
        boolean yCondition = !this.movable.get() || (height() >= getResizeCornerSize() * 3 + 2
                && dY > height() / 2 - getResizeCornerSize() / 2 && dY < height() / 2 + getResizeCornerSize() / 2);
        if (dX < cornerWidthX && yCondition)
            setResizeArea(Align.LEFT);
        else if (dX > width() - cornerWidthX && yCondition)
            setResizeArea(Align.RIGHT);
        else
            setResizeArea(Align.CENTER);
    }

    /**
     * Set keyboard focus for this component and emit focus-change event if changed.
     *
     * @param b true to set focus, false to remove
     * @return true if focus changed
     */
    public boolean setFocus(boolean b) {
        boolean hasChanged = focused != b;
        this.focused = b;
        if (hasChanged) {
            invokeEvent(this, new DLGuiStandardEvents.FocusChangedEvent(b), true);
        }
        return hasChanged;
    }

    /**
     * Set the mouse-down state and emit related events.
     *
     * @param b new mouse-down state
     * @param mouseX mouse X local
     * @param mouseY mouse Y local
     * @param button mouse button index
     * @return true if state changed
     */
    public boolean setMouseDown(boolean b, double mouseX, double mouseY, int button) {
        this.mouseDownX = mouseX;
        this.mouseDownY = mouseY;
        this.mouseDownButton = button;
        boolean hasChanged = mouseDown != b;
        if (hasChanged) {
            if (b) {
                invokeEvent(this, new DLGuiStandardEvents.MouseDownEvent(mouseX, mouseY, button), true);
            } else {
                invokeEvent(this, new DLGuiStandardEvents.MouseReleaseEvent(mouseX, mouseY, button), true);
                ticksHoldingDown = 0;
            }
            this.mouseDown = b;
        }
        return hasChanged;
    }

    private int dragOffsetX, dragOffsetY, dragOriginalWidth, dragOriginalHeight;

    /**
     * 
     * @param b
     * @param mouseX
     * @param mouseY
     * @param button
     * @param mouseOriginX
     * @param mouseOriginY
     * @param dragX
     * @param dragY
     * @param dragThresholdTriggered
     * @param dragOverComponents
     * @return Special Dragging
     */
    public boolean setDragging(boolean b, double mouseX, double mouseY, int button, double mouseOriginX,
            double mouseOriginY, double dragX, double dragY, boolean dragThresholdTriggered,
            List<DLGuiComponent> dragOverComponents) {
        boolean hasChanged = dragging != b;
        this.dragging = b;
        if (hasChanged && b) {
            dragOffsetX = (int) (mouseX * getGlobalScale() - x());
            dragOffsetY = (int) (mouseY * getGlobalScale() - y());
            dragOriginalWidth = width();
            dragOriginalHeight = height();
        }

        if (getResizeArea() != Align.CENTER) {
            int newX1 = x(), newY1 = y(), newX2 = x() + width(), newY2 = y() + height();
            switch (getResizeArea()) {
                case TOP_LEFT -> {
                    newX1 = MathUtils.clamp((int) (mouseX - dragOffsetX), (int) (newX2 - this.maxSize.get().w()),
                            (int) (newX2 - this.minSize.get().w()));
                    newY1 = MathUtils.clamp((int) (mouseY - dragOffsetY), (int) (newY2 - this.maxSize.get().h()),
                            (int) (newY2 - this.minSize.get().h()));
                }
                case TOP -> newY1 = MathUtils.clamp((int) (mouseY - dragOffsetY),
                        (int) (newY2 - this.maxSize.get().h()), (int) (newY2 - this.minSize.get().h()));
                case LEFT -> newX1 = MathUtils.clamp((int) (mouseX - dragOffsetX),
                        (int) (newX2 - this.maxSize.get().w()), (int) (newX2 - this.minSize.get().w()));
                case BOTTOM_RIGHT -> {
                    newX2 = MathUtils.clamp((int) (mouseX + (dragOriginalWidth - dragOffsetX)),
                            (int) (newX1 + this.minSize.get().w()), (int) (newX1 + this.maxSize.get().w()));
                    newY2 = MathUtils.clamp((int) (mouseY + (dragOriginalHeight - dragOffsetY)),
                            (int) (newY1 + this.minSize.get().h()), (int) (newY1 + this.maxSize.get().h()));
                }
                case RIGHT -> newX2 = MathUtils.clamp((int) (mouseX + (dragOriginalWidth - dragOffsetX)),
                        (int) (newX1 + this.minSize.get().w()), (int) (newX1 + this.maxSize.get().w()));
                case BOTTOM -> newY2 = MathUtils.clamp((int) (mouseY + (dragOriginalHeight - dragOffsetY)),
                        (int) (newY1 + this.minSize.get().h()), (int) (newY1 + this.maxSize.get().h()));
                case BOTTOM_LEFT -> {
                    newX1 = MathUtils.clamp((int) (mouseX - dragOffsetX), (int) (newX2 - this.maxSize.get().w()),
                            (int) (newX2 - this.minSize.get().w()));
                    newY2 = MathUtils.clamp((int) (mouseY + (dragOriginalHeight - dragOffsetY)),
                            (int) (newY1 + this.minSize.get().h()), (int) (newY1 + this.maxSize.get().h()));
                }
                case TOP_RIGHT -> {
                    newX2 = MathUtils.clamp((int) (mouseX + (dragOriginalWidth - dragOffsetX)),
                            (int) (newX1 + this.minSize.get().w()), (int) (newX1 + this.maxSize.get().w()));
                    newY1 = MathUtils.clamp((int) (mouseY - dragOffsetY), (int) (newY2 - this.maxSize.get().h()),
                            (int) (newY2 - this.minSize.get().h()));
                }
                default -> {
                }
            }
            if (hasChanged) {
                if (b)
                    invokeEvent(this, new DLGuiStandardEvents.ResizeBeginEvent(mouseX, mouseY), true);
                else {
                    newBounds = Rectangle.withPoints(newX1, newY1, newX2, newY2);
                    MutableBoolean cancel = new MutableBoolean(false);
                    invokeEvent(this, new DLGuiStandardEvents.ResizeEndEvent(mouseX, mouseY, newX1, newY1,
                            newX2 - newX1, newY2 - newY1, cancel), true);
                    if (cancel.isFalse()) {
                        setPosition(newX1, newY1);
                        setSize(newX2 - newX1, newY2 - newY1);
                    }
                }
            } else if (b) {
                newBounds = Rectangle.withPoints(newX1, newY1, newX2, newY2);
                invokeEvent(this,
                        new DLGuiStandardEvents.ResizeEvent(mouseX, mouseY, newX1, newY1, newX2 - newX1, newY2 - newY1),
                        true);
            }
            return true;
        } else if (dragThresholdTriggered && isInMoveArea()) {
            if (hasChanged) {
                if (b)
                    invokeEvent(this,
                            new DLGuiStandardEvents.DragComponentBeginEvent(mouseX, mouseY, button, dragOverComponents),
                            true);
                else {
                    newBounds = Rectangle.withSize((int) (mouseX - dragOffsetX), (int) (mouseY - dragOffsetY), width(),
                            height());
                    MutableBoolean cancel = new MutableBoolean(false);
                    invokeEvent(this, new DLGuiStandardEvents.DragComponentEndEvent(mouseX, mouseY, button,
                            (int) (mouseX - dragOffsetX), (int) (mouseY - dragOffsetY), dragOverComponents, cancel),
                            true);
                    if (cancel.isFalse()) {
                        setPosition((mouseX * getGlobalScale() - dragOffsetX), (mouseY * getGlobalScale() - dragOffsetY));
                    }
                }
            } else if (b) {
                newBounds = Rectangle.withSize((int) (mouseX - dragOffsetX), (int) (mouseY - dragOffsetY), width(),
                        height());
                invokeEvent(this, new DLGuiStandardEvents.DragComponentEvent(mouseX, mouseY, button,
                        (int) (mouseX - dragOffsetX), (int) (mouseY - dragOffsetY), dragX, dragY, dragOverComponents),
                        true);
            }
            for (DLGuiComponent c : dragOverComponents) {
                if (b) {
                } else {
                    c.invokeEvent(c, new DLGuiStandardEvents.DropComponentEvent(this, mouseX, mouseY), true);
                }
            }
            return true;
        } else {
            if (hasChanged) {
                if (b)
                    invokeEvent(this, new DLGuiStandardEvents.DragBeginEvent(mouseX, mouseY, button), true);
                else
                    invokeEvent(this, new DLGuiStandardEvents.DragEndEvent(mouseX, mouseY, button, mouseOriginX,
                            mouseOriginY, this.mouseDownX, this.mouseDownY), true);
            } else if (b)
                invokeEvent(this, new DLGuiStandardEvents.DragEvent(mouseX, mouseY, button, mouseOriginX, mouseOriginY,
                        this.mouseDownX, this.mouseDownY, dragX, dragY), true);
        }
        return false;
    }

    private boolean isComponentDraggedOver = false;

    /**
     * 
     * @param b
     * @param other
     * @param mouseX
     * @param mouseY
     * @param button
     * @return
     */
    public boolean setDragComponentOver(boolean b, List<DLGuiComponent> other, double mouseX, double mouseY,
            int button) {
        boolean hasChanged = isComponentDraggedOver != b;
        if (hasChanged) {
            if (b) {
                invokeEvent(this, new DLGuiStandardEvents.DragComponentOverBeginEvent(other, mouseX, mouseY, button),
                        true);
            } else {
                invokeEvent(this, new DLGuiStandardEvents.DragComponentOverEndEvent(other, mouseX, mouseY, button),
                        true);
            }
            this.isComponentDraggedOver = b;
        } else if (b) {
            invokeEvent(this, new DLGuiStandardEvents.DragComponentOverEvent(other, mouseX, mouseY, button), true);
        }
        return hasChanged;
    }

    /**
     * Set the resize area (which border/corner is hovered) based on mouse position.
     *
     * @param align new resize area
     */
    public void setResizeArea(Align align) {
        boolean b = resizeArea != align;
        this.resizeArea = align;
        if (b) {
            CursorType.set(getCursor());
        }
    }

    /**
     * Update the move area status (for dragging) based on mouse position.
     *
     * @param inside true if mouse is inside the move area
     */
    public void setInMoveArea(boolean inside) {
        boolean b = mouseInMoveArea != inside;
        this.mouseInMoveArea = inside;
        if (b) {
            CursorType.set(getCursor());
        }
    }

    /**
     * Get the current resize area enum value.
     *
     * @return current resize area
     */
    public Align getResizeArea() {
        return resizeArea;
    }

    /**
     * Check if the mouse is currently in the move area of this component.
     *
     * @return true if in move area
     */
    public boolean isInMoveArea() {
        return mouseInMoveArea;
    }

    /**
     * Convenience method to dispatch mouse click events and multi-click logic.
     *
     * @param mouseX mouse X in local coords
     * @param mouseY mouse Y in local coords
     * @param button mouse button
     */
    public void mouseClickDispatcher(double mouseX, double mouseY, int button) {
        if (dragging && ((getResizeArea() != Align.CENTER) || (isInMoveArea()))) {
            return;
        }

        if (multiClickLastMs < System.currentTimeMillis() - MULTI_CLICK_SPEED_MS) {
            multiClickCount = 0;
        }

        if (multiClickCount == 0) {
            invokeEvent(this, new DLGuiStandardEvents.MousePressedEvent(mouseX, mouseY, button), false);
            switch (button) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT ->
                    invokeEvent(this, new DLGuiStandardEvents.ClickEvent(mouseX, mouseY), false);
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT ->
                    invokeEvent(this, new DLGuiStandardEvents.RightClickEvent(mouseX, mouseY), false);
            }
        } else {
            invokeEvent(this,
                    new DLGuiStandardEvents.MultiClickEvent(mouseX, mouseY, button, (byte) (multiClickCount + 1)),
                    false);
        }

        multiClickCount++;
        multiClickCount %= Math.min(multiClickable.get(), Byte.MAX_VALUE - 1);
        multiClickLastMs = System.currentTimeMillis();
    }

    /**
     * A compact struct describing iteration flags used by hit-testing.
     *
     * @param eventConsumed whether an event has been consumed by ancestors
     * @param focusFound whether a focused component has already been found
     * @param ignored whether this subtree should be ignored
     * @param enabled whether event dispatch should consider this enabled
     * @param ignoredComponents set of components to ignore in hit-testing
     * @param nonConsumable set of components whose events are non-consumable
     */
    public record Flags(boolean eventConsumed, boolean focusFound, boolean ignored, boolean enabled, ImmutableSet<DLGuiComponent> ignoredComponents, ImmutableSet<DLGuiComponent> nonConsumable) {
        public static final Flags EMPTY = new Flags(false, false, false, true, ImmutableSet.of(), ImmutableSet.of());
    }

    /**
     * Perform hierarchical hit-testing and produce a HitResult containing components under the cursor.
     *
     * @param mouseX absolute mouse X in screen coords
     * @param mouseY absolute mouse Y in screen coords
     * @param xOffset the x offset applied to this subtree when rendering/hit-testing
     * @param yOffset the y offset applied to this subtree when rendering/hit-testing
     * @param bounds clipping bounds for this subtree
     * @param flags iteration flags controlling consumption/ignoring behavior
     * @param type the type of input being tested (click/drag/scroll)
     * @param parentScale cumulative parent scale factor
     * @return a HitResult describing components and selection states under the cursor
     */
    public HitResult iterateComponents(double mouseX, double mouseY, double xOffset, double yOffset, Rectangle bounds, Flags flags, ConsumptionType type, double parentScale) {
        HitResult result = new HitResult();
        if (flags.ignoredComponents().contains(this))
            return result;

        double currentScale = this.scale.get() * parentScale;
        boolean ignored = flags.ignored() || !this.visible.get();
        boolean focusFound = flags.focusFound();
        boolean eventConsumed = flags.eventConsumed();
        boolean enabled = flags.enabled() && this.enabled.get();

        // Lokale Mauskoordinaten (vor Skalierung)
        double localMouseX = (mouseX - xOffset) / parentScale;
        double localMouseY = (mouseY - yOffset) / parentScale;

        // Scrolloffset muss ebenfalls skaliert werden!
        double scrollOffsetX = getScrollOffsetX() * currentScale;
        double scrollOffsetY = getScrollOffsetY() * currentScale;

        // Die Mausposition muss den Scroll berücksichtigen:
        double scrolledMouseX = mouseX + scrollOffsetX;
        double scrolledMouseY = mouseY + scrollOffsetY;

        double scaledLocalMouseX = localMouseX / this.scale.get();
        double scaledLocalMouseY = localMouseY / this.scale.get();

        Rectangle newBounds = Rectangle.intersection(bounds, Rectangle.offset(getChildInteractionBounds(), xOffset, yOffset));
        Rectangle childBounds = Rectangle.offset(newBounds, scrollOffsetX, scrollOffsetY);

        List<DLGuiComponent> childs = getComponents();
        for (ListIterator<DLGuiComponent> children = childs.listIterator(childs.size()); children.hasPrevious();) {
            DLGuiComponent component = children.previous();
            if (flags.ignoredComponents().contains(component))
                continue;

            HitResult hit = component.iterateComponents(
                scrolledMouseX, scrolledMouseY,
                xOffset + component.x() * currentScale,
                yOffset + component.y() * currentScale,
                childBounds,
                new Flags(eventConsumed, focusFound, ignored, enabled, flags.ignoredComponents(), flags.nonConsumable()),
                type,
                currentScale
            );
            result.addAll(hit.components());
            if (childBounds.collision(scrolledMouseX, scrolledMouseY)) {
                eventConsumed |= hit.consumed();
                focusFound |= result.isPresent();
            }
        }

        boolean valid = isMouseOver(scaledLocalMouseX, scaledLocalMouseY)
            && newBounds.collision(mouseX, mouseY)
            && !ignored && !eventConsumed;

        if (valid) {
            result.add(
                enabled
                    ? (focusFound ? ComponentSelectionState.HIT : ComponentSelectionState.FOCUSED)
                    : ComponentSelectionState.UNSELECTED,
                new ComponentHitContext(this, scaledLocalMouseX, scaledLocalMouseY, (int) xOffset, (int) yOffset, enabled)
            );
            result.consume(!flags.nonConsumable().contains(this) && inputConsumptionPolicy.get().test(type));
        } else {
            result.add(ComponentSelectionState.UNSELECTED,
                new ComponentHitContext(this, scaledLocalMouseX, scaledLocalMouseY, (int) xOffset, (int) yOffset, enabled));
            result.consume(!flags.nonConsumable().contains(this) && eventConsumed);
        }

        return result;
    }


    /**
     * Internal rendering entry used by the window manager to render this component and children.
     *
     * @param graphics rendering helper
     * @param mouseX mouse X for rendering context
     * @param mouseY mouse Y for rendering context
     * @param layer the render layer to render
     * @param xOffset X offset of this subtree in screen space
     * @param yOffset Y offset of this subtree in screen space
     * @param scrollOffsetX effective scroll offset X
     * @param scrollOffsetY effective scroll offset Y
     * @param scissorBounds scissor/clipping bounds
     * @param globalScale cumulative scale at this level
     */
    public final void renderEvent(DLGuiGraphics graphics, double mouseX, double mouseY, RenderLayer layer, double xOffset, double yOffset, double scrollOffsetX, double scrollOffsetY, Rectangle scissorBounds, double globalScale) {
        double currentScale = this.scale.get();
        globalScale *= currentScale;
        graphics.poseStack().scale((float) currentScale, (float) currentScale, (float) currentScale);
        graphics.poseStack().pushPose();
        
        invokeEvent(this, new DLGuiStandardEvents.RenderPreEvent(graphics, mouseX, mouseY, layer, scissorBounds), true);

        final boolean useScissor = !layer.isSpecial() && layer != RenderLayer.FRONT && layer != RenderLayer.SCREEN_SPACE;
        final int maxWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        final int maxHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        if (useScissor) {
            int x1 = (int) Math.floor(Math.max(scissorBounds.x(), -maxWidth));
            int y1 = (int) Math.floor(Math.max(scissorBounds.y(), -maxHeight));
            int w = (int) Math.ceil(scissorBounds.width());
            int h = (int) Math.ceil(scissorBounds.height());
            GuiUtils.enableScissor(graphics, x1, y1, w, h);
        }

        mouseX += getScrollOffsetX();
        mouseY += getScrollOffsetY();
        invokeEvent(this, new DLGuiStandardEvents.RenderEvent(graphics, mouseX, mouseY, layer, scissorBounds), true);
        if (layer.isSpecial()) {
            if (useScissor)
                GuiUtils.disableScissor(graphics);
            graphics.poseStack().popPose();
            return;
        }

        Rectangle childRenderBounds = getChildRenderBounds();
        Rectangle childClipBounds = Rectangle.intersection(scissorBounds, Rectangle.offset(childRenderBounds, xOffset, yOffset));

        if (doLayout) {
            for (DLGuiComponent child : getComponents()) {
                if (!child.visible.get())
                    continue;

                double localChildX = child.x() - getScrollOffsetX();
                double localChildY = child.y() - getScrollOffsetY();
                Rectangle renderBounds = child.getRenderBounds();
                double childScreenX = xOffset + (localChildX + renderBounds.x()) * globalScale;
                double childScreenY = yOffset + (localChildY + renderBounds.y()) * globalScale;
                double childScreenW = renderBounds.width() * globalScale;
                double childScreenH = renderBounds.height() * globalScale;

                if (childScreenX + childScreenW <= childClipBounds.x() || childScreenY + childScreenH <= childClipBounds.y()
                        || childScreenX >= childClipBounds.x() + childClipBounds.width()
                        || childScreenY >= childClipBounds.y() + childClipBounds.height())
                    continue;

                Rectangle intersection = Rectangle.intersection(childClipBounds, Rectangle.withSize((renderBounds.x() + localChildX) * globalScale + xOffset, (renderBounds.y() + localChildY) * globalScale + yOffset, renderBounds.width() * globalScale, renderBounds.height() * globalScale));
                if (intersection.width() <= 0 || intersection.height() <= 0)
                    continue;

                graphics.poseStack().pushPose();
                graphics.poseStack().translate(localChildX, localChildY, 0);
                child.renderEvent(graphics, mouseX - localChildX * globalScale, mouseY - localChildY * globalScale, layer,
                        xOffset + (localChildX * globalScale), yOffset + (localChildY * globalScale),
                        scrollOffsetX + getScrollOffsetX(), scrollOffsetY + getScrollOffsetY(), intersection, globalScale);
                graphics.poseStack().popPose();
            }
        }

        if (useScissor)
            GuiUtils.disableScissor(graphics);
            
        invokeEvent(this, new DLGuiStandardEvents.RenderPostEvent(graphics, mouseX, mouseY, layer, scissorBounds), true);

        graphics.poseStack().popPose();
    }
    
    /**
     * Render on-screen overlays (e.g. tooltips) for this component and its children.
     *
     * @param graphics rendering helper
     * @param mouseX mouse X in screen coords
     * @param mouseY mouse Y in screen coords
     */
    public final void renderOnScreenEvent(DLGuiGraphics graphics, double mouseX, double mouseY) {        
        invokeEvent(this, new DLGuiStandardEvents.RenderOnScreenEvent(graphics, mouseX, mouseY), true);
        for (DLGuiComponent child : getComponents()) {
            if (!child.visible.get())
                continue;
            graphics.poseStack().pushPose();
            child.renderOnScreenEvent(graphics, mouseX, mouseY);
            graphics.poseStack().popPose();
        }
    }

    /**
     * Trigger a screen layout update cycle on this component and descendants.
     *
     * @param screenWidth current screen width
     * @param screenHeight current screen height
     */
    public final void updateLayoutEvent(int screenWidth, int screenHeight) {
        invokeEvent(this, new DLGuiStandardEvents.ScreenLayoutUpdatedEvent(Phase.PRE), true);
        invalidateGlobalCoordinates(true, true);
        updateScreenLayout();
        invokeEvent(this, new DLGuiStandardEvents.ScreenLayoutUpdatedEvent(Phase.POST), true);
        for (DLGuiComponent child : getComponents()) {
            child.updateLayoutEvent(screenWidth, screenHeight);
        }
    }

    /**
     * Per-tick update handler. Handles hold-detection and forwards tick to children.
     */
    public void tick() {
        if (isMouseDown()) {
            if (ticksHoldingDown <= 0 || ticksHoldingDown > MOUSE_DOWN_INITIAL_DELAY) {
                invokeEvent(this, new DLGuiStandardEvents.MouseHoldDownEvent(mouseDownX, mouseDownY, mouseDownButton,
                        ticksHoldingDown), true);
            }
            ticksHoldingDown++;
        }

        getComponents().forEach(DLGuiComponent::tick);
        invokeEvent(this, new DLGuiStandardEvents.TickEvent(), true);
    }

    /**
     * Render back/background layer for this component. Subclasses may override.
     *
     * @param graphics graphics helper
     * @param mouseX local mouse X
     * @param mouseY local mouse Y
     * @param renderBounds current render bounds
     */
    public void renderBackLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
    }

    /**
     * Render main/content layer for this component. Subclasses should override to paint content.
     *
     * @param graphics graphics helper
     * @param mouseX local mouse X
     * @param mouseY local mouse Y
     * @param renderBounds current render bounds
     */
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
    }

    /**
     * Render front/overlay layer for this component. Subclasses may override.
     *
     * @param graphics graphics helper
     * @param mouseX local mouse X
     * @param mouseY local mouse Y
     * @param renderBounds current render bounds
     */
    public void renderFrontLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
    }

    /**
     * Render additional on-screen decorations for this component.
     *
     * @param graphics graphics helper
     * @param mouseX local mouse X
     * @param mouseY local mouse Y
     */
    public void renderOnScreen(DLGuiGraphics graphics, double mouseX, double mouseY) {
    }

    /**
     * Render a special overlay (for example during resize/drag operations).
     *
     * @param graphics graphics helper
     * @param mouseX local mouse X
     * @param mouseY local mouse Y
     * @param renderBounds current render bounds
     */
    public void renderSpecialOverlay(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        renderBoundingBox(graphics, (int)((newBounds.x() - x())), (int)((newBounds.y() - y())), (int) newBounds.width(), (int) newBounds.height());
    }

    /**
     * Helper to draw a rectangular bounding box for debugging.
     *
     * @param graphics rendering helper
     * @param x left coordinate
     * @param y top coordinate
     * @param w width
     * @param h height
     */
    public static void renderBoundingBox(DLGuiGraphics graphics, int x, int y, int w, int h) {
        graphics.graphics().fill(RenderType.guiTextHighlight(), x + 0, y + 0, x + w, y + 1, 0xFF0000FF);
        graphics.graphics().fill(RenderType.guiTextHighlight(), x + 0, y + h - 1, x + w, y + h, 0xFF0000FF);
        graphics.graphics().fill(RenderType.guiTextHighlight(), x + 0, y + 1, x + 1, y + h - 1, 0xFF0000FF);
        graphics.graphics().fill(RenderType.guiTextHighlight(), x + w - 1, y + 1, x + w, y + h - 1, 0xFF0000FF);
    }
}