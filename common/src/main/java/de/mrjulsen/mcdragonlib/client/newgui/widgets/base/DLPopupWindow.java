package de.mrjulsen.mcdragonlib.client.newgui.widgets.base;

import de.mrjulsen.mcdragonlib.client.newgui.events.DLGuiCommonEvents;

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
