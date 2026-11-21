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

package de.mrjulsen.mcdragonlib.client.gui.v2.theme;

import de.mrjulsen.mcdragonlib.client.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.v2.render.DLGuiGraphics;

@FunctionalInterface
/**
 * Draws the background of a widget, so the look can be swapped out through the theme instead of by
 * subclassing. Widgets keep their renderer in a theme key of their own, e.g. Button#RENDERER.
 * <p>Coordinates are the widget's local ones, so the rectangle usually starts at 0/0.</p>
 */
public interface WidgetRenderer<T extends GuiComponent> {

    /**
     * Draws nothing. For widgets that should have no background at all.
     */
    WidgetRenderer<?> NONE = (g, component, x, y, width, height) -> {};

    void render(DLGuiGraphics g, T component, double x, double y, double width, double height);
}
