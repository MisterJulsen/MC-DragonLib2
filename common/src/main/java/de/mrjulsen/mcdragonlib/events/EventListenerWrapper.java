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

package de.mrjulsen.mcdragonlib.events;

import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public class EventListenerWrapper<E extends IEventListener<?, ?>> implements Comparable<EventListenerWrapper<E>> {

    private final @NotNull E event;
    private final int priority;
    private final EventListenerId id;

    public EventListenerWrapper(@NotNull E event, int priority, EventListenerId id) {
        this.event = event;
        this.priority = priority;
        this.id = id;
    }

    public E event() {
        return event;
    }

    public int priority() {
        return priority;
    }

    @Override
    public int compareTo(EventListenerWrapper<E> o) {
        return Integer.compare(priority(), o.priority());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventListenerWrapper<?> o) {
            return id.equals(o.id);
        } else if (obj instanceof EventListenerId o) {
            return id.equals(o);
        } else if (obj instanceof UUID o) {
            return id.equals(o);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s[priority=%s,id=%s]", event().toString(), priority(), id);
    }
}
