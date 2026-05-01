package de.mrjulsen.mcdragonlib.events;


@FunctionalInterface
public interface IEventListener<S extends IEventDispatcher<S>, T extends IEvent> {
    /**
     * Executes the event.
     * @param source The component this event is assigned to.
     * @param event The event parameters.
     * @return Indicates whether the event is consumed. {@code true} if the execution of other event listeners should be canceled, {@code false} otherwise. If it doesn't matter, use {@code false} (default).
     */
    boolean invoke(S source, T event);
}
