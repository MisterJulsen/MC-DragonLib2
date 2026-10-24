package de.mrjulsen.mcdragonlib.gui.v2;

import java.util.*;
import java.util.function.Predicate;

import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.gui.v2.behaviour.Behaviour;
import de.mrjulsen.mcdragonlib.gui.v2.behaviour.CursorSource;
import de.mrjulsen.mcdragonlib.gui.v2.event.EventListenerRegistry;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.gui.v2.event.IGuiEventDispatcher;
import de.mrjulsen.mcdragonlib.gui.v2.input.InputKind;
import de.mrjulsen.mcdragonlib.gui.v2.layout.Layout;
import de.mrjulsen.mcdragonlib.gui.v2.layout.NoLayout;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.gui.v2.theme.Theme;
import de.mrjulsen.mcdragonlib.gui.v2.window.WindowManager;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.mcdragonlib.util.math.Point;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.math.Size;
import de.mrjulsen.mcdragonlib.util.properties.BitflagProperty;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.NumberProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

/**
 * Base class of every v2 GUI element. A component is a node in a tree: it owns a {@link Transform},
 * a list of children, a {@link Layout} that arranges them, a set of event listeners and optional
 * {@link Behaviour}s.
 *
 * <p><b>Layout happens in two passes.</b> {@link #measure(double, double)} asks bottom up how big
 * everything wants to be, {@link #applyLayout()} then arranges top down inside the space that is
 * actually there. Both are cached; anything that changes a size has to call
 * {@link #invalidateLayout()}, which the built in properties already do.</p>
 *
 * <p><b>Sizes come in two flavours.</b> {@link #width()} and friends return the value snapped to
 * whole pixels, which is what ends up on screen; the d-prefixed ones like {@link #dWidth()} return
 * the raw double. Use the int ones for rendering, the double ones for math.</p>
 *
 * <p><b>Subclasses</b> typically override {@link #render(DLGuiGraphics, double, double)} and, if they
 * draw content of their own rather than children, {@link #measureContent(double, double)}.</p>
 */
public abstract class GuiComponent implements IGuiEventDispatcher<GuiComponent>, GuiEvents {

    /**
     * Ticks a mouse button has to be held before {@link GuiEvents.MouseHoldDownEvent} starts repeating.
     */
    public static final int MOUSE_HOLD_INITIAL_DELAY = 10;

    private static final int THEME_ANCHOR_LIMIT = 16;
    private static final double PIXEL_EPSILON = 1.0E-6D;

    private final EventListenerRegistry eventListeners = new EventListenerRegistry();
    private final Transform transform = new Transform();
    private final List<GuiComponent> children = new ArrayList<>();
    private final List<GuiComponent> childrenView = Collections.unmodifiableList(children);

    private GuiComponent parent;
    private WindowManager windowManager;
    private List<Behaviour> behaviours;

    private boolean hovered;
    private boolean focused;
    private boolean pressed;
    private double pressScreenX;
    private double pressScreenY;
    private int pressButton = -1;
    private int holdTicks;

    private AnchorFrame anchorFrame;
    private Rectangle anchorBounds;

    private boolean layoutDirty = true;
    private boolean inLayout;

    private boolean measureDirty = true;
    private final double[] measureKeys = {Double.NaN, Double.NaN, Double.NaN, Double.NaN};
    private final Size[] measureValues = new Size[2];
    private int measureSlot;
    private final double[] contentKeys = {Double.NaN, Double.NaN};
    private Size contentValue = Size.EMPTY;
    private Size desiredSize = Size.EMPTY;

    /**
     * Whether this component renders and takes part in layout. Fires
     * {@link GuiEvents.VisibilityChangedEvent}.
     * <p>Only covers this component itself; use {@link #isVisible()} to include the parents.</p>
     */
    public final BooleanProperty visible = new BooleanProperty(true);
    /**
     * Whether this component reacts to input. Fires {@link GuiEvents.EnabledChangedEvent}.
     * <p>Only covers this component itself; use {@link #isEnabled()} to include the parents.</p>
     */
    public final BooleanProperty enabled = new BooleanProperty(true);
    /**
     * Whether children are clipped to this component's content area.
     * <p>Turning it off also lets children keep rendering when this component itself is off screen.</p>
     */
    public final BooleanProperty clipsChildren = new BooleanProperty(true);
    /**
     * Whether this component's own drawing is clipped to its bounds.
     */
    public final BooleanProperty clipsContent = new BooleanProperty(true);
    /**
     * Whether this component can take keyboard focus, see {@link #requestFocus()}.
     */
    public final BooleanProperty focusable = new BooleanProperty(false);
    /**
     * Whether hit testing can land on this component. When false, input passes straight through to
     * whatever is behind it; children are still testable. Useful for labels and decorations.
     */
    public final BooleanProperty hitTestVisible = new BooleanProperty(true);
    /**
     * Which kinds of input this component consumes. Anything not in the set is left to the parent,
     * which is how a scrollable list can sit inside another one.
     */
    public final Property<Set<InputKind>> acceptedInput = new Property<>(InputKind.ALL);

    /**
     * Space kept free around this component. Honoured by the parent's layout, not by this component.
     */
    public final Property<Insets> margin = new Property<>(Insets.ZERO);
    /**
     * Space kept free inside this component. Shrinks the content area children and content live in.
     */
    public final Property<Insets> padding = new Property<>(Insets.ZERO);

    /**
     * Where the width comes from, see {@link SizeMode}.
     */
    public final Property<SizeMode> widthMode = new Property<>(SizeMode.FIXED);
    /**
     * Where the height comes from, see {@link SizeMode}.
     */
    public final Property<SizeMode> heightMode = new Property<>(SizeMode.FIXED);
    /**
     * Fraction for {@link SizeMode#PERCENT} and weight for {@link SizeMode#WEIGHT} on the x axis.
     * Ignored by the other modes.
     */
    public final NumberProperty<Double> widthFactor = new NumberProperty<>(1D, 0D, Double.MAX_VALUE);
    /**
     * Fraction for {@link SizeMode#PERCENT} and weight for {@link SizeMode#WEIGHT} on the y axis.
     * Ignored by the other modes.
     */
    public final NumberProperty<Double> heightFactor = new NumberProperty<>(1D, 0D, Double.MAX_VALUE);

    /**
     * Lower size limit. Enforced while measuring and in {@link #setBounds(double, double, double, double)}.
     */
    public final Property<Size> minSize = new Property<>(Size.EMPTY);
    /**
     * Upper size limit. Enforced while measuring and in {@link #setBounds(double, double, double, double)}.
     */
    public final Property<Size> maxSize = new Property<>(Size.INFINITY);

    /**
     * Scale factor for this component and everything inside it. Positions and sizes stay in unscaled
     * local units; only the screen rectangle changes.
     */
    public final NumberProperty<Double> scale = new NumberProperty<>(1D, 0.01D, 10D);

    /**
     * The layout that measures and arranges the children. Defaults to {@link NoLayout}, which leaves
     * them where they are and only applies their anchors.
     */
    public final Property<Layout> layout = new Property<>(NoLayout.INSTANCE);
    /**
     * Extra information for the parent's layout, e.g. the region in a BorderLayout. The expected type
     * depends on that layout.
     */
    public final Property<Object> layoutConstraint = new Property<>(null);
    /**
     * Edges this component sticks to when the parent's content area changes size, see {@link Anchor}.
     */
    public final BitflagProperty<Anchor> anchor = new BitflagProperty<>(Anchor.class, Anchor.LEFT, Anchor.TOP);

    /**
     * Mouse cursor while hovering this component, or null for the default. Behaviours can override it,
     * see {@link #getCurrentCursor()}.
     */
    public final Property<CursorType> cursor = new Property<>(null);
    /**
     * Free slot for whatever a mod wants to hang onto a component. Never read by the library.
     */
    public final Property<Object> customData = new Property<>(null);
    /**
     * Theme layer of this component. Only needs to hold the keys that should differ; everything else
     * falls through to the parents, see {@link #getThemeValue(Theme.Key)}.
     */
    public final Property<Theme> theme = new Property<>(null);
    /**
     * Where the theme lookup continues once there is no parent left. Needed for detached trees like
     * popups and context menus, which should still inherit from the component that opened them.
     */
    public final Property<GuiComponent> themeAnchor = new Property<>(null);
    /**
     * When false, the theme lookup stops here instead of walking up, so unset keys end up at their
     * default value rather than the parent's.
     */
    public final BooleanProperty inheritsTheme = new BooleanProperty(true);

    /**
     * @param x position within the parent, in local coordinates
     * @param y position within the parent, in local coordinates
     * @param width initial width; only kept as is with {@link SizeMode#FIXED}
     * @param height initial height; only kept as is with {@link SizeMode#FIXED}
     */
    public GuiComponent(int x, int y, int width, int height) {
        transform.setX(x);
        transform.setY(y);
        transform.setWidth(width);
        transform.setHeight(height);

        visible.addChangeListener((o, n) -> {
            invalidateLayout();
            invokeEvent(this, new GuiEvents.VisibilityChangedEvent(n));
        });
        enabled.addChangeListener((o, n) -> invokeEvent(this, new GuiEvents.EnabledChangedEvent(n)));
        scale.addChangeListener((o, n) -> transform.setScale(n));

        theme.addChangeListener((o, n) -> invalidateLayout());
        margin.addChangeListener((o, n) -> invalidateLayout());
        padding.addChangeListener((o, n) -> invalidateLayout());
        widthMode.addChangeListener((o, n) -> invalidateLayout());
        heightMode.addChangeListener((o, n) -> invalidateLayout());
        widthFactor.addChangeListener((o, n) -> invalidateLayout());
        heightFactor.addChangeListener((o, n) -> invalidateLayout());
        minSize.addChangeListener((o, n) -> invalidateLayout());
        maxSize.addChangeListener((o, n) -> invalidateLayout());
        layoutConstraint.addChangeListener((o, n) -> invalidateLayout());
        anchor.addChangeListener((o, n) -> {
            anchorFrame = null;
            invalidateLayout();
        });
        layout.addChangeListener((o, n) -> {
            if (o != null) {
                o.detachHost(this);
            }
            if (n != null) {
                n.attachHost(this);
            }
            invalidateLayout();
        });
        layout.get().attachHost(this);
    }

    @Override
    public EventListenerRegistry getEventListeners() {
        return eventListeners;
    }

    public Transform getTransform() {
        return transform;
    }

    /**
     * Resolves a theme key by walking up the tree: this component's own {@link #theme} first, then its
     * parents, then the {@link #themeAnchor} chain, then the global default theme, and finally the key's
     * own fallback. Never returns null.
     */
    public <T> T getThemeValue(Theme.Key<T> key) {
        GuiComponent current = this;
        GuiComponent anchor = null;
        int anchorHops = 0;
        while (current != null) {
            Theme layer = current.theme.get();
            if (layer != null) {
                T value = layer.find(key);
                if (value != null) {
                    return value;
                }
            }
            if (!current.inheritsTheme.get()) {
                return key.fallback();
            }
            if (anchor == null) {
                anchor = current.themeAnchor.get();
            }
            if (current.parent != null) {
                current = current.parent;
            } else if (anchor != null && anchorHops++ < THEME_ANCHOR_LIMIT) {
                current = anchor;
                anchor = null;
            } else {
                current = null;
            }
        }
        T value = Theme.getDefault().find(key);
        return value != null ? value : key.fallback();
    }

    /**
     * Sets a single theme key on this component, keeping the rest of its theme layer intact.
     *
     * @return this component, for chaining
     */
    public <T> GuiComponent overrideThemeKey(Theme.Key<T> key, T value) {
        Theme own = theme.get();
        theme.set(own == null ? Theme.builder().set(key, value).build() : own.with(key, value));
        return this;
    }

    /**
     * Rounds a coordinate to a whole pixel, but keeps the fraction if it is more than a rounding error
     * away from it. Keeps deliberate sub-pixel values from being flattened.
     */
    public static int toPixels(double value) {
        double whole = Math.round(value);
        return (int) (Math.abs(value - whole) < PIXEL_EPSILON ? whole : value);
    }

    public int x() {
        return toPixels(transform.x());
    }

    public int y() {
        return toPixels(transform.y());
    }

    public int width() {
        return toPixels(transform.width());
    }

    public int height() {
        return toPixels(transform.height());
    }

    public int right() {
        return toPixels(transform.x() + transform.width());
    }

    public int bottom() {
        return toPixels(transform.y() + transform.height());
    }

    public double dX() {
        return transform.x();
    }

    public double dY() {
        return transform.y();
    }

    public double dWidth() {
        return transform.width();
    }

    public double dHeight() {
        return transform.height();
    }

    public double dRight() {
        return transform.x() + transform.width();
    }

    public double dBottom() {
        return transform.y() + transform.height();
    }

    public void setX(double x) {
        setBounds(x, dY(), dWidth(), dHeight());
    }

    public void setY(double y) {
        setBounds(dX(), y, dWidth(), dHeight());
    }

    public void setPosition(double x, double y) {
        setBounds(x, y, dWidth(), dHeight());
    }

    public void setPosition(Point p) {
        setBounds(p.x(), p.y(), dWidth(), dHeight());
    }

    public void setWidth(double width) {
        setBounds(dX(), dY(), width, dHeight());
    }

    public void setHeight(double height) {
        setBounds(dX(), dY(), dWidth(), height);
    }

    public void setSize(double width, double height) {
        setBounds(dX(), dY(), width, height);
    }

    public void setSize(Size size) {
        setSize(size.w(), size.h());
    }

    /**
     * Moves and resizes this component in one go. The size is clamped to {@link #minSize} and
     * {@link #maxSize}; a changed size invalidates the layout. Fires
     * {@link GuiEvents.BoundsChangedEvent} unless nothing actually changed.
     * <p>Note that a layout will overwrite this on the next pass unless the size mode is
     * {@link SizeMode#FIXED}.</p>
     */
    public void setBounds(double x, double y, double w, double h) {
        Size min = minSize.get();
        Size max = maxSize.get();
        double oldX = transform.x();
        double oldY = transform.y();
        double oldRequestedW = transform.requestedWidth();
        double oldRequestedH = transform.requestedHeight();
        double oldW = transform.width();
        double oldH = transform.height();

        transform.setX(x);
        transform.setY(y);
        transform.setWidth(MathUtils.clamp(w, min.w(), max.w()));
        transform.setHeight(MathUtils.clamp(h, min.h(), max.h()));

        boolean moved = oldX != transform.x() || oldY != transform.y();
        boolean resized = oldRequestedW != transform.requestedWidth() || oldRequestedH != transform.requestedHeight();
        if (!moved && !resized) {
            return;
        }

        if (resized) {
            invalidateLayout();
        }
        invokeEvent(this, new GuiEvents.BoundsChangedEvent(oldX, transform.x(), oldY, transform.y(), oldW, transform.width(), oldH, transform.height()));
    }

    public Rectangle bounds() {
        return Rectangle.withSize(dX(), dY(), dWidth(), dHeight());
    }

    /**
     * Repositions this component inside the given area according to its {@link #anchor} flags. Called by
     * layouts that respect anchors; the gaps to the edges are taken from the first call and kept.
     *
     * @param area the parent's content area
     * @param measured this component's measured size, used for the unanchored axes
     */
    public void applyAnchor(Rectangle area, Size measured) {
        Rectangle current = bounds();
        if (anchorFrame == null || !current.equals(anchorBounds)) {
            anchorFrame = AnchorFrame.capture(current, area);
        }
        Rectangle target = anchorFrame.resolve(anchor, area, measured);
        setBounds(target.x(), target.y(), target.width(), target.height());
        anchorBounds = bounds();
    }

    public double scrollX() {
        return transform.scrollX();
    }

    public double scrollY() {
        return transform.scrollY();
    }

    public void setScrollX(double v) {
        setScroll(v, transform.scrollY());
    }

    public void setScrollY(double v) {
        setScroll(transform.scrollX(), v);
    }

    /**
     * Scrolls the content space, clamped to {@link #maxScrollX()}/{@link #maxScrollY()}. Fires
     * {@link GuiEvents.ScrollChangedEvent} if the offset really moved.
     */
    public void setScroll(double x, double y) {
        double oldX = transform.scrollX();
        double oldY = transform.scrollY();
        transform.setScrollX(MathUtils.clamp(x, 0, maxScrollX()));
        transform.setScrollY(MathUtils.clamp(y, 0, maxScrollY()));
        double newX = transform.scrollX();
        double newY = transform.scrollY();

        if (oldX == newX && oldY == newY) {
            return;
        }
        invokeEvent(this, new GuiEvents.ScrollChangedEvent(oldX, newX, oldY, newY));
    }

    /**
     * How far this component can scroll, i.e. how much the content sticks out of the content area.
     */
    public double maxScrollX() {
        return Math.max(0, desiredSize.w() - dContentWidth());
    }

    /**
     * How far this component can scroll, i.e. how much the content sticks out of the content area.
     */
    public double maxScrollY() {
        return Math.max(0, desiredSize.h() - dContentHeight());
    }

    public int contentX() {
        return toPixels(dContentX());
    }

    public int contentY() {
        return toPixels(dContentY());
    }

    public int contentWidth() {
        return toPixels(dContentWidth());
    }

    public int contentHeight() {
        return toPixels(dContentHeight());
    }

    public double dContentX() {
        return padding.get().left();
    }

    public double dContentY() {
        return padding.get().top();
    }

    public double dContentWidth() {
        return Math.max(0, dWidth() - padding.get().horizontal());
    }

    public double dContentHeight() {
        return Math.max(0, dHeight() - padding.get().vertical());
    }

    /**
     * The area inside the padding, in local coordinates. This is what layouts arrange children in.
     */
    public Rectangle contentArea() {
        Insets p = padding.get();
        return Rectangle.withSize(p.left(), p.top(), Math.max(0, dWidth() - p.horizontal()), Math.max(0, dHeight() - p.vertical()));
    }

    /**
     * Size the content needed at the last layout pass. Can be bigger than the content area, which is
     * exactly what makes scrolling possible.
     */
    public Size contentSize() {
        return desiredSize;
    }

    /**
     * Screen rectangle children are clipped to, or null if {@link #clipsChildren} is off.
     */
    public Rectangle childClipScreenRect() {
        if (!clipsChildren.get()) {
            return null;
        }
        return transform.toScreen(dContentX(), dContentY(), dContentWidth(), dContentHeight());
    }

    /**
     * Screen rectangle this component's own drawing is clipped to, or null if {@link #clipsContent} is off.
     */
    public Rectangle selfClipScreenRect() {
        if (!clipsContent.get()) {
            return null;
        }
        return transform.toScreen(0, 0, dWidth(), dHeight());
    }

    public Optional<GuiComponent> getParent() {
        return Optional.ofNullable(parent);
    }

    public GuiComponent getRootComponent() {
        GuiComponent current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }

    /**
     * Walks up the parent chain and returns the first ancestor matching the condition. Starts at the
     * parent, so this component itself is never returned.
     */
    public Optional<GuiComponent> getParent(Predicate<GuiComponent> condition) {
        GuiComponent current = parent;
        while (current != null) {
            if (condition.test(current)) {
                return Optional.of(current);
            }
            current = current.parent;
        }
        return Optional.empty();
    }

    /**
     * Walks up the tree and returns the first component of the given type. Starts at this component, so
     * it may return itself.
     */
    public <T> Optional<T> getParent(Class<T> type) {
        GuiComponent current = this;
        while (current != null) {
            if (type.isInstance(current)) {
                return Optional.of(type.cast(current));
            }
            current = current.parent;
        }
        return Optional.empty();
    }

    /**
     * Whether the given component is this one or one of its ancestors.
     */
    public boolean isDescendantOf(GuiComponent ancestor) {
        GuiComponent current = this;
        while (current != null) {
            if (current == ancestor) {
                return true;
            }
            current = current.parent;
        }
        return false;
    }

    /**
     * Live, unmodifiable view of the children, in render order: last on top.
     */
    public List<GuiComponent> getComponents() {
        return childrenView;
    }

    public List<GuiComponent> getComponentsIf(Predicate<GuiComponent> condition) {
        List<GuiComponent> result = new ArrayList<>(getComponentsCount());
        for (GuiComponent c : getComponents()) {
            if (condition.test(c)) {
                result.add(c);
            }
        }
        return result;
    }

    public <T extends GuiComponent> List<T> getComponentsOfType(Class<T> type) {
        List<T> result = new ArrayList<>(getComponentsCount());
        for (GuiComponent c : getComponents()) {
            if (type.isInstance(c)) {
                try {
                    result.add(type.cast(c));
                } catch (ClassCastException ignored) {}
            }
        }
        return result;
    }

    public int getComponentsCount() {
        return children.size();
    }

    /**
     * Adds a child on top of the others, detaching it from its previous parent first. Fires
     * {@link GuiEvents.ComponentAddedEvent}.
     *
     * @return the child, so it can be assigned in the same line
     * @throws IllegalArgumentException if the child is null or this component itself
     */
    public <T extends GuiComponent> T addComponent(T child) {
        if (child == null || child == this) {
            throw new IllegalArgumentException("Invalid child component.");
        }
        child.detach();
        children.add(child);
        child.attachTo(this);
        invalidateLayout();
        invokeEvent(this, new GuiEvents.ComponentAddedEvent(List.of(child)));
        return child;
    }

    /**
     * Adds several children at once, firing a single {@link GuiEvents.ComponentAddedEvent}. Null entries
     * and this component itself are skipped instead of throwing.
     */
    public void addComponents(Collection<? extends GuiComponent> components) {
        List<GuiComponent> added = new ArrayList<>(components.size());
        for (GuiComponent child : components) {
            if (child == null || child == this) {
                continue;
            }
            child.detach();
            children.add(child);
            child.attachTo(this);
            added.add(child);
        }
        if (added.isEmpty()) {
            return;
        }
        invalidateLayout();
        invokeEvent(this, new GuiEvents.ComponentAddedEvent(List.copyOf(added)));
    }

    public boolean removeComponent(GuiComponent child) {
        if (!children.remove(child)) {
            return false;
        }
        child.attachTo(null);
        invalidateLayout();
        invokeEvent(this, new GuiEvents.ComponentRemovedEvent(List.of(child)));
        return true;
    }

    public void clearComponents() {
        clearComponents(c -> true);
    }

    /**
     * Removes every child matching the filter, firing a single {@link GuiEvents.ComponentsClearedEvent}.
     */
    public void clearComponents(Predicate<GuiComponent> filter) {
        List<GuiComponent> removed = new ArrayList<>();
        for (int i = children.size() - 1; i >= 0; i--) {
            GuiComponent child = children.get(i);
            if (filter.test(child)) {
                children.remove(i);
                child.attachTo(null);
                removed.add(child);
            }
        }
        if (removed.isEmpty()) {
            return;
        }
        invalidateLayout();
        invokeEvent(this, new GuiEvents.ComponentsClearedEvent(List.copyOf(removed)));
    }

    /**
     * Removes this component from its parent, if it has one.
     */
    public void detach() {
        if (parent != null) {
            parent.removeComponent(this);
        }
    }

    /**
     * Moves a child to the end of the list, so it renders on top and is hit tested first.
     *
     * @return false if it is not a child of this component, or already on top
     */
    public boolean bringToFront(GuiComponent child) {
        int index = children.indexOf(child);
        if (index < 0 || index >= children.size() - 1) {
            return false;
        }
        children.remove(index);
        children.add(child);
        return true;
    }

    /**
     * Brings this component to the front within its parent.
     */
    public boolean bringToFront() {
        return getParent().map(x -> x.bringToFront(this)).orElse(false);
    }

    /**
     * Moves a child to the start of the list, so it renders behind its siblings.
     *
     * @return false if it is not a child of this component, or already at the back
     */
    public boolean sendToBack(GuiComponent child) {
        int index = children.indexOf(child);
        if (index <= 0) {
            return false;
        }
        children.remove(index);
        children.add(0, child);
        return true;
    }

    /**
     * Sends this component to the back within its parent.
     */
    public boolean sendToBack() {
        return getParent().map(x -> x.sendToBack(this)).orElse(false);
    }

    void attachTo(GuiComponent newParent) {
        Optional<GuiComponent> oldParent = Optional.ofNullable(parent);
        this.parent = newParent;
        this.anchorFrame = null;
        transform.setParent(newParent == null ? null : newParent.transform);
        setWindowManager(newParent == null ? null : newParent.windowManager);
        invokeEvent(this, new GuiEvents.ParentChangedEvent(oldParent, Optional.ofNullable(newParent)));
        if (newParent == null) {
            onDetached();
        } else {
            onAttached();
        }
    }

    /**
     * Called after this component got a new parent. Override to hook into the tree.
     */
    protected void onAttached() {
    }

    /**
     * Called after this component was removed from its parent. Override to clean up.
     */
    protected void onDetached() {
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    /**
     * Assigns the window manager and passes it down to all children. Normally handled automatically when
     * components are attached; fires {@link GuiEvents.WindowManagerChangedEvent}.
     */
    public void setWindowManager(WindowManager manager) {
        if (this.windowManager == manager) {
            return;
        }
        WindowManager old = this.windowManager;
        this.windowManager = manager;
        invokeEvent(this, new GuiEvents.WindowManagerChangedEvent(old, manager));
        for (GuiComponent c : children) {
            c.setWindowManager(manager);
        }
    }

    /**
     * Attaches a behaviour, replacing an already attached one of the same class.
     *
     * @return the behaviour that was passed in
     */
    public <T extends Behaviour> T addBehaviour(T behaviour) {
        Objects.requireNonNull(behaviour);
        if (behaviours == null) {
            behaviours = new ArrayList<>();
        }
        Behaviour existing = addBehaviour(behaviour.getClass());
        if (existing != null) {
            removeBehaviour(existing);
        }
        behaviours.add(behaviour);
        behaviour.onAttach(this);
        return behaviour;
    }

    public boolean removeBehaviour(Behaviour behaviour) {
        if (behaviours == null || !behaviours.remove(behaviour)) {
            return false;
        }
        behaviour.onDetach(this);
        return true;
    }

    /**
     * Removes the attached behaviour of this type, if there is one.
     *
     * @return whether something was removed
     */
    public boolean removeBehaviour(Class<? extends Behaviour> type) {
        Behaviour existing = addBehaviour(type);
        return existing != null && removeBehaviour(existing);
    }

    @SuppressWarnings("unchecked")
    /**
     * Looks up the attached behaviour of the given type. Despite the name this adds nothing.
     *
     * @return the behaviour, or null if none of that type is attached
     */
    public <T extends Behaviour> T addBehaviour(Class<T> type) {
        if (behaviours == null) {
            return null;
        }
        for (Behaviour behaviour : behaviours) {
            if (type.isInstance(behaviour)) {
                return (T) behaviour;
            }
        }
        return null;
    }

    public boolean hasBehaviour(Class<? extends Behaviour> type) {
        return addBehaviour(type) != null;
    }

    public List<Behaviour> getBehaviours() {
        return behaviours == null ? List.of() : Collections.unmodifiableList(behaviours);
    }

    private void detachAllBehaviours() {
        if (behaviours == null) {
            return;
        }
        for (int i = behaviours.size() - 1; i >= 0; i--) {
            behaviours.get(i).onDetach(this);
        }
        behaviours.clear();
    }

    public boolean isHovered() {
        return hovered;
    }

    public boolean isFocused() {
        return focused;
    }

    public boolean isPressed() {
        return pressed;
    }

    /**
     * Set by the input router while the mouse is over this component. Not meant to be called by hand.
     */
    public void setHovered(boolean value) {
        this.hovered = value;
    }

    /**
     * Set by the input router while a mouse button is held on this component; the position and button are
     * kept for the repeating hold events. Not meant to be called by hand.
     */
    public void setPressed(boolean value, double screenX, double screenY, int button) {
        this.pressed = value;
        this.pressScreenX = screenX;
        this.pressScreenY = screenY;
        this.pressButton = value ? button : -1;
        if (!value) {
            this.holdTicks = 0;
        }
    }

    /**
     * Sets the focus flag and fires {@link GuiEvents.FocusChangedEvent}. Called by the window manager;
     * use {@link #requestFocus()} to actually move the focus, otherwise two components end up focused.
     */
    public void setFocused(boolean value) {
        if (this.focused == value) {
            return;
        }
        this.focused = value;
        invokeEvent(this, new GuiEvents.FocusChangedEvent(value));
    }

    /**
     * Asks the window manager to move the keyboard focus here.
     *
     * @return false if there is no window manager, or this component is not focusable, enabled and visible
     */
    public boolean requestFocus() {
        WindowManager manager = windowManager;
        if (manager == null || !focusable.get() || !isEnabled() || !isVisible()) {
            return false;
        }
        manager.focus(this);
        return true;
    }

    /**
     * Whether this component is really visible, i.e. it and all of its ancestors are.
     */
    public boolean isVisible() {
        GuiComponent current = this;
        while (current != null) {
            if (!current.visible.get()) {
                return false;
            }
            current = current.parent;
        }
        return true;
    }

    /**
     * Whether this component is really enabled, i.e. it and all of its ancestors are.
     */
    public boolean isEnabled() {
        GuiComponent current = this;
        while (current != null) {
            if (!current.enabled.get()) {
                return false;
            }
            current = current.parent;
        }
        return true;
    }

    /**
     * Whether a screen position is inside this component's bounds. Ignores clipping by the parents, see
     * HitTester for real hit testing.
     */
    public boolean containsScreenPoint(double screenX, double screenY) {
        return transform.containsScreenPoint(screenX, screenY);
    }

    /**
     * The cursor to show right now. Behaviours implementing {@link CursorSource} get asked first, e.g. so
     * a resize handle can show an arrow; otherwise the {@link #cursor} property wins.
     */
    public CursorType getCurrentCursor() {
        if (!enabled.get()) {
            return CursorType.NOT_ALLOWED;
        }
        if (behaviours != null) {
            for (Behaviour b : behaviours) {
                if (b instanceof CursorSource source) {
                    CursorType type = source.cursor();
                    if (type != null) {
                        return type;
                    }
                }
            }
        }
        return cursor.get();
    }

    /**
     * Marks this component as needing a new layout pass and bubbles that up to the window manager. Call
     * this whenever something changed that affects positions or sizes.
     */
    public void invalidateLayout() {
        invalidateMeasure();
        if (layoutDirty) {
            return;
        }
        layoutDirty = true;
        if (parent != null) {
            parent.invalidateLayout();
        } else if (windowManager != null) {
            windowManager.requestLayout();
        }
    }

    /**
     * Throws away the cached measurements of this component and all of its ancestors, without scheduling
     * a layout pass. {@link #invalidateLayout()} already does this.
     */
    public void invalidateMeasure() {
        GuiComponent current = this;
        while (current != null) {
            current.measureDirty = true;
            current.measureKeys[0] = Double.NaN;
            current.measureKeys[2] = Double.NaN;
            current.contentKeys[0] = Double.NaN;
            current = current.parent;
        }
    }

    public boolean isLayoutDirty() {
        return layoutDirty;
    }

    /**
     * Asks how big this component wants to be in the offered space, cached per argument pair.
     * <p>Subtracts the padding, asks {@link #measureContent(double, double)}, adds the padding back,
     * applies the {@link SizeMode} of each axis and finally clamps to min and max size. Override
     * measureContent, not this.</p>
     *
     * @param availableWidth space the parent can offer, may be {@link Double#POSITIVE_INFINITY}
     * @param availableHeight space the parent can offer, may be {@link Double#POSITIVE_INFINITY}
     * @return the size including this component's own padding, but without its margin
     */
    public final Size measure(double availableWidth, double availableHeight) {
        if (!measureDirty) {
            for (int slot = 0; slot < 2; slot++) {
                if (measureValues[slot] != null
                    && measureKeys[slot * 2] == availableWidth
                    && measureKeys[slot * 2 + 1] == availableHeight) {
                    return measureValues[slot];
                }
            }
        } else {
            measureValues[0] = null;
            measureValues[1] = null;
            measureSlot = 0;
        }

        Insets p = padding.get();
        double innerWidth = Double.isInfinite(availableWidth) ? availableWidth : Math.max(0, availableWidth - p.horizontal());
        double innerHeight = Double.isInfinite(availableHeight) ? availableHeight : Math.max(0, availableHeight - p.vertical());

        Size content = resolveContent(innerWidth, innerHeight);

        double w = resolveLength(widthMode.get(), transform.width(), content.w() + p.horizontal(), availableWidth, widthFactor.get());
        double h = resolveLength(heightMode.get(), transform.height(), content.h() + p.vertical(), availableHeight, heightFactor.get());

        Size min = minSize.get();
        Size max = maxSize.get();
        Size result = Size.of(
            Math.max(0, MathUtils.clamp(w, min.w(), max.w())),
            Math.max(0, MathUtils.clamp(h, min.h(), max.h()))
        );

        int slot = measureSlot;
        measureKeys[slot * 2] = availableWidth;
        measureKeys[slot * 2 + 1] = availableHeight;
        measureValues[slot] = result;
        measureSlot = slot == 0 ? 1 : 0;
        measureDirty = false;
        return result;
    }

    /**
     * Re-clamps the current scroll offset against the content size, after the content may have shrunk.
     */
    protected void scrollToContent() {
        setScroll(transform.scrollX(), transform.scrollY());
    }

    /**
     * How much room this component's content needs, without its own padding.
     * <p>By default this asks the {@link #layout}, i.e. it is the size of the arranged children.
     * Components that draw their own content, like text or icons, override it and return that size
     * instead. The result also decides how far the component can scroll, so return the full content
     * size, not the visible part.</p>
     * <p>Must be free of side effects and give the same answer for the same arguments, because the
     * result is cached. If the content changes, call {@link #invalidateLayout()}.</p>
     *
     * @param availableWidth room for the content, already without padding; may be infinite, which means
     *                       unconstrained and has to be handled, e.g. by not wrapping text
     * @param availableHeight room for the content, already without padding; may be infinite
     */
    protected Size measureContent(double availableWidth, double availableHeight) {
        return layout.get().measure(this, availableWidth, availableHeight);
    }

    private Size resolveContent(double availableWidth, double availableHeight) {
        if (contentKeys[0] == availableWidth && contentKeys[1] == availableHeight) {
            return contentValue;
        }
        contentValue = measureContent(availableWidth, availableHeight);
        contentKeys[0] = availableWidth;
        contentKeys[1] = availableHeight;
        return contentValue;
    }

    /**
     * Runs the arrange pass: measures the content, lets the {@link #layout} place the children, re-clamps
     * the scroll offset and recurses into children that are still dirty. Fires
     * {@link GuiEvents.LayoutUpdatedEvent}.
     * <p>Called by the window manager, so normally there is no reason to call it yourself. Use
     * {@link #invalidateLayout()} instead.</p>
     */
    public final void applyLayout() {
        if (inLayout) {
            return;
        }
        inLayout = true;
        try {
            Rectangle area = contentArea();
            desiredSize = resolveContent(area.width(), area.height());
            layout.get().arrange(this, area);
            scrollToContent();
            for (GuiComponent c : children) {
                if (c.layoutDirty) {
                    c.applyLayout();
                }
            }
            layoutDirty = false;
            invokeEvent(this, new GuiEvents.LayoutUpdatedEvent(desiredSize));
        } finally {
            inLayout = false;
        }
    }

    private static double resolveLength(SizeMode mode, double fixed, double auto, double available, double factor) {
        return switch (mode) {
            case AUTO, WEIGHT -> auto;
            case FILL -> Double.isInfinite(available) ? auto : available;
            case PERCENT -> Double.isInfinite(available) ? auto : available * factor;
            default -> fixed;
        };
    }

    /**
     * Renders this component and its subtree: own content first, then the children in list order, then
     * {@link #postRender(DLGuiGraphics, double, double)} on top of them. Components outside the visible
     * area are skipped.
     *
     * @param screenMouseX mouse position in screen space, converted to local coordinates per component
     * @param screenMouseY mouse position in screen space, converted to local coordinates per component
     */
    public final void renderComponents(DLGuiGraphics g, double screenMouseX, double screenMouseY) {
        if (!visible.get()) {
            return;
        }

        boolean onScreen = dWidth() > 0 && dHeight() > 0 && g.isVisible(transform.screenX(), transform.screenY(), transform.screenWidth(), transform.screenHeight());

        if (!onScreen && (clipsChildren.get() || children.isEmpty())) {
            g.notifyCulled(this);
            return;
        }
        if (onScreen) {
            g.notifyRendered(this);
        }

        double localX = transform.toLocalX(screenMouseX);
        double localY = transform.toLocalY(screenMouseY);

        g.beginComponent(this);
        if (onScreen) {
            g.beginSelf(this);
            if (eventListeners.has(GuiEvents.RenderPreEvent.class)) {
                invokeEvent(this, new GuiEvents.RenderPreEvent(g, localX, localY));
            }
            if (!eventListeners.has(GuiEvents.RenderEvent.class) || invokeEvent(this, new GuiEvents.RenderEvent(g, localX, localY))) {
                render(g, localX, localY);
            }
            g.endSelf(this);
        }
        g.beginChildren(this);
        for (GuiComponent c : children) {
            c.renderComponents(g, screenMouseX, screenMouseY);
        }
        g.endChildren(this);
        if (onScreen) {
            g.beginSelf(this);
            postRender(g, localX, localY);
            if (eventListeners.has(GuiEvents.RenderPostEvent.class)) {
                invokeEvent(this, new GuiEvents.RenderPostEvent(g, localX, localY));
            }
            g.endSelf(this);
        }
        g.endComponent(this);
    }

    /**
     * Draws this component, below its children. Coordinates are local, so the origin is the top left
     * corner of this component.
     */
    protected void render(DLGuiGraphics g, double mouseX, double mouseY) {}

    /**
     * Draws on top of the children, e.g. overlays, borders or selection markers.
     */
    protected void postRender(DLGuiGraphics g, double mouseX, double mouseY) {}

    /**
     * One game tick for this component and its subtree: drives the repeating mouse-hold events and fires
     * {@link GuiEvents.TickEvent}.
     */
    public void tick() {
        if (pressed && (holdTicks <= 0 || holdTicks > MOUSE_HOLD_INITIAL_DELAY)) {
            if (eventListeners.has(GuiEvents.MouseHoldDownEvent.class)) {
                invokeEvent(this, new GuiEvents.MouseHoldDownEvent(this, pressScreenX, pressScreenY, pressButton, holdTicks));
            }
        }
        if (pressed) {
            holdTicks++;
        }
        for (GuiComponent c : children) {
            c.tick();
        }
        invokeEvent(this, new GuiEvents.TickEvent());
    }

    /**
     * Shuts this component and its subtree down: fires {@link GuiEvents.CloseEvent} and detaches all
     * behaviours. Called when the screen closes.
     */
    public void dispose() {
        invokeEvent(this, new GuiEvents.CloseEvent());
        for (GuiComponent child : children) {
            child.dispose();
        }
        detachAllBehaviours();
    }

    /**
     * Scrolls the given direct child into the visible content area, by the shortest distance. Does
     * nothing for components that are not a direct child.
     */
    public void scrollIntoView(GuiComponent child) {
        if (child.parent != this) {
            return;
        }
        double areaWidth = dContentWidth();
        double areaHeight = dContentHeight();
        if (child.dX() + child.dWidth() > scrollX() + areaWidth) {
            setScrollX(child.dX() + child.dWidth() - areaWidth);
        } else if (child.dX() < scrollX()) {
            setScrollX(child.dX());
        }
        if (child.dY() + child.dHeight() > scrollY() + areaHeight) {
            setScrollY(child.dY() + child.dHeight() - areaHeight);
        } else if (child.dY() < scrollY()) {
            setScrollY(child.dY());
        }
    }
}
