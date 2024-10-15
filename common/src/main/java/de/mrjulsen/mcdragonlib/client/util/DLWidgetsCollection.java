package de.mrjulsen.mcdragonlib.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.mrjulsen.mcdragonlib.client.gui.widgets.IDragonLibWidget;

public class DLWidgetsCollection {

    public final List<IDragonLibWidget> components = new ArrayList<>(); 
    
    private boolean enabled = true;
    private boolean visible = true;

    public void performForEach(Predicate<? super IDragonLibWidget> filter, Consumer<? super IDragonLibWidget> consumer) {
        components.stream().filter(filter).forEach(consumer);
    }

    public void performForEach(Consumer<? super IDragonLibWidget> consumer) {
        performForEach(x -> true, consumer);
    }

    public <C extends IDragonLibWidget> void performForEachOfType(Class<C> clazz, Predicate<C> filter, Consumer<C> consumer) {
        components.stream().filter(clazz::isInstance).map(clazz::cast).filter(filter).forEach(consumer);
    }

    public <C extends IDragonLibWidget> void performForEachOfType(Class<C> clazz, Consumer<C> consumer) {
        performForEachOfType(clazz, x -> true, consumer);
    }

    

    public boolean isVisible() {
        return visible;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setVisible(boolean v) {
        this.visible = v;
        performForEach(x -> x.set_visible(v));
    }

    public void setEnabled(boolean e) {
        this.enabled = e;
        performForEach(x -> x.set_active(e));
    }

    public <W extends IDragonLibWidget> void add(W widget) {
        widget.set_active(enabled);
        widget.set_visible(visible);
        components.add(widget);
    }

    public void clear() {
        components.clear();
    }

    public void clear(Consumer<IDragonLibWidget> onRemove) {
        performForEach(x -> onRemove.accept(x));
        clear();
    }
}


