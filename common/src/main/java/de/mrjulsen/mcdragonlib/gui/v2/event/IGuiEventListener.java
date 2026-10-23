package de.mrjulsen.mcdragonlib.gui.v2.event;

import de.mrjulsen.mcdragonlib.events.IEvent;

@FunctionalInterface
/**
 * A listener for one kind of GUI event.
 *
 * @param <S> type of the component the event is delivered to
 * @param <T> the event type
 */
public interface IGuiEventListener<S, T extends IEvent> {
    void invoke(S source, T event);
}
