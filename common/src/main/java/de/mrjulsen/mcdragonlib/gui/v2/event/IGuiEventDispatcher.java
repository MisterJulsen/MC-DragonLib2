package de.mrjulsen.mcdragonlib.gui.v2.event;

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
