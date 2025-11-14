package de.mrjulsen.mcdragonlib.client.gui.events;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableBoolean;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.events.IEvent.NotCancellable;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

/**
 * A collection of all standard GUI events, which are supported and implemented by all Dragonlib GUI Components {@link DLGuiComponent}.
 */
public final class DLGuiStandardEvents {
    private DLGuiStandardEvents() {}

    /**
     * Triggered when the mouse button is pressed. PLEASE NOTE! This event is only
     * triggered ONCE and not as long as the mouse button is down; The
     * {@link MouseHoldDownEvent} event can be used for this.
     * @param mouseX The local mouse x position on the component.
     * @param mouseY The local mouse y position on the component.
     * @param button The pressed mouse button.
     */
    public record MouseDownEvent(double mouseX, double mouseY, int button) implements IEvent {}
    
    /**
     * Triggered while the mouse button is pressed. The event has an initial delay of
     * {@link DLGuiComponent#MOUSE_DOWN_INITIAL_DELAY} ticks after the first trigger
     * (as is common in many GUI frameworks). After that, the event is triggered once
     * every tick as long as a mouse button is pressed.
     * @param mouseX The local mouse x position on the component.
     * @param mouseY The local mouse y position on the component.
     * @param button The pressed mouse button.
     * @param ticks The number of ticks the mouse button is pressed for. This is NOT
     * the number of times this event has been triggered.
     */
    public record MouseHoldDownEvent(double mouseX, double mouseY, int button, int ticks) implements IEvent {}

    /**
     * Triggered when the mouse enters the component's bounds.
     * @param mouseX The local mouse x position on the component.
     * @param mouseY The local mouse Y position on the component.
     */
    public record MouseEnterEvent(double mouseX, double mouseY) implements IEvent {}
    
    /**
     * Triggered when the mouse leaves the component's bounds.
     * @param mouseX The local mouse x position on the component.
     * @param mouseY The local mouse Y position on the component.
     */
    public record MouseLeaveEvent(double mouseX, double mouseY) implements IEvent {}
    
    /**
     * Triggered when the mouse is moved above the component's bounds.
     * @param mouseX The local mouse x position on the component.
     * @param mouseY The local mouse Y position on the component.
     */
    public record MouseMoveEvent(double mouseX, double mouseY) implements IEvent {}
    
    /**
     * Triggered when the mouse leaves the component's bounds.
     * @param mouseX The local mouse x position on the component.
     * @param mouseY The local mouse Y position on the component.
     */
    public record MouseReleaseEvent(double mouseX, double mouseY, int button) implements IEvent {}
    
    /**
     * Triggered when the a mouse button is pressed. This event is very similar to {@link ClickEvent} with the difference, that this event is fired every time any mouse button is pressed.
     * @param mouseX The local mouse x position on the component.
     * @param mouseY The local mouse Y position on the component.
     * @param button The mouse button pressed.
     */
    public record MousePressedEvent(double mouseX, double mouseY, int button) implements IEvent {}
    
    /**
     * Triggered when the left mouse button is pressed. This event is a simplified version of {@link MousePressedEvent}. For simplified right click detection, check {@link RightClickEvent}.
     * @param mouseX The local mouse x position on the component.
     * @param mouseY The local mouse Y position on the component.
     */
    public record ClickEvent(double mouseX, double mouseY) implements IEvent {}
    
    /**
     * Triggered when the right mouse button is pressed. This event is a simplified version of {@link MousePressedEvent}. For simplified left click detection, check {@link ClickEvent}.
     * @param mouseX The local mouse x position on the component.
     * @param mouseY The local mouse Y position on the component.
     */
    public record RightClickEvent(double mouseX, double mouseY) implements IEvent {}
    
    /**
     * This event is triggered when a mouse button is clicked multiple times in succession. It can be used for double-click detection, for example, but allows for significantly
     * more consecutive mouse clicks (e.g. triple clicks).
     * @param mouseX The local mouse x position on the component.
     * @param mouseY The local mouse Y position on the component.
     * @param button The mouse button pressed.
     * @param clickCount How many times the button has been clicked in a row.
     */
    public record MultiClickEvent(double mouseX, double mouseY, int button, byte clickCount) implements IEvent {}

    /**
     * Triggered when the user scrolls with the mouse wheel.
     * @param mouseX The local mouse x position on the component.
     * @param mouseY The local mouse y position on the component.
     * @param deltaX The scroll value on the x axis.
     * @param deltaY The scroll value on the y axis.
     */
    public record ScrollEvent(double mouseX, double mouseY, double deltaX, double deltaY) implements IEvent {}
    
    /**
     * Triggered every game tick.
     */
    public record TickEvent() implements IEvent {}
    
    /**
     * Triggered every time a new component is added to this component.
     * @param child The new component.
     */
    public record ComponentAddedEvent(DLGuiComponent child) implements IEvent {}
    
    /**
     * Triggered every time a child component is removed from this component.
     * @param child The child component to be removed.
     */
    public record ComponentRemovedEvent(DLGuiComponent child) implements IEvent {}
    
    /**
     * Triggered when the focus of this component changes.
     * @param focus The new focus state.
     */
    public record FocusChangedEvent(boolean focus) implements IEvent {}
    
    /**
     * Triggered every time a new component is added to this component.
     * @param child The new component.
     */
    public record DragBeginEvent(double mouseX, double mouseY, int button) implements IEvent {}
    public record DragEvent(double mouseX, double mouseY, int button, double screenMouseOriginX, double screenMouseOriginY, double localMouseOriginX, double localMouseOriginY, double dragX, double dragY) implements IEvent {}
    public record DragEndEvent(double mouseX, double mouseY, int button, double screenMouseOriginX, double screenMouseOriginY, double localMouseOriginX, double localMouseOriginY) implements IEvent {}
    public record KeyPressEvent(int keyCode, int scanCode, int modifiers) implements IEvent {}
    public record KeyReleaseEvent(int keyCode, int scanCode, int modifiers) implements IEvent {}
    public record CharTypeEvent(char codePoint, int modifiers) implements IEvent {}
    public record ResizeBeginEvent(double mouseX, double mouseY) implements IEvent {}
    public record ResizeEvent(double mouseX, double mouseY, int newX, int newY, int newWidth, int newHeight) implements IEvent {}
    public record ResizeEndEvent(double mouseX, double mouseY, int newX, int newY, int newWidth, int newHeight, MutableBoolean cancel) implements IEvent {}
    
    /**
     * Triggered when the component's position and size values have changed.
     * This event is similar to {@link ResizeEndEvent}, except that {@link ResizeEndEvent}
     * is only triggered by user interactions, while this event reacts purely to changes
     * in the values. The event is executed by calling {@link DLGuiComponent#setX}, {@link DLGuiComponent#setY}, {@link DLGuiComponent#setWidth} and {@link DLGuiComponent#setHeight}.
     * @param newX The new local x position on the parent component.
     * @param newY The new local y position on the parent component.
     * @param newWidth The new width.
     * @param newHeight The new height.
     */
    public record ComponentPosAndSizeChanged(int oldX, int newX, int oldY, int newY, int oldWidth, int newWidth, int oldHeight, int newHeight) implements IEvent {
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
    public record DragComponentBeginEvent(double mouseX, double mouseY, int button, List<DLGuiComponent> draggedOverComponents) implements IEvent {}
    public record DragComponentEvent(double mouseX, double mouseY, int button, int newX, int newY, double dragX, double dragY, List<DLGuiComponent> draggedOverComponents) implements IEvent {}
    public record DragComponentEndEvent(double mouseX, double mouseY, int button, int newX, int newY, List<DLGuiComponent> draggedOverComponents, MutableBoolean cancel) implements IEvent {}

    public record DragComponentOverBeginEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button) implements IEvent {}
    public record DragComponentOverEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button) implements IEvent {}
    public record DragComponentOverEndEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button) implements IEvent {}
    
    public record DraggingOverEvent(List<DLGuiComponent> other, double mouseX, double mouseY, int button) implements IEvent {}

    public record DropComponentEvent(DLGuiComponent other, double mouseX, double mouseY) implements IEvent {}
    public record DragAndDropFilesEvent(List<Path> paths, double mouseX, double mouseY) implements IEvent {}

    public record VisibilityChangedEvent(boolean visible) implements IEvent {}
    public record EnabledChangedEvent(boolean enabled) implements IEvent {}
    public record ResizableChangedEvent(boolean resizable) implements IEvent {}
    public record MovableChangedEvent(boolean movable) implements IEvent {}
    @NotCancellable public record LayoutUpdateEvent(Phase order) implements IEvent {}
    public record ComponentsClearEvent(Phase order, MutableBoolean cancel) implements IEvent {}
    public record ParentChangedEvent(Optional<DLGuiComponent> oldParent, Optional<DLGuiComponent> newParent) implements IEvent {}
    @NotCancellable public record WindowManagerChangeEvent(DLWindowManager oldWindowManager, DLWindowManager newWindowManager) implements IEvent {}
    public record RenderPreEvent(DLGuiGraphics graphics, double mouseX, double mouseY, RenderLayer layer, Rectangle renderBounds) implements IEvent {}
    public record RenderEvent(DLGuiGraphics graphics, double mouseX, double mouseY, RenderLayer layer, Rectangle renderBounds) implements IEvent {}
    public record RenderPostEvent(DLGuiGraphics graphics, double mouseX, double mouseY, RenderLayer layer, Rectangle renderBounds) implements IEvent {}
    @NotCancellable public record CloseEvent() implements IEvent {}
}
