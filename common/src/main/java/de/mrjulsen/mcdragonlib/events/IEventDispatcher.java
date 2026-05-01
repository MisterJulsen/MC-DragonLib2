package de.mrjulsen.mcdragonlib.events;

import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;

import java.util.Collections;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

/**
 * Implemented by classes that use the DragonLib event system.
 * <p>
 * While this interface already provides methods for adding, notifying, and removing {@code EventListener}s,
 * the storage of the {@code EventListener}s must be done in the class itself and passed to the interface using
 * {@link IEventDispatcher#getEventListeners}.
 * </p>
 * <p>
 * This interface requires that the implementing class also uses the {@link SupportsEvents} annotation
 * to specify which {@link IEvent}s should be supported. If an event is added with
 * {@link IEventDispatcher#addEventListener} that is not listed in {@link SupportsEvents#value}, or if the
 * annotation is not used at all, an {@link EventNotSupportedException} is thrown, stating that the event is
 * not supported.
 */
public interface IEventDispatcher<S extends IEventDispatcher<S>> {

    Map<Class<? extends IEvent>, PriorityQueue<EventListenerWrapper<?>>> getEventListeners();

    default <T extends IEvent> EventListenerId addEventListener(Class<T> aClass, IEventListener<S, T> iEventListener) throws EventNotSupportedException {
        return addEventListener(aClass, iEventListener, 0);
    }

    @SuppressWarnings("unlikely-arg-type")
    default <T extends IEvent> EventListenerId addEventListener(Class<T> aClass, IEventListener<S, T> iEventListener, int priority) throws EventNotSupportedException {
        if (!isEventSupported(aClass)) {
            throw new EventNotSupportedException(getClass(), aClass);
        }
        PriorityQueue<EventListenerWrapper<?>> queue = getEventListeners().computeIfAbsent(aClass, k -> new PriorityQueue<>(Collections.reverseOrder()));
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (queue.contains(id));
        EventListenerId listenerId = new EventListenerId(id);
        queue.add(new EventListenerWrapper<>(iEventListener, priority, listenerId));
        return listenerId;
    }
    
    default <T extends IEvent> boolean invokeEvent(S src, T event) {
        return invokeEvent(src, event, true);
    }

    default <T extends IEvent> boolean invokeEvent(S src, T event, boolean allowCancellation) {
        PriorityQueue<EventListenerWrapper<?>> l = getEventListeners().get(event.getClass());
        if (l != null) {
            PriorityQueue<EventListenerWrapper<?>> tempQueue = new PriorityQueue<>(l);
            while (!tempQueue.isEmpty()) {
                if (((IEventListener<S, T>)tempQueue.poll().event()).invoke(src, event) && allowCancellation) {
                    if (!event.isCancellable()) {
                        throw new IllegalStateException("The event " + event + " cannot be cancelled.");
                    }
                    return false;
                }
            }
        }
        return true;
    }
    
    default boolean isEventSupported(Class<? extends IEvent> eventType) {
        Class<?> currentClass = this.getClass();
        while (currentClass != null) {
            SupportsEvents annotation = currentClass.getAnnotation(SupportsEvents.class);
            if (annotation != null) {
                for (Class<? extends IEvent> supported : annotation.value()) {
                    if (supported.equals(eventType)) {
                        return true;
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return false;
    }

    @SuppressWarnings("unlikely-arg-type")
    default <T extends IEvent> boolean removeEventListener(Class<T> aClass, EventListenerId id) {
        if (getEventListeners().containsKey(aClass)) {
            return getEventListeners().get(aClass).removeIf(x -> x.equals(id));
        }
        return false;
    }
}