package de.mrjulsen.mcdragonlib.client.gui.events;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableBoolean;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.LayoutResult;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.events.IEvent.NotCancellable;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

/**
 * A container of standard GUI event types used across DragonLib components.
 *
 * <p>Each nested public {@code record} represents a specific GUI event payload
 * dispatched by DLGuiComponent and related classes. These records are simple,
 * immutable DTOs intended for the event system and should contain all state
 * required by event listeners.</p>
 *
 * <p>This class is non-instantiable and only provides the event record types.</p>
 */
public final class DLGuiStandardEvents {
    private DLGuiStandardEvents() {
    }

    /**
     * Event fired when a mouse button is initially pressed on a component.
     *
     * <p>Listeners receive the local mouse coordinates (relative to the component)
     * and the button index that was pressed.</p>
     *
     * @param mouseX the local x coordinate where the press occurred
     * @param mouseY the local y coordinate where the press occurred
     * @param button the mouse button index (platform-specific)
     */
    public record MouseDownEvent(double mouseX, double mouseY, int button) implements IEvent {
    }

    /**
     * Event fired repeatedly while a mouse button is held down on a component.
     *
     * <p>The event is fired once per tick after an initial delay as defined by
     * {@link de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent#MOUSE_DOWN_INITIAL_DELAY}.</p>
     *
     * @param mouseX the current local x coordinate of the mouse
     * @param mouseY the current local y coordinate of the mouse
     * @param button the mouse button index being held
     * @param ticks  number of ticks the button has been held so far
     */
    public record MouseHoldDownEvent(double mouseX, double mouseY, int button, int ticks) implements IEvent {
    }

    /**
     * Event fired when the mouse cursor enters a component's interactive area.
     *
     * @param mouseX the local x coordinate at entry time
     * @param mouseY the local y coordinate at entry time
     */
    public record MouseEnterEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Event fired when the mouse cursor leaves a component's interactive area.
     *
     * @param mouseX the local x coordinate at exit time
     * @param mouseY the local y coordinate at exit time
     */
    public record MouseLeaveEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Event fired when the mouse is moved within a component's interactive area.
     *
     * @param mouseX the current local x coordinate of the mouse
     * @param mouseY the current local y coordinate of the mouse
     */
    public record MouseMoveEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Event fired when a mouse button is released over a component.
     *
     * @param mouseX the local x coordinate where the release occurred
     * @param mouseY the local y coordinate where the release occurred
     * @param button the mouse button index that was released
     */
    public record MouseReleaseEvent(double mouseX, double mouseY, int button) implements IEvent {
    }

    /**
     * Generic event fired for any mouse button press. Use {@link ClickEvent}
     * for left-button convenience handling.
     *
     * @param mouseX the local x coordinate of the press
     * @param mouseY the local y coordinate of the press
     * @param button the mouse button index that was pressed
     */
    public record MousePressedEvent(double mouseX, double mouseY, int button) implements IEvent {
    }

    /**
     * Convenience event representing a left-click on a component.
     *
     * @param mouseX the local x coordinate of the click
     * @param mouseY the local y coordinate of the click
     */
    public record ClickEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Convenience event representing a right-click on a component.
     *
     * @param mouseX the local x coordinate of the right-click
     * @param mouseY the local y coordinate of the right-click
     */
    public record RightClickEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Event for multiple successive clicks (e.g. double- or triple-click).
     *
     * @param mouseX     the local x coordinate of the click
     * @param mouseY     the local y coordinate of the click
     * @param button     the mouse button index used
     * @param clickCount the number of consecutive clicks detected
     */
    public record MultiClickEvent(double mouseX, double mouseY, int button, byte clickCount) implements IEvent {
    }

    /**
     * Event fired when the mouse wheel (or equivalent) is scrolled over a component.
     *
     * @param mouseX the local x coordinate of the cursor during scroll
     * @param mouseY the local y coordinate of the cursor during scroll
     * @param deltaX the horizontal scroll delta
     * @param deltaY the vertical scroll delta
     */
    public record ScrollEvent(double mouseX, double mouseY, double deltaX, double deltaY) implements IEvent {
    }

    /**
     * Event fired once per engine/work loop tick for components that require periodic updates.
     */
    public record TickEvent() implements IEvent {
    }

    /**
     * Event fired after a child component was added to a parent.
     *
     * @param child the component that was added
     */
    public record ComponentAddedEvent(DLGuiComponent child) implements IEvent {
    }

    /**
     * Event fired after a child component was removed from a parent.
     *
     * @param child the component that was removed
     */
    public record ComponentRemovedEvent(DLGuiComponent child) implements IEvent {
    }

    /**
     * Event indicating a change of focus state for a component.
     *
     * @param focus true when the component gained focus, false when it lost focus
     */
    public record FocusChangedEvent(boolean focus) implements IEvent {
    }

    /**
     * Event fired when the user begins dragging with a mouse button over a component.
     *
     * @param mouseX the local x coordinate where the drag began
     * @param mouseY the local y coordinate where the drag began
     * @param button the mouse button index initiating the drag
     */
    public record DragBeginEvent(double mouseX, double mouseY, int button) implements IEvent {
    }

    /**
     * Event fired continuously while a drag operation is active.
     *
     * <p>Provides both screen and local origins plus the current drag offsets.</p>
     *
     * @param mouseX             current local mouse x
     * @param mouseY             current local mouse y
     * @param button             mouse button index performing the drag
     * @param screenMouseOriginX screen-space x where the drag began
     * @param screenMouseOriginY screen-space y where the drag began
     * @param localMouseOriginX  local-space x where the drag began
     * @param localMouseOriginY  local-space y where the drag began
     * @param dragX              current horizontal drag offset
     * @param dragY              current vertical drag offset
     */
    public record DragEvent(
            double mouseX,
            double mouseY,
            int button,
            double screenMouseOriginX,
            double screenMouseOriginY,
            double localMouseOriginX,
            double localMouseOriginY,
            double dragX,
            double dragY) implements IEvent {
    }

    /**
     * Event fired when a drag operation ends over a component.
     *
     * @param mouseX             final local mouse x
     * @param mouseY             final local mouse y
     * @param button             mouse button index used for dragging
     * @param screenMouseOriginX screen-space x where drag began
     * @param screenMouseOriginY screen-space y where drag began
     * @param localMouseOriginX  local-space x where drag began
     * @param localMouseOriginY  local-space y where drag began
     */
    public record DragEndEvent(
            double mouseX,
            double mouseY,
            int button,
            double screenMouseOriginX,
            double screenMouseOriginY,
            double localMouseOriginX,
            double localMouseOriginY) implements IEvent {
    }

    /**
     * Keyboard key-press event providing key code, hardware scan code and modifiers.
     *
     * @param keyCode  platform-independent key code
     * @param scanCode hardware scan code
     * @param modifiers bitmask of active modifier keys (Shift, Ctrl, Alt, etc.)
     */
    public record KeyPressEvent(int keyCode, int scanCode, int modifiers) implements IEvent {
    }

    /**
     * Keyboard key-release event providing key code, hardware scan code and modifiers.
     *
     * @param keyCode  platform-independent key code
     * @param scanCode hardware scan code
     * @param modifiers bitmask of active modifier keys
     */
    public record KeyReleaseEvent(int keyCode, int scanCode, int modifiers) implements IEvent {
    }

    /**
     * Character typing event delivering a Unicode code point and active modifiers.
     *
     * @param codePoint the typed character
     * @param modifiers bitmask of active modifier keys
     */
    public record CharTypeEvent(char codePoint, int modifiers) implements IEvent {
    }

    /**
     * Event fired when a resize operation is started by the user.
     *
     * @param mouseX the local x coordinate where the resize was initiated
     * @param mouseY the local y coordinate where the resize was initiated
     */
    public record ResizeBeginEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Event fired while a component is being resized by the user.
     *
     * @param mouseX    current local mouse x
     * @param mouseY    current local mouse y
     * @param newX      proposed new x position for the component
     * @param newY      proposed new y position for the component
     * @param newWidth  proposed new width for the component
     * @param newHeight proposed new height for the component
     */
    public record ResizeEvent(
            double mouseX,
            double mouseY,
            int newX,
            int newY,
            int newWidth,
            int newHeight) implements IEvent {
    }

    /**
     * Event fired when a user completes a resize operation.
     *
     * <p>Includes a mutable {@link MutableBoolean} cancel flag that listeners can set
     * to true to prevent the resize from being applied.</p>
     *
     * @param mouseX    final local mouse x
     * @param mouseY    final local mouse y
     * @param newX      final proposed x position
     * @param newY      final proposed y position
     * @param newWidth  final proposed width
     * @param newHeight final proposed height
     * @param cancel    mutable flag to cancel the resize if set to true
     */
    public record ResizeEndEvent(
            double mouseX,
            double mouseY,
            int newX,
            int newY,
            int newWidth,
            int newHeight,
            MutableBoolean cancel) implements IEvent {
    }

    /**
     * Event fired when the internal values representing position and size change.
     *
     * <p>This event is triggered by programmatic calls to setters (setX/setY/setWidth/setHeight),
     * not only by user interaction.</p>
     *
     * @param oldX old local x position
     * @param newX new local x position
     * @param oldY old local y position
     * @param newY new local y position
     * @param oldWidth old width
     * @param newWidth new width
     * @param oldHeight old height
     * @param newHeight new height
     */
    public record ComponentPosAndSizeChanged(int oldX, int newX, int oldY, int newY, int oldWidth, int newWidth,
            int oldHeight, int newHeight) implements IEvent {
        public boolean widthChanged() {
            return oldWidth != newWidth;
        }

        public boolean heightChanged() {
            return oldHeight != newHeight;
        }

        public boolean xChanged() {
            return oldX != newX;
        }

        public boolean yChanged() {
            return oldY != newY;
        }

        public boolean positionChanged() {
            return xChanged() || yChanged();
        }

        public boolean sizeChanged() {
            return widthChanged() || heightChanged();
        }
    }

    /**
     * Event fired when a component is being dragged by the user (component-as-drag-source).
     *
     * @param mouseX                the current local mouse x
     * @param mouseY                the current local mouse y
     * @param button                the mouse button index used to drag
     * @param draggedOverComponents list of components currently being hovered by the dragged item
     */
    public record DragComponentBeginEvent(double mouseX, double mouseY, int button,
            List<DLGuiComponent> draggedOverComponents) implements IEvent {
    }

    /**
     * Event fired repeatedly while a component is being dragged (as source).
     *
     * @param mouseX                the current local mouse x
     * @param mouseY                the current local mouse y
     * @param button                the mouse button index used
     * @param newX                  the proposed new local x for the dragged component
     * @param newY                  the proposed new local y for the dragged component
     * @param dragX                 horizontal drag offset
     * @param dragY                 vertical drag offset
     * @param draggedOverComponents list of components currently under the dragged element
     */
    public record DragComponentEvent(double mouseX, double mouseY, int button, int newX, int newY,
            double dragX, double dragY,
            List<DLGuiComponent> draggedOverComponents) implements IEvent {
    }

    /**
     * Event fired when a component drag operation ends; may be used to drop onto targets.
     *
     * @param mouseX                final local mouse x
     * @param mouseY                final local mouse y
     * @param button                mouse button index used for dragging
     * @param newX                  final local x position proposed for the dragged component
     * @param newY                  final local y position proposed for the dragged component
     * @param draggedOverComponents components that were under the dragged component at drop time
     * @param cancel                mutable flag that can be set to true to cancel the drop action
     */
    public record DragComponentEndEvent(double mouseX, double mouseY, int button, int newX, int newY,
            List<DLGuiComponent> draggedOverComponents, MutableBoolean cancel) implements IEvent {
    }

    /**
     * Event fired when an external drag operation enters this component's bounds.
     *
     * @param other  components being dragged over this component
     * @param mouseX local mouse x at the event moment
     * @param mouseY local mouse y at the event moment
     * @param button mouse button used for dragging
     */
    public record DragComponentOverBeginEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button)
            implements IEvent {
    }

    /**
     * Event fired continuously while external dragged components are over this component.
     *
     * @param other  components currently over this component
     * @param mouseX local mouse x
     * @param mouseY local mouse y
     * @param button mouse button used
     */
    public record DragComponentOverEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button)
            implements IEvent {
    }

    /**
     * Event fired when external dragged components leave this component's bounds.
     *
     * @param other  components leaving this component
     * @param mouseX local mouse x at the event moment
     * @param mouseY local mouse y at the event moment
     * @param button mouse button used for dragging
     */
    public record DragComponentOverEndEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button)
            implements IEvent {
    }

    /**
     * Event fired while a component is being dragged over several targets; a more generic variant.
     *
     * @param other  components currently being hovered
     * @param mouseX local mouse x
     * @param mouseY local mouse y
     * @param button mouse button used
     */
    public record DraggingOverEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button)
            implements IEvent {
    }

    /**
     * Event indicating a component has been dropped onto another component.
     *
     * @param other  the dropped component target
     * @param mouseX local mouse x at drop time
     * @param mouseY local mouse y at drop time
     */
    public record DropComponentEvent(DLGuiComponent other, double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Event representing a list of file system paths dropped onto a component.
     *
     * @param paths  the dropped file paths
     * @param mouseX local mouse x at drop time
     * @param mouseY local mouse y at drop time
     */
    public record DragAndDropFilesEvent(List<Path> paths, double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Event indicating visibility has changed for a component.
     *
     * @param visible true when the component became visible; false when hidden
     */
    public record VisibilityChangedEvent(boolean visible) implements IEvent {
    }

    /**
     * Event indicating the enabled state of a component changed.
     *
     * @param enabled true when the component is enabled; false when disabled
     */
    public record EnabledChangedEvent(boolean enabled) implements IEvent {
    }

    /**
     * Event indicating whether a component became resizable or not.
     *
     * @param resizable true when the component is resizable
     */
    public record ResizableChangedEvent(boolean resizable) implements IEvent {
    }

    /**
     * Event indicating whether a component became movable or not.
     *
     * @param movable true when the component is movable
     */
    public record MovableChangedEvent(boolean movable) implements IEvent {
    }

    /**
     * Non-cancellable event used to indicate the start/finish of a screen layout update.
     *
     * @param order the phase (PRE/POST) of the layout update
     */
    @NotCancellable
    public record ScreenLayoutUpdatedEvent(Phase order) implements IEvent {
    }
    
    /**
     * Non-cancellable event containing the result produced by a layout manager arrange operation.
     *
     * @param layoutResult the computed layout result
     */
    @NotCancellable
    public record ComponentLayoutUpdatedEvent(LayoutResult layoutResult) implements IEvent {
    }

    /**
     * Event fired during clearing of children. Includes a phase and a mutable cancel flag.
     *
     * @param order the phase of the clear operation (PRE/POST)
     * @param cancel mutable flag that can be set to true to abort during PRE phase
     */
    public record ComponentsClearEvent(Phase order, MutableBoolean cancel) implements IEvent {
    }

    /**
     * Event fired when a component's parent reference changes.
     *
     * @param oldParent optional previous parent
     * @param newParent optional new parent
     */
    public record ParentChangedEvent(Optional<DLGuiComponent> oldParent, Optional<DLGuiComponent> newParent)
            implements IEvent {
    }

    /**
     * Non-cancellable event notifying listeners of a change to a component's window manager assignment.
     *
     * @param oldWindowManager previous manager (may be null)
     * @param newWindowManager new manager (may be null)
     */
    @NotCancellable
    public record WindowManagerChangeEvent(DLWindowManager oldWindowManager, DLWindowManager newWindowManager)
            implements IEvent {
    }

    /**
     * Event fired immediately before rendering a component (per-layer).
     *
     * @param graphics     the graphics context used to draw
     * @param mouseX       the current local mouse x (subject to scroll/scale)
     * @param mouseY       the current local mouse y (subject to scroll/scale)
     * @param layer        the render layer currently being processed
     * @param renderBounds clipping bounds for this render pass
     */
    public record RenderPreEvent(DLGuiGraphics graphics, double mouseX, double mouseY,
            RenderLayer layer, Rectangle renderBounds) implements IEvent {
    }

    /**
     * Event fired while a render layer is being processed for a component.
     *
     * @param graphics     the graphics context used to draw
     * @param mouseX       the current local mouse x
     * @param mouseY       the current local mouse y
     * @param layer        the render layer being drawn
     * @param renderBounds clipping bounds for this render pass
     */
    public record RenderEvent(DLGuiGraphics graphics, double mouseX, double mouseY,
            RenderLayer layer, Rectangle renderBounds) implements IEvent {
    }

    /**
     * Event fired after rendering of a layer has completed for a component.
     *
     * @param graphics     the graphics context used to draw
     * @param mouseX       the current local mouse x
     * @param mouseY       the current local mouse y
     * @param layer        the render layer that was drawn
     * @param renderBounds clipping bounds that were applied
     */
    public record RenderPostEvent(DLGuiGraphics graphics, double mouseX, double mouseY,
            RenderLayer layer, Rectangle renderBounds) implements IEvent {
    }

    /**
     * Event used for on-screen overlays rendering (tooltips, screen-space UI).
     *
     * @param graphics the graphics context
     * @param mouseX current mouse x in screen space
     * @param mouseY current mouse y in screen space
     */
    public record RenderOnScreenEvent(DLGuiGraphics graphics, double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Non-cancellable event signalling that a component or window is being closed.
     * Useful for cleanup and disposing of resources.
     */
    @NotCancellable
    public record CloseEvent() implements IEvent {
    }

}
