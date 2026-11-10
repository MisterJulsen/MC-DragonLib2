package de.mrjulsen.mcdragonlib.events;

public class EventNotSupportedException extends RuntimeException {
    
    @SuppressWarnings("rawtypes")
    public EventNotSupportedException(Class<?> dispatcher, Class<? extends IEvent> event) {
        super(String.format("The event '%s' is not supported by '%s'. Please check the '@SupportsEvents' annotation.", event.getSimpleName(), dispatcher.getSimpleName()));
    }

}
