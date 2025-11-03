package de.mrjulsen.mcdragonlib.client.gui.container;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.menu.PlayerInventoryContainerMenu;

public class DLPlayerInventoryComponent<T extends PlayerInventoryContainerMenu> extends DLGuiComponent {

    public DLPlayerInventoryComponent(int x, int y, T menu) {
        super(x, y, 100, 100);
        int size = menu.slots.size();
        setSize(18 * 9, size / 9 * 18 + 4);
        for (int i = 0; i < size; i++) {
            DLSlot slot = new DLSlot((i % 9) * 18, (int)(i / 9) * 18 + (size - i <= 9 ? 4 : 0), 18, 18, menu.slots.get(i), menu);
            addComponent(slot);
        }        
    }
    
}
