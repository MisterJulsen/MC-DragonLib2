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
     * <p>This event represents the initial press action for any given mouse button
     * that occurs while the pointer is over a component. Listeners typically use
     * this event to begin interactions such as selection, focus changes, or
     * starting drag operations. This record is immutable and intended to be a
     * simple data carrier for the GUI event system.</p>
     *
     * @param mouseX the x coordinate of the mouse relative to the component's local coordinate space at the time of press
     * @param mouseY the y coordinate of the mouse relative to the component's local coordinate space at the time of press
     * @param button an integer identifying the mouse button pressed (platform-dependent mapping; e.g. 0 = left, 1 = right)
     */
    public record MouseDownEvent(double mouseX, double mouseY, int button) implements IEvent {
    }

    /**
     * Event fired repeatedly while a mouse button is held down on a component.
     *
     * <p>This event is emitted after an initial hold delay and then periodically
     * while the button remains depressed. It is useful for implementing auto-repeat
     * behaviors (e.g. continuous scrolling or incremental adjustments) and for
     * tracking the duration of a press. Implementations should expect this event
     * to be fired once per tick after the initial delay.</p>
     *
     * @param mouseX the current x coordinate of the mouse in the component's local space
     * @param mouseY the current y coordinate of the mouse in the component's local space
     * @param button the mouse button index that is being held
     * @param ticks  the number of update ticks that have elapsed since the button was first pressed (including the current tick)
     */
    public record MouseHoldDownEvent(double mouseX, double mouseY, int button, int ticks) implements IEvent {
    }

    /**
     * Event fired when the mouse cursor enters a component's interactive area.
     *
     * <p>This event signals that the cursor transitioned from outside to inside
     * the component's interactive bounds. Typical uses include showing hover
     * states, tooltips, or prefetching resources related to the component.</p>
     *
     * @param mouseX the local x coordinate inside the component where the enter occurred
     * @param mouseY the local y coordinate inside the component where the enter occurred
     */
    public record MouseEnterEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Event fired when the mouse cursor leaves a component's interactive area.
     *
     * <p>Occurs when the pointer moves from inside the component bounds to outside.
     * Common handlers hide hover states, tooltips, or cancel hover-specific actions.</p>
     *
     * @param mouseX the local x coordinate at the time the pointer left the component bounds
     * @param mouseY the local y coordinate at the time the pointer left the component bounds
     */
    public record MouseLeaveEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Event fired when the mouse is moved within a component's interactive area.
     *
     * <p>This event is delivered whenever the cursor moves while over the component.
     * It is intended for pointer-tracking features such as dynamic tooltips,
     * tracking hover coordinates, or interactive drawing cursors.</p>
     *
     * @param mouseX the current x coordinate of the mouse in the component's local coordinate space
     * @param mouseY the current y coordinate of the mouse in the component's local coordinate space
     */
    public record MouseMoveEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Event fired when a mouse button is released over a component.
     *
     * <p>This event indicates the completion of a press interaction. It is suitable
     * for triggering click semantics, ending drag operations, or committing changes
     * started by a MouseDownEvent.</p>
     *
     * @param mouseX the local x coordinate where the release occurred
     * @param mouseY the local y coordinate where the release occurred
     * @param button the mouse button index that was released
     */
    public record MouseReleaseEvent(double mouseX, double mouseY, int button) implements IEvent {
    }

    /**
     * Generic event fired for any mouse button press.
     *
     * <p>Use this when handlers need to react to any button press and cannot rely
     * on the platform mapping for specific convenience events. For common left-click
     * semantics prefer {@link ClickEvent}.</p>
     *
     * @param mouseX the x coordinate of the press in local component coordinates
     * @param mouseY the y coordinate of the press in local component coordinates
     * @param button the mouse button index pressed
     */
    public record MousePressedEvent(double mouseX, double mouseY, int button) implements IEvent {
    }

    /**
     * Convenience event representing a left-button click on a component.
     *
     * <p>This event is typically emitted when a press and release sequence is
     * detected for the primary button and can be used to perform default actions
     * such as activation or selection.</p>
     *
     * @param mouseX the local x coordinate of the click
     * @param mouseY the local y coordinate of the click
     */
    public record ClickEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Convenience event representing a right-click on a component.
     *
     * <p>Emitted for context-menu semantics or secondary interactions mapped to the
     * platform-specific right mouse action.</p>
     *
     * @param mouseX the local x coordinate of the right-click
     * @param mouseY the local y coordinate of the right-click
     */
    public record RightClickEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Event for multiple successive clicks (for example double- or triple-click).
     *
     * <p>Handlers can use this event to perform different actions depending on the
     * number of sequential clicks detected within the configured multi-click timeout.</p>
     *
     * @param mouseX     the local x coordinate of the latest click in the sequence
     * @param mouseY     the local y coordinate of the latest click in the sequence
     * @param button     the mouse button index used for the clicks
     * @param clickCount the number of consecutive clicks in this sequence (1 = single, 2 = double, etc.)
     */
    public record MultiClickEvent(double mouseX, double mouseY, int button, byte clickCount) implements IEvent {
    }

    /**
     * Event fired when the mouse wheel or equivalent scrolling input occurs over a component.
     *
     * <p>The delta values represent the amount scrolled and may vary in scale
     * depending on input device. Components should interpret positive/negative
     * values according to the coordinate conventions used by the system.</p>
     *
     * @param mouseX the local x coordinate of the cursor during the scroll event
     * @param mouseY the local y coordinate of the cursor during the scroll event
     * @param deltaX horizontal scroll delta (device-dependent units)
     * @param deltaY vertical scroll delta (device-dependent units)
     */
    public record ScrollEvent(double mouseX, double mouseY, double deltaX, double deltaY) implements IEvent {
    }

    /**
     * Event fired once per engine/update tick for components that require periodic updates.
     *
     * <p>This tick event is independent of input and is useful for animations,
     * timeout checks, or other periodic logic that must run regularly while the
     * component is active.</p>
     */
    public record TickEvent() implements IEvent {
    }

    /**
     * Event fired after a child component was added to a parent.
     *
     * <p>Listeners receive a reference to the added child and can use this to
     * initialize state, attach listeners, or re-run layout depending on the
     * parent implementation's semantics.</p>
     *
     * @param children the component instance that was added as a child
     */
    public record ComponentAddedEvent(List<DLGuiComponent> children) implements IEvent {
    }

    /**
     * Event fired after a child component was removed from a parent.
     *
     * <p>Emitted when a component is detached from its parent. Typical handlers
     * perform cleanup, detach resources or update layout and focus state.</p>
     *
     * @param children the component instance that was removed
     */
    public record ComponentRemovedEvent(List<DLGuiComponent> children) implements IEvent {
    }

    /**
     * Event indicating a change of focus state for a component.
     *
     * <p>Used to notify listeners that a component either gained or lost input
     * focus. Handlers may update visual focus rings, keyboard routing or other
     * dependent state.</p>
     *
     * @param focus true when the component has gained focus; false when it lost focus
     */
    public record FocusChangedEvent(boolean focus) implements IEvent {
    }

    /**
     * Event fired when the user begins dragging with a mouse button over a component.
     *
     * <p>Represents the initiation of a drag gesture. Typically followed by
     * DragEvent records as the user moves the pointer while holding the button.</p>
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
     * <p>This record provides both the screen-space and local-space origins of
     * the drag as well as the current offsets relative to the drag origin. It is
     * useful for implementations that must reconcile component or window movement
     * with cursor movement across coordinate spaces.</p>
     *
     * @param mouseX             current mouse x in local component coordinates
     * @param mouseY             current mouse y in local component coordinates
     * @param button             the mouse button index performing the drag
     * @param screenMouseOriginX the screen-space x coordinate where the drag began
     * @param screenMouseOriginY the screen-space y coordinate where the drag began
     * @param localMouseOriginX  the component-local x coordinate where the drag began
     * @param localMouseOriginY  the component-local y coordinate where the drag began
     * @param dragX              the current horizontal offset from localMouseOriginX to the current mouse x
     * @param dragY              the current vertical offset from localMouseOriginY to the current mouse y
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
     * <p>Delivers final coordinates and origin information for the drag.
     * Handlers typically use this to commit or cancel drop operations and to
     * restore transient state used during dragging.</p>
     *
     * @param mouseX             final mouse x in the component's local coordinates
     * @param mouseY             final mouse y in the component's local coordinates
     * @param button             the mouse button index that was used for dragging
     * @param screenMouseOriginX screen-space x coordinate where the drag was started
     * @param screenMouseOriginY screen-space y coordinate where the drag was started
     * @param localMouseOriginX  local-space x coordinate where the drag was started
     * @param localMouseOriginY  local-space y coordinate where the drag was started
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
     * <p>Emitted when a physical key is depressed. This event is low-level and
     * contains both a logical key code and the hardware scan code; modifiers is
     * a bitmask representing active modifier keys (shift, ctrl, alt, etc.).</p>
     *
     * @param keyCode  platform-independent key code identifying the pressed key
     * @param scanCode hardware/platform scan code for the key (raw device code)
     * @param modifiers a bitmask of modifiers active at the time of the key press
     */
    public record KeyPressEvent(int keyCode, int scanCode, int modifiers) implements IEvent {
    }

    /**
     * Keyboard key-release event providing key code, hardware scan code and modifiers.
     *
     * <p>Emitted when a previously pressed key is released. Modifiers reflect the
     * state at release time.</p>
     *
     * @param keyCode  platform-independent key code identifying the released key
     * @param scanCode hardware/platform scan code for the key (raw device code)
     * @param modifiers a bitmask of modifiers active at the time of the key release
     */
    public record KeyReleaseEvent(int keyCode, int scanCode, int modifiers) implements IEvent {
    }

    /**
     * Character typing event delivering a Unicode code point and active modifiers.
     *
     * <p>This event conveys character input as UTF-16 code units (char) after
     * platform input processing. It is intended for text input handling and
     * receives modifier state to allow for alternate behaviors.</p>
     *
     * @param codePoint the typed Unicode character (Java char)
     * @param modifiers bitmask of active modifier keys when the character event occurred
     */
    public record CharTypeEvent(char codePoint, int modifiers) implements IEvent {
    }

    /**
     * Event fired when a resize operation is started by the user.
     *
     * <p>Indicates the beginning of a user-initiated resize gesture. The initial
     * mouse coordinates are provided for possible use in tracking or calculating
     * constraints during subsequent ResizeEvents.</p>
     *
     * @param mouseX the local x coordinate where the resize was initiated
     * @param mouseY the local y coordinate where the resize was initiated
     */
    public record ResizeBeginEvent(double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Event fired while a component is being resized by the user.
     *
     * <p>This event provides the proposed new position and size during an active
     * resize gesture. Handlers may use this to preview constraints or to update
     * live layout feedback. This record is a proposal and can be validated or
     * canceled later via ResizeEndEvent.</p>
     *
     * @param mouseX    current mouse x in the component's local coordinates during the resize
     * @param mouseY    current mouse y in the component's local coordinates during the resize
     * @param newX      proposed new x position for the component (typically local coordinates)
     * @param newY      proposed new y position for the component (typically local coordinates)
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
     * <p>This event represents the final proposed geometry for the component.
     * Listeners are given a MutableBoolean 'cancel' flag which they can set to
     * true to prevent the new size/position from being applied. This allows for
     * validation and vetoing of user-driven changes.</p>
     *
     * @param mouseX    final local mouse x at completion
     * @param mouseY    final local mouse y at completion
     * @param newX      the final proposed x position for the component
     * @param newY      the final proposed y position for the component
     * @param newWidth  the final proposed width for the component
     * @param newHeight the final proposed height for the component
     * @param cancel    a mutable boolean which listeners can set to true to cancel applying the proposed resize
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
     * Event fired when internal position and size values change.
     *
     * <p>This event is emitted when the component's X/Y/Width/Height values are
     * updated programmatically or by user interaction. Utility methods are
     * provided to determine which axes changed.</p>
     *
     * @param oldX old local x position before the change
     * @param newX new local x position after the change
     * @param oldY old local y position before the change
     * @param newY new local y position after the change
     * @param oldWidth old width prior to change
     * @param newWidth new width after change
     * @param oldHeight old height prior to change
     * @param newHeight new height after change
     */
    public record ComponentPosAndSizeChanged(int oldX, int newX, int oldY, int newY, int oldWidth, int newWidth,
            int oldHeight, int newHeight) implements IEvent {
        /**
         * Returns true if the width value differs between old and new state.
         *
         * @return true when width changed; false otherwise
         */
        public boolean widthChanged() {
            return oldWidth != newWidth;
        }

        /**
         * Returns true if the height value differs between old and new state.
         *
         * @return true when height changed; false otherwise
         */
        public boolean heightChanged() {
            return oldHeight != newHeight;
        }

        /**
         * Returns true if the x coordinate differs between old and new state.
         *
         * @return true when x changed; false otherwise
         */
        public boolean xChanged() {
            return oldX != newX;
        }

        /**
         * Returns true if the y coordinate differs between old and new state.
         *
         * @return true when y changed; false otherwise
         */
        public boolean yChanged() {
            return oldY != newY;
        }

        /**
         * Returns true if either x or y changed.
         *
         * @return true when position changed; false otherwise
         */
        public boolean positionChanged() {
            return xChanged() || yChanged();
        }

        /**
         * Returns true if either width or height changed.
         *
         * @return true when size changed; false otherwise
         */
        public boolean sizeChanged() {
            return widthChanged() || heightChanged();
        }
    }

    /**
     * Event fired when a component is being dragged by the user (component as drag source).
     *
     * <p>This event describes the start of a component-as-draggable operation and
     * includes the list of components currently underneath the dragged item which
     * can be used to compute drop targets or highlight possible receivers.</p>
     *
     * @param mouseX                current mouse x in the dragged component's local coordinates
     * @param mouseY                current mouse y in the dragged component's local coordinates
     * @param button                mouse button index used for dragging
     * @param draggedOverComponents list of components currently being hovered over by the dragged component
     */
    public record DragComponentBeginEvent(double mouseX, double mouseY, int button,
            List<DLGuiComponent> draggedOverComponents) implements IEvent {
    }

    /**
     * Event fired repeatedly while a component is being dragged (as source).
     *
     * <p>Provides the proposed new local position for the dragged component as
     * well as the raw drag offsets and the components currently under the drag
     * point. Listeners can use this to snap or constrain movement and to update
     * potential drop targets.</p>
     *
     * @param mouseX                current mouse x relative to the dragged component
     * @param mouseY                current mouse y relative to the dragged component
     * @param button                the mouse button index being used to drag
     * @param newX                  proposed new local x position for the dragged component
     * @param newY                  proposed new local y position for the dragged component
     * @param dragX                 cumulative horizontal drag offset since drag start
     * @param dragY                 cumulative vertical drag offset since drag start
     * @param draggedOverComponents components currently under the dragged element
     */
    public record DragComponentEvent(double mouseX, double mouseY, int button, int newX, int newY,
            double dragX, double dragY,
            List<DLGuiComponent> draggedOverComponents) implements IEvent {
    }

    /**
     * Event fired when a component drag operation ends; may be used to drop onto targets.
     *
     * <p>The list of components under the dragged component at drop time is
     * provided. Listeners receive a MutableBoolean 'cancel' to reject the drop,
     * allowing for custom validation before the final placement occurs.</p>
     *
     * @param mouseX                final mouse x in local coordinates at drop time
     * @param mouseY                final mouse y in local coordinates at drop time
     * @param button                mouse button index used for dragging
     * @param newX                  final local x position proposed for the dragged component
     * @param newY                  final local y position proposed for the dragged component
     * @param draggedOverComponents components that were beneath the dragged component when dropped
     * @param cancel                a mutable boolean that can be set to true by listeners to cancel the drop
     */
    public record DragComponentEndEvent(double mouseX, double mouseY, int button, int newX, int newY,
            List<DLGuiComponent> draggedOverComponents, MutableBoolean cancel) implements IEvent {
    }

    /**
     * Event fired when an external drag operation enters this component's bounds.
     *
     * <p>Represents the start of an external drag-over operation; useful for
     * indicating potential drop targets and initializing hover feedback.</p>
     *
     * @param other  list of components being dragged that entered this component's area
     * @param mouseX mouse x in local coordinates at the event moment
     * @param mouseY mouse y in local coordinates at the event moment
     * @param button mouse button index used to perform the external drag
     */
    public record DragComponentOverBeginEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button)
            implements IEvent {
    }

    /**
     * Event fired continuously while external dragged components are over this component.
     *
     * <p>Use this to update highlight state, compute potential insertion indices,
     * or display live feedback during a drag-over.</p>
     *
     * @param other  list of components currently dragged over this component
     * @param mouseX local mouse x coordinate
     * @param mouseY local mouse y coordinate
     * @param button mouse button index used for the external drag
     */
    public record DragComponentOverEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button)
            implements IEvent {
    }

    /**
     * Event fired when external dragged components leave this component's bounds.
     *
     * <p>Indicates the termination of a drag-over interaction for the items
     * represented in {@code other}. Handlers typically clear hover indicators
     * and other visual states established in DragComponentOverBeginEvent.</p>
     *
     * @param other  list of components leaving this component's area
     * @param mouseX local mouse x at the event moment
     * @param mouseY local mouse y at the event moment
     * @param button mouse button used for the external drag
     */
    public record DragComponentOverEndEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button)
            implements IEvent {
    }

    /**
     * Event fired while a component is being dragged over several targets; a more generic variant.
     *
     * <p>This general-purpose event aggregates multiple hovered targets in {@code other}
     * and is intended for higher-level drag management logic that does not need
     * to distinguish between begin/over/end phases for each target individually.</p>
     *
     * @param other  components currently being hovered by the dragged content
     * @param mouseX local mouse x coordinate
     * @param mouseY local mouse y coordinate
     * @param button mouse button index used to drag
     */
    public record DraggingOverEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button)
            implements IEvent {
    }

    /**
     * Event indicating a component has been dropped onto another component.
     *
     * <p>Used when a component (the current event recipient) receives another
     * component as a drop. The {@code other} parameter references the dropped
     * component so that the recipient can handle insertion, acceptance, or rejection.</p>
     *
     * @param other  the dropped component being delivered to the target
     * @param mouseX local mouse x coordinate at drop time
     * @param mouseY local mouse y coordinate at drop time
     */
    public record DropComponentEvent(DLGuiComponent other, double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Event representing a list of file system paths dropped onto a component.
     *
     * <p>Delivered when the user drags file entries from the OS or another source
     * and releases them over the component. Handlers should validate file paths,
     * check for supported types and perform any asynchronous processing as needed.</p>
     *
     * @param paths  an immutable list of file system Paths dropped onto the component
     * @param mouseX local mouse x coordinate at drop time
     * @param mouseY local mouse y coordinate at drop time
     */
    public record DragAndDropFilesEvent(List<Path> paths, double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Event indicating visibility has changed for a component.
     *
     * <p>Listeners can use this notification to start or stop animations, suspend
     * timers, or free resources when a component becomes hidden.</p>
     *
     * @param visible true when the component became visible; false when it was hidden
     */
    public record VisibilityChangedEvent(boolean visible) implements IEvent {
    }

    /**
     * Event indicating the enabled state of a component changed.
     *
     * <p>When a component is disabled it typically should not respond to input.
     * Handlers may update appearance and interaction behavior based on this state.</p>
     *
     * @param enabled true when the component is enabled; false when it is disabled
     */
    public record EnabledChangedEvent(boolean enabled) implements IEvent {
    }

    /**
     * Event indicating whether a component became resizable or not.
     *
     * <p>Handlers may toggle UI affordances or constraints when resizability changes.</p>
     *
     * @param resizable true when component becomes resizable; false otherwise
     */
    public record ResizableChangedEvent(boolean resizable) implements IEvent {
    }

    /**
     * Event indicating whether a component became movable or not.
     *
     * <p>Used to enable or disable move handles and related behaviors.</p>
     *
     * @param movable true when the component becomes movable; false otherwise
     */
    public record MovableChangedEvent(boolean movable) implements IEvent {
    }

    /**
     * Non-cancellable event used to indicate the start/finish of a screen layout update.
     *
     * <p>This event is intended to notify listeners about layout pass phases.
     * Because layout changes are part of the GUI system's internal consistency,
     * listeners cannot cancel this event.</p>
     *
     * @param order the phase (PRE/POST) of the layout update
     */
    @NotCancellable
    public record ScreenLayoutUpdatedEvent(Phase order) implements IEvent {
    }
    
    /**
     * Non-cancellable event containing the result produced by a layout manager arrange operation.
     *
     * <p>Contains the LayoutResult computed by a layout manager for a component or container.
     * Listeners can inspect computed constraints, positions and sizes but cannot
     * alter the result via this notification.</p>
     *
     * @param layoutResult the computed layout result produced by the layout manager
     */
    @NotCancellable
    public record ComponentLayoutUpdatedEvent(LayoutResult layoutResult) implements IEvent {
    }

    /**
     * Event fired during clearing of children. Includes a phase and a mutable cancel flag.
     *
     * <p>During the PRE phase listeners may set {@code cancel} to true to abort the
     * clearing operation. POST phase is delivered after children were removed.
     * This allows for validation or vetoing of destructive operations.</p>
     *
     * @param order the phase of the clearing operation (PRE or POST)
     * @param cancel a MutableBoolean that can be set to true during PRE to cancel the clear operation
     */
    public record ComponentsClearEvent(Phase order, MutableBoolean cancel) implements IEvent {
    }

    /**
     * Event fired when a component's parent reference changes.
     *
     * <p>Listeners are provided with Optional references for the old and new
     * parent to handle re-parenting logic, reattachment of listeners, or layout updates.</p>
     *
     * @param oldParent an Optional containing the previous parent if present
     * @param newParent an Optional containing the new parent if present
     */
    public record ParentChangedEvent(Optional<DLGuiComponent> oldParent, Optional<DLGuiComponent> newParent)
            implements IEvent {
    }

    /**
     * Non-cancellable event notifying listeners of a change to a component's window manager assignment.
     *
     * <p>This notification indicates that the component has been attached to or
     * detached from a DLWindowManager. The event is non-cancellable because it
     * reflects an internal routing change that should always be observable.</p>
     *
     * @param oldWindowManager the previously assigned window manager, or null if none
     * @param newWindowManager the newly assigned window manager, or null if removed
     */
    @NotCancellable
    public record WindowManagerChangeEvent(DLWindowManager oldWindowManager, DLWindowManager newWindowManager)
            implements IEvent {
    }

    /**
     * Event fired immediately before rendering a component (per-layer).
     *
     * <p>Delivered prior to the render pass for the specified layer. Listeners
     * can use this to prepare temporary graphics state or to adjust rendering
     * bounds. Modifying the provided graphics instance may affect the subsequent draw call.</p>
     *
     * @param graphics     the graphics context used for rendering; may be manipulated by listeners
     * @param mouseX       current mouse x in the component's local coordinates (may be adjusted by scroll/scale)
     * @param mouseY       current mouse y in the component's local coordinates (may be adjusted by scroll/scale)
     * @param layer        the render layer that is about to be processed
     * @param renderBounds the clipping bounds to be applied for this render pass
     */
    public record RenderPreEvent(DLGuiGraphics graphics, double mouseX, double mouseY,
            RenderLayer layer, Rectangle renderBounds) implements IEvent {
    }

    /**
     * Event fired while a render layer is being processed for a component.
     *
     * <p>Listeners receive the graphics context and may draw additional content
     * or perform overlay rendering scoped to the current layer and clipping bounds.</p>
     *
     * @param graphics     the graphics context used to draw during this layer
     * @param mouseX       the current mouse x in local coordinates
     * @param mouseY       the current mouse y in local coordinates
     * @param layer        the render layer that is currently being drawn
     * @param renderBounds clipping bounds that limit drawing for this render pass
     */
    public record RenderEvent(DLGuiGraphics graphics, double mouseX, double mouseY,
            RenderLayer layer, Rectangle renderBounds) implements IEvent {
    }

    /**
     * Event fired after rendering of a layer has completed for a component.
     *
     * <p>Useful for cleaning up temporary graphics state or performing post-process
     * overlays that must appear after the component's own drawing has executed.</p>
     *
     * @param graphics     the graphics context that was used to render the layer
     * @param mouseX       the mouse x coordinate that applied during rendering
     * @param mouseY       the mouse y coordinate that applied during rendering
     * @param layer        the render layer that was just drawn
     * @param renderBounds the clipping bounds that were used for the pass
     */
    public record RenderPostEvent(DLGuiGraphics graphics, double mouseX, double mouseY,
            RenderLayer layer, Rectangle renderBounds) implements IEvent {
    }

    /**
     * Event used for on-screen overlays rendering (tooltips, screen-space UI).
     *
     * <p>This event is intended for UI elements that are drawn in screen space
     * rather than component-local space, such as tooltips or HUD overlays.</p>
     *
     * @param graphics the graphics context for screen-space drawing
     * @param mouseX   current mouse x in screen coordinates
     * @param mouseY   current mouse y in screen coordinates
     */
    public record RenderOnScreenEvent(DLGuiGraphics graphics, double mouseX, double mouseY) implements IEvent {
    }

    /**
     * Non-cancellable event signalling that a component or window is being closed.
     *
     * <p>Use this event for cleanup and resource disposal. Because closing is a
     * structural operation of the GUI system, this event cannot be cancelled by listeners.</p>
     */
    @NotCancellable
    public record CloseEvent() implements IEvent {
    }

}
