/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
