/*
 * DragonLib 3 - 1.20.1
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
