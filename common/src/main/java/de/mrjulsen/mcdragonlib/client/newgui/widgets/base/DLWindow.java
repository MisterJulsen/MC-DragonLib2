package de.mrjulsen.mcdragonlib.client.newgui.widgets.base;

import java.util.Optional;

import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiCommonEvents;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLWindowManager.ModalId;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.util.Property;
import net.minecraft.client.Minecraft;

@SupportsEvents({
    DLGuiCommonEvents.WindowCreatedEvent.class,
    DLGuiCommonEvents.WindowFocusEvent.class,
})
public class DLWindow extends DLGuiComponent {

    public static enum WindowPosition {
        CUSTOM,
        CENTER,
        PARENT
    }

    public final Property<WindowPosition> windowSpawnPosition = new Property<>(WindowPosition.CUSTOM); 

    private ModalId modal;

    public DLWindow(DLWindowManager manager) {
        super(0, 0, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
        setWindowManager(manager);
        this.anchor.set2();
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
}
