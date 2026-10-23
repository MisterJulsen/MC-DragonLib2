package de.mrjulsen.mcdragonlib.gui.v2.window;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.render.DLGuiGraphics;

/**
 * A stack of windows that belong together. Layers sit on top of each other; only the topmost one
 * gets input, the ones below are suspended.
 * <p>A modal layer dims everything beneath it. Within a layer the windows are sorted into three
 * bands, bottom, normal and top, and stacking only happens inside a band, see Window#topLevel.</p>
 * <p>Layers are created and discarded by the {@link WindowManager}; the methods here are for
 * looking, not for touching.</p>
 */
public final class ModalLayer {

    static final class LayerPane extends GuiComponent {

        private final boolean modal;

        LayerPane(boolean modal) {
            super(0, 0, 0, 0);
            this.modal = modal;
            clipsChildren.set(false);
            hitTestVisible.set(false);
        }

        @Override
        protected void render(DLGuiGraphics g, double mouseX, double mouseY) {
            if (modal) {
                g.graphics().fillGradient(0, 0, width(), height(), -1072689136, -804253680);
            }
        }
    }

    private final ModalId id;
    private final boolean modal;
    private final LayerPane pane;
    private final List<Window> windows = new ArrayList<>();
    private Window activeWindow;

    ModalLayer(ModalId id, boolean modal) {
        this.id = id;
        this.modal = modal;
        this.pane = new LayerPane(modal);
    }

    public ModalId getId() {
        return id;
    }

    /**
     * Whether this layer blocks the layers below and dims them.
     */
    public boolean isModal() {
        return modal;
    }

    /**
     * Snapshot of the windows on this layer, sorted by band and stacking order: last on top.
     */
    public List<Window> getWindows() {
        return List.copyOf(windows);
    }

    public int windowCount() {
        return windows.size();
    }

    public boolean isEmpty() {
        return windows.isEmpty();
    }

    public boolean contains(Window window) {
        return windows.contains(window);
    }

    /**
     * The window that currently has the keyboard focus on this layer, or null if there is none.
     */
    public Window getActiveWindow() {
        return activeWindow;
    }

    GuiComponent pane() {
        return pane;
    }

    void resize(int width, int height) {
        pane.setSize(width, height);
    }

    void add(Window window) {
        if (windows.contains(window)) {
            return;
        }
        windows.add(window);
        pane.addComponent(window);
        reorder();
    }

    boolean remove(Window window) {
        if (!windows.remove(window)) {
            return false;
        }
        pane.removeComponent(window);
        if (activeWindow == window) {
            activeWindow = null;
        }
        reorder();
        return true;
    }

    void reorder() {
        List<Window> bottom = new ArrayList<>(windows.size());
        List<Window> normal = new ArrayList<>(windows.size());
        List<Window> top = new ArrayList<>(windows.size());
        for (Window window : windows) {
            if (window.bottomLevel.get()) {
                bottom.add(window);
            } else if (window.topLevel.get()) {
                top.add(window);
            } else {
                normal.add(window);
            }
        }
        windows.clear();
        windows.addAll(bottom);
        windows.addAll(normal);
        windows.addAll(top);

        for (Window window : windows) {
            pane.bringToFront(window);
        }
    }

    void moveToBandFront(Window window) {
        if (!windows.remove(window)) {
            return;
        }
        windows.add(window);
        reorder();
    }

    void moveToBandBack(Window window) {
        if (!windows.remove(window)) {
            return;
        }
        windows.add(0, window);
        reorder();
    }

    Window topmostWindow() {
        List<GuiComponent> children = pane.getComponents();
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i) instanceof Window window && window.visible.get()) {
                return window;
            }
        }
        return null;
    }

    void suspend() {
        if (activeWindow != null) {
            activeWindow.notifyActivationChanged(false);
        }
    }

    void resume() {
        if (activeWindow != null) {
            activeWindow.notifyActivationChanged(true);
        }
    }

    boolean setActiveWindow(Window window) {
        if (activeWindow == window) {
            return false;
        }
        Window previous = activeWindow;
        activeWindow = window;
        if (previous != null) {
            previous.notifyActivationChanged(false);
        }
        if (window != null) {
            moveToBandFront(window);
            window.notifyActivationChanged(true);
        }
        return true;
    }
}
