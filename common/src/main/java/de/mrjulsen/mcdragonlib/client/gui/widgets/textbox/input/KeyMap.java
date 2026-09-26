package de.mrjulsen.mcdragonlib.client.gui.widgets.textbox.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeyMap<T> {

    @FunctionalInterface
    public interface ICommand<T> {
        boolean run(T target, KeyEvent event);
    }

    public record Binding<T>(IKeyStroke stroke, ICommand<T> command) {}

    private final List<Binding<T>> bindings = new ArrayList<>();

    public KeyMap<T> bind(IKeyStroke stroke, ICommand<T> command) {
        bindings.add(new Binding<>(stroke, command));
        return this;
    }

    public boolean unbind(IKeyStroke stroke) {
        return bindings.removeIf(binding -> binding.stroke() == stroke);
    }

    public void clear() {
        bindings.clear();
    }

    public List<Binding<T>> bindings() {
        return Collections.unmodifiableList(bindings);
    }

    public boolean handle(T target, KeyEvent event) {
        for (int i = bindings.size() - 1; i >= 0; i--) {
            Binding<T> binding = bindings.get(i);
            if (binding.stroke().matches(event) && binding.command().run(target, event)) {
                return true;
            }
        }
        return false;
    }
}
