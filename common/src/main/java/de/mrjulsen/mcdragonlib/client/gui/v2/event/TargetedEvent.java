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

package de.mrjulsen.mcdragonlib.client.gui.v2.event;

import de.mrjulsen.mcdragonlib.client.gui.v2.GuiComponent;

/**
 * An event that belongs to one specific component but is also delivered to its ancestors.
 */
public abstract class TargetedEvent extends GuiEvent {

    private final GuiComponent target;

    protected TargetedEvent(GuiComponent target) {
        this.target = target;
    }

    /**
     * The component the event originally happened on, not necessarily the one currently handling it.
     */
    public GuiComponent target() {
        return target;
    }

    /**
     * Whether the given component is the original target rather than just an ancestor of it.
     */
    public boolean isDirect(GuiComponent component) {
        return target == component;
    }
}
