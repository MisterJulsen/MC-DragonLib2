package de.mrjulsen.mcdragonlib.client.gui.widgets.base;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.util.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@SupportsEvents({
    DLWindow.WindowCreatedEvent.class,
    DLWindow.WindowFocusEvent.class,
})
public abstract class DLWindow extends DLGuiComponent {

    public record WindowCreatedEvent(DLWindowManager windowManager, ModalId id, int screenWidth, int screenHeight) implements IEvent {}
    public record WindowFocusEvent(DLWindowManager windowManager, boolean focus) implements IEvent {}

    public static enum WindowPosition {
        CUSTOM,
        CENTER,
        PARENT,
        PARENT_CENTER
    }

    private static record WindowSettings(double x, double y, double w, double h, EAlign[] anchor) {}

    private WindowSettings windowedSettings = null;
    private boolean resizeBypass = false;
    
    public final Property<WindowPosition> windowSpawnPosition = new Property<>(WindowPosition.CUSTOM); 
    public final BooleanProperty fullscreen = new BooleanProperty(false, false)
        .withAfterPropertyChangedCallback((o, n) -> {
            if (n) {
                resizeBypass = true;
                windowedSettings = new WindowSettings(x(), y(), width(), height(), anchor.get());
                setPosition(0, 0);
                setSize(getWindowManager().getScreenWidth(), getWindowManager().getScreenHeight());
                anchor.set(EAlign.values());
                resizeBypass = false;
            } else if (!n && windowedSettings != null) {
                setPosition(windowedSettings.x(), windowedSettings.y());
                setSize(windowedSettings.w(), windowedSettings.h());
                anchor.set(windowedSettings.anchor());
            }
        });
        
    public final BooleanProperty topLevel = new BooleanProperty(false, false);
    public final BooleanProperty focusOnSpawn = new BooleanProperty(true, false);
    public final BooleanProperty pauseGame = new BooleanProperty(false, false);


    private ModalId modal;

    public DLWindow(DLWindowManager manager) {
        super(0, 0, 150, 150);
        setWindowManager(manager);
        this.anchor.set2();


        addEventListener(WindowCreatedEvent.class, (s, e) -> {
            switch (windowSpawnPosition.get()) {
                case CENTER -> setPosition(e.windowManager().getScreenWidth() / 2 - width() / 2, e.windowManager().getScreenHeight() / 2 - height() / 2);
                case PARENT -> getParent().ifPresent(p -> {
                    if (p instanceof DLWindow win) {
                        setPosition(win.x() + win.width() / 2 - width() / 2, win.y() + win.height() / 2 - height() / 2);
                    }
                });
                case PARENT_CENTER -> getParent().map(p -> {
                    if (p instanceof DLWindow win) {
                        setPosition(win.x(), win.y());
                    }
                    return null;
                }).orElseGet(() -> {
                    setPosition(e.windowManager().getScreenWidth() / 2 - width() / 2, e.windowManager().getScreenHeight() / 2 - height() / 2);
                    return null;
                });
                default -> {}
            }
            return false;
        });
    }


    public void setParent(DLWindow parent) {
        super.setParent(parent);
    }

    final void assignToModal(ModalId id) {        
        if (id != null && modal != null) {
            throw new IllegalStateException("Each window instance can only be shown once.");
        }
        this.modal = id;
    }

    public Optional<ModalId> getAssignedModal() {
        return Optional.ofNullable(modal);
    }

    @Override
    public void setWidth(double width) {
        if (fullscreen.get() && !resizeBypass && !getWindowManager().isUpdatingLayout()) {
            return;
        }
        super.setWidth(width);
    }

    @Override
    public void setHeight(double height) {
        if (fullscreen.get() && !resizeBypass && !getWindowManager().isUpdatingLayout()) {
            return;
        }
        super.setHeight(height);
    }

    @Override
    public void setX(double x) {
        if (fullscreen.get() && !resizeBypass && !getWindowManager().isUpdatingLayout()) {
            return;
        }
        super.setX(x);
    }

    @Override
    public void setY(double y) {
        if (fullscreen.get() && !resizeBypass && !getWindowManager().isUpdatingLayout()) {
            return;
        }
        super.setY(y);
    }



    public static <T extends DLWindow> T openWindow(WindowBuilder<T> builder) {
        AtomicReference<T> window = new AtomicReference<>(null);
        DLScreenWrapper<?> wrapper = new DLScreenWrapper<>(null, (mgr) -> {
            T win = builder.build(mgr);
            window.set(win);
            return win;
        });
        Minecraft.getInstance().setScreen(wrapper);
        return window.get();
    }

    public static void closeWindow() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof DLScreenWrapper wrapper) {
            wrapper.getWindowManager().close();
        } else {
            Minecraft.getInstance().setScreen(null);
        }
    }
    
}
