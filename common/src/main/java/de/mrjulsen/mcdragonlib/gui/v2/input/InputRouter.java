package de.mrjulsen.mcdragonlib.gui.v2.input;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import de.mrjulsen.mcdragonlib.client.gui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.dnd.DragSession;
import de.mrjulsen.mcdragonlib.gui.v2.dnd.DragSource;
import de.mrjulsen.mcdragonlib.gui.v2.dnd.DropTarget;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvent;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;

/**
 * Turns raw mouse and keyboard input into GUI events and keeps the state that goes with it: hover,
 * press, focus, drag and click counting. One per {@link de.mrjulsen.mcdragonlib.gui.v2.window.WindowManager}.
 * <p>Pointer events are delivered along the hit stack, innermost first, until someone consumes them.
 * Keyboard events instead start at the focused component and bubble up to the root. A press captures
 * the mouse, so the components pressed keep receiving the drag and the release even if the cursor
 * leaves them.</p>
 */
public final class InputRouter {

    /**
     * Pixels the mouse has to travel with a button down before it counts as a drag rather than a click.
     */
    public static final double DRAG_THRESHOLD = 4D;
    /**
     * Maximum time between two clicks on the same component for them to count as a double click.
     */
    public static final long MULTI_CLICK_INTERVAL_MS = 400L;

    private List<GuiComponent> hoverStack = List.of();
    private GuiComponent focused;
    private GuiComponent pressed;
    private List<GuiComponent> pressedStack = List.of();
    private double pressScreenX;
    private double pressScreenY;
    private double lastScreenX;
    private double lastScreenY;

    private boolean dragging;
    private boolean dragThresholdExceeded;
    private boolean dragConsumed;
    private DragSession dragSession;

    private int clickCount;
    private long lastClickMs;
    private GuiComponent lastClickTarget;

    public GuiComponent focusedComponent() {
        return focused;
    }

    /**
     * The running drag and drop session, or null if nothing is being dragged or the drag is a plain
     * pointer drag.
     */
    public DragSession dragSession() {
        return dragSession;
    }

    /**
     * Whether a drag is in progress, no matter of which kind.
     */
    public boolean isDragging() {
        return dragging;
    }

    /**
     * The innermost component a mouse button is currently held on.
     */
    public GuiComponent pressedComponent() {
        return pressed;
    }

    /**
     * The components under the mouse, innermost first. Shrinks to the captured components while a button
     * is held.
     */
    public List<GuiComponent> hoverStack() {
        return hoverStack;
    }

    public double mouseX() {
        return lastScreenX;
    }

    public double mouseY() {
        return lastScreenY;
    }

    /**
     * Moves the keyboard focus and scrolls the new component into view. Pass null to clear it.
     */
    public void focus(GuiComponent component) {
        if (focused == component) {
            return;
        }
        if (focused != null) {
            focused.setFocused(false);
        }
        focused = component;
        if (focused != null) {
            focused.setFocused(true);
            focused.getParent().ifPresent(x -> x.scrollIntoView(focused));
        }
    }

    /**
     * Drops all input state: hover, press, drag and cursor. Called when the screen closes.
     */
    public void clearState() {
        for (GuiComponent c : hoverStack) {
            c.setHovered(false);
        }
        hoverStack = List.of();
        releasePressed();
        dragging = false;
        if (dragSession != null) {
            dragSession.cancel();
            dragSession = null;
        }
        lastClickTarget = null;
        clickCount = 0;
        Cursors.set(null);
    }

    /**
     * Drops the input state belonging to a subtree that is about to disappear, so no events are
     * delivered to detached components. Called when a window or popup closes.
     */
    public void releaseInside(GuiComponent subtree) {
        if (focused != null && focused.isDescendantOf(subtree)) {
            focus(null);
        }
        if (pressed != null && pressed.isDescendantOf(subtree)) {
            releasePressed();
            dragging = false;
        }
        if (dragSession != null && dragSession.sourceComponent().isDescendantOf(subtree)) {
            dragSession.cancel();
            dragSession = null;
        }
        if (lastClickTarget != null && lastClickTarget.isDescendantOf(subtree)) {
            lastClickTarget = null;
            clickCount = 0;
        }
        if (!hoverStack.isEmpty()) {
            List<GuiComponent> remaining = new ArrayList<>(hoverStack.size());
            for (int i = 0; i < hoverStack.size(); i++) {
                GuiComponent component = hoverStack.get(i);
                if (component.isDescendantOf(subtree)) {
                    component.setHovered(false);
                } else {
                    remaining.add(component);
                }
            }
            hoverStack = List.copyOf(remaining);
        }
    }

    private void releasePressed() {
        for (int i = 0; i < pressedStack.size(); i++) {
            pressedStack.get(i).setPressed(false, lastScreenX, lastScreenY, -1);
        }
        pressed = null;
        pressedStack = List.of();
    }

    /**
     * Updates the hover state and fires the enter, leave and move events. Does nothing beyond
     * remembering the position while a drag is running.
     */
    public void mouseMoved(GuiComponent root, double screenX, double screenY) {
        lastScreenX = screenX;
        lastScreenY = screenY;
        if (dragging) {
            return;
        }
        updateHover(root, screenX, screenY);
    }

    private void updateHover(GuiComponent root, double screenX, double screenY) {
        List<GuiComponent> next = HitTester.hitStack(root, screenX, screenY, InputKind.POINTER);
        if (pressed != null) {
            List<GuiComponent> captured = new ArrayList<>(next.size());
            for (int i = 0; i < next.size(); i++) {
                if (pressedStack.contains(next.get(i))) {
                    captured.add(next.get(i));
                }
            }
            next = captured;
        }

        for (int i = 0; i < hoverStack.size(); i++) {
            GuiComponent component = hoverStack.get(i);
            if (!next.contains(component)) {
                component.setHovered(false);
                component.invokeEvent(component, new GuiEvents.MouseLeaveEvent(component, screenX, screenY));
            }
        }
        for (int i = 0; i < next.size(); i++) {
            GuiComponent component = next.get(i);
            if (!hoverStack.contains(component)) {
                component.setHovered(true);
                component.invokeEvent(component, new GuiEvents.MouseEnterEvent(component, screenX, screenY));
            }
        }
        hoverStack = next;
        deliver(next, new GuiEvents.MouseMoveEvent(head(next), screenX, screenY));
        updateCursor();
    }

    /**
     * Sets the cursor from the hovered components, taking the first one that asks for a shape.
     */
    public void updateCursor() {
        for (int i = 0; i < hoverStack.size(); i++) {
            CursorType type = hoverStack.get(i).getCurrentCursor();
            if (type != null) {
                Cursors.set(type);
                return;
            }
        }
        Cursors.set(null);
    }

    /**
     * Handles a mouse press: moves the focus to the first focusable component in the stack and marks
     * everything up to the first consumer as pressed, which captures the mouse until the release.
     *
     * @return whether a component took the press
     */
    public boolean mousePressed(GuiComponent root, double screenX, double screenY, int button) {
        lastScreenX = screenX;
        lastScreenY = screenY;
        pressScreenX = screenX;
        pressScreenY = screenY;

        List<GuiComponent> stack = HitTester.hitStack(root, screenX, screenY, InputKind.CLICK);
        if (stack.isEmpty()) {
            pressedStack = List.of();
            focus(null);
            pressed = null;
            return false;
        }

        focus(firstFocusable(stack));

        GuiEvents.MouseDownEvent event = new GuiEvents.MouseDownEvent(stack.get(0), screenX, screenY, button);
        List<GuiComponent> reached = new ArrayList<>(stack.size());
        for (int i = 0; i < stack.size(); i++) {
            GuiComponent component = stack.get(i);
            if (!component.isEnabled()) {
                continue;
            }
            component.setPressed(true, screenX, screenY, button);
            reached.add(component);
            component.invokeEvent(component, event);
            if (event.isConsumed()) {
                break;
            }
        }
        pressedStack = List.copyOf(reached);
        pressed = pressedStack.isEmpty() ? null : pressedStack.get(0);
        return pressed != null;
    }

    /**
     * Handles mouse movement with a button held. Below the drag threshold nothing happens; afterwards it
     * either starts a drag and drop session, if a component offers a payload, or delivers plain drag
     * events.
     *
     * @return whether the movement was treated as a drag
     */
    public boolean mouseDragged(GuiComponent root, double screenX, double screenY, int button, double deltaX, double deltaY) {
        lastScreenX = screenX;
        lastScreenY = screenY;
        if (pressed == null) {
            return false;
        }

        boolean wasThresholdExceeded = dragThresholdExceeded;
        dragThresholdExceeded |= Math.abs(screenX - pressScreenX) + Math.abs(screenY - pressScreenY) >= DRAG_THRESHOLD;
        if (!dragging || wasThresholdExceeded != dragThresholdExceeded) {
            boolean wasDragging = dragging;
            dragging = true;
            startDrag(screenX, screenY, button, !wasDragging);
        }
        if (dragSession != null) {
            updateDropTarget(root, screenX, screenY);
        } else {
            dragConsumed |= !deliver(pressedStack, new GuiEvents.DragEvent(pressed, screenX, screenY, button, pressScreenX, pressScreenY, deltaX, deltaY, dragThresholdExceeded));
        }
        updateCursor();
        return true;
    }

    private void startDrag(double screenX, double screenY, int button, boolean fresh) {
        for (int i = 0; i < pressedStack.size(); i++) {
            GuiComponent component = pressedStack.get(i);
            if (!component.isEnabled()) {
                continue;
            }
            if (component.hasEventListeners(GuiEvents.DragBeginEvent.class) || component.hasEventListeners(GuiEvents.DragEvent.class)) {
                break;
            }
            if (!(component instanceof DragSource source)) {
                continue;
            }
            Object payload = source.createDragPayload(pressScreenX, pressScreenY);
            if (payload == null) {
                continue;
            }
            dragSession = new DragSession(source, component, payload, button, screenX, screenY);
            source.onDragStarted(payload);
            component.invokeEvent(component, new GuiEvents.DragStartEvent(payload));
            return;
        }
        dragSession = null;

        if (fresh) {
            dragConsumed |= !deliver(pressedStack, new GuiEvents.DragBeginEvent(pressed, pressScreenX, pressScreenY, button));
        }
        if (dragThresholdExceeded) {
            dragConsumed |= !deliver(pressedStack, new GuiEvents.DragThresholdBeginEvent(pressed, pressScreenX, pressScreenY, button));
        }
    }

    private void updateDropTarget(GuiComponent root, double screenX, double screenY) {
        GuiComponent component = null;
        DropTarget target = null;
        List<GuiComponent> stack = HitTester.hitStack(root, screenX, screenY, InputKind.DROP);
        for (int i = 0; i < stack.size(); i++) {
            GuiComponent candidate = stack.get(i);
            if (candidate instanceof DropTarget dropTarget && candidate.isEnabled()) {
                component = candidate;
                target = dropTarget;
                break;
            }
        }
        dragSession.update(component, target, screenX, screenY);
    }

    /**
     * Handles a mouse release: finishes a drag, or emits the click events if the cursor is still on the
     * pressed component. Releases the capture either way.
     *
     * @return whether the release belonged to a press of this router
     */
    public boolean mouseReleased(GuiComponent root, double screenX, double screenY, int button) {
        lastScreenX = screenX;
        lastScreenY = screenY;
        if (pressed == null) {
            return false;
        }

        if (dragging) {
            if (dragSession != null) {
                updateDropTarget(root, screenX, screenY);
                dragSession.finish();
                dragSession = null;
            } else {
                dragConsumed |= !deliver(pressedStack, new GuiEvents.DragEndEvent(pressed, screenX, screenY, button, pressScreenX, pressScreenY, dragThresholdExceeded));
            }
        }

        if (dragSession == null && !dragConsumed) {
            deliver(pressedStack, new GuiEvents.MouseReleaseEvent(pressed, screenX, screenY, button));
            if (pressed.containsScreenPoint(screenX, screenY)) {
                emitClick(screenX, screenY, button);
            }
        }

        releasePressed();
        dragging = false;
        dragThresholdExceeded = false;
        dragConsumed = false;
        updateHover(root, screenX, screenY);
        return true;
    }

    private void emitClick(double screenX, double screenY, int button) {
        long now = System.currentTimeMillis();
        if (lastClickTarget == pressed && now - lastClickMs <= MULTI_CLICK_INTERVAL_MS) {
            clickCount++;
        } else {
            clickCount = 1;
        }
        lastClickMs = now;
        lastClickTarget = pressed;

        List<GuiComponent> stack = pressedStack;
        if (!deliver(stack, new GuiEvents.MouseClickEvent(pressed, screenX, screenY, button, clickCount))) {
            return;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !deliver(stack, new GuiEvents.ClickEvent(pressed, screenX, screenY))) {
            return;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !deliver(stack, new GuiEvents.RightClickEvent(pressed, screenX, screenY))) {
            return;
        }
        if (clickCount > 1) {
            deliver(stack, new GuiEvents.MultiClickEvent(pressed, screenX, screenY, button, clickCount));
        }
    }

    /**
     * Delivers a wheel event along the hit stack.
     *
     * @return whether a component consumed it
     */
    public boolean mouseScrolled(GuiComponent root, double screenX, double screenY, double deltaX, double deltaY) {
        lastScreenX = screenX;
        lastScreenY = screenY;
        List<GuiComponent> stack = HitTester.hitStack(root, screenX, screenY, InputKind.SCROLL);
        if (stack.isEmpty()) {
            return false;
        }
        return !deliver(stack, new GuiEvents.ScrollEvent(head(stack), screenX, screenY, deltaX, deltaY));
    }

    public boolean filesDropped(GuiComponent root, List<Path> paths) {
        List<GuiComponent> stack = HitTester.hitStack(root, lastScreenX, lastScreenY, InputKind.FILES);
        if (stack.isEmpty()) {
            return false;
        }
        return !deliver(stack, new GuiEvents.FilesDroppedEvent(head(stack), paths, lastScreenX, lastScreenY));
    }

    /**
     * Delivers a key press, starting at the focused component and bubbling up to the root.
     *
     * @param fallback where to start when nothing is focused, or the focused component is outside the
     *                 root, usually the active window
     * @return whether a component consumed the key
     */
    public boolean keyPressed(GuiComponent root, GuiComponent fallback, int keyCode, int scanCode, int modifiers) {
        return bubble(root, fallback, new GuiEvents.KeyPressEvent(keyCode, scanCode, modifiers));
    }

    public boolean keyReleased(GuiComponent root, GuiComponent fallback, int keyCode, int scanCode, int modifiers) {
        return bubble(root, fallback, new GuiEvents.KeyReleaseEvent(keyCode, scanCode, modifiers));
    }

    public boolean charTyped(GuiComponent root, GuiComponent fallback, char codePoint, int modifiers) {
        return bubble(root, fallback, new GuiEvents.CharTypeEvent(codePoint, modifiers));
    }

    private boolean bubble(GuiComponent root, GuiComponent fallback, GuiEvent event) {
        GuiComponent start = focused;
        if (start == null || (root != null && !start.isDescendantOf(root)) || !start.isEnabled()) {
            start = fallback;
        }
        GuiComponent current = start;
        while (current != null) {
            if (current.isEnabled()) {
                current.invokeEvent(current, event);
                if (event.isConsumed()) {
                    return true;
                }
            }
            if (current == root) {
                return false;
            }
            current = current.getParent().orElse(null);
        }
        return false;
    }

    /**
     * Moves the focus to the next focusable component in the subtree, in tree order, wrapping around at
     * the end. What Tab does.
     *
     * @return false if the subtree has nothing focusable
     */
    public boolean focusNext(GuiComponent root, boolean backwards) {
        List<GuiComponent> candidates = new ArrayList<>();
        collectFocusable(root, candidates);
        if (candidates.isEmpty()) {
            focus(null);
            return false;
        }
        int index = candidates.indexOf(focused);
        int size = candidates.size();
        int next;
        if (index < 0) {
            next = backwards ? size - 1 : 0;
        } else {
            next = ((index + (backwards ? -1 : 1)) % size + size) % size;
        }
        focus(candidates.get(next));
        return true;
    }

    private static void collectFocusable(GuiComponent component, List<GuiComponent> out) {
        if (component == null || !component.visible.get() || !component.enabled.get()) {
            return;
        }
        if (component.focusable.get()) {
            out.add(component);
        }
        List<GuiComponent> children = component.getComponents();
        for (int i = 0; i < children.size(); i++) {
            collectFocusable(children.get(i), out);
        }
    }

    private static GuiComponent head(List<GuiComponent> stack) {
        return stack.isEmpty() ? null : stack.get(0);
    }

    private static GuiComponent firstFocusable(List<GuiComponent> stack) {
        for (int i = 0; i < stack.size(); i++) {
            GuiComponent component = stack.get(i);
            if (component.focusable.get() && component.isEnabled()) {
                return component;
            }
        }
        return null;
    }

    private static boolean deliver(List<GuiComponent> stack, IEvent event) {
        GuiEvent cancellable = event instanceof GuiEvent gui ? gui : null;
        for (int i = 0; i < stack.size(); i++) {
            GuiComponent component = stack.get(i);
            if (!component.isEnabled()) {
                continue;
            }
            component.invokeEvent(component, event);
            if (cancellable != null && cancellable.isConsumed()) {
                return false;
            }
        }
        return true;
    }
}
