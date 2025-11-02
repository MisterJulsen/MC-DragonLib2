package de.mrjulsen.mcdragonlib.client.gui.widgets.base;

import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class DLMenuWindow<M extends AbstractContainerMenu> extends DLWindow implements MenuAccess<M> {

    protected final M menu;

    public DLMenuWindow(Class<M> menuType, DLWindowManager manager) {
        super(manager);
        if (menuType == null) {
            throw new IllegalArgumentException("No menu type defined.");
        }
        if (!manager.supportsMenus()) {
            throw new IllegalArgumentException("The current window manager doesn't support menus.");
        }
        if (!menuType.isInstance(getWindowManager().getMenu())) {
            throw new IllegalArgumentException("The provided menu type is not supported by the current window manager. Expected: " + manager.getMenu().getClass().getSimpleName() + ", Provided: " + menuType.getSimpleName());
        }
        this.menu = menuType.cast(manager.getMenu());
    }

    @Override
    public final M getMenu() {
        return menu;
    }
    
}
