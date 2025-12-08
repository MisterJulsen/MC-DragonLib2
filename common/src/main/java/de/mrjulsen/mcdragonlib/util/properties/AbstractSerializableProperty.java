package de.mrjulsen.mcdragonlib.util.properties;

import java.util.Objects;
import java.util.Optional;

/**
 * Base implementation of a property that supports serialization of its value.
 *
 * <p>This abstract class represents a property whose runtime type {@code T}
 * can be converted to/from a serialized representation {@code S}. It implements
 * common behavior such as default values, inheritance, modification callbacks
 * and equality based on the serialized value.</p>
 *
 * @param <T> the runtime type of the property value
 * @param <S> the serialized/stored representation type
 */
public abstract class AbstractSerializableProperty<T, S> implements IProperty<T> {

    /**
     * Functional callback used to modify/validate a value before it is applied.
     *
     * <p>Implementations receive the old and the new value and should return
     * the final value that will be set.</p>
     *
     * @param <T> the property value type
     */
    @FunctionalInterface
    public static interface IPropertyUpdateCallback<T> {
        /**
         * Called before the property value is changed to allow modification or validation.
         *
         * @param oldValue the current value of the property (before change)
         * @param newValue the requested new value
         * @return the value that should actually be stored (may be {@code newValue} or a transformed value)
         */
        T update(T oldValue, T newValue);
    }

    /**
     * Callback invoked after a property change has been applied.
     *
     * <p>Implementations can react to the change but must not modify the stored value.</p>
     *
     * @param <T> the property value type
     */
    @FunctionalInterface
    public static interface IPropertyAfterUpdateCallback<T> {
        /**
         * Called after the property value has been changed.
         *
         * @param oldValue the previous value of the property
         * @param newValue the updated value of the property
         */
        void update(T oldValue, T newValue);
    }

    private final S defaultValue;
    private Optional<IPropertyUpdateCallback<T>> onModify = Optional.empty();
    private Optional<IPropertyAfterUpdateCallback<T>> afterChanged = Optional.empty();
    
    private S value;

    /**
     * Construct a new property with the provided default value.
     *
     * <p>The default value is immediately serialized and used as the initial stored value.</p>
     *
     * @param defaultValue the default runtime value for this property
     */
    public AbstractSerializableProperty(T defaultValue) {
        this.defaultValue = serialize(defaultValue);
        this.value = this.defaultValue;
    }

    /**
     * Register a callback that is invoked before a value is applied.
     *
     * @param callback the modification callback, may be {@code null} to clear
     * @param <P> the concrete property type to allow fluent usage
     * @return this property instance for chaining
     */
    @Override
    public <P extends IProperty<T>> P withModificationCallback(IPropertyUpdateCallback<T> callback) {
        this.onModify = Optional.ofNullable(callback);
        return (P)this;
    }

    /**
     * Register a callback that is invoked after a value has changed.
     *
     * @param callback the after-change callback, may be {@code null} to clear
     * @param <P> the concrete property type to allow fluent usage
     * @return this property instance for chaining
     */
    @Override
    public <P extends IProperty<T>> P withAfterPropertyChangedCallback(IPropertyAfterUpdateCallback<T> callback) {
        this.afterChanged = Optional.ofNullable(callback);
        return (P)this;
    }

    protected abstract S serialize(T t);
    protected abstract T deserialize(S s);


    /**
     * Get the locally stored value for this property (deserialized).
     *
     * @return the local runtime value represented by the stored serialized value
     */
    @Override
    public T get() {
        return deserialize(this.value);
    }


    /**
     * Set a new value for this property. The modification callback (if present)
     * will be invoked first and may alter the stored value; the after-change
     * callback is invoked after applying the value.
     *
     * @param t the new runtime value to set
     * @return the original requested value {@code t}
     */
    @Override
    public T set(T t) {
        T old = get();
        T val = getModificationCallback().map(x -> x.update(old, t)).orElse(t);
        this.value = serialize(val);
        getAfterChangeCallback().ifPresent(x -> x.update(old, val));
        return t;
    }

    /**
     * Return the property's default value (deserialized).
     *
     * @return the default runtime value for this property
     */
    @Override
    public T getDefaultValue() {
        return deserialize(this.defaultValue);
    }

    /**
     * Reset the property to its default value. Modification and after-change
     * callbacks are executed similarly to {@link #set(Object)}.
     *
     * @return the default runtime value that was applied
     */
    @Override
    public T reset() {
        T t = getDefaultValue();
        T old = get();
        T val = getModificationCallback().map(x -> x.update(old, t)).orElse(t);
        this.value = serialize(val);
        getAfterChangeCallback().ifPresent(x -> x.update(old, val));
        return t;
    }

    protected Optional<IPropertyUpdateCallback<T>> getModificationCallback() {
        return onModify;
    }

    protected Optional<IPropertyAfterUpdateCallback<T>> getAfterChangeCallback() {
        return afterChanged;
    }

    protected S getSerializedCurrentValue() {
        return getSerializedValue();
    }

    protected S getSerializedValue() {
        return value;
    }

    protected S getSerializedDefaultValue() {
        return defaultValue;
    }

    
    /**
     * Equality is based on the serialized stored value.
     *
     * @param obj the other object to compare
     * @return true if the other object is an AbstractSerializableProperty with an equal serialized value
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractSerializableProperty o) {
            return value != null && o.value != null && value.equals(o.value);
        }
        return false;
    }

    /**
     * Compute hash code based on the serialized stored value when available.
     *
     * @return a hash code consistent with equals
     */
    @Override
    public int hashCode() {
        if (value == null) {
            return super.hashCode();
        }
        return Objects.hashCode(value);
    }

    /**
     * Create a concise string representation including stored, default and inherited values.
     *
     * @return a string describing the property internal state
     */
    @Override
    public String toString() {
        return String.format("%s[value=%s,defaultValue=%s]", getClass().getSimpleName(), value, defaultValue);
    }
     
}
