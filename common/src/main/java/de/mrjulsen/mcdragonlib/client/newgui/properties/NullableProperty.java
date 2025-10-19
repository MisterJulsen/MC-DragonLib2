package de.mrjulsen.mcdragonlib.client.newgui.properties;

import java.util.Objects;
import java.util.Optional;

public class NullableProperty<T> {
    private final Optional<T> defaultValue;
    private Optional<T> value;

    public NullableProperty(T defaultValue) {
        this.defaultValue = Optional.ofNullable(defaultValue);
    }

    public Optional<T> get() {
        return value;
    }

    public Optional<T> set(T value) {
        return this.value = Optional.ofNullable(value);
    }

    public Optional<T> getDefaultValue() {
        return defaultValue;
    }

    public Optional<T> reset() {
        return this.value = this.defaultValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NullableProperty<?> o) {
            return value.map(x -> o.value.map(y -> y.equals(x)).orElse(false)).orElse(false);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (!value.isPresent()) {
            return super.hashCode();
        }
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return String.format("Property[value=%s,defaultValue=%s]", value, defaultValue);
    }
}
