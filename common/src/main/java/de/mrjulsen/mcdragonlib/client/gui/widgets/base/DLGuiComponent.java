package de.mrjulsen.mcdragonlib.client.gui.widgets.base;

import de.mrjulsen.mcdragonlib.DragonLib;
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
import de.mrjulsen.mcdragonlib.util.properties.IProperty;
import de.mrjulsen.mcdragonlib.util.properties.InheritableProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * DLGuiComponent is the base for every DragonLib GUI component.
 * <p>
 * This component offers basic functionallity shared among all DragonLib GUI
 * components and handles basic
 * user interactions, rendering, layout and component management. It also
 * supports and implements all
 * standard GUI events from {@link DLGuiStandardEvents}.
 * </p>
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

    public static final int getResizeBorderSize() {
        return (int) (10.0D / Minecraft.getInstance().getWindow().getGuiScale());
    }

    public static final int getResizeCornerSize() {
        return getResizeBorderSize() * 8;
    }

    public static final int MOUSE_DRAG_THRESHOLD = 5;
    public static final byte DOUBLE_CLICK_COUNT = 2;
    public static final int MULTI_CLICK_SPEED_MS = 500;
    public static final int MOUSE_DOWN_INITIAL_DELAY = 10;

    // Container
    private final ConcurrentLinkedDeque<DLGuiComponent> components = new ConcurrentLinkedDeque<>();

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


    public enum ConsumptionType {
        CLICK,
        MOUSE_MOVE,
        DRAG,
        SCROLL,
        DRAG_AND_DROP;
    }

    @InheritableProperty(overrideLocal = true)
    public final BooleanProperty enabled = new BooleanProperty(true, true)
            .withAfterPropertyChangedCallback(this::onEnabledChanged);
    @InheritableProperty(overrideLocal = true)
    public final BooleanProperty visible = new BooleanProperty(true, true)
            .withAfterPropertyChangedCallback(this::onVisibilityChanged);
    public final BooleanProperty resizable = new BooleanProperty(false, true)
            .withAfterPropertyChangedCallback((o, v) -> invokeEvent(this, new DLGuiStandardEvents.ResizableChangedEvent(v), true));
    public final BooleanProperty movable = new BooleanProperty(false, true)
            .withAfterPropertyChangedCallback((o, v) -> invokeEvent(this, new DLGuiStandardEvents.MovableChangedEvent(v), true));
    public final NumberProperty<Byte> multiClickable = new NumberProperty<>((byte) 1, (byte) 1, Byte.MAX_VALUE);
    public final Property<CursorType> cursor = new Property<>(null);
    public final Property<Predicate<ConsumptionType>> inputConsumptionPolicy = new Property<>(
            (context) -> context != ConsumptionType.SCROLL);
    public final BitflagProperty<EAlign> anchor = new BitflagProperty<>(EAlign.class, EAlign.LEFT, EAlign.TOP);
    public final Property<Size> minSize = new Property<Size>(Size.of(5, 5));
    public final Property<Size> maxSize = new Property<>(Size.INFINITY);
    public final NumberProperty<Double> scale = new NumberProperty<>(1D, 0.01D, 10D);
    public final Property<DLTooltip> tooltip = new Property<DLTooltip>(DLTooltip.EMPTY);
    public final Property<ILayoutManager> layout = new Property<ILayoutManager>(NoLayout.INSTANCE)
        .withAfterPropertyChangedCallback((o, v) -> v.arrangeComponents(this));

    public final Property<Object> layoutContraint = new Property<>(null);

    public final Property<Object> customData = new Property<>(null);

    
    protected final Cache<Double> globalX = new Cache<>(() -> getParent().map(p -> p.getXOnScreen()).orElse(0D) + (dX() * getParent().map(p -> p.scale.get()).orElse(1D)));
    protected final Cache<Double> globalY = new Cache<>(() -> getParent().map(p -> p.getYOnScreen()).orElse(0D) + (dY() * getParent().map(p -> p.scale.get()).orElse(1D)));

    private boolean layoutLoopFix = false;
    private boolean applyingLayout = false;

    public DLGuiComponent(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;

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
    }

    protected final void applyLayout() {
        if (applyingLayout) return;
        applyingLayout = true;
        LayoutResult result = this.layout.get().arrangeComponents(this);
        invokeEvent(this, new DLGuiStandardEvents.ComponentLayoutUpdatedEvent(result));
        applyingLayout = false;
    }

    protected void updateScreenLayout() {
    }

    @Override
    public void close() throws Exception {
        invokeEvent(this, new DLGuiStandardEvents.CloseEvent(), true);
        for (DLGuiComponent child : getComponents()) {
            child.close();
        }
    }

    public boolean isDragged() {
        return dragging;
    }

    public boolean isSelected() {
        return mouseSelected;
    }

    public boolean isFocused() {
        return focused;
    }

    public boolean isMouseDown() {
        return mouseDown;
    }

    public boolean isComponentDraggedOver() {
        return isComponentDraggedOver;
    }

    public int height() {
        return (int) this.height;
    }

    public int width() {
        return (int) this.width;
    }

    public int x() {
        return (int) this.x;
    }

    public int y() {
        return (int) this.y;
    }

    public double dHeight() {
        return this.height;
    }

    public double dWidth() {
        return this.width;
    }

    public double dX() {
        return this.x;
    }

    public double dY() {
        return this.y;
    }

    public double getXOnScreen() {
        return globalX.get();
    }

    public double getYOnScreen() {
        return globalY.get();
    }

    public Point toScreenCoordinates() {
        Point local = Point.of(
            dX() - getScrollOffsetX(),
            dY() - getScrollOffsetY()
        );

        return getParent()
            .map(parent -> parent.toScreenCoordinates().add(local))
            .orElse(local);
    }


    public double getGlobalScale() {
        return getParent().map(x -> x.getGlobalScale()).orElse(1D) * scale.get();
    }

    protected void invalidateGlobalCoordinates(boolean x, boolean y) {
        if (x)
            globalX.clear();
        if (y)
            globalY.clear();
        forEachComponentMatching(t -> true, c -> c.invalidateGlobalCoordinates(x, y));
    }

    public void setX(double x) {
        double oldX = x;
        this.x = x;
        invalidateGlobalCoordinates(true, false);
        invokeEvent(this, new DLGuiStandardEvents.ComponentPosAndSizeChanged((int)oldX, (int)x, (int)y, (int)y, (int)width, (int)width, (int)height, (int)height));
    }

    public void setY(double y) {
        double oldY = x;
        this.y = y;
        invalidateGlobalCoordinates(false, true);
        invokeEvent(this, new DLGuiStandardEvents.ComponentPosAndSizeChanged((int)x, (int)x, (int)oldY, (int)y, (int)width, (int)width, (int)height, (int)height));
    }

    public void setLeft(double x) {
        double diff = dX() - x;
        setX(x);
        setWidth(dWidth() + diff);
    }

    public void setTop(double y) {
        double diff = y - dY();
        setY(y);
        setHeight(dHeight() + diff);
    }

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

    public void setPosition(double x, double y) {
        setX(x);
        setY(y);
    }

    public void setSize(double width, double height) {
        setWidth(width);
        setHeight(height);
    }

    public boolean bringToFront(DLGuiComponent component) {
        if (components.contains(component)) {
            components.remove(component);
            components.addLast(component);
            return true;
        }
        return false;
    }    

    public boolean sendToBack(DLGuiComponent component) {
        if (components.contains(component)) {
            components.remove(component);
            components.addFirst(component);
            return true;
        }
        return false;
    }

    public void setScrollOffsetX(double scrollOffsetX) {
        this.scrollOffsetX = MathUtils.clamp(scrollOffsetX, 0, Integer.MAX_VALUE);
    }

    public void setScrollOffsetY(double scrollOffsetY) {
        this.scrollOffsetY = MathUtils.clamp(scrollOffsetY, 0, Integer.MAX_VALUE);
    }

    public double getScrollOffsetX() {
        return scrollOffsetX;
    }

    public double getScrollOffsetY() {
        return scrollOffsetY;
    }

    /**
     * The bounds in which all child components can be interacted with. Cannot be
     * larger than {@link #getCollisionBox}.
     * 
     * @return The rectangle that represents the boundaries
     */
    public Rectangle getChildInteractionBounds() {
        return Rectangle.withSize(0, 0, Math.max(width(), 0), Math.max(height(), 0));
    }

    /**
     * The bounds in which this and all child components can be interacted with.
     * 
     * @return The rectangle that represents the boundaries
     */
    public Rectangle getInteractionBounds() {
        return Rectangle.withSize(0, 0, Math.max(width(), 0), Math.max(height(), 0));
    }

    /**
     * The bounds in which this component and all child components are rendered.
     * Anything that exceeds these bounds will be cut off.
     * 
     * @return The rectangle that represents the boundaries
     */
    public Rectangle getRenderBounds() {
        return Rectangle.withSize(0, 0, Math.max(width(), 0), Math.max(height(), 0));
    }

    /**
     * The bounds within which the child components are rendered. Cannot be larger
     * than {@link #getRenderBounds}. Anything that exceeds these bounds will be cut
     * off.
     * 
     * @return The rectangle that represents the boundaries
     */
    public Rectangle getChildRenderBounds() {
        return Rectangle.withSize(0, 0, Math.max(width(), 0), Math.max(height(), 0));
    }

    public Rectangle getPositionBox() {
        return Rectangle.withSize(x(), y(), Math.max(width(), 0), Math.max(height(), 0));
    }

    /**
     * The bounds that enclose this and all child components. In other words, a
     * collision box large enough to enclose this and all child components at their
     * respective positions.
     * 
     * @return The rectangle that represents the boundaries
     */
    public Rectangle getSurroundingCollisionBox() {
        return Rectangle.surroundingBase(getInteractionBounds(),
                getComponents().stream().map(DLGuiComponent::getSurroundingCollisionBox).toArray(Rectangle[]::new));
    }

    public double getLocalMouseX() {
        if (getWindowManager() == null)
            return 0;
        return getWindowManager().mouseXOnScreen() - getXOnScreen();
    }

    public double getLocalMouseY() {
        if (getWindowManager() == null)
            return 0;
        return getWindowManager().mouseYOnScreen() - getYOnScreen();
    }

    private void onEnabledChanged(boolean oldState, boolean newState) {
        invokeEvent(this, new DLGuiStandardEvents.EnabledChangedEvent(newState), false);
        update(this);
    }

    private void onVisibilityChanged(boolean oldState, boolean newState) {
        invokeEvent(this, new DLGuiStandardEvents.VisibilityChangedEvent(newState), false);
        update(this);
    }

    /**
     * Aktuelisiert sämtliche Zustände vom diesem und allen child komponenten
     * basierend auf der Referenz-komponente.
     * 
     * @param reference
     */
    protected void update(DLGuiComponent reference) {
        if (reference != this) {
            applyInheritanceFrom(reference);
        }
        for (DLGuiComponent child : getComponents()) {
            child.update(reference);
        }
    }

    protected void applyInheritanceFrom(DLGuiComponent ref) {
        for (Field field : this.getClass().getFields()) {
            if (!IProperty.class.isAssignableFrom(field.getType()))
                continue;
            InheritableProperty annotation = field.getAnnotation(InheritableProperty.class);
            if (annotation == null)
                continue;
            field.setAccessible(true);

            try {
                Object thisValue = field.get(this);
                if (!(thisValue instanceof IProperty<?> thisProp))
                    continue;

                Object refValue = field.get(ref);
                if (!(refValue instanceof IProperty<?> refProp))
                    continue;

                if (!thisProp.getClass().equals(refProp.getClass())) {
                    DragonLib.LOGGER.warn("Skipping inheritance for field {}: Incompatible types ({} is not {})",
                            field.getName(), thisProp.getClass(), refProp.getClass());
                    continue;
                }

                thisProp.inheritFrom(refProp, annotation.overrideLocal());
            } catch (Exception e) {
                DragonLib.LOGGER.debug("Skipping inheritance for field " + field.getName() + ": " + e.getMessage());
            }
        }
    }

    public <T extends DLGuiComponent> T addComponent(T component) {
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
        invokeEvent(this, new DLGuiStandardEvents.ComponentAddedEvent(component), true);
        update(this);
        return component;
    }

    public <T extends DLGuiComponent> boolean removeComponent(T component) {
        Objects.requireNonNull(component);
        boolean b = this.components.remove(component);
        if (b) {
            component.setParent(null);
            component.setWindowManager(null);
            component.invalidateGlobalCoordinates(true, true);
            invokeEvent(this, new DLGuiStandardEvents.ComponentRemovedEvent(component), true);
            update(component);
        }
        return b;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return getInteractionBounds().collision(mouseX, mouseY);
    }

    public CursorType getCursor() {
        return resizeArea == Align.CENTER ? (mouseInMoveArea ? CursorType.ALLRESIZE : cursor.get())
                : resizeArea.getCursor();
    }

    public int componentsCount() {
        return this.components.size();
    }

    public boolean hasComponents() {
        return !this.components.isEmpty();
    }

    public Optional<DLGuiComponent> getParent() {
        return Optional.ofNullable(this.parent);
    }

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

    public void forEachComponentMatching(Predicate<DLGuiComponent> test, Consumer<DLGuiComponent> action) {
        for (DLGuiComponent component : getComponents()) {
            if (test.test(component))
                action.accept(component);
        }
    }

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
     * Returns the {@code DLWindowManager} to which this component is assigned. Can
     * be {@code null} if the component has not yet been initialized or has no
     * parent.
     * 
     * @return the current {@code DLWindowManager} instance
     */
    public DLWindowManager getWindowManager() {
        return windowManager;
    }

    public void setWindowManager(DLWindowManager windowManager) {
        DLWindowManager oldManager = this.windowManager;
        this.windowManager = windowManager;
        invokeEvent(this, new DLGuiStandardEvents.WindowManagerChangeEvent(oldManager, windowManager), true);
        for (DLGuiComponent child : getComponents()) {
            child.setWindowManager(windowManager);
        }
    }

    <T extends DLGuiComponent> void setParent(T parent) {
        Optional<DLGuiComponent> oldParent = getParent();
        this.parent = parent;
        invokeEvent(this, new DLGuiStandardEvents.ParentChangedEvent(oldParent, Optional.ofNullable(parent)), true);
    }

    public List<DLGuiComponent> getComponents() {
        return ImmutableList.copyOf(components);
    }

    public List<DLGuiComponent> getComponentsMatching(Predicate<DLGuiComponent> predicate) {
        return components.stream().filter(predicate::test).toList();
    }

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

    public void clearComponents() {
        clearComponents(c -> true);
    }

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
        invokeEvent(this, new DLGuiStandardEvents.ComponentsClearEvent(Phase.POST, bool), true);
    }

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

    public void updateMoveArea(double mouseX, double mouseY) {
        if (!this.movable.get() || getResizeArea() != Align.CENTER) {
            setInMoveArea(false);
            return;
        }
        int dX = (int) mouseX, dY = (int) mouseY;
        setInMoveArea(dX <= getResizeBorderSize() || dX >= width() - getResizeBorderSize() || dY <= getResizeBorderSize() || dY >= height() - getResizeBorderSize());
    }

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

    public boolean setFocus(boolean b) {
        boolean hasChanged = focused != b;
        this.focused = b;
        if (hasChanged) {
            invokeEvent(this, new DLGuiStandardEvents.FocusChangedEvent(b), true);
        }
        return hasChanged;
    }

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

    public void setResizeArea(Align align) {
        boolean b = resizeArea != align;
        this.resizeArea = align;
        if (b) {
            CursorType.set(getCursor());
        }
    }

    public void setInMoveArea(boolean inside) {
        boolean b = mouseInMoveArea != inside;
        this.mouseInMoveArea = inside;
        if (b) {
            CursorType.set(getCursor());
        }
    }

    public Align getResizeArea() {
        return resizeArea;
    }

    public boolean isInMoveArea() {
        return mouseInMoveArea;
    }

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
     * @param eventConsumed
     * @param focusFound
     * @param ignored
     * @param enabled
     * @param ignoredComponents
     * @param nonConsumable
     */
    public record Flags(boolean eventConsumed, boolean focusFound, boolean ignored, boolean enabled, ImmutableSet<DLGuiComponent> ignoredComponents, ImmutableSet<DLGuiComponent> nonConsumable) {
        public static final Flags EMPTY = new Flags(false, false, false, true, ImmutableSet.of(), ImmutableSet.of());
    }

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

        for (ListIterator<DLGuiComponent> children = getComponents().listIterator(componentsCount()); children.hasPrevious();) {
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

        if (useScissor)
            GuiUtils.disableScissor(graphics);
            
        invokeEvent(this, new DLGuiStandardEvents.RenderPostEvent(graphics, mouseX, mouseY, layer, scissorBounds), true);

        graphics.poseStack().popPose();
    }
    
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


    public final void updateLayoutEvent(int screenWidth, int screenHeight) {
        invokeEvent(this, new DLGuiStandardEvents.ScreenLayoutUpdatedEvent(Phase.PRE), true);
        invalidateGlobalCoordinates(true, true);
        updateScreenLayout();
        invokeEvent(this, new DLGuiStandardEvents.ScreenLayoutUpdatedEvent(Phase.POST), true);
        for (DLGuiComponent child : getComponents()) {
            child.updateLayoutEvent(screenWidth, screenHeight);
        }
    }

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

    public void renderBackLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
    }

    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
    }

    public void renderFrontLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
    }

    public void renderOnScreen(DLGuiGraphics graphics, double mouseX, double mouseY) {
    }

    public void renderSpecialOverlay(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        renderBoundingBox(graphics, (int)((newBounds.x() - x())), (int)((newBounds.y() - y())), (int) newBounds.width(), (int) newBounds.height());
    }

    public static void renderBoundingBox(DLGuiGraphics graphics, int x, int y, int w, int h) {
        graphics.graphics().fill(RenderType.guiTextHighlight(), x + 0, y + 0, x + w, y + 1, 0xFF0000FF);
        graphics.graphics().fill(RenderType.guiTextHighlight(), x + 0, y + h - 1, x + w, y + h, 0xFF0000FF);
        graphics.graphics().fill(RenderType.guiTextHighlight(), x + 0, y + 1, x + 1, y + h - 1, 0xFF0000FF);
        graphics.graphics().fill(RenderType.guiTextHighlight(), x + w - 1, y + 1, x + w, y + h - 1, 0xFF0000FF);
    }
}