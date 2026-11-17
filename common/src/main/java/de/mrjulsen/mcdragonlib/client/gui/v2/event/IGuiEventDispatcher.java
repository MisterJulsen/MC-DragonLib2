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

import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.events.IEvent;

/**
 * Everything that can hold event listeners and fire events, i.e. every {@link GuiComponent}.
 */
public interface IGuiEventDispatcher<S extends IGuiEventDispatcher<S>> {

    EventListenerRegistry getEventListeners();

    /**
     * Registers a listener at normal priority.
     *
     * @return the id needed to remove it again
     */
    default <T extends IEvent> EventListenerId addEventListener(Class<T> type, IGuiEventListener<S, T> listener) {
        return addEventListener(type, listener, 0);
    }

    /**
     * Registers a listener. Listeners with a higher priority run first, see the PRIORITY constants in
     * {@link GuiEvent}.
     *
     * @return the id needed to remove it again
     */
    default <T extends IEvent> EventListenerId addEventListener(Class<T> type, IGuiEventListener<S, T> listener, int priority) {
        return getEventListeners().add(type, listener, priority);
    }

    default <T extends IEvent> boolean removeEventListener(Class<T> type, EventListenerId id) {
        return getEventListeners().remove(type, id);
    }

    /**
     * Whether anybody listens for this event type, counting listeners registered on its supertypes.
     * Used to skip building events nobody wants.
     */
    default boolean hasEventListeners(Class<? extends IEvent> type) {
        return getEventListeners().has(type);
    }

    /**
     * Fires an event at this dispatcher.
     *
     * @return false if a listener consumed the event, true if it may be handled as usual
     */
    default <T extends IEvent> boolean invokeEvent(S src, T event) {
        return invokeEvent(src, event, true);
    }

    /**
     * Fires an event at this dispatcher.
     *
     * @param allowCancellation false ignores {@link GuiEvent#consume()}, so every listener runs
     * @return false if a listener consumed the event, true if it may be handled as usual
     */
    default <T extends IEvent> boolean invokeEvent(S src, T event, boolean allowCancellation) {
        return getEventListeners().dispatch(src, event, allowCancellation);
    }
}
