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

package de.mrjulsen.mcdragonlib.client.gui.widgets.base;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ModalWindowStack extends ConcurrentLinkedDeque<DLWindow> {
    private final ModalId id;

    public ModalWindowStack(UUID id) {
        Objects.requireNonNull(id);
        this.id = new ModalId(id);
    }

    @Override
    public boolean add(DLWindow win) {
        win.assignToModal(id);
        boolean b = super.add(win);
        return b;
    }

    @Override
    public DLWindow remove() {
        DLWindow win = super.remove();
        win.assignToModal(null);
        return win;
    }

    @Override
    public void clear() {
        for (DLWindow win : this) {
            win.assignToModal(null);
        }
        super.clear();
    }

    public ModalId id() {
        return id;
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModalWindowStack o) {
            return id.equals(o.id);
        }
        if (obj instanceof ModalId o) {
            return id.equals(o);
        }
        if (obj instanceof UUID o) {
            return id.equals(o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
