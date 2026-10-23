package de.mrjulsen.mcdragonlib.gui.v2.behaviour;

import java.util.ArrayList;
import java.util.List;

import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.event.IGuiEventListener;

/**
 * Base class for behaviours. It keeps the host reference and removes every listener registered via
 * {@link #listen(Class, IGuiEventListener)} on detach, so a behaviour leaves nothing behind.
 */
public abstract class AbstractBehaviour implements Behaviour {

    private record Registration(GuiComponent owner, Class<? extends IEvent> type, EventListenerId id) {}

    private final List<Registration> registrations = new ArrayList<>(3);
    private GuiComponent host;

    @Override
    public final void onAttach(GuiComponent host) {
        this.host = host;
        onAttached(host);
    }

    @Override
    public final void onDetach(GuiComponent host) {
        onDetached(host);
        for (Registration reg : registrations) {
            reg.owner().removeEventListener(reg.type(), reg.id());
        }
        registrations.clear();
        this.host = null;
    }

    /**
     * Override this instead of onAttach. The right place to register listeners.
     */
    protected void onAttached(GuiComponent host) {
    }

    /**
     * Override this instead of onDetach. Runs before the listeners are removed.
     */
    protected void onDetached(GuiComponent host) {
    }

    /**
     * The component this behaviour is attached to, or null while it is not attached.
     */
    public final GuiComponent host() {
        return host;
    }

    public final boolean isAttached() {
        return host != null;
    }

    /**
     * Removes this behaviour from its host.
     */
    public final void detach() {
        if (host != null) {
            host.removeBehaviour(this);
        }
    }

    /**
     * Registers a listener on the host that is removed again automatically on detach.
     */
    protected final <T extends IEvent> void listen(Class<T> type, IGuiEventListener<GuiComponent, T> listener) {
        listen(host, type, listener, 0);
    }

    protected final <T extends IEvent> void listen(Class<T> type, IGuiEventListener<GuiComponent, T> listener, int priority) {
        listen(host, type, listener, priority);
    }

    /**
     * Registers a listener on another component, also cleaned up on detach. Needed by behaviours that
     * watch a component other than their host.
     */
    protected final <T extends IEvent> void listen(GuiComponent owner, Class<T> type, IGuiEventListener<GuiComponent, T> listener, int priority) {
        registrations.add(new Registration(owner, type, owner.addEventListener(type, listener, priority)));
    }
}
