package de.mrjulsen.mcdragonlib.client.newgui.properties;

import java.util.Optional;

import de.mrjulsen.mcdragonlib.client.newgui.properties.AbstractSerializableProperty.IPropertyAfterUpdateCallback;
import de.mrjulsen.mcdragonlib.client.newgui.properties.AbstractSerializableProperty.IPropertyUpdateCallback;

public interface IProperty<T> {
    /**
     * Returns the current valid value. This is either the user-defined value in the property or the inherited value if the user-defined value is undefined or the inherited value is prioritized.
     * @return The current valid value.
     */
    T get();
    /**
     * Returns the user-defined property value.
     * @return The user-defined property value.
     */
    T getValue();
    /**
     * The inherited value.
     * @return The inherited value
     */
    Optional<T> getInheritedValue();

    T getDefaultValue();
    /**
     * Changes the user-defined value.
     * @param t The new value.
     * @return The new value.
     */
    T set(T t);

    T reset();
    /**
     * Uses the currently valid value of another property as the inherited property value. This is used by the internal API and shouldn't be used manually.
     */
    void inheritFrom(IProperty<?> refProp, boolean overrideLocal);

    <P extends IProperty<T>> P withAfterPropertyChangedCallback(IPropertyAfterUpdateCallback<T> callback);
    <P extends IProperty<T>> P withModificationCallback(IPropertyUpdateCallback<T> callback);
}
