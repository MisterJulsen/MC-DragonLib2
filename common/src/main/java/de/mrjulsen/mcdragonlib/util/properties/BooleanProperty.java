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

public class BooleanProperty extends Property<Boolean> {

    /**
     * Create a new BooleanProperty with the specified default and inheritance behavior.
     *
     * @param defaultValue the default boolean value for this property
     */
    public BooleanProperty(boolean defaultValue) {
        super(defaultValue);
    }

    /**
     * Toggle the locally stored boolean value and return the requested new value.
     *
     * <p>This delegates to {@link #set(Object)} with the negated current local value.
     * The modification and after-change callbacks (if any) are applied as usual by {@code set}.</p>
     *
     * @return the new boolean value that was requested to be set
     */
    public boolean toggle() {
        return set(!get());
    }
}
