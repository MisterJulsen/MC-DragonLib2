package de.mrjulsen.mcdragonlib.gui.v2.window;

import java.util.Optional;

import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.events.IEvent.NotCancellable;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.input.Hotkeys;
import de.mrjulsen.mcdragonlib.gui.v2.input.MoveHandle;
import de.mrjulsen.mcdragonlib.gui.v2.input.ResizeHandle;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;

/**
 * A window: the root component of one screen's worth of content, managed by a {@link WindowManager}.
 * <p>A window is not usable on its own, it has to be opened through the manager, which puts it on a
 * {@link ModalLayer} and keeps track of stacking and activation. Being movable, resizable or
 * fullscreen is just a matter of the properties below; the handles behind them are attached on
 * demand.</p>
 */
public class Window extends GuiComponent {

    @NotCancellable
    /**
     * Fired once the window has been added to a layer and placed. Its size is final at this point.
     */
    public record WindowOpenedEvent(WindowManager manager, ModalId modal) implements IEvent {}

    @NotCancellable
    /**
     * Fired after the window was removed from its layer.
     */
    public record WindowClosedEvent(WindowManager manager) implements IEvent {}

    @NotCancellable
    /**
     * Fired when the window becomes the active one of its layer, or stops being it. Only one window per
     * layer is active, and a suspended layer has none.
     */
    public record WindowActivationEvent(boolean active) implements IEvent {}

    /**
     * Where a window ends up when it is opened, and where it goes when the screen is resized.
     */
    public enum SpawnPosition {
        CUSTOM,
        CENTER,
        OWNER_CENTER
    }

    private ModalId modalId;
    private boolean active;
    private Rectangle windowedBounds;
    private boolean applyingFullscreen;

    /**
     * Whether Escape closes this window. On a modal layer the whole layer goes with it.
     */
    public final BooleanProperty closeOnEscape = new BooleanProperty(true);
    /**
     * Where the window is placed when it opens, and where it goes when the screen is resized.
     */
    public final Property<SpawnPosition> spawnPosition = new Property<>(SpawnPosition.CENTER);
    /**
     * The window this one belongs to. Closing the owner closes this window too, and
     * {@link SpawnPosition#OWNER_CENTER} centers on it.
     */
    public final Property<Window> owner = new Property<>(null);
    /**
     * Keeps this window above all normal ones of its layer. Within that band the usual stacking applies.
     */
    public final BooleanProperty topLevel = new BooleanProperty(false);
    /**
     * Keeps this window below all normal ones of its layer, e.g. for a background panel.
     */
    public final BooleanProperty bottomLevel = new BooleanProperty(false);
    /**
     * Whether the game pauses in single player while this window is visible.
     */
    public final BooleanProperty pauseGame = new BooleanProperty(false);
    /**
     * Whether the window becomes the active one of its layer right after opening.
     */
    public final BooleanProperty activateOnSpawn = new BooleanProperty(true);
    /**
     * Whether the user can drag the window around. Grabs the whole window; use
     * {@link #moveWith(GuiComponent)} to limit that to a title bar.
     */
    public final BooleanProperty movable = new BooleanProperty(false);
    /**
     * Whether the user can resize the window at its edges, see {@link #resizeHandle()} for the details.
     */
    public final BooleanProperty resizable = new BooleanProperty(false);
    /**
     * Stretches the window across the whole screen and blocks moving and resizing. The previous bounds
     * are remembered and restored when it is turned off again.
     */
    public final BooleanProperty fullscreen = new BooleanProperty(false);

    /**
     * @param width initial width, which the spawn position is calculated from
     * @param height initial height, which the spawn position is calculated from
     */
    public Window(int width, int height) {
        super(0, 0, width, height);
        movable.addChangeListener((o, n) -> updateMoveHandle(n));
        resizable.addChangeListener((o, n) -> updateResizeHandle(n));
        fullscreen.addChangeListener((o, n) -> applyFullscreen(n));
        topLevel.addChangeListener((o, n) -> requestReorder());
        bottomLevel.addChangeListener((o, n) -> requestReorder());
    }

    /**
     * The hotkey scope of this window, creating it on first use. Hotkeys registered here work as long as
     * the window has the focus.
     */
    public Hotkeys hotkeys() {
        return Hotkeys.of(this);
    }

    private void requestReorder() {
        WindowManager manager = getWindowManager();
        if (manager != null) {
            manager.bringToFront(this);
        }
    }

    private void updateMoveHandle(boolean value) {
        if (!value && addBehaviour(MoveHandle.class) == null) {
            return;
        }
        moveHandle().enabled.set(value && !fullscreen.get());
    }

    private void updateResizeHandle(boolean value) {
        if (!value && addBehaviour(ResizeHandle.class) == null) {
            return;
        }
        resizeHandle().enabled.set(value && !fullscreen.get());
    }

    /**
     * The behaviour that handles dragging, created on first use. Use it to set the grab border or to
     * restrict movement to one axis.
     */
    public MoveHandle moveHandle() {
        MoveHandle handle = addBehaviour(MoveHandle.class);
        if (handle == null) {
            handle = MoveHandle.attach(this);
            handle.enabled.set(movable.get() && !fullscreen.get());
        }
        return handle;
    }

    /**
     * The behaviour that handles resizing, created on first use. Use it to set the grab border or which
     * edges may be dragged.
     */
    public ResizeHandle resizeHandle() {
        ResizeHandle handle = addBehaviour(ResizeHandle.class);
        if (handle == null) {
            handle = ResizeHandle.attach(this);
            handle.enabled.set(resizable.get() && !fullscreen.get());
        }
        return handle;
    }

    /**
     * Makes the window draggable by another component instead of by itself, e.g. a title bar. Replaces
     * the window's own move handle.
     *
     * @param handle the component the user grabs
     * @return the move handle attached to that component
     */
    public MoveHandle moveWith(GuiComponent handle) {
        removeBehaviour(MoveHandle.class);
        MoveHandle moveHandle = MoveHandle.attach(handle, this);
        moveHandle.enabled.set(movable.get());
        return moveHandle;
    }

    private void applyFullscreen(boolean value) {
        WindowManager manager = getWindowManager();
        if (manager == null) {
            return;
        }
        applyingFullscreen = true;
        try {
            if (value) {
                windowedBounds = Rectangle.withSize(dX(), dY(), dWidth(), dHeight());
                setBounds(0, 0, manager.screenWidth(), manager.screenHeight());
            } else if (windowedBounds != null) {
                setBounds(windowedBounds.x(), windowedBounds.y(), windowedBounds.width(), windowedBounds.height());
                windowedBounds = null;
            }
        } finally {
            applyingFullscreen = false;
        }
        MoveHandle moveHandle = addBehaviour(MoveHandle.class);
        if (moveHandle != null) {
            moveHandle.enabled.set(!value && movable.get());
        }
        ResizeHandle resizeHandle = addBehaviour(ResizeHandle.class);
        if (resizeHandle != null) {
            resizeHandle.enabled.set(!value && resizable.get());
        }
    }

    void refitFullscreen(double screenWidth, double screenHeight) {
        if (!fullscreen.get()) {
            return;
        }
        applyingFullscreen = true;
        try {
            setBounds(0, 0, screenWidth, screenHeight);
        } finally {
            applyingFullscreen = false;
        }
    }

    @Override
    public void setBounds(double x, double y, double width, double height) {
        if (fullscreen.get() && !applyingFullscreen) {
            return;
        }
        super.setBounds(x, y, width, height);
    }

    /**
     * The layer this window is on, or empty while it is not open.
     */
    public Optional<ModalId> getModalId() {
        return Optional.ofNullable(modalId);
    }

    /**
     * Whether this is the active window of its layer, i.e. the one that gets the keyboard input.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Whether this window is currently managed by a window manager.
     */
    public boolean isOpen() {
        return modalId != null;
    }

    /**
     * Makes this the active window of its layer and brings it to the front. Only works on the topmost
     * layer.
     */
    public void activate() {
        WindowManager manager = getWindowManager();
        if (manager != null) {
            manager.activate(this);
        }
    }

    /**
     * Closes this window and everything it owns. Closing the last window of a layer discards the layer,
     * and closing the last layer closes the screen.
     */
    public void close() {
        WindowManager manager = getWindowManager();
        if (manager != null) {
            manager.closeWindow(this);
        }
    }

    public void centerOnScreen() {
        WindowManager manager = getWindowManager();
        if (manager == null) {
            return;
        }
        setPosition(
            Math.round((manager.screenWidth() - dWidth()) / 2D),
            Math.round((manager.screenHeight() - dHeight()) / 2D)
        );
    }

    /**
     * Centers this window on its {@link #owner}, or on the screen if it has none.
     */
    public void centerOnOwner() {
        Window parentWindow = owner.get();
        if (parentWindow == null) {
            centerOnScreen();
            return;
        }
        setPosition(
            Math.round(parentWindow.dX() + (parentWindow.dWidth() - dWidth()) / 2D),
            Math.round(parentWindow.dY() + (parentWindow.dHeight() - dHeight()) / 2D)
        );
    }

    /**
     * Applies the {@link #spawnPosition} right now. Called when the window opens and when the screen is
     * resized; {@link SpawnPosition#CUSTOM} keeps the current position.
     */
    public void applySpawnPosition() {
        if (fullscreen.get()) {
            WindowManager manager = getWindowManager();
            if (manager != null) {
                refitFullscreen(manager.screenWidth(), manager.screenHeight());
            }
            return;
        }
        switch (spawnPosition.get()) {
            case CENTER -> centerOnScreen();
            case OWNER_CENTER -> centerOnOwner();
            default -> {
            }
        }
    }

    /**
     * Called when the window was opened and positioned. Override to build content that needs the final
     * size.
     */
    protected void onOpened() {
    }

    /**
     * Called when the window was closed. Override to clean up.
     */
    protected void onClosed() {
    }

    void notifyOpened(WindowManager manager, ModalId id) {
        this.modalId = id;
        if (fullscreen.get()) {
            applyFullscreen(true);
        }
        applySpawnPosition();
        onOpened();
        invokeEvent(this, new WindowOpenedEvent(manager, id));
    }

    void notifyClosed(WindowManager manager) {
        this.modalId = null;
        this.active = false;
        onClosed();
        invokeEvent(this, new WindowClosedEvent(manager));
    }

    void notifyActivationChanged(boolean value) {
        if (this.active == value) {
            return;
        }
        this.active = value;
        invokeEvent(this, new WindowActivationEvent(value));
    }
}
