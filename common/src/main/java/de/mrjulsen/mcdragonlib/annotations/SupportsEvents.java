package de.mrjulsen.mcdragonlib.annotations;

import de.mrjulsen.mcdragonlib.events.EventNotSupportedException;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.events.IEventDispatcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A list of event classes supported by this component. Classes with this annotation must implement the {@link IEventDispatcher} interface; otherwise, this annotation has no effect.
 * <p>
 * If an event is passed with {@link IEventDispatcher#addEventListener} that is not listed in this annotation, an {@link EventNotSupportedException} is thrown with the reason that the event is not supported.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SupportsEvents {
    /**
     * @return A list of event classes supported by this component.
     */
    Class<? extends IEvent>[] value();
}
