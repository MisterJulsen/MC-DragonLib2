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
