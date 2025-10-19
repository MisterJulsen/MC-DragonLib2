package de.mrjulsen.mcdragonlib.client.newgui.properties;

public class Property<T> extends AbstractSerializableProperty<T, T> {

    public Property(T defaultValue) {
        super(defaultValue);
    }

    @Override
    protected T serialize(T t) {
        return t;
    }

    @Override
    protected T deserialize(T s) {
        return s;
    }
}
