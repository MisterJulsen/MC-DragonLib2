/*
 * DragonLib 3 - 1.21.1
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

package de.mrjulsen.mcdragonlib.util;

import java.util.ArrayList;
import java.util.Collection;

public class OrderableArrayList<T> extends ArrayList<T> {

    public OrderableArrayList() {}

    public OrderableArrayList(Collection<T> initialElements) {
        addAll(initialElements);
    }

    public void moveForth(int srcIndex, int amount) {
        T obj = remove(srcIndex);
        add(srcIndex + amount, obj);
    }

    public void moveBack(int srcIndex, int amount) {
        T obj = remove(srcIndex);
        add(srcIndex - amount, obj);
    }

    public void moveToStart(int srcIndex) {
        moveBack(srcIndex, srcIndex);
    }

    public void moveToEnd(int srcIndex) {
        moveForth(srcIndex, size() - 1 - srcIndex);
    }
    
}
