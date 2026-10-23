package de.mrjulsen.mcdragonlib.gui.v2.input;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import de.mrjulsen.mcdragonlib.gui.v2.GuiComponent;
import de.mrjulsen.mcdragonlib.gui.v2.behaviour.AbstractBehaviour;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvent;
import de.mrjulsen.mcdragonlib.gui.v2.event.GuiEvents;

/**
 * Keyboard shortcuts of a component. Attach it to a window for window-wide shortcuts, or to the
 * window manager's root for global ones.
 * <p>Key events bubble up from the focused component, so a hotkey table catches everything that
 * happens inside its own subtree. Bindings run at low priority, so a focused text box sees the key
 * first.</p>
 */
public final class Hotkeys extends AbstractBehaviour {

    /**
     * One shortcut: a combination, the action it runs, and optionally a condition and an id.
     */
    public static final class Binding {

        private final String id;
        private Hotkey hotkey;
        private Runnable action;
        private BooleanSupplier enabled = () -> true;

        private Binding(String id, Hotkey hotkey, Runnable action) {
            this.id = id;
            this.hotkey = hotkey;
            this.action = action;
        }

        public String id() {
            return id;
        }

        public Hotkey hotkey() {
            return hotkey;
        }

        /**
         * Rebinds this shortcut to another combination.
         *
         * @return this binding, for chaining
         */
        public Binding hotkey(Hotkey value) {
            this.hotkey = value;
            return this;
        }

        /**
         * Replaces the action.
         *
         * @return this binding, for chaining
         */
        public Binding action(Runnable value) {
            this.action = value;
            return this;
        }

        /**
         * Only runs the action while the condition holds, e.g. so paste is inactive with an empty clipboard.
         * A disabled binding does not swallow the key either.
         *
         * @return this binding, for chaining
         */
        public Binding enabledWhen(BooleanSupplier condition) {
            this.enabled = condition == null ? () -> true : condition;
            return this;
        }

        public boolean isEnabled() {
            return enabled.getAsBoolean();
        }

        public void run() {
            if (action != null) {
                action.run();
            }
        }
    }

    private final List<Binding> bindings = new ArrayList<>(4);

    /**
     * The component's hotkey table, created on first use.
     */
    public static Hotkeys of(GuiComponent component) {
        Hotkeys existing = component.addBehaviour(Hotkeys.class);
        return existing != null ? existing : component.addBehaviour(new Hotkeys());
    }

    @Override
    protected void onAttached(GuiComponent host) {
        listen(GuiEvents.KeyPressEvent.class, this::onKeyPressed, GuiEvent.PRIORITY_LOW);
    }

    /**
     * Registers a shortcut without an id.
     *
     * @return the binding, so it can be changed or removed later
     */
    public Binding add(Hotkey hotkey, Runnable action) {
        return add(hotkey, null, action);
    }

    /**
     * Registers a shortcut under an id, which lets menu entries look up and display the combination,
     * see {@link #lookup(GuiComponent, String)}.
     *
     * @return the binding, so it can be changed or removed later
     */
    public Binding add(Hotkey hotkey, String id, Runnable action) {
        Binding binding = new Binding(id, hotkey, action);
        bindings.add(binding);
        return binding;
    }

    public boolean remove(Binding binding) {
        return bindings.remove(binding);
    }

    public boolean remove(String id) {
        return bindings.removeIf(binding -> id != null && id.equals(binding.id()));
    }

    public void clear() {
        bindings.clear();
    }

    public List<Binding> bindings() {
        return List.copyOf(bindings);
    }

    /**
     * The combination registered under this id in this table, or null.
     */
    public Hotkey find(String id) {
        for (Binding binding : bindings) {
            if (id != null && id.equals(binding.id())) {
                return binding.hotkey();
            }
        }
        return null;
    }

    /**
     * Searches the tables of this component and its ancestors for the id. Used to show the right
     * combination next to a menu entry.
     *
     * @return the combination, or null if nothing is registered under that id
     */
    public static Hotkey lookup(GuiComponent from, String id) {
        GuiComponent current = from;
        while (current != null) {
            Hotkeys table = current.addBehaviour(Hotkeys.class);
            if (table != null) {
                Hotkey hotkey = table.find(id);
                if (hotkey != null) {
                    return hotkey;
                }
            }
            current = current.getParent().orElse(null);
        }
        return null;
    }

    /**
     * Runs the first matching, enabled binding. Called from the key listener, but also useful to fire a
     * shortcut by hand.
     *
     * @return whether a binding ran
     */
    public boolean trigger(int keyCode, int modifiers) {
        for (Binding binding : bindings) {
            Hotkey hotkey = binding.hotkey();
            if (hotkey != null && hotkey.matches(keyCode, modifiers) && binding.isEnabled()) {
                binding.run();
                return true;
            }
        }
        return false;
    }

    private void onKeyPressed(GuiComponent source, GuiEvents.KeyPressEvent event) {
        if (trigger(event.keyCode(), event.modifiers())) {
            event.consume();
        }
    }
}
