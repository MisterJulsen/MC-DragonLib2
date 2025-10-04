package de.mrjulsen.mcdragonlib.client.newgui.widgets.windows;

import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiCommonEvents;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.newgui.widgets.base.DLWindowManager;

public class DLPopupWindow extends DLWindow {

    public DLPopupWindow(DLWindowManager manager, int x, int y, int w) {
        super(manager);
        setPosition(x, y);
        setSize(w, 100);
        addEventListener(DLGuiCommonEvents.WindowFocusEvent.class, (src, e) -> {
            if (!e.focus()) {
                e.windowManager().closeModal(this.getAssignedModal().get());
            }
            return false;
        });
    }
    
}
