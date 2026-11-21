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

package de.mrjulsen.mcdragonlib.client.gui.v2.widget;

import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.client.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.v2.event.GuiEvents;
import de.mrjulsen.mcdragonlib.util.properties.BooleanProperty;

import java.util.List;

public abstract class DLAbstractToggleButton extends DLAbstractButton {

    public record CheckedChangedEvent(boolean checked) implements IEvent {}

    public final BooleanProperty checked = new BooleanProperty(false);
    public final BooleanProperty radioButtonMode = new BooleanProperty(false);

    public final EventListenerId defaultToggleActionEventId;

    public DLAbstractToggleButton(int x, int y, int w, int h) {
        super(x, y, w, h);

        checked.addChangeListener((o, n) -> {
            invokeEvent(this, new CheckedChangedEvent(n));
        });

        defaultToggleActionEventId = addEventListener(GuiEvents.ClickEvent.class, (s, e) -> {
            if (!checked.get() || !radioButtonMode.get()) {
                checked.toggle();
            }
            if (checked.get() && radioButtonMode.get()) {
                getParent().ifPresent(p -> {
                    List<GuiComponent> components = p.getComponentsIf(c -> c != this && c instanceof DLAbstractToggleButton t && t.radioButtonMode.get());
                    for (GuiComponent c : components) {
                        ((DLAbstractToggleButton)c).checked.set(false);
                    }
                });
            }
        });
    }
}
