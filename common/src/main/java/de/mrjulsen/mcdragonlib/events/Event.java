package de.mrjulsen.mcdragonlib.events;

public class Event<T> {
    private final Class<T> type;
    private final String id;

    public Event(Class<T> type, String id) {
        this.type = type;
        this.id = id;
    }

    public Class<T> getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    // equals & hashCode könnten auf ID basieren, wenn nötig
}
