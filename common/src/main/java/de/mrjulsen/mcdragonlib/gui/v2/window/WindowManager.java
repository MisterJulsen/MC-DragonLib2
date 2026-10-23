package de.mrjulsen.mcdragonlib.gui.v2.window;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.Tooltip;
import de.mrjulsen.mcdragonlib.gui.v2.dnd.DragSession;
import de.mrjulsen.mcdragonlib.gui.v2.input.Hotkeys;
import de.mrjulsen.mcdragonlib.gui.v2.input.InputRouter;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Host of a v2 GUI: owns the component tree, the input router and the render pass, and manages the
 * windows on top of it.
 * <p>Windows are grouped into {@link ModalLayer}s, where only the topmost layer receives input.
 * {@link Popup}s sit on a separate pane above all of them. Input arrives through the mouseX and keyX
 * methods, usually from a {@link DLScreen}, and is handed to the {@link InputRouter}.</p>
 */
public class WindowManager {

    private static final class Pane extends GuiComponent {

        private Pane() {
            super(0, 0, 0, 0);
            clipsChildren.set(false);
            clipsContent.set(false);
            hitTestVisible.set(false);
        }
    }

    private final Pane root = new Pane();
    private final Pane popupRoot = new Pane();
    private final InputRouter router = new InputRouter();
    private final List<ModalLayer> layers = new ArrayList<>();
    private final List<Popup> popups = new ArrayList<>();
    private final Queue<Runnable> pendingTasks = new ConcurrentLinkedQueue<>();
    private final DLGuiGraphics graphics = new DLGuiGraphics();

    private int screenWidth;
    private int screenHeight;
    private boolean layoutRequested = true;
    private Consumer<WindowManager> closeHandler;

    /**
     * Whether closing the last window returns to the screen that was open before, instead of to the
     * game.
     */
    public final BooleanProperty showPreviousScreenOnClose = new BooleanProperty(true);
    /**
     * Whether Tab moves the focus to the next focusable component, and Shift+Tab to the previous one.
     */
    public final BooleanProperty focusTraversal = new BooleanProperty(true);

    public WindowManager() {
        root.setWindowManager(this);
        root.addComponent(popupRoot);
    }

    /**
     * The root of the whole tree. All layers and the popup pane hang below it.
     */
    public GuiComponent root() {
        return root;
    }

    /**
     * The pane all popups live on, kept in front of every layer.
     */
    public GuiComponent popupRoot() {
        return popupRoot;
    }

    public InputRouter router() {
        return router;
    }

    public DLGuiGraphics graphics() {
        return graphics;
    }

    /**
     * The global hotkey scope of this manager, i.e. hotkeys that work regardless of which window is
     * active.
     */
    public Hotkeys hotkeys() {
        return Hotkeys.of(root);
    }

    public int screenWidth() {
        return screenWidth;
    }

    public int screenHeight() {
        return screenHeight;
    }

    /**
     * Sets what happens when the last layer is gone. {@link DLScreen} uses this to leave the screen.
     */
    public void setCloseHandler(Consumer<WindowManager> handler) {
        this.closeHandler = handler;
    }

    /**
     * Schedules a layout pass for the next frame. Cheap to call repeatedly.
     */
    public void requestLayout() {
        layoutRequested = true;
    }

    /**
     * Queues a task to run before the next frame. Use this to change the component tree from code that
     * is currently iterating over it, e.g. inside an event listener.
     */
    public void invokeLater(Runnable task) {
        pendingTasks.add(task);
    }

    /**
     * Adapts everything to a new screen size: refits fullscreen windows, re-centers or clamps the rest
     * and closes all popups.
     */
    public void resize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        root.setSize(width, height);
        popupRoot.setSize(width, height);
        for (ModalLayer layer : layers) {
            layer.resize(width, height);
            for (Window window : layer.getWindows()) {
                repositionAfterResize(window);
            }
        }
        closeAllPopups();
        requestLayout();
    }

    private void repositionAfterResize(Window window) {
        if (window.fullscreen.get()) {
            window.refitFullscreen(screenWidth, screenHeight);
            return;
        }
        if (window.spawnPosition.get() != Window.SpawnPosition.CUSTOM) {
            window.applySpawnPosition();
            return;
        }
        double maxX = Math.max(0, screenWidth - window.dWidth());
        double maxY = Math.max(0, screenHeight - window.dHeight());
        window.setPosition(Math.min(Math.max(window.dX(), 0), maxX), Math.min(Math.max(window.dY(), 0), maxY));
    }

    /**
     * Snapshot of the layers, bottom first.
     */
    public List<ModalLayer> layers() {
        return List.copyOf(layers);
    }

    public int layerCount() {
        return layers.size();
    }

    public boolean hasWindows() {
        for (ModalLayer layer : layers) {
            if (!layer.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * All windows across all layers.
     */
    public List<Window> allWindows() {
        List<Window> result = new ArrayList<>();
        for (ModalLayer layer : layers) {
            result.addAll(layer.getWindows());
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * The topmost layer, i.e. the only one that receives input, or null if nothing is open.
     */
    public ModalLayer currentLayer() {
        return layers.isEmpty() ? null : layers.get(layers.size() - 1);
    }

    /**
     * Id of the topmost layer, or null if nothing is open.
     */
    public ModalId currentModalId() {
        ModalLayer layer = currentLayer();
        return layer == null ? null : layer.getId();
    }

    public Optional<ModalLayer> layerOf(ModalId id) {
        for (ModalLayer layer : layers) {
            if (layer.getId().equals(id)) {
                return Optional.of(layer);
            }
        }
        return Optional.empty();
    }

    public Optional<ModalLayer> layerOf(Window window) {
        for (ModalLayer layer : layers) {
            if (layer.contains(window)) {
                return Optional.of(layer);
            }
        }
        return Optional.empty();
    }

    public List<Window> windowsOf(ModalId id) {
        return layerOf(id).map(ModalLayer::getWindows).orElse(List.of());
    }

    /**
     * Whether the given layer blocks the ones below it.
     */
    public boolean isModal(ModalId id) {
        return layerOf(id).map(ModalLayer::isModal).orElse(false);
    }

    private ModalLayer createLayer(boolean modal) {
        ModalLayer previous = currentLayer();
        if (previous != null) {
            previous.suspend();
        }
        ModalLayer layer = new ModalLayer(ModalId.random(), modal);
        layer.resize(screenWidth, screenHeight);
        layers.add(layer);
        root.addComponent(layer.pane());

        root.bringToFront(popupRoot);
        return layer;
    }

    /**
     * Opens a window on the topmost layer, creating a non-modal layer if nothing is open yet.
     *
     * @return the window, so it can be assigned in the same line
     */
    public <T extends Window> T openWindow(T window) {
        ModalLayer layer = currentLayer();
        if (layer == null) {
            layer = createLayer(false);
        }
        return openWindow(window, layer);
    }

    /**
     * Opens a window on a specific layer, e.g. a second window belonging to a modal dialog.
     *
     * @return the window, so it can be assigned in the same line
     * @throws IllegalArgumentException if the layer does not exist
     */
    public <T extends Window> T openWindow(T window, ModalId id) {
        ModalLayer layer = layerOf(id).orElseThrow(() -> new IllegalArgumentException("Unknown modal layer: " + id));
        return openWindow(window, layer);
    }

    /**
     * Opens one or more windows on a new modal layer, which suspends everything below it and closes all
     * popups.
     *
     * @return the id of the new layer, needed to close it again
     * @throws IllegalArgumentException if no window was passed
     */
    public ModalId openModal(Window... windows) {
        if (windows.length <= 0) {
            throw new IllegalArgumentException("A modal layer needs at least one window.");
        }
        closeAllPopups();
        ModalLayer layer = createLayer(true);
        router.focus(null);
        for (Window window : windows) {
            openWindow(window, layer);
        }
        return layer.getId();
    }

    private <T extends Window> T openWindow(T window, ModalLayer layer) {
        window.setWindowManager(this);
        layer.add(window);
        window.notifyOpened(this, layer.getId());
        if (window.activateOnSpawn.get()) {
            layer.setActiveWindow(window);
        }
        requestLayout();
        return window;
    }

    /**
     * Closes a window and every window it owns. An empty layer is discarded afterwards, and once the
     * last layer is gone the close handler runs.
     */
    public void closeWindow(Window window) {
        ModalLayer layer = layerOf(window).orElse(null);
        if (layer == null) {
            return;
        }
        for (Window owned : allWindows()) {
            if (owned != window && owned.owner.get() == window) {
                closeWindow(owned);
            }
        }
        detachWindow(layer, window);

        if (layer.isEmpty()) {
            discardLayer(layer);
        } else if (layer.getActiveWindow() == null) {
            layer.setActiveWindow(layer.topmostWindow());
        }
        requestLayout();
        closeIfEmpty();
    }

    /**
     * Closes a whole layer with all of its windows and resumes the one below.
     */
    public void closeModal(ModalId id) {
        ModalLayer layer = layerOf(id).orElse(null);
        if (layer == null) {
            return;
        }
        for (Window window : layer.getWindows()) {
            detachWindow(layer, window);
        }
        discardLayer(layer);
        requestLayout();
        closeIfEmpty();
    }

    /**
     * Closes the topmost layer, but only if it is actually modal.
     */
    public void closeCurrentModal() {
        ModalLayer layer = currentLayer();
        if (layer != null && layer.isModal()) {
            closeModal(layer.getId());
        }
    }

    /**
     * Closes every layer and window. The close handler runs afterwards.
     */
    public void closeAll() {
        for (ModalLayer layer : List.copyOf(layers)) {
            closeModal(layer.getId());
        }
    }

    private void detachWindow(ModalLayer layer, Window window) {
        closePopupsOf(window);
        router.releaseInside(window);
        layer.remove(window);
        window.notifyClosed(this);

        window.dispose();
        window.setWindowManager(null);
    }

    private void discardLayer(ModalLayer layer) {
        layers.remove(layer);
        root.removeComponent(layer.pane());
        ModalLayer current = currentLayer();
        if (current == null) {
            return;
        }
        if (current.getActiveWindow() == null) {
            current.setActiveWindow(current.topmostWindow());
        } else {
            current.resume();
        }
    }

    private void closeIfEmpty() {
        if (layers.isEmpty() && closeHandler != null) {
            closeHandler.accept(this);
        }
    }

    /**
     * Makes a window the active one of its layer and brings it to the front. Ignored for windows on a
     * suspended layer.
     */
    public void activate(Window window) {
        ModalLayer layer = layerOf(window).orElse(null);
        if (layer == null || layer != currentLayer()) {
            return;
        }
        layer.setActiveWindow(window);
    }

    /**
     * Pushes a window to the back of its band and, if it was the active one, activates whatever is now
     * on top.
     */
    public void sendToBack(Window window) {
        layerOf(window).ifPresent(layer -> {
            layer.moveToBandBack(window);
            if (layer == currentLayer() && layer.getActiveWindow() == window) {
                layer.setActiveWindow(layer.topmostWindow());
            }
        });
    }

    /**
     * Pulls a window to the front of its band, without changing which window is active.
     */
    public void bringToFront(Window window) {
        layerOf(window).ifPresent(layer -> layer.moveToBandFront(window));
    }

    /**
     * The window on the topmost layer that receives keyboard input, or null if there is none.
     */
    public Window activeWindow() {
        ModalLayer layer = currentLayer();
        return layer == null ? null : layer.getActiveWindow();
    }

    /**
     * The frontmost visible window of the topmost layer, which is not necessarily the active one.
     */
    public Window topmostWindow() {
        ModalLayer layer = currentLayer();
        return layer == null ? null : layer.topmostWindow();
    }

    /**
     * Whether any visible window asks for the game to pause, see Window#pauseGame.
     */
    public boolean isPauseScreen() {
        for (Window window : allWindows()) {
            if (window.visible.get() && window.pauseGame.get()) {
                return true;
            }
        }
        return false;
    }

    GuiComponent inputRoot() {
        ModalLayer layer = currentLayer();
        return layer == null ? root : layer.pane();
    }

    /**
     * Moves the keyboard focus, or clears it with null. Prefer GuiComponent#requestFocus, which checks
     * whether the component can take focus at all.
     */
    public void focus(GuiComponent component) {
        router.focus(component);
    }

    void addPopup(Popup popup) {
        if (popups.contains(popup)) {
            return;
        }
        popups.add(popup);
        popupRoot.addComponent(popup.content());
        requestLayout();
    }

    void removePopup(Popup popup) {
        if (!popups.remove(popup)) {
            return;
        }
        GuiComponent content = popup.content();
        router.releaseInside(content);
        popupRoot.removeComponent(content);
        requestLayout();
    }

    /**
     * Snapshot of the open popups, in opening order.
     */
    public List<Popup> popups() {
        return List.copyOf(popups);
    }

    public boolean hasPopups() {
        return !popups.isEmpty();
    }

    /**
     * The popup that was opened last, i.e. the one that gets the keyboard input, or null.
     */
    public Popup topmostPopup() {
        return popups.isEmpty() ? null : popups.get(popups.size() - 1);
    }

    /**
     * Closes every open popup, e.g. before a modal dialog opens.
     */
    public void closeAllPopups() {
        for (Popup popup : List.copyOf(popups)) {
            if (popup.isOpen()) {
                popup.close();
            }
        }
    }

    /**
     * Closes every popup owned by a component inside the given subtree, plus those without an owner.
     * Happens automatically when a window closes.
     */
    public void closePopupsOf(GuiComponent subtree) {
        for (Popup popup : List.copyOf(popups)) {
            GuiComponent owner = popup.owner();
            if (popup.isOpen() && (owner == null || owner.isDescendantOf(subtree))) {
                popup.close();
            }
        }
    }

    private boolean isInsidePopup(double screenX, double screenY) {
        for (Popup popup : popups) {
            GuiComponent content = popup.content();
            if (content.visible.get() && content.containsScreenPoint(screenX, screenY)) {
                return true;
            }
        }
        return false;
    }

    private boolean dismissLightPopups() {
        boolean closed = false;
        for (Popup popup : List.copyOf(popups)) {
            if (popup.isOpen() && popup.lightDismiss.get()) {
                popup.close();
                closed = true;
            }
        }
        return closed;
    }

    private GuiComponent pointerRoot(double screenX, double screenY) {
        return !popups.isEmpty() && isInsidePopup(screenX, screenY) ? popupRoot : inputRoot();
    }

    private GuiComponent keyboardScope() {
        Popup popup = topmostPopup();
        if (popup != null) {
            return popup.content();
        }
        Window window = activeWindow();
        return window != null ? window : inputRoot();
    }

    /**
     * Runs a pending layout pass right away. Called once per frame; only needed by hand when sizes are
     * read before the next frame.
     */
    public void performLayout() {
        if (!layoutRequested) {
            return;
        }
        layoutRequested = false;
        root.applyLayout();
    }

    /**
     * Renders the whole GUI: pending tasks, layout, component tree, tooltip of the hovered component,
     * the drag ghost and finally the deferred layers.
     */
    public void render(GuiGraphics guiGraphics, Font font, float partialTick, double mouseX, double mouseY) {
        runPendingTasks();
        performLayout();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        graphics.beginFrame(guiGraphics, font, partialTick, screenWidth, screenHeight, mouseX, mouseY);
        root.renderComponents(graphics, mouseX, mouseY);

        renderHoveredTooltip(mouseX, mouseY);

        DragSession session = router.dragSession();
        if (session != null) {
            session.renderGhost(graphics);
        }
        graphics.flushDeferred();
        graphics.flushDebugOverlay();
        guiGraphics.flush();
    }

    private void renderHoveredTooltip(double mouseX, double mouseY) {
        if (router.isDragging()) {
            return;
        }
        List<GuiComponent> hoverStack = router.hoverStack();
        for (GuiComponent hovered : hoverStack) {
            while (hovered != null) {
                Tooltip tooltip = Tooltip.of(hovered);
                if (!tooltip.isEmpty()) {
                    tooltip.render(graphics, mouseX, mouseY);
                    return;
                }
                hovered = hovered.getParent().orElse(null);
            }
        }
    }

    /**
     * One game tick for the whole tree, see GuiComponent#tick.
     */
    public void tick() {
        runPendingTasks();
        root.tick();
    }

    private void runPendingTasks() {
        Runnable task;
        while ((task = pendingTasks.poll()) != null) {
            try {
                task.run();
            } catch (Exception e) {
                DragonLib.LOGGER.error("Error while running a queued GUI task.", e);
            }
        }
    }

    public void mouseMoved(double mouseX, double mouseY) {
        router.mouseMoved(pointerRoot(mouseX, mouseY), mouseX, mouseY);
    }

    /**
     * Routes a mouse press. A click outside an open popup dismisses it instead, and a press inside a
     * window activates that window.
     *
     * @return whether the GUI handled the press
     */
    public boolean mousePressed(double mouseX, double mouseY, int button) {
        if (!isInsidePopup(mouseX, mouseY) && dismissLightPopups()) {
            return true;
        }
        GuiComponent target = pointerRoot(mouseX, mouseY);
        boolean handled = router.mousePressed(target, mouseX, mouseY, button);
        if (handled && target != popupRoot) {
            windowOf(router.pressedComponent()).ifPresent(this::activate);
        }
        return handled;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return router.mouseReleased(pointerRoot(mouseX, mouseY), mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return router.mouseDragged(pointerRoot(mouseX, mouseY), mouseX, mouseY, button, deltaX, deltaY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        return router.mouseScrolled(pointerRoot(mouseX, mouseY), mouseX, mouseY, deltaX, deltaY);
    }

    /**
     * Routes a key press to the focused component and up its parent chain, and handles Tab traversal if
     * nobody consumed it. Escape is not handled here, see {@link #escapePressed()}.
     *
     * @return whether the GUI handled the key
     */
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (router.keyPressed(root, keyboardScope(), keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_TAB && focusTraversal.get()) {
            return router.focusNext(keyboardScope(), (modifiers & GLFW.GLFW_MOD_SHIFT) != 0);
        }
        return false;
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return router.keyReleased(root, keyboardScope(), keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return router.charTyped(root, keyboardScope(), codePoint, modifiers);
    }

    public boolean filesDropped(List<Path> paths) {
        return router.filesDropped(inputRoot(), paths);
    }

    /**
     * Handles Escape: closes the topmost popup, otherwise the active window or its whole layer if that
     * layer is modal. Respects closeOnEscape on both.
     *
     * @return whether something was actually closed
     */
    public boolean escapePressed() {
        Popup popup = topmostPopup();
        if (popup != null) {
            if (!popup.closeOnEscape.get()) {
                return false;
            }
            popup.close();
            return true;
        }
        ModalLayer layer = currentLayer();
        if (layer == null) {
            return false;
        }
        Window window = layer.getActiveWindow() != null ? layer.getActiveWindow() : layer.topmostWindow();
        if (window == null || !window.closeOnEscape.get()) {
            return false;
        }
        if (layer.isModal()) {
            closeModal(layer.getId());
        } else {
            closeWindow(window);
        }
        return true;
    }

    /**
     * Shuts the GUI down: closes all popups and windows and drops the input state. Call this from the
     * screen's onClose.
     */
    public void onClosed() {
        closeAllPopups();
        router.clearState();
        closeAll();
    }

    private static Optional<Window> windowOf(GuiComponent component) {
        return component == null ? Optional.empty() : component.getParent(Window.class);
    }
}
