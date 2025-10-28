package de.mrjulsen.mcdragonlib.client.gui.properties;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class VirtualProperty<T> extends Property<T> {

    private final Supplier<T> getter;
    private final Consumer<T> setter;

    public VirtualProperty(T defaultValue, Supplier<T> getter, Consumer<T> setter) {
        super(defaultValue);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public T get() {
        return getter.get();
    }

    @Override
    public T set(T t) {
        this.setter.accept(t);
        return t;
    }
}
