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

import de.mrjulsen.mcdragonlib.events.IEvent;

/**
 * Base class of all GUI events.
 * <p>Input events travel along the hit stack from the innermost component outwards;
 * {@link #consume()} stops that right there and tells the caller the input was handled. Events
 * annotated with NotCancellable cannot be consumed, which is the case for everything that merely
 * reports a change after the fact.</p>
 */
public abstract class GuiEvent implements IEvent {

    private static final ClassValue<Boolean> CANCELLABLE = new ClassValue<>() {

        @Override
        protected Boolean computeValue(Class<?> type) {
            return !type.isAnnotationPresent(NotCancellable.class);
        }
    };

    /**
     * Listener priorities. Higher priority listeners run first, see
     * {@link IGuiEventDispatcher#addEventListener(Class, IGuiEventListener, int)}.
     */
    public static final int PRIORITY_HIGHEST = 2000;
    public static final int PRIORITY_HIGH = 1000;
    public static final int PRIORITY_NORMAL = 0;
    public static final int PRIORITY_LOW = -1000;
    public static final int PRIORITY_LOWEST = -2000;

    private boolean consumed;

    @Override
    public boolean isCancellable() {
        return CANCELLABLE.get(getClass());
    }

    /**
     * Marks this event as handled, so neither the remaining listeners nor the outer components see it.
     * Does nothing for events that are not cancellable.
     */
    public void consume() {
        if (isCancellable()) {
            consumed = true;
        }
    }

    /**
     * Sets the consumed flag explicitly, e.g. to un-consume an event again.
     */
    public void setConsumed(boolean value) {
        this.consumed = value && isCancellable();
    }

    public boolean isConsumed() {
        return consumed;
    }
}
