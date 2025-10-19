package de.mrjulsen.mcdragonlib.client.newgui.properties;

import java.util.Objects;
import java.util.Optional;

public abstract class AbstractSerializableProperty<T, S> implements IProperty<T> {

    @FunctionalInterface
    public static interface IPropertyUpdateCallback<T> {
        T update(T oldValue, T newValue);
    }
    @FunctionalInterface
    public static interface IPropertyAfterUpdateCallback<T> {
        void update(T oldValue, T newValue);
    }

    private final S defaultValue;
    private Optional<IPropertyUpdateCallback<T>> onModify = Optional.empty();
    private Optional<IPropertyAfterUpdateCallback<T>> afterChanged = Optional.empty();
    
    private S value;
    private S inheritedValue = null;
    private boolean overrideLocal = false;

    public AbstractSerializableProperty(T defaultValue) {
        this.defaultValue = serialize(defaultValue);
        this.value = this.defaultValue;
    }

    @Override
    public <P extends IProperty<T>> P withModificationCallback(IPropertyUpdateCallback<T> callback) {
        this.onModify = Optional.ofNullable(callback);
        return (P)this;
    }

    @Override
    public <P extends IProperty<T>> P withAfterPropertyChangedCallback(IPropertyAfterUpdateCallback<T> callback) {
        this.afterChanged = Optional.ofNullable(callback);
        return (P)this;
    }

    protected abstract S serialize(T t);
    protected abstract T deserialize(S s);

    @Override
    public T get() {
        return inheritedValue != null && shouldOverrideLocal() ? getInheritedValue().orElse(getValue()) : getValue();
    }

    @Override
    public T getValue() {
        return deserialize(this.value);
    }

    @Override
    public Optional<T> getInheritedValue() {
        return this.inheritedValue == null ? Optional.empty() : Optional.ofNullable(deserialize(this.inheritedValue));
    }

    @Override
    public T set(T t) {
        T old = get();
        T val = getModificationCallback().map(x -> x.update(old, t)).orElse(t);
        this.value = serialize(val);
        getAfterChangeCallback().ifPresent(x -> x.update(old, val));
        return t;
    }

    @Override
    public T getDefaultValue() {
        return deserialize(this.defaultValue);
    }

    @Override
    public T reset() {
        T t = getDefaultValue();
        T old = get();
        T val = getModificationCallback().map(x -> x.update(old, t)).orElse(t);
        this.value = serialize(val);
        getAfterChangeCallback().ifPresent(x -> x.update(old, val));
        return t;
    }

    public boolean shouldOverrideLocal() {
        return overrideLocal;
    }

    protected Optional<IPropertyUpdateCallback<T>> getModificationCallback() {
        return onModify;
    }

    protected Optional<IPropertyAfterUpdateCallback<T>> getAfterChangeCallback() {
        return afterChanged;
    }

    @Override
    public void inheritFrom(IProperty<?> refProp, boolean overrideLocal) {
        if (refProp == null) return;

        if (!this.getClass().equals(refProp.getClass())) {
            throw new IllegalArgumentException("Incompatible property types for inheritance: " +
                this.getClass().getSimpleName() + " vs " + refProp.getClass().getSimpleName());
        }
        this.inheritedValue = serialize((T)refProp.get());
        this.overrideLocal = overrideLocal;
    }

    protected S getSerializedCurrentValue() {
        return inheritedValue != null && shouldOverrideLocal() ? getSerializedInheritedValue().orElse(getSerializedValue()) : getSerializedValue();
    }

    protected S getSerializedValue() {
        return value;
    }

    protected S getSerializedDefaultValue() {
        return defaultValue;
    }

    protected Optional<S> getSerializedInheritedValue() {
        return Optional.ofNullable(inheritedValue);
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractSerializableProperty o) {
            return value != null && o.value != null && value.equals(o.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (value == null) {
            return super.hashCode();
        }
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return String.format("%s[value=%s,defaultValue=%s,inheritedValue=%s]", getClass().getSimpleName(), value, defaultValue, inheritedValue);
    }
     
}
