package de.mrjulsen.mcdragonlib.util.properties;

import de.mrjulsen.mcdragonlib.util.properties.AbstractSerializableProperty.IPropertyAfterUpdateCallback;
import de.mrjulsen.mcdragonlib.util.properties.AbstractSerializableProperty.IPropertyUpdateCallback;

public interface IProperty<T> {
    /**
     * Returns the current valid value. This is either the user-defined value in the property or the inherited value if the user-defined value is undefined or the inherited value is prioritized.
     * @return The current valid value.
     */
    T get();

    T getDefaultValue();
    /**
     * Changes the user-defined value.
     * @param t The new value.
     * @return The new value.
     */
    T set(T t);

    T reset();

    <P extends IProperty<T>> P withAfterPropertyChangedCallback(IPropertyAfterUpdateCallback<T> callback);
    <P extends IProperty<T>> P withModificationCallback(IPropertyUpdateCallback<T> callback);
}
