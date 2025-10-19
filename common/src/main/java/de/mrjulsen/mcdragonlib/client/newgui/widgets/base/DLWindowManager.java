
package de.mrjulsen.mcdragonlib.client.newgui.widgets.base;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiCommonEvents;
import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent.ConsumptionType;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLGuiComponent.Flags;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.CursorType;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.HitResult;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.HitResult.ComponentHitContext;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.HitResult.ComponentSelectionState;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.EventListenerWrapper;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.events.IEventDispatcher;
import de.mrjulsen.mcdragonlib.util.Color;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@SupportsEvents({
    DLGuiStandardEvents.ClickEvent.class,
    DLGuiStandardEvents.RightClickEvent.class,
    DLGuiStandardEvents.MousePressedEvent.class,
    DLGuiStandardEvents.MouseUpEvent.class,
    DLGuiStandardEvents.MouseMoveEvent.class,
    DLGuiStandardEvents.ScrollEvent.class,
    DLGuiStandardEvents.TickEvent.class,
    DLGuiStandardEvents.KeyPressEvent.class,
    DLGuiStandardEvents.KeyReleaseEvent.class,
    DLGuiStandardEvents.CharTypeEvent.class
})
public class DLWindowManager implements IEventDispatcher<DLWindowManager> {
    public static final int DRAG_THRESHOLD = 5;    

    private final Map<Class<? extends IEvent>, PriorityQueue<EventListenerWrapper<?>>> eventListeners = new HashMap<>();

    @Override
    public Map<Class<? extends IEvent>, PriorityQueue<EventListenerWrapper<?>>> getEventListeners() {
        return eventListeners;
    }

    public static class ModalWindowStack extends ConcurrentLinkedDeque<DLWindow> {
        private final ModalId id;

        public ModalWindowStack(UUID id) {
            Objects.requireNonNull(id);
            this.id = new ModalId(id);
        }

        @Override
        public boolean add(DLWindow win) {
            win.assignToModal(id);
            boolean b = super.add(win);
            return b;
        }

        @Override
        public DLWindow remove() {            
            DLWindow win = super.remove();
            win.assignToModal(null);
            return win;
        }

        @Override
        public void clear() {
            for (DLWindow win : this) {
                win.assignToModal(null);
            }
            super.clear();
        }

        public ModalId id() {
            return id;
        }

        @SuppressWarnings("unlikely-arg-type")
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ModalWindowStack o) {
                return id.equals(o.id);
            }
            if (obj instanceof ModalId o) {
                return id.equals(o);
            }
            if (obj instanceof UUID o) {
                return id.equals(o);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    public static class ModalId {
        private final UUID id;

        public ModalId(UUID id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ModalId o) {
                return id.equals(o.id);
            }
            if (obj instanceof UUID o) {
                return id.equals(o);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }

    private DLGuiComponent focusedComponent = null;
    private final Set<DLGuiComponent> mouseDownComponents = new HashSet<>();
    private final Set<ComponentHitContext> draggingComponents = new HashSet<>();
    private final List<DLGuiComponent> dragOverComponents = new LinkedList<>();
    private final Queue<ComponentHitContext> renderInputOverlayComponents = new ConcurrentLinkedQueue<>();

    private final ConcurrentLinkedDeque<ModalWindowStack> windows = new ConcurrentLinkedDeque<>();
    private final Runnable close;

    private double width;
    private double height;

    private boolean isDragging = false;
    private boolean isMouseDown = false;

    private int mouseDownButton = -1;
    private double mouseDownX;
    private double mouseDownY;
    private boolean specialDragAction = false;
    private boolean updatingLayout = false;

    private DLWindow focusedWindow;


    public <T extends DLWindow> DLWindowManager(WindowBuilder<T> windowBuilder, double width, double height, Runnable close) {
        this.close = close;
        this.width = width;
        this.height = height;
        createWindow(windowBuilder);
    }

    public void updateLayout(double width, double height) {
        updatingLayout = true;
        setWidth(width);
        setHeight(height);
        init();
        updatingLayout = false;
    }

    protected void setWidth(double width) {
        double oldWidth = this.width;
        this.width = width;
        double diff = width - oldWidth;

        iterateAll(false, (win, i) -> {
            double k = win.dWidth() / 2D;
            if (win.anchor.has(EAlign.RIGHT)) {
                if (win.anchor.has(EAlign.LEFT)) {
                    win.setWidth(win.dWidth() + diff);
                } else {
                    win.setX(this.width - win.dWidth());
                }
            }
            if (win.anchor.hasNone(EAlign.LEFT, EAlign.RIGHT)) {
                if (oldWidth > 0 && width > 0) {
                    double p = Math.max(1D / oldWidth * (win.dX() + k), 0);
                    win.setX(p * width - k);
                }
            }
            return true;
        });
    }

    protected void setHeight(double height) {
        double oldHeight = this.height;
        this.height = height;
        double diff = height - oldHeight;

        iterateAll(false, (win, i) -> {
            double k = win.dHeight() / 2D;
            if (win.anchor.has(EAlign.BOTTOM)) {                
                if (win.anchor.has(EAlign.TOP)) {
                    win.setHeight(win.dHeight() + diff);
                } else {
                    win.setX(this.height - win.dHeight());
                }
            }
            if (win.anchor.hasNone(EAlign.TOP, EAlign.BOTTOM)) {
                if (oldHeight > 0 && height > 0) {
                    double p = Math.max(1D / oldHeight * (win.dY() + k), 0);
                    win.setY(p * height - k);
                }
            }
            return true;
        });
    }

    protected void init() {
        iterateAll(false, (win, i) -> {
            win.updateLayoutEvent((int)width, (int)height);            
            return true;
        });
    }

    public void render(Graphics graphics, int mouseX, int mouseY) {
        for (RenderLayer layer : RenderLayer.values()) {
            graphics.poseStack().pushPose();
            graphics.poseStack().translate(0, 0, -layer.z());
            if (!layer.isSpecial()) {
                iterateAll(false, (win, i) -> {
                    graphics.poseStack().pushPose();
                    graphics.poseStack().translate(win.x(), win.y(), i);
                    win.renderEvent(graphics, mouseX - win.x(), mouseY - win.y(), layer, win.x(), win.y(), 0, 0, win.getPositionBox());
                    graphics.poseStack().popPose();
                    return true;
                });
            } else if (layer == RenderLayer.OVERLAY) {
                renderInputOverlayComponents.forEach(x -> {
                    graphics.poseStack().pushPose();
                    graphics.poseStack().translate(x.xOffset(), x.yOffset(), 0);
                    x.component().renderEvent(graphics, x.mouseX() - x.xOffset(), x.mouseY() - x.yOffset(), layer,x.xOffset(), x.yOffset(), 0, 0, Rectangle.withSize(0, 0, width, height));
                    graphics.poseStack().popPose();
                });
            }
            graphics.poseStack().popPose();
        }

        GuiUtils.drawString(graphics, Minecraft.getInstance().font, 1, 1, Minecraft.getInstance().fpsString, Color.WHITE, ETextAlignment.LEFT, true);
    }

    public void tick() {
        invokeEvent(this, new DLGuiStandardEvents.TickEvent());
        iterateAll(false, (win, i) -> {
            win.tick();
            return true;
        });
    }

    public double getMouseDownX() {
        return mouseDownX;
    }

    public double getMouseDownY() {
        return mouseDownY;
    }

    public int getMouseDownButton() {
        return mouseDownButton;
    }

    public boolean isMouseDragging() {
        return isDragging;
    }

    public boolean isMouseDown() {
        return isMouseDown;
    }

    public DLWindow getFocusedWindow() {
        return focusedWindow;
    }

    public double mouseXOnScreen() {
        return Minecraft.getInstance().mouseHandler.xpos() * (double)Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double)Minecraft.getInstance().getWindow().getScreenWidth();
    }

    public double mouseYOnScreen() {
        return Minecraft.getInstance().mouseHandler.ypos() * (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)Minecraft.getInstance().getWindow().getScreenHeight();
    }




    public ModalId getCurrentModalId() {
        return windows.getLast().id();
    }

    private ModalWindowStack getCurrentModal() {
        Iterator<ModalWindowStack> iterator = windows.descendingIterator();
        while (iterator.hasNext()) {
            ModalWindowStack stack = iterator.next();
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return null;
    }

    @SuppressWarnings("unlikely-arg-type")
    public boolean hasModal(ModalId id) {
        return windows.contains(id);
    }

    @SuppressWarnings("unlikely-arg-type")
    private ModalWindowStack getModalById(ModalId id) {
        return windows.stream().filter(x -> x.equals(id)).findFirst().orElseThrow();
    }

    public DLWindow[] getWindows(ModalId id) {
        return getModalById(id).toArray(DLWindow[]::new);
    }

    public boolean hasWindows() {
        return !windows.isEmpty();
    }

    public DLWindow getCurrentActiveWindow() {
        if (!hasWindows()) {
            return null;
        }
        return getCurrentModal().getLast();
    }


    public void closeWindow(DLWindow window) {
        closeWindows(List.of(window));
    }

    public void closeWindows(Collection<DLWindow> wins) {
        Set<DLWindow> removedWindows = new HashSet<>(wins.size());

        Iterator<ModalWindowStack> stacks = windows.descendingIterator();
        while (stacks.hasNext()) {
            ModalWindowStack stack = stacks.next();
            for (DLWindow win : wins) {
                if (stack.remove(win)) {
                    removedWindows.add(win);
                }
            }
            if (stack.isEmpty()) {
                stacks.remove();
            }
        }

        if (windows.isEmpty()) {
            closeInternal();
        }

        if (wins.contains(focusedWindow)) {
            focusedWindow = null;
        }
        updateWindowFocus(false);

        // Cleanup
        for (DLWindow win : removedWindows) {
            try {
                win.close();
            } catch (Exception e) {
            }
            win.setWindowManager(null);
        }
    }

    public void closeModal(ModalId id) {
        ModalWindowStack stack = getModalById(id);        
        stack.forEach(win -> {
            try {
                win.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        windows.remove(stack);
        if (windows.isEmpty()) {
            closeInternal();
        }
        if (stack.contains(focusedWindow)) {
            focusedWindow = null;
        }
        updateWindowFocus(false);

        // Cleanup
        stack.forEach(win -> {
            win.setWindowManager(null);
        });
    }

    /*
    @SuppressWarnings("unlikely-arg-type")
    public ModalId createModal(DLWindow window) {
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (windows.contains(id));
        windows.add(new ModalWindowStack(id));
        return createWindow(window);
    }

    public ModalId createWindow(DLWindow window) {
        if (!hasWindows()) {
            return createModal(window);
        }
        return createWindow(window, getCurrentModalId());
    }

    public ModalId createWindow(DLWindow window, ModalId id) {
        if (!hasWindows()) {
            return createModal(window);
        }
        ModalWindowStack stack = getModalById(id);
        stack.add(window);
        window.setRoot(() -> this);
        window.invoke(window, new DLGuiBuiltInEvents.WindowCreatedEvent(this, (int)width, (int)height));
        updateWindowFocus(false);
        return stack.id();
    }
        */

    public <T extends DLWindow> ModalId createModal(WindowBuilder<T> windowBuilder) {
        createModalInternal();
        return createWindow(windowBuilder);
    }

    @SuppressWarnings("unlikely-arg-type")
    public ModalWindowStack createModalInternal() {
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (windows.contains(id));
        ModalWindowStack stack = new ModalWindowStack(id);
        windows.add(stack);
        return stack;
    }

    public <T extends DLWindow> ModalId createWindow(WindowBuilder<T> windowBuilder) {
        if (!hasWindows()) {
            createModalInternal();
        }
        return createWindow(windowBuilder, getCurrentModalId());
    }

    public <T extends DLWindow> ModalId createWindow(WindowBuilder<T> windowBuilder, ModalId id) {
        if (!hasWindows()) {
            createModalInternal();
        }
        ModalWindowStack stack = getModalById(id);
        T window = windowBuilder.build(this);
        stack.add(window);
        window.invokeEvent(window, new DLGuiCommonEvents.WindowCreatedEvent(this, id, (int)width, (int)height));
        updateWindowFocus(false);
        return stack.id();
    }

    public static interface WindowBuilder<T extends DLWindow> {
        T build(DLWindowManager manager);
    }

    public void bringWindowToFront(DLWindow window) {
        ModalWindowStack stack = getCurrentModal();
        DLWindow previousWindow = stack.getLast();
        boolean windowChanged = previousWindow != window;

        if (windowChanged) {
            stack.remove(window);
            stack.addLast(window);
        }
        if (windowChanged || focusedWindow == null) {
            updateWindowFocus(false);
        }
    }

    public void sendWindowToBack(DLWindow window) {
        ModalWindowStack stack = getCurrentModal();
        stack.remove(window);
        stack.addFirst(window);
    }

    private boolean updateFocusLocked = false;

    public void updateWindowFocus(boolean unfocus) {
        boolean wasLocked = updateFocusLocked;
        updateFocusLocked = true;

        DLWindow previouslyFocusedWindow = focusedWindow;

        if (!wasLocked) {
            updateFocusLocked = false;

            AtomicReference<DLWindow> window = new AtomicReference<>(null);
            if (unfocus) {
                ModalWindowStack stack = getCurrentModal();
                if (stack != null) {
                    Iterator<DLWindow> wins = stack.descendingIterator();
                    while (wins.hasNext()) {
                        DLWindow win = wins.next();
                        if (win.getPositionBox().collision(mouseXOnScreen(), mouseYOnScreen())) {
                            window.set(win);
                            break;
                        }
                    }
                }
            } else {
                window.set(getCurrentActiveWindow());
            }

            focusedWindow = window.get();
            
            if (previouslyFocusedWindow != null) {
                previouslyFocusedWindow.invokeEvent(previouslyFocusedWindow, new DLGuiCommonEvents.WindowFocusEvent(this, false));
            }
            if (focusedWindow != null) {
                focusedWindow.invokeEvent(focusedWindow, new DLGuiCommonEvents.WindowFocusEvent(this, true));
            }
        }
    }

    private void closeInternal() {
        onClose();
        close.run();
    }

    public void close() {
        for (ModalWindowStack stack : windows) {
            closeModal(stack.id());
        }
        closeInternal();
    }


    public void focusComponent(DLGuiComponent component) {
        if (focusedComponent != null) {
            focusedComponent.setFocus(false);
        }
        component.setFocus(true);
    }


     // TODO Neues InputReceivedEvent, das durch irgeneinen dieser Inputs getriggert wird und Nutzer dann in ihren Components selbst bestimmen können was passieren soll. Aktuell werden feste Aktionen ausgeführt, z.b. setze Fokus.


    private void clearMouseInteractionData() {        
        mouseDownComponents.clear();
        draggingComponents.clear();
        dragOverComponents.clear();
        renderInputOverlayComponents.clear();
        specialDragAction = false;
        isDragging = false;
        mouseDownButton = -1;
    }

    protected void iterateAll(boolean backwards, BiFunction<DLWindow, Integer, Boolean> callback) { 
        if (backwards) {
            iterateAllBackwards(callback);
        } else {
            iterateAllForwards(callback);
        }
    }
    
    private void iterateAllBackwards(BiFunction<DLWindow, Integer, Boolean> callback) {
        int i = 0;
        Iterator<ModalWindowStack> stacks = windows.descendingIterator();
        main: while (stacks.hasNext()) {
            ModalWindowStack stack = stacks.next();
            Iterator<DLWindow> wins = stack.descendingIterator();
            while (wins.hasNext()) {
                DLWindow win = wins.next();
                if (!callback.apply(win, i)) {
                    break main;
                }
                i++;
            }
        }
    }
    
    private void iterateAllForwards(BiFunction<DLWindow, Integer, Boolean> callback) {
        int i = 0;
        main: for (ModalWindowStack stack : windows) {
            for (DLWindow win : stack) {
                if (!callback.apply(win, i)) {
                    break main;
                }
                i++;
            }
        }
    }

    public boolean iterateCurrentModal(BiFunction<DLWindow, Boolean, Boolean> callback, Runnable prepare, Consumer<Boolean> andThen) {
        MutableBoolean consumed = new MutableBoolean();
        if (hasWindows()) {
            DLUtils.doIfNotNull(prepare, Runnable::run);
            Iterator<DLWindow> windows = getCurrentModal().descendingIterator();
            while (windows.hasNext()) {
                DLWindow window = windows.next();
                consumed.setValue(consumed.getValue() || callback.apply(window, consumed.getValue()));
            }
        }
        DLUtils.doIfNotNull(andThen, x -> x.accept(consumed.getValue()));
        return false;//consumed.getValue();
    }

    public void prepareMouseClick(double mouseX, double mouseY, int button) {
        clearMouseInteractionData();
        focusedComponent = null;
        mouseDownX = mouseX;
        mouseDownY = mouseY;
        mouseDownButton = button;
        isMouseDown = true;
    }

    public boolean mouseClicked(DLWindow window, boolean consumed, double mouseX, double mouseY, int button) {
        if (!hasWindows()) {
            return false;
        }

        switch (button) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> invokeEvent(this, new DLGuiStandardEvents.ClickEvent(mouseX, mouseY));
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> invokeEvent(this, new DLGuiStandardEvents.RightClickEvent(mouseX, mouseY));
        }
        
        if (!hasWindows()) {
            return false;
        }
        invokeEvent(this, new DLGuiStandardEvents.MousePressedEvent(mouseX, mouseY, button));

        Flags flags = new Flags(consumed, consumed, false, true, ImmutableSet.of(), ImmutableSet.of());
        HitResult result = window.iterateComponents(mouseX, mouseY, window.x(), window.y(), Rectangle.INFINITY, flags, ConsumptionType.CLICK);        
        for (Map.Entry<ComponentSelectionState, LinkedList<ComponentHitContext>> e : result.components().entrySet()) {
            for (ComponentHitContext c : e.getValue()) {
                c.component().setFocus(e.getKey() == ComponentSelectionState.FOCUSED);
                if (e.getKey().isHit()) {                        
                    c.component().setMouseDown(true, c.mouseX(), c.mouseY(), button);
                    mouseDownComponents.add(c.component());
                }
                if (e.getKey() == ComponentSelectionState.FOCUSED) {
                    focusedComponent = c.component();
                }
            }
        }
        boolean b = result.consumed();        
        if (!hasWindows()) {
            return b;
        }

        if (b && !consumed) {
            bringWindowToFront(window);
        }
        return b;
    }

    public boolean mouseMoved(DLWindow window, boolean consumed, double mouseX, double mouseY) {
        if (!hasWindows()) {
            return false;
        }
        invokeEvent(this, new DLGuiStandardEvents.MouseMoveEvent(mouseX, mouseY));
        if (isDragging) {
            return false;
        }

        Flags flags = new Flags(consumed, consumed, false, true, ImmutableSet.of(), ImmutableSet.of());
        HitResult result = window.iterateComponents(mouseX, mouseY, window.x(), window.y(), Rectangle.INFINITY, flags, ConsumptionType.MOUSE_MOVE);        
        for (Map.Entry<ComponentSelectionState, LinkedList<ComponentHitContext>> e : result.components().entrySet()) {
            for (ComponentHitContext c : e.getValue()) {                    
                c.component().setSelected(e.getKey().isHit(), c.mouseX(), c.mouseY());
            }
        }
        return result.consumed();
    }

    public void finishMouseRelease(double mouseX, double mouseY) {
        clearMouseInteractionData();
        isMouseDown = false;
        iterateCurrentModal((win, consumed) -> mouseMoved(win, consumed, mouseX, mouseY), null, null);
    }

    public boolean mouseReleased(DLWindow window, boolean consumed, double mouseX, double mouseY, int button) {
        if (!hasWindows()) {
            return false;
        }
        invokeEvent(this, new DLGuiStandardEvents.MouseUpEvent(mouseX, mouseY, button));

        Flags flags = new Flags(consumed, consumed, false, true, ImmutableSet.of(), ImmutableSet.of());
        HitResult result = window.iterateComponents(mouseX, mouseY, window.x(), window.y(), Rectangle.INFINITY, flags, ConsumptionType.CLICK);        
        for (Map.Entry<ComponentSelectionState, LinkedList<ComponentHitContext>> e : result.components().entrySet()) {
            for (ComponentHitContext c : e.getValue()) {
                if (e.getKey().isHit() && mouseDownComponents.contains(c.component())) {
                    c.component().mouseClickDispatcher(c.mouseX(), c.mouseY(), button);
                }
                c.component().setMouseDown(false, c.mouseX(), c.mouseY(), button);
                if (c.component().isDragged()) {
                    c.component().setDragging(false, c.mouseX(), c.mouseY(), button, mouseDownX, mouseDownY, 0, 0, specialDragAction, dragOverComponents);
                }
            }
        };
        return false;//result.consumed();
    }

    public boolean mouseScrolled(DLWindow window, boolean consumed, double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!hasWindows()) {
            return false;
        }
        invokeEvent(this, new DLGuiStandardEvents.ScrollEvent(mouseX, mouseY, scrollX, scrollY));

        Flags flags = new Flags(consumed, consumed, false, true, ImmutableSet.of(), ImmutableSet.of());
        HitResult result = window.iterateComponents(mouseX, mouseY, window.x(), window.y(), Rectangle.INFINITY, flags, ConsumptionType.SCROLL);  
        for (Map.Entry<ComponentSelectionState, LinkedList<ComponentHitContext>> e : result.components().entrySet()) {
            for (ComponentHitContext c : e.getValue()) {
                if (!hasWindows()) {
                    return result.consumed();
                }
                if (e.getKey().isHit()) {
                    c.component().invokeEvent(c.component(), new DLGuiStandardEvents.ScrollEvent(c.mouseX(), c.mouseY(), -scrollX, -scrollY), true);
                }
            }
        }
        mouseMoved(window, consumed, mouseX, mouseY);
        return result.consumed();
    }

    public void prepareMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        specialDragAction |= Math.abs(mouseDownX - mouseX) > DRAG_THRESHOLD || Math.abs(mouseDownY - mouseY) > DRAG_THRESHOLD;
        draggingComponents.clear();
        dragOverComponents.clear();
        renderInputOverlayComponents.clear();
    }

    public void finishMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        List<DLGuiComponent> immutableDragOverComponents = ImmutableList.copyOf(dragOverComponents);
        for (ComponentHitContext c : draggingComponents) {
            if (c.component().setDragging(true, c.mouseX(), c.mouseY(), button, mouseDownX, mouseDownY, dragX, dragY, specialDragAction, immutableDragOverComponents)) {
                renderInputOverlayComponents.add(c);
            }
        }
    }

    public boolean mouseDragged(DLWindow window, boolean consumed, double mouseX, double mouseY, int button, double dragX, double dragY) {
        isDragging = true;
        if (mouseDownComponents.isEmpty()) return false;

        Flags flags = new Flags(consumed, consumed, false, true, ImmutableSet.of(), ImmutableSet.copyOf(mouseDownComponents));
        HitResult result = window.iterateComponents(mouseX, mouseY, window.x(), window.y(), Rectangle.INFINITY, flags, ConsumptionType.DRAG);
        for (Map.Entry<ComponentSelectionState, LinkedList<ComponentHitContext>> e : result.components().entrySet()) {
            for (ComponentHitContext c : e.getValue()) {
                if (mouseDownComponents.contains(c.component())) {
                    draggingComponents.add(c);
                } else if (e.getKey().isHit()) {
                    dragOverComponents.add(c.component());
                } else if (c.component().isComponentDraggedOver()) {                    
                    for (DLGuiComponent o : mouseDownComponents) {                        
                        c.component().setDragComponentOver(false, o, c.mouseX(), c.mouseY());
                    }
                }
            }
        }
        
        return result.consumed();
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        invokeEvent(this, new DLGuiStandardEvents.KeyPressEvent(keyCode, scanCode, modifiers));
        if (focusedComponent != null) {
            focusedComponent.invokeEvent(focusedComponent, new DLGuiStandardEvents.KeyPressEvent(keyCode, scanCode, modifiers), true);
            return true;
        }
        return false;
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        invokeEvent(this, new DLGuiStandardEvents.KeyReleaseEvent(keyCode, scanCode, modifiers));
        if (focusedComponent != null) {
            focusedComponent.invokeEvent(focusedComponent, new DLGuiStandardEvents.KeyReleaseEvent(keyCode, scanCode, modifiers), true);
            return true;
        }
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        invokeEvent(this, new DLGuiStandardEvents.CharTypeEvent(codePoint, modifiers));
        if (focusedComponent != null) {
            focusedComponent.invokeEvent(focusedComponent, new DLGuiStandardEvents.CharTypeEvent(codePoint, modifiers), true);
            return true;
        }
        return false;
    }
    
    public boolean onFilesDrop(DLWindow window, boolean consumed, List<Path> packs) {
        double mouseX = mouseXOnScreen();
        double mouseY = mouseYOnScreen();
        Flags flags = new Flags(consumed, consumed, false, true, ImmutableSet.of(), ImmutableSet.of());
        HitResult result = window.iterateComponents(mouseX, mouseY, window.x(), window.y(), Rectangle.INFINITY, flags, ConsumptionType.DRAG_AND_DROP);
        for (Map.Entry<ComponentSelectionState, LinkedList<ComponentHitContext>> e : result.components().entrySet()) {
            for (ComponentHitContext c : e.getValue()) {
                if (e.getKey().isHit()) {
                    c.component().invokeEvent(c.component(), new DLGuiStandardEvents.DragAndDropFilesEvent(packs, c.mouseX(), c.mouseY()), true);
                }
            }
        }
        boolean b = result.consumed();
        if (b) {
            bringWindowToFront(window);
        }
        return b;
    }

    public void onClose() {
        CursorType.set(null);
        iterateAll(false, (win, i) -> {
            try {
                win.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        });
    }

    public static boolean hasControlDown() {
        return Screen.hasControlDown();
    }

    public static boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    public static boolean hasAltDown() {
        return Screen.hasAltDown();
    }

    public static boolean isCut(int keyCode) {
        return Screen.isCut(keyCode);
    }

    public static boolean isPaste(int keyCode) {
        return Screen.isPaste(keyCode);
    }

    public static boolean isCopy(int keyCode) {
        return Screen.isCopy(keyCode);
    }

    public static boolean isSelectAll(int keyCode) {
        return Screen.isSelectAll(keyCode);
    }

    public double getScreenWidth() {
        return width;
    }

    public double getScreenHeight() {
        return height;
    }

    public boolean isUpdatingLayout() {
        return updatingLayout;
    }
}
