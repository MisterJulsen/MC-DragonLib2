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

/**
 * A simple property wrapper that holds a value of type {@code T} and provides
 * identity-based serialization and deserialization.
 *
 * <p>This class extends {@code AbstractSerializableProperty<T, T>} and implements
 * the serialization contract by returning the value unchanged. It is useful for
 * properties where the in-memory representation and the serialized representation
 * are the same type and do not require transformation.</p>
 *
 * @param <T> the type of the property value and its serialized form
 */
public class Property<T> extends AbstractSerializableProperty<T, T> {

	/**
	 * Creates a new Property with the given default value.
	 *
	 * @param defaultValue the default value to initialize the property with;
	 *                     may be {@code null} depending on usage
	 */
    public Property(T defaultValue) {
        super(defaultValue);
    }

    /**
     * Serialize the given value to its storage representation.
     *
     * <p>This implementation performs an identity mapping and returns the input
     * value unchanged. Subclasses may override this method to apply custom
     * transformations (for example, converting complex objects to primitive or
     * serializable forms).</p>
     *
     * @param t the value to serialize
     * @return the serialized representation of {@code t} (here, {@code t} itself)
     */
    @Override
    protected T serialize(T t) {
        return t;
    }

    /**
     * Deserialize the stored representation back to the property value.
     *
     * <p>This implementation performs an identity mapping and returns the stored
     * value unchanged. Subclasses may override this method to reconstruct complex
     * objects from their serialized forms.</p>
     *
     * @param s the stored representation to deserialize
     * @return the deserialized property value (here, {@code s} itself)
     */
    @Override
    protected T deserialize(T s) {
        return s;
    }
}
