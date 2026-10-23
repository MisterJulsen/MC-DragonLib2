package de.mrjulsen.mcdragonlib.gui.v2.event;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import de.mrjulsen.mcdragonlib.events.IEvent.NotCancellable;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.dnd.DragSource;
import de.mrjulsen.mcdragonlib.gui.v2.dnd.DropPosition;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.gui.v2.window.WindowManager;
import de.mrjulsen.mcdragonlib.util.math.Size;
import org.lwjgl.glfw.GLFW;

@SuppressWarnings("unused")
/**
 * Namespace of all built in GUI events. {@link GuiComponent} implements this interface, so inside a
 * component the nested names can be used without the prefix.
 * <p>Listen with addEventListener, e.g.
 * <code>addEventListener(GuiEvents.ClickEvent.class, (src, e) -> ...)</code>.</p>
 */
public interface GuiEvents {

    /**
     * Base class of the three render events. Mouse coordinates are local to the rendered component.
     */
    abstract class RenderEventBase extends GuiEvent {

        private final DLGuiGraphics graphics;
        private final double mouseX;
        private final double mouseY;

        protected RenderEventBase(DLGuiGraphics graphics, double mouseX, double mouseY) {
            this.graphics = graphics;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        public DLGuiGraphics graphics() {
            return graphics;
        }

        public double mouseX() {
            return mouseX;
        }

        public double mouseY() {
            return mouseY;
        }
    }

    @NotCancellable
    /**
     * Fired before a component draws itself. For drawing a background behind it.
     */
    class RenderPreEvent extends RenderEventBase {

        public RenderPreEvent(DLGuiGraphics graphics, double mouseX, double mouseY) {
            super(graphics, mouseX, mouseY);
        }
    }

    /**
     * Fired instead of nothing: consuming it skips the component's own render method, which is how a
     * component's look can be replaced from the outside.
     */
    class RenderEvent extends RenderEventBase {

        public RenderEvent(DLGuiGraphics graphics, double mouseX, double mouseY) {
            super(graphics, mouseX, mouseY);
        }
    }

    @NotCancellable
    /**
     * Fired after a component and its children were drawn. For overlays on top.
     */
    class RenderPostEvent extends RenderEventBase {

        public RenderPostEvent(DLGuiGraphics graphics, double mouseX, double mouseY) {
            super(graphics, mouseX, mouseY);
        }
    }

    /**
     * The mouse entered this component. Fired per component, not along the stack.
     */
    class MouseEnterEvent extends PointerEvent {

        public MouseEnterEvent(GuiComponent target, double screenX, double screenY) {
            super(target, screenX, screenY);
        }
    }

    /**
     * The mouse left this component. Fired per component, not along the stack.
     */
    class MouseLeaveEvent extends PointerEvent {

        public MouseLeaveEvent(GuiComponent target, double screenX, double screenY) {
            super(target, screenX, screenY);
        }
    }

    class MouseMoveEvent extends PointerEvent {

        public MouseMoveEvent(GuiComponent target, double screenX, double screenY) {
            super(target, screenX, screenY);
        }
    }

    /**
     * A pointer event that knows which mouse button is involved, as a GLFW button index.
     */
    abstract class ButtonEvent extends PointerEvent {

        private final int button;

        protected ButtonEvent(GuiComponent target, double screenX, double screenY, int button) {
            super(target, screenX, screenY);
            this.button = button;
        }

        public int button() {
            return button;
        }

        public boolean leftClick() {
            return button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
        }

        public boolean middleClick() {
            return button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
        }

        public boolean rightClick() {
            return button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        }
    }

    /**
     * A mouse button went down on this component. Consuming it keeps the components behind from being
     * pressed as well.
     */
    class MouseDownEvent extends ButtonEvent {

        public MouseDownEvent(GuiComponent target, double screenX, double screenY, int button) {
            super(target, screenX, screenY, button);
        }
    }

    /**
     * A mouse button was released again. Not fired when the press turned into a drag.
     */
    class MouseReleaseEvent extends ButtonEvent {

        public MouseReleaseEvent(GuiComponent target, double screenX, double screenY, int button) {
            super(target, screenX, screenY, button);
        }
    }

    /**
     * Fired repeatedly, once per tick, while a button stays down, after an initial delay. Handy for
     * buttons that should keep firing, like a spinner.
     * <p>{@code ticks()} counts how long the button has been held.</p>
     */
    class MouseHoldDownEvent extends ButtonEvent {

        private final int ticks;

        public MouseHoldDownEvent(GuiComponent target, double screenX, double screenY, int button, int ticks) {
            super(target, screenX, screenY, button);
            this.ticks = ticks;
        }

        public int ticks() {
            return ticks;
        }
    }

    /**
     * A full click, i.e. press and release on the same component, for any button.
     * <p>Fired before {@link ClickEvent}, {@link RightClickEvent} and {@link MultiClickEvent};
     * consuming it swallows those as well. {@code clickCount()} counts clicks in quick succession.</p>
     */
    class MouseClickEvent extends ButtonEvent {

        private final int clickCount;

        public MouseClickEvent(GuiComponent target, double screenX, double screenY, int button, int clickCount) {
            super(target, screenX, screenY, button);
            this.clickCount = clickCount;
        }

        public int clickCount() {
            return clickCount;
        }

        public boolean isDoubleClick() {
            return clickCount == 2;
        }
    }

    /**
     * A left click. The usual event for buttons.
     */
    class ClickEvent extends PointerEvent {

        public ClickEvent(GuiComponent target, double screenX, double screenY) {
            super(target, screenX, screenY);
        }
    }

    /**
     * A right click, typically used to open a context menu.
     */
    class RightClickEvent extends PointerEvent {

        public RightClickEvent(GuiComponent target, double screenX, double screenY) {
            super(target, screenX, screenY);
        }
    }

    /**
     * Fired in addition to the normal click events once a component is clicked repeatedly in quick
     * succession, i.e. from the second click on. {@code clickCount()} says which one it is.
     */
    class MultiClickEvent extends ButtonEvent {

        private final int clickCount;

        public MultiClickEvent(GuiComponent target, double screenX, double screenY, int button, int clickCount) {
            super(target, screenX, screenY, button);
            this.clickCount = clickCount;
        }

        public int clickCount() {
            return clickCount;
        }
    }

    /**
     * The mouse wheel was turned over this component. The deltas are wheel notches, not pixels, and
     * positive y means scrolling up. Consume it to keep the parents from scrolling too.
     */
    class ScrollEvent extends PointerEvent {

        private final double deltaX;
        private final double deltaY;

        public ScrollEvent(GuiComponent target, double screenX, double screenY, double deltaX, double deltaY) {
            super(target, screenX, screenY);
            this.deltaX = deltaX;
            this.deltaY = deltaY;
        }

        public double deltaX() {
            return deltaX;
        }

        public double deltaY() {
            return deltaY;
        }
    }

    /**
     * Base class of the key events. Key and scan code are GLFW values, the modifiers a GLFW bitmask;
     * use the hasX helpers instead of masking by hand.
     * <p>Key events do not travel along the hit stack but bubble up from the focused component.</p>
     */
    abstract class KeyEventBase extends GuiEvent {

        private final int keyCode;
        private final int scanCode;
        private final int modifiers;

        protected KeyEventBase(int keyCode, int scanCode, int modifiers) {
            this.keyCode = keyCode;
            this.scanCode = scanCode;
            this.modifiers = modifiers;
        }

        public int keyCode() {
            return keyCode;
        }

        public int scanCode() {
            return scanCode;
        }

        public int modifiers() {
            return modifiers;
        }

        public boolean hasShift() {
            return (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        }

        public boolean hasControl() {
            return (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        }

        public boolean hasAlt() {
            return (modifiers & GLFW.GLFW_MOD_ALT) != 0;
        }
    }

    /**
     * A key went down, including auto-repeat. Consume it to keep the key from reaching the parents and
     * the screen itself, e.g. so Escape does not close the window.
     */
    class KeyPressEvent extends KeyEventBase {

        public KeyPressEvent(int keyCode, int scanCode, int modifiers) {
            super(keyCode, scanCode, modifiers);
        }
    }

    class KeyReleaseEvent extends KeyEventBase {

        public KeyReleaseEvent(int keyCode, int scanCode, int modifiers) {
            super(keyCode, scanCode, modifiers);
        }
    }

    /**
     * A character was typed. This is the one to use for text input, as it already respects the keyboard
     * layout, unlike {@link KeyPressEvent}.
     */
    class CharTypeEvent extends GuiEvent {

        private final char codePoint;
        private final int modifiers;

        public CharTypeEvent(char codePoint, int modifiers) {
            this.codePoint = codePoint;
            this.modifiers = modifiers;
        }

        public char codePoint() {
            return codePoint;
        }

        public int modifiers() {
            return modifiers;
        }
    }

    /**
     * This component gained or lost the keyboard focus.
     */
    class FocusChangedEvent extends GuiEvent {

        private final boolean focused;

        public FocusChangedEvent(boolean focused) {
            this.focused = focused;
        }

        public boolean focused() {
            return focused;
        }
    }

    class VisibilityChangedEvent extends GuiEvent {

        private final boolean visible;

        public VisibilityChangedEvent(boolean visible) {
            this.visible = visible;
        }

        public boolean visible() {
            return visible;
        }
    }

    class EnabledChangedEvent extends GuiEvent {

        private final boolean enabled;

        public EnabledChangedEvent(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean enabled() {
            return enabled;
        }
    }

    /**
     * The mouse moved far enough with a button down to count as a drag.
     * <p>This is the raw pointer drag, used for moving and resizing. It is only fired when no drag and
     * drop payload was created, see {@link DragStartEvent}.</p>
     */
    class DragBeginEvent extends ButtonEvent {

        public DragBeginEvent(GuiComponent target, double screenX, double screenY, int button) {
            super(target, screenX, screenY, button);
        }
    }

    /**
     * The mouse moved while dragging. {@code dragX}/{@code dragY} are the step since the last event,
     * {@code originX}/{@code originY} where the drag started, and {@code totalDelta} the distance
     * covered so far.
     */
    class DragEvent extends ButtonEvent {

        private final double originX;
        private final double originY;
        private final double dragX;
        private final double dragY;

        public DragEvent(GuiComponent target, double screenX, double screenY, int button, double originX, double originY, double dragX, double dragY) {
            super(target, screenX, screenY, button);
            this.originX = originX;
            this.originY = originY;
            this.dragX = dragX;
            this.dragY = dragY;
        }

        public double originX() {
            return originX;
        }

        public double originY() {
            return originY;
        }

        public double dragX() {
            return dragX;
        }

        public double dragY() {
            return dragY;
        }

        public double totalDeltaX() {
            return screenX() - originX;
        }

        public double totalDeltaY() {
            return screenY() - originY;
        }
    }

    /**
     * The button was released and the drag is over. {@code originX}/{@code originY} are where it started.
     */
    class DragEndEvent extends ButtonEvent {

        private final double originX;
        private final double originY;

        public DragEndEvent(GuiComponent target, double screenX, double screenY, int button, double originX, double originY) {
            super(target, screenX, screenY, button);
            this.originX = originX;
            this.originY = originY;
        }

        public double originX() {
            return originX;
        }

        public double originY() {
            return originY;
        }
    }

    /**
     * Base class of the resize events, carrying the new bounds.
     */
    abstract class BoundsEvent extends GuiEvent {

        private final double x;
        private final double y;
        private final double width;
        private final double height;

        protected BoundsEvent(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public double newX() {
            return x;
        }

        public double newY() {
            return y;
        }

        public double newWidth() {
            return width;
        }

        public double newHeight() {
            return height;
        }
    }

    /**
     * The user grabbed a resize handle.
     */
    class ResizeBeginEvent extends PointerEvent {

        public ResizeBeginEvent(GuiComponent target, double screenX, double screenY) {
            super(target, screenX, screenY);
        }
    }

    /**
     * Fired while the user resizes a component by hand. Consume it to refuse the new bounds.
     */
    class ResizeEvent extends BoundsEvent {

        public ResizeEvent(double x, double y, double width, double height) {
            super(x, y, width, height);
        }
    }

    /**
     * The resize is finished, e.g. the moment to save the new size.
     */
    class ResizeEndEvent extends BoundsEvent {

        public ResizeEndEvent(double x, double y, double width, double height) {
            super(x, y, width, height);
        }
    }

    /**
     * The user grabbed a move handle, e.g. a window title bar.
     */
    class MoveBeginEvent extends PointerEvent {

        public MoveBeginEvent(GuiComponent target, double screenX, double screenY) {
            super(target, screenX, screenY);
        }
    }

    /**
     * Fired while the user drags a component around. Consume it to refuse the new position.
     */
    class MoveEvent extends GuiEvent {

        private final double x;
        private final double y;

        public MoveEvent(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double newX() {
            return x;
        }

        public double newY() {
            return y;
        }
    }

    /**
     * The move is finished, e.g. the moment to save the new position.
     */
    class MoveEndEvent extends MoveEvent {

        public MoveEndEvent(double x, double y) {
            super(x, y);
        }
    }

    /**
     * Base class of the drag and drop events. The payload is whatever the drag source handed out, so
     * a drop target has to check its type.
     */
    abstract class PayloadEvent extends GuiEvent {

        private final Object payload;

        protected PayloadEvent(Object payload) {
            this.payload = payload;
        }

        public Object payload() {
            return payload;
        }
    }

    /**
     * Fired on the drag source once it produced a payload, i.e. a real drag and drop started.
     */
    class DragStartEvent extends PayloadEvent {

        public DragStartEvent(Object payload) {
            super(payload);
        }
    }

    /**
     * Fired on a drop target while the payload hovers over it; {@code position()} says where exactly,
     * e.g. above or below a list entry.
     */
    class DragOverEvent extends PayloadEvent {

        private final DropPosition position;

        public DragOverEvent(Object payload, DropPosition position) {
            super(payload);
            this.position = position;
        }

        public DropPosition position() {
            return position;
        }
    }

    /**
     * Fired on a drop target once the payload leaves it again. Time to drop any insert markers.
     */
    class DragLeaveEvent extends PayloadEvent {

        public DragLeaveEvent(Object payload) {
            super(payload);
        }
    }

    /**
     * The payload was dropped on this target. {@code source()} is where it came from, which matters for
     * move versus copy.
     */
    class DropEvent extends PayloadEvent {

        private final DropPosition position;
        private final DragSource source;

        public DropEvent(Object payload, DropPosition position, DragSource source) {
            super(payload);
            this.position = position;
            this.source = source;
        }

        public DropPosition position() {
            return position;
        }

        public DragSource source() {
            return source;
        }
    }

    /**
     * Files were dropped onto the game window from outside.
     */
    class FilesDroppedEvent extends PointerEvent {

        private final List<Path> paths;

        public FilesDroppedEvent(GuiComponent target, List<Path> paths, double screenX, double screenY) {
            super(target, screenX, screenY);
            this.paths = paths;
        }

        public List<Path> paths() {
            return paths;
        }
    }

    /**
     * Base class of the events about children, carrying the ones that were added or removed.
     */
    abstract class ChildrenEvent extends GuiEvent {

        private final List<GuiComponent> children;

        protected ChildrenEvent(List<GuiComponent> children) {
            this.children = children;
        }

        public List<GuiComponent> children() {
            return children;
        }
    }

    @NotCancellable
    class ComponentAddedEvent extends ChildrenEvent {

        public ComponentAddedEvent(List<GuiComponent> children) {
            super(children);
        }
    }

    @NotCancellable
    class ComponentRemovedEvent extends ChildrenEvent {

        public ComponentRemovedEvent(List<GuiComponent> children) {
            super(children);
        }
    }

    @NotCancellable
    /**
     * Several children were removed at once, see GuiComponent#clearComponents.
     */
    class ComponentsClearedEvent extends ChildrenEvent {

        public ComponentsClearedEvent(List<GuiComponent> children) {
            super(children);
        }
    }

    @NotCancellable
    /**
     * This component was attached to or detached from a parent; both are empty when it is the root.
     */
    class ParentChangedEvent extends GuiEvent {

        private final Optional<GuiComponent> oldParent;
        private final Optional<GuiComponent> newParent;

        public ParentChangedEvent(Optional<GuiComponent> oldParent, Optional<GuiComponent> newParent) {
            this.oldParent = oldParent;
            this.newParent = newParent;
        }

        public Optional<GuiComponent> oldParent() {
            return oldParent;
        }

        public Optional<GuiComponent> newParent() {
            return newParent;
        }
    }

    @NotCancellable
    /**
     * This component ended up under a different window manager, usually because it was attached
     * somewhere else. Either side may be null.
     */
    class WindowManagerChangedEvent extends GuiEvent {

        private final WindowManager oldManager;
        private final WindowManager newManager;

        public WindowManagerChangedEvent(WindowManager oldManager, WindowManager newManager) {
            this.oldManager = oldManager;
            this.newManager = newManager;
        }

        public WindowManager oldManager() {
            return oldManager;
        }

        public WindowManager newManager() {
            return newManager;
        }
    }

    @NotCancellable
    /**
     * Position or size of this component changed. Reported after the fact, no matter whether it was a
     * layout, a resize handle or a setter; use the xChanged/sizeChanged helpers to tell what moved.
     */
    class BoundsChangedEvent extends GuiEvent {

        private final double oldX;
        private final double newX;
        private final double oldY;
        private final double newY;
        private final double oldWidth;
        private final double newWidth;
        private final double oldHeight;
        private final double newHeight;

        public BoundsChangedEvent(double oldX, double newX, double oldY, double newY, double oldWidth, double newWidth, double oldHeight, double newHeight) {
            this.oldX = oldX;
            this.newX = newX;
            this.oldY = oldY;
            this.newY = newY;
            this.oldWidth = oldWidth;
            this.newWidth = newWidth;
            this.oldHeight = oldHeight;
            this.newHeight = newHeight;
        }

        public double oldX() {
            return oldX;
        }

        public double newX() {
            return newX;
        }

        public double oldY() {
            return oldY;
        }

        public double newY() {
            return newY;
        }

        public double oldWidth() {
            return oldWidth;
        }

        public double newWidth() {
            return newWidth;
        }

        public double oldHeight() {
            return oldHeight;
        }

        public double newHeight() {
            return newHeight;
        }

        public boolean xChanged() {
            return oldX != newX;
        }

        public boolean yChanged() {
            return oldY != newY;
        }

        public boolean widthChanged() {
            return oldWidth != newWidth;
        }

        public boolean heightChanged() {
            return oldHeight != newHeight;
        }

        public boolean positionChanged() {
            return xChanged() || yChanged();
        }

        public boolean sizeChanged() {
            return widthChanged() || heightChanged();
        }
    }

    @NotCancellable
    /**
     * The scroll offset changed. Only fired when it really moved, so a scrollbar can follow along.
     */
    class ScrollChangedEvent extends GuiEvent {

        private final double oldScrollX;
        private final double newScrollX;
        private final double oldScrollY;
        private final double newScrollY;

        public ScrollChangedEvent(double oldScrollX, double newScrollX, double oldScrollY, double newScrollY) {
            this.oldScrollX = oldScrollX;
            this.newScrollX = newScrollX;
            this.oldScrollY = oldScrollY;
            this.newScrollY = newScrollY;
        }

        public double oldScrollX() {
            return oldScrollX;
        }

        public double newScrollX() {
            return newScrollX;
        }

        public double oldScrollY() {
            return oldScrollY;
        }

        public double newScrollY() {
            return newScrollY;
        }

        public double deltaX() {
            return newScrollX - oldScrollX;
        }

        public double deltaY() {
            return newScrollY - oldScrollY;
        }

        public boolean scrollXChanged() {
            return oldScrollX != newScrollX;
        }

        public boolean scrollYChanged() {
            return oldScrollY != newScrollY;
        }
    }

    @NotCancellable
    /**
     * This component finished a layout pass. {@code contentSize()} is the size the content needed, which
     * is what the scroll range is built from.
     */
    class LayoutUpdatedEvent extends GuiEvent {

        private final Size contentSize;

        public LayoutUpdatedEvent(Size contentSize) {
            this.contentSize = contentSize;
        }

        public Size contentSize() {
            return contentSize;
        }
    }

    @NotCancellable
    /**
     * One game tick, 20 times per second. For animations and timers, not for rendering.
     */
    class TickEvent extends GuiEvent {
    }

    @NotCancellable
    /**
     * This component is being disposed of. Last chance to release resources.
     */
    class CloseEvent extends GuiEvent {
    }
}
