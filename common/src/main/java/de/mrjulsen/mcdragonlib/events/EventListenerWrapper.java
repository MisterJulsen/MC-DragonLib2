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
