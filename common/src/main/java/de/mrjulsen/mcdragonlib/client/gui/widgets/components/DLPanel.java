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

package de.mrjulsen.mcdragonlib.client.gui.widgets.components;

import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.properties.ColorProperty;

@SupportsEvents({
    DLPanel.BackgroundColorChangedEvent.class,
    DLPanel.TextColorChangedEvent.class
})
public class DLPanel extends DLGuiComponent {
    
    public record BackgroundColorChangedEvent(DLColor color) implements IEvent {}
    public record TextColorChangedEvent(DLColor color) implements IEvent {}

    public final ColorProperty textColor = new ColorProperty(DLColor.UNDEFINED, DLColor.TRANSPARENT)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLPanel.TextColorChangedEvent(a), true));
    public final ColorProperty backgroundTint = new ColorProperty(DLColor.UNDEFINED, DLColor.TRANSPARENT)
        .withAfterPropertyChangedCallback((o, a) -> invokeEvent(this, new DLPanel.BackgroundColorChangedEvent(a), true));

    public DLPanel(int x, int y, int w, int h) {
        super(x, y, w, h);
        this.inputConsumptionPolicy.set(c -> true);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.fill(graphics, 0, 0, width(), height(), backgroundTint.get());
    }
    
}
