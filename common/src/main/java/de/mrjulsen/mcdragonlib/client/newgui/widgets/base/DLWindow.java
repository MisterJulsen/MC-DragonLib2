package de.mrjulsen.mcdragonlib.client.newgui.widgets.base;

import java.util.Optional;

import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiCommonEvents;
import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiCommonEvents.WindowCreatedEvent;
import de.mrjulsen.mcdragonlib.client.newgui.properties.BooleanProperty;
import de.mrjulsen.mcdragonlib.client.newgui.properties.Property;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLWindowManager.ModalId;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.EAlign;

@SupportsEvents({
    DLGuiCommonEvents.WindowCreatedEvent.class,
    DLGuiCommonEvents.WindowFocusEvent.class,
})
public class DLWindow extends DLGuiComponent {

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
        if (modal != null) {
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
    
}
