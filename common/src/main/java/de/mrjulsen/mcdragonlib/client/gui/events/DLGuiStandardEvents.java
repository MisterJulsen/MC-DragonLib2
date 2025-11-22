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
 * A collection of all standard GUI events, which are supported and implemented
 * by all Dragonlib GUI Components {@link DLGuiComponent}.
 */
public final class DLGuiStandardEvents {
    private DLGuiStandardEvents() {
    }

    /**
     * Represents an event triggered when a mouse button is pressed on a component.
     * <p>
     * This event is fired only once at the moment of the press, not continuously
     * while the button remains held down. For continuous detection, use
     * {@link MouseHoldDownEvent}.
     * </p>
     *
     * @param mouseX the local x-coordinate of the mouse on the component
     * @param mouseY the local y-coordinate of the mouse on the component
     * @param button the mouse button that was pressed
     */
    public record MouseDownEvent(double mouseX, double mouseY, int button) implements IEvent {
    }

    /**
     * Represents an event triggered while a mouse button is being held down
     * on a component.
     * <p>
     * This event has an initial delay of
     * {@link DLGuiComponent#MOUSE_DOWN_INITIAL_DELAY} ticks after the first
     * trigger,
     * as is common in many GUI frameworks. After the delay, the event is fired
     * once per tick for as long as the button remains pressed.
     * </p>
     *
     * @param mouseX the local x-coordinate of the mouse on the component
     * @param mouseY the local y-coordinate of the mouse on the component
     * @param button the mouse button being held down
     * @param ticks  the number of ticks the button has been held down for
     */
    public record MouseHoldDownEvent(double mouseX, double mouseY, int button, int ticks) implements IEvent {
    }

    /**
     * Represents an event triggered when the mouse enters the bounds of a
     * component.
     * <p>
     * This event provides the local mouse position at the moment the cursor
     * crosses into the component's area.
     * </p>
     *
     * @param mouseX the local x-coordinate of the mouse on the component
     * @param mouseY the local y-coordinate of the mouse on the component
     */
    public record MouseEnterEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Represents an event triggered when the mouse leaves the bounds of a
     * component.
     * <p>
     * This event provides the local mouse position at the moment the cursor
     * exits the component's area.
     * </p>
     *
     * @param mouseX the local x-coordinate of the mouse on the component
     * @param mouseY the local y-coordinate of the mouse on the component
     */
    public record MouseLeaveEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Represents an event triggered when the mouse is moved within the bounds
     * of a component.
     * <p>
     * This event provides the current local mouse position relative to the
     * component.
     * </p>
     *
     * @param mouseX the local x-coordinate of the mouse on the component
     * @param mouseY the local y-coordinate of the mouse on the component
     */
    public record MouseMoveEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Represents an event triggered when the mouse button is released
     * after being pressed on a component.
     * <p>
     * This event provides the local mouse position and the button
     * that was released.
     * </p>
     *
     * @param mouseX the local x-coordinate of the mouse on the component
     * @param mouseY the local y-coordinate of the mouse on the component
     * @param button the mouse button that was released
     */
    public record MouseReleaseEvent(double mouseX, double mouseY, int button) implements IEvent {
    }

    /**
     * Represents an event triggered when any mouse button is pressed
     * on a component.
     * <p>
     * This event is similar to {@link ClickEvent}, but it is fired
     * for every mouse button press, not just the left button.
     * </p>
     *
     * @param mouseX the local x-coordinate of the mouse on the component
     * @param mouseY the local y-coordinate of the mouse on the component
     * @param button the mouse button that was pressed
     */
    public record MousePressedEvent(double mouseX, double mouseY, int button) implements IEvent {
    }

    /**
     * Represents an event triggered when the left mouse button is pressed
     * on a component.
     * <p>
     * This is a simplified version of {@link MousePressedEvent}.
     * For right-click detection, see {@link RightClickEvent}.
     * </p>
     *
     * @param mouseX the local x-coordinate of the mouse on the component
     * @param mouseY the local y-coordinate of the mouse on the component
     */
    public record ClickEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Represents an event triggered when the right mouse button is pressed
     * on a component.
     * <p>
     * This is a simplified version of {@link MousePressedEvent}.
     * For left-click detection, see {@link ClickEvent}.
     * </p>
     *
     * @param mouseX the local x-coordinate of the mouse on the component
     * @param mouseY the local y-coordinate of the mouse on the component
     */
    public record RightClickEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Represents an event triggered when a mouse button is clicked
     * multiple times in succession.
     * <p>
     * This event can be used for double-click detection, but also supports
     * higher click counts such as triple-clicks.
     * </p>
     *
     * @param mouseX     the local x-coordinate of the mouse on the component
     * @param mouseY     the local y-coordinate of the mouse on the component
     * @param button     the mouse button that was clicked
     * @param clickCount the number of consecutive clicks performed
     */
    public record MultiClickEvent(double mouseX, double mouseY, int button, byte clickCount) implements IEvent {
    }

    /**
     * Represents an event triggered when the user scrolls with the mouse wheel
     * over a component.
     * <p>
     * This event provides the local mouse position and the scroll delta values
     * along both the x and y axes.
     * </p>
     *
     * @param mouseX the local x-coordinate of the mouse on the component
     * @param mouseY the local y-coordinate of the mouse on the component
     * @param deltaX the scroll delta along the x-axis
     * @param deltaY the scroll delta along the y-axis
     */
    public record ScrollEvent(double mouseX, double mouseY, double deltaX, double deltaY) implements IEvent {
    }

    /**
     * Represents an event that is triggered on every game tick.
     * <p>
     * This event can be used to perform periodic updates such as animations,
     * physics calculations, or other time-based logic.
     * </p>
     */
    public record TickEvent() implements IEvent {
    }

    /**
     * Represents an event triggered when a new child component is added
     * to a parent component.
     * <p>
     * This event provides access to the newly added component, allowing
     * initialization or layout adjustments.
     * </p>
     *
     * @param child the component that was added
     */
    public record ComponentAddedEvent(DLGuiComponent child) implements IEvent {
    }

    /**
     * Represents an event triggered when a child component is removed
     * from a parent component.
     * <p>
     * This event provides access to the removed component, allowing cleanup
     * or updates to the parent layout.
     * </p>
     *
     * @param child the component that was removed
     */
    public record ComponentRemovedEvent(DLGuiComponent child) implements IEvent {
    }

    /**
     * Represents an event triggered when the focus state of a component changes.
     * <p>
     * This event indicates whether the component has gained or lost focus,
     * which can be used to update visuals or handle input accordingly.
     * </p>
     *
     * @param focus {@code true} if the component gained focus,
     *              {@code false} if it lost focus
     */
    public record FocusChangedEvent(boolean focus) implements IEvent {
    }

    /**
     * Represents an event triggered when the user begins dragging the mouse
     * over a component.
     * <p>
     * This event provides the local mouse position and the button used
     * to initiate the drag.
     * </p>
     *
     * @param mouseX the local x-coordinate of the mouse on the component
     * @param mouseY the local y-coordinate of the mouse on the component
     * @param button the mouse button used to start dragging
     */
    public record DragBeginEvent(double mouseX, double mouseY, int button) implements IEvent {
    }

    /**
     * Represents an event triggered while the user is dragging the mouse
     * over a component.
     * <p>
     * This event provides both the current mouse position and the origin
     * of the drag in screen and local coordinates, as well as the drag offset.
     * </p>
     *
     * @param mouseX             the current local x-coordinate of the mouse
     * @param mouseY             the current local y-coordinate of the mouse
     * @param button             the mouse button used for dragging
     * @param screenMouseOriginX the initial screen x-coordinate where dragging
     *                           began
     * @param screenMouseOriginY the initial screen y-coordinate where dragging
     *                           began
     * @param localMouseOriginX  the initial local x-coordinate where dragging began
     * @param localMouseOriginY  the initial local y-coordinate where dragging began
     * @param dragX              the horizontal offset of the drag since it began
     * @param dragY              the vertical offset of the drag since it began
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
     * Represents an event triggered when the user ends a mouse drag
     * over a component.
     * <p>
     * This event provides the final mouse position and the origin
     * of the drag in both screen and local coordinates.
     * </p>
     *
     * @param mouseX             the final local x-coordinate of the mouse
     * @param mouseY             the final local y-coordinate of the mouse
     * @param button             the mouse button used for dragging
     * @param screenMouseOriginX the initial screen x-coordinate where dragging
     *                           began
     * @param screenMouseOriginY the initial screen y-coordinate where dragging
     *                           began
     * @param localMouseOriginX  the initial local x-coordinate where dragging began
     * @param localMouseOriginY  the initial local y-coordinate where dragging began
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
     * Triggered when a key is pressed on the keyboard.
     * <p>
     * This event provides information about the key code, the hardware scan code,
     * and any active modifier keys at the time of the press.
     * </p>
     *
     * @param keyCode   the virtual key code representing the key pressed
     * @param scanCode  the hardware-dependent scan code of the key
     * @param modifiers a bitmask representing active modifier keys (e.g. Shift,
     *                  Ctrl, Alt)
     */
    public record KeyPressEvent(int keyCode, int scanCode, int modifiers) implements IEvent {
    }

    /**
     * Triggered when a key is released on the keyboard.
     * <p>
     * This event provides information about the key code, the hardware scan code,
     * and any active modifier keys at the time of the release.
     * </p>
     *
     * @param keyCode   the virtual key code representing the key released
     * @param scanCode  the hardware-dependent scan code of the key
     * @param modifiers a bitmask representing active modifier keys (e.g. Shift,
     *                  Ctrl, Alt)
     */
    public record KeyReleaseEvent(int keyCode, int scanCode, int modifiers) implements IEvent {
    }

    /**
     * Represents an event triggered when a character is typed.
     * <p>
     * This event provides the Unicode code point of the typed character
     * along with any active modifier keys at the time of typing.
     * </p>
     *
     * @param codePoint the Unicode character that was typed
     * @param modifiers a bitmask representing active modifier keys (e.g., Shift,
     *                  Ctrl, Alt)
     */
    public record CharTypeEvent(char codePoint, int modifiers) implements IEvent {
    }

    /**
     * Represents an event triggered when the user begins resizing a component.
     * <p>
     * This event provides the local mouse position at the start of the resize
     * action.
     * </p>
     *
     * @param mouseX the local x-coordinate of the mouse on the component
     * @param mouseY the local y-coordinate of the mouse on the component
     */
    public record ResizeBeginEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Represents an event triggered while the user is resizing a component.
     * <p>
     * This event provides the current mouse position along with the new
     * position and dimensions of the component being resized.
     * </p>
     *
     * @param mouseX    the current local x-coordinate of the mouse
     * @param mouseY    the current local y-coordinate of the mouse
     * @param newX      the new x-coordinate of the component after resizing
     * @param newY      the new y-coordinate of the component after resizing
     * @param newWidth  the new width of the component after resizing
     * @param newHeight the new height of the component after resizing
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
     * Represents an event triggered when the user finishes resizing a component.
     * <p>
     * This event provides the final mouse position, the new position and dimensions
     * of the component, and a flag that can be used to cancel the resize operation.
     * </p>
     *
     * @param mouseX    the final local x-coordinate of the mouse
     * @param mouseY    the final local y-coordinate of the mouse
     * @param newX      the new x-coordinate of the component after resizing
     * @param newY      the new y-coordinate of the component after resizing
     * @param newWidth  the new width of the component after resizing
     * @param newHeight the new height of the component after resizing
     * @param cancel    a mutable flag that can be set to {@code true} to cancel the
     *                  resize
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
     * Triggered when the component's position and size values have changed.
     * This event is similar to {@link ResizeEndEvent}, except that
     * {@link ResizeEndEvent}
     * is only triggered by user interactions, while this event reacts purely to
     * changes
     * in the values. The event is executed by calling {@link DLGuiComponent#setX},
     * {@link DLGuiComponent#setY}, {@link DLGuiComponent#setWidth} and
     * {@link DLGuiComponent#setHeight}.
     * 
     * @param newX      The new local x position on the parent component.
     * @param newY      The new local y position on the parent component.
     * @param newWidth  The new width.
     * @param newHeight The new height.
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
     * Represents an event triggered when the user begins dragging a component.
     * <p>
     * This event provides the local mouse position, the button used to initiate
     * the drag, and the list of components currently being dragged over.
     * </p>
     *
     * @param mouseX                the local x-coordinate of the mouse on the
     *                              component
     * @param mouseY                the local y-coordinate of the mouse on the
     *                              component
     * @param button                the mouse button used to start dragging
     * @param draggedOverComponents the components currently being dragged over
     */
    public record DragComponentBeginEvent(double mouseX, double mouseY, int button,
            List<DLGuiComponent> draggedOverComponents) implements IEvent {
    }

    /**
     * Represents an event triggered while a component is being dragged.
     * <p>
     * This event provides the current mouse position, the new position of the
     * dragged component, the drag offset, and the list of components being dragged
     * over.
     * </p>
     *
     * @param mouseX                the current local x-coordinate of the mouse
     * @param mouseY                the current local y-coordinate of the mouse
     * @param button                the mouse button used for dragging
     * @param newX                  the new x-coordinate of the dragged component
     * @param newY                  the new y-coordinate of the dragged component
     * @param dragX                 the horizontal offset of the drag since it began
     * @param dragY                 the vertical offset of the drag since it began
     * @param draggedOverComponents the components currently being dragged over
     */
    public record DragComponentEvent(double mouseX, double mouseY, int button, int newX, int newY,
            double dragX, double dragY,
            List<DLGuiComponent> draggedOverComponents) implements IEvent {
    }

    /**
     * Represents an event triggered when the user ends dragging a component.
     * <p>
     * This event provides the final mouse position, the new position of the
     * dragged component, the list of components dragged over, and a flag that
     * can be used to cancel the drop.
     * </p>
     *
     * @param mouseX                the final local x-coordinate of the mouse
     * @param mouseY                the final local y-coordinate of the mouse
     * @param button                the mouse button used for dragging
     * @param newX                  the new x-coordinate of the dragged component
     * @param newY                  the new y-coordinate of the dragged component
     * @param draggedOverComponents the components that were dragged over
     * @param cancel                a mutable flag that can be set to {@code true}
     *                              to cancel the drop
     */
    public record DragComponentEndEvent(double mouseX, double mouseY, int button, int newX, int newY,
            List<DLGuiComponent> draggedOverComponents, MutableBoolean cancel) implements IEvent {
    }

    /**
     * Represents an event triggered when the dragged components first enter
     * the bounds of this component.
     *
     * @param other  the components being entered
     * @param mouseX the local x-coordinate of the mouse
     * @param mouseY the local y-coordinate of the mouse
     * @param button the mouse button used for dragging
     */
    public record DragComponentOverBeginEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button)
            implements IEvent {
    }

    /**
     * Represents an event triggered while the dragged components are over
     * this component.
     *
     * @param other  the components currently being hovered over
     * @param mouseX the local x-coordinate of the mouse
     * @param mouseY the local y-coordinate of the mouse
     * @param button the mouse button used for dragging
     */
    public record DragComponentOverEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button)
            implements IEvent {
    }

    /**
     * Represents an event triggered when the dragged components leave
     * the bounds of this component.
     *
     * @param other  the components being exited
     * @param mouseX the local x-coordinate of the mouse
     * @param mouseY the local y-coordinate of the mouse
     * @param button the mouse button used for dragging
     */
    public record DragComponentOverEndEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button)
            implements IEvent {
    }

    /**
     * Represents an event triggered while this component is being dragged
     * over other components.
     *
     * @param other  the components currently being hovered over
     * @param mouseX the local x-coordinate of the mouse
     * @param mouseY the local y-coordinate of the mouse
     * @param button the mouse button used for dragging
     */
    public record DraggingOverEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button)
            implements IEvent {
    }

    /**
     * Represents an event triggered when a dragged component is dropped
     * onto this component.
     *
     * @param other  the component onto which the dragged component was dropped
     * @param mouseX the local x-coordinate of the mouse
     * @param mouseY the local y-coordinate of the mouse
     */
    public record DropComponentEvent(DLGuiComponent other, double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Represents an event triggered when files are dragged and dropped
     * onto a component.
     *
     * @param paths  the list of file paths that were dropped
     * @param mouseX the local x-coordinate of the mouse
     * @param mouseY the local y-coordinate of the mouse
     */
    public record DragAndDropFilesEvent(List<Path> paths, double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Represents an event triggered when the visibility of a component changes.
     * <p>
     * This event indicates whether the component has become visible or hidden.
     * </p>
     *
     * @param visible {@code true} if the component is now visible,
     *                {@code false} if it is hidden
     */
    public record VisibilityChangedEvent(boolean visible) implements IEvent {
    }

    /**
     * Represents an event triggered when the enabled state of a component changes.
     * <p>
     * This event indicates whether the component has been enabled or disabled.
     * </p>
     *
     * @param enabled {@code true} if the component is enabled,
     *                {@code false} if it is disabled
     */
    public record EnabledChangedEvent(boolean enabled) implements IEvent {
    }

    /**
     * Represents an event triggered when the resizable state of a component
     * changes.
     * <p>
     * This event indicates whether the component can be resized.
     * </p>
     *
     * @param resizable {@code true} if the component is resizable,
     *                  {@code false} otherwise
     */
    public record ResizableChangedEvent(boolean resizable) implements IEvent {
    }

    /**
     * Represents an event triggered when the movable state of a component changes.
     * <p>
     * This event indicates whether the component can be moved.
     * </p>
     *
     * @param movable {@code true} if the component is movable,
     *                {@code false} otherwise
     */
    public record MovableChangedEvent(boolean movable) implements IEvent {
    }

    /**
     * Represents an event triggered when the layout of a component is updated.
     * <p>
     * This event is not cancellable and provides the phase of the update process.
     * </p>
     *
     * @param order the phase of the layout update
     */
    @NotCancellable
    public record ScreenLayoutUpdatedEvent(Phase order) implements IEvent {
    }
    
    @NotCancellable
    public record ComponentLayoutUpdatedEvent(LayoutResult layoutResult) implements IEvent {
    }

    /**
     * Represents an event triggered when all child components of a parent
     * are cleared.
     * <p>
     * This event provides the phase of the clearing process and a flag
     * that can be used to cancel the operation.
     * </p>
     *
     * @param order  the phase of the clearing process
     * @param cancel a mutable flag that can be set to {@code true} to cancel the
     *               operation
     */
    public record ComponentsClearEvent(Phase order, MutableBoolean cancel) implements IEvent {
    }

    /**
     * Represents an event triggered when the parent of a component changes.
     * <p>
     * This event provides both the old and new parent components, if present.
     * </p>
     *
     * @param oldParent the previous parent component, or empty if none
     * @param newParent the new parent component, or empty if none
     */
    public record ParentChangedEvent(Optional<DLGuiComponent> oldParent, Optional<DLGuiComponent> newParent)
            implements IEvent {
    }

    /**
     * Represents an event triggered when the window manager of a component changes.
     * <p>
     * This event is not cancellable and provides both the old and new window
     * managers.
     * </p>
     *
     * @param oldWindowManager the previous window manager
     * @param newWindowManager the new window manager
     */
    @NotCancellable
    public record WindowManagerChangeEvent(DLWindowManager oldWindowManager, DLWindowManager newWindowManager)
            implements IEvent {
    }

    /**
     * Represents an event triggered before a component is rendered.
     * <p>
     * This event provides the graphics context, mouse position, render layer,
     * and the bounds of the rendering area.
     * </p>
     *
     * @param graphics     the graphics context used for rendering
     * @param mouseX       the current local x-coordinate of the mouse
     * @param mouseY       the current local y-coordinate of the mouse
     * @param layer        the render layer being drawn
     * @param renderBounds the bounds of the rendering area
     */
    public record RenderPreEvent(DLGuiGraphics graphics, double mouseX, double mouseY,
            RenderLayer layer, Rectangle renderBounds) implements IEvent {
    }

    /**
     * Represents an event triggered during the rendering of a component.
     * <p>
     * This event provides the graphics context, mouse position, render layer,
     * and the bounds of the rendering area.
     * </p>
     *
     * @param graphics     the graphics context used for rendering
     * @param mouseX       the current local x-coordinate of the mouse
     * @param mouseY       the current local y-coordinate of the mouse
     * @param layer        the render layer being drawn
     * @param renderBounds the bounds of the rendering area
     */
    public record RenderEvent(DLGuiGraphics graphics, double mouseX, double mouseY,
            RenderLayer layer, Rectangle renderBounds) implements IEvent {
    }

    /**
     * Represents an event triggered after a component has been rendered.
     * <p>
     * This event provides the graphics context, mouse position, render layer,
     * and the bounds of the rendering area.
     * </p>
     *
     * @param graphics     the graphics context used for rendering
     * @param mouseX       the current local x-coordinate of the mouse
     * @param mouseY       the current local y-coordinate of the mouse
     * @param layer        the render layer that was drawn
     * @param renderBounds the bounds of the rendering area
     */
    public record RenderPostEvent(DLGuiGraphics graphics, double mouseX, double mouseY,
            RenderLayer layer, Rectangle renderBounds) implements IEvent {
    }

    
    public record RenderOnScreenEvent(DLGuiGraphics graphics, double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Represents an event triggered when a component or window is closed.
     * <p>
     * This event is not cancellable and indicates the termination of the component.
     * </p>
     */
    @NotCancellable
    public record CloseEvent() implements IEvent {
    }

}
