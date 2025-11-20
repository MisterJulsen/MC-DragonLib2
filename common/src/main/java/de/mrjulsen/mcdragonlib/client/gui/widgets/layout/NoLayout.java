package de.mrjulsen.mcdragonlib.client.gui.widgets.layout;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;

public class NoLayout implements ILayoutManager {

    public static final NoLayout INSTANCE = new NoLayout();

    private NoLayout() {}

    @Override
    public void arrangeComponents(DLGuiComponent host) {
    }    
}
