package de.mrjulsen.mcdragonlib.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.client.gui.components.AbstractWidget;

public class WidgetsCollection {

    public final List<AbstractWidget> components = new ArrayList<>(); 
    
    private boolean enabled = true;
    private boolean visible = true;

    public void performForEach(Predicate<? super AbstractWidget> filter, Consumer<? super AbstractWidget> consumer) {
        components.stream().filter(filter).forEach(consumer);
    }

    public void performForEach(Consumer<? super AbstractWidget> consumer) {
        performForEach(x -> true, consumer);
    }

    public <C extends AbstractWidget> void performForEachOfType(Class<C> clazz, Predicate<C> filter, Consumer<C> consumer) {
        components.stream().filter(clazz::isInstance).map(clazz::cast).filter(filter).forEach(consumer);
    }

    public <C extends AbstractWidget> void performForEachOfType(Class<C> clazz, Consumer<C> consumer) {
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
        performForEach(x -> x.visible = v);
    }

    public void setEnabled(boolean e) {
        this.enabled = e;
        performForEach(x -> x.active = e);
    }

    public <W extends AbstractWidget> void add(W widget) {
        widget.active = enabled;
        widget.visible = visible;
        components.add(widget);
    }

    public void clear() {
        components.clear();
    }

    public void clear(Consumer<AbstractWidget> onRemove) {
        performForEach(x -> onRemove.accept(x));
        clear();
    }
}


