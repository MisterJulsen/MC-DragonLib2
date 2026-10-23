package de.mrjulsen.mcdragonlib.gui.v2.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.events.IEvent;

/**
 * The listener table of a single component: event type to listeners, sorted by priority.
 * <p>Dispatching walks the event's class hierarchy upwards, so a listener registered on a base type
 * like PointerEvent also sees the concrete subtypes.</p>
 */
public final class EventListenerRegistry {

    private record Entry(EventListenerId id, IGuiEventListener<?, ?> listener, int priority) {}

    private static final ClassValue<Class<?>[]> DISPATCH_CHAIN = new ClassValue<>() {

        @Override
        protected Class<?>[] computeValue(Class<?> type) {
            List<Class<?>> chain = new ArrayList<>(4);
            Class<?> current = type;
            while (current != null && IEvent.class.isAssignableFrom(current)) {
                chain.add(current);
                current = current.getSuperclass();
            }
            return chain.toArray(new Class<?>[0]);
        }
    };

    private final Map<Class<? extends IEvent>, List<Entry>> listeners = new HashMap<>();

    /**
     * Registers a listener, inserted ahead of all listeners with a lower priority.
     *
     * @return the id needed to remove it again
     */
    public EventListenerId add(Class<? extends IEvent> type, IGuiEventListener<?, ?> listener, int priority) {
        EventListenerId id = new EventListenerId(UUID.randomUUID());
        List<Entry> current = listeners.get(type);
        List<Entry> next = current == null ? new ArrayList<>(1) : new ArrayList<>(current);
        int index = next.size();
        for (int i = 0; i < next.size(); i++) {
            if (next.get(i).priority() < priority) {
                index = i;
                break;
            }
        }
        next.add(index, new Entry(id, listener, priority));
        listeners.put(type, List.copyOf(next));
        return id;
    }

    /**
     * Removes a listener by its id.
     *
     * @return false if no listener with that id was registered for the type
     */
    public boolean remove(Class<? extends IEvent> type, EventListenerId id) {
        List<Entry> current = listeners.get(type);
        if (current == null) {
            return false;
        }
        List<Entry> next = new ArrayList<>(current);
        if (!next.removeIf(entry -> entry.id().equals(id))) {
            return false;
        }
        if (next.isEmpty()) {
            listeners.remove(type);
        } else {
            listeners.put(type, List.copyOf(next));
        }
        return true;
    }

    /**
     * Removes every listener of every type.
     */
    public void clear() {
        listeners.clear();
    }

    /**
     * Whether a listener exists for this type or any of its supertypes.
     */
    public boolean has(Class<? extends IEvent> type) {
        if (listeners.isEmpty()) {
            return false;
        }
        Class<?>[] chain = DISPATCH_CHAIN.get(type);
        for (int i = 0; i < chain.length; i++) {
            if (listeners.containsKey(chain[i])) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    /**
     * Delivers the event to the listeners of its own class and then of each supertype.
     *
     * @param allowCancellation false runs every listener even if one consumes the event
     * @return false if the event was consumed or had no listener at all, true if it ran through
     */
    public <S, T extends IEvent> boolean dispatch(S src, T event, boolean allowCancellation) {
        if (listeners.isEmpty() || !listeners.containsKey(event.getClass())) {
            return false;
        }
        GuiEvent cancellable = allowCancellation && event instanceof GuiEvent gui ? gui : null;
        Class<?>[] chain = DISPATCH_CHAIN.get(event.getClass());
        for (int c = 0; c < chain.length; c++) {
            List<Entry> current = listeners.get(chain[c]);
            if (current == null) {
                continue;
            }
            for (int i = 0; i < current.size(); i++) {
                ((IGuiEventListener<S, T>) current.get(i).listener()).invoke(src, event);
                if (cancellable != null && cancellable.isConsumed()) {
                    return false;
                }
            }
        }
        return true;
    }
}
