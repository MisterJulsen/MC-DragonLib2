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
