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

package de.mrjulsen.mcdragonlib.client.gui.v2.event;

import de.mrjulsen.mcdragonlib.client.gui.v2.GuiComponent;

/**
 * An event with a mouse position. The position is stored in screen space, because the same event is
 * delivered to several components; use the conversion methods to get it in the space you need.
 */
public abstract class PointerEvent extends TargetedEvent {

    private final double screenX;
    private final double screenY;

    protected PointerEvent(GuiComponent target, double screenX, double screenY) {
        super(target);
        this.screenX = screenX;
        this.screenY = screenY;
    }

    public double screenX() {
        return screenX;
    }

    public double screenY() {
        return screenY;
    }

    /**
     * Mouse x relative to the given component's top left corner, ignoring its scroll offset.
     */
    public double localX(GuiComponent component) {
        return component.getTransform().toLocalX(screenX);
    }

    /**
     * Mouse y relative to the given component's top left corner, ignoring its scroll offset.
     */
    public double localY(GuiComponent component) {
        return component.getTransform().toLocalY(screenY);
    }

    /**
     * Mouse x in the given component's content space, i.e. including its scroll offset.
     */
    public double contentX(GuiComponent component) {
        return component.getTransform().screenToContentX(screenX);
    }

    /**
     * Mouse y in the given component's content space, i.e. including its scroll offset.
     */
    public double contentY(GuiComponent component) {
        return component.getTransform().screenToContentY(screenY);
    }
}
