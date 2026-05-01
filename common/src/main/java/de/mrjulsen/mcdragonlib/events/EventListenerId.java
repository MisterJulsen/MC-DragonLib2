package de.mrjulsen.mcdragonlib.events;

import java.util.UUID;

public class EventListenerId {
    private final UUID id;

    EventListenerId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventListenerId o) {
            return id.equals(o.id);
        } else if (obj instanceof UUID o) {
            return id.equals(o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
