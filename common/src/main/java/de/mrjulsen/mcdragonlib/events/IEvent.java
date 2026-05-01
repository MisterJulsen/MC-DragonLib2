package de.mrjulsen.mcdragonlib.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The basis of an event class for the DragonLib Event system.
 */
public interface IEvent {
    public enum Phase {
        PRE,
        POST
    }

    /**
     * @return Whether the event can be canceled or not.
     */
    default boolean isCancellable() {
        return !this.getClass().isAnnotationPresent(NotCancellable.class);
    }

    default boolean ifCancellable(boolean cancel) {
        return isCancellable() && cancel;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface NotCancellable {}
}
