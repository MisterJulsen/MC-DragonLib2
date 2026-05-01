package de.mrjulsen.mcdragonlib.client.newgui.events;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableBoolean;

import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
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
    public record MouseMoveEvent(double mouseX, double mouseY) implements IEvent {}
    public record MouseUpEvent(double mouseX, double mouseY, int button) implements IEvent {}
    public record MousePressedEvent(double mouseX, double mouseY, int button) implements IEvent {}
    public record ClickEvent(double mouseX, double mouseY) implements IEvent {}
    public record RightClickEvent(double mouseX, double mouseY) implements IEvent {}
    public record MultiClickEvent(double mouseX, double mouseY, int button, byte clickCount) implements IEvent {}
    public record ScrollEvent(double mouseX, double mouseY, double deltaX, double deltaY) implements IEvent {}
    public record TickEvent() implements IEvent {}
    public record ComponentAddedEvent(DLGuiComponent child) implements IEvent {}
    public record ComponentRemovedEvent(DLGuiComponent child) implements IEvent {}
    public record FocusChangedEvent(boolean focus) implements IEvent {}
    public record DragBeginEvent(double mouseX, double mouseY, int button) implements IEvent {}
    public record DragEvent(double mouseX, double mouseY, int button, double mouseOriginX, double mouseOriginY, double dragX, double dragY) implements IEvent {}
    public record DragEndEvent(double mouseX, double mouseY, int button, double mouseOriginX, double mouseOriginY) implements IEvent {}
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
    public record ComponentPosAndSizeChanged(int newX, int newY, int newWidth, int newHeight) implements IEvent {}
    public record DragComponentBeginEvent(double mouseX, double mouseY, int button, List<DLGuiComponent> draggedOverComponents) implements IEvent {}
    public record DragComponentEvent(double mouseX, double mouseY, int button, int newX, int newY, double dragX, double dragY, List<DLGuiComponent> draggedOverComponents) implements IEvent {}
    public record DragComponentEndEvent(double mouseX, double mouseY, int button, int newX, int newY, List<DLGuiComponent> draggedOverComponents, MutableBoolean cancel) implements IEvent {}
    public record DragComponentOverBeginEvent(DLGuiComponent other, double mouseX, double mouseY) implements IEvent {}
    public record DragComponentOverEvent(DLGuiComponent other, double mouseX, double mouseY) implements IEvent {}
    public record DragComponentOverEndEvent(DLGuiComponent other, double mouseX, double mouseY) implements IEvent {}
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
    public record RenderEvent(Graphics graphics, double mouseX, double mouseY, RenderLayer layer, Rectangle renderBounds) implements IEvent {}
    @NotCancellable public record CloseEvent() implements IEvent {}
}
