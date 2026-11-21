/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
