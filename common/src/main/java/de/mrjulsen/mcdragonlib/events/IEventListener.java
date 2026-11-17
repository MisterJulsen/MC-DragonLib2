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

package de.mrjulsen.mcdragonlib.events;


@FunctionalInterface
public interface IEventListener<S extends IEventDispatcher<S>, T extends IEvent> {
    /**
     * Executes the event.
     * @param source The component this event is assigned to.
     * @param event The event parameters.
     * @return Indicates whether the event is consumed. {@code true} if the execution of other event listeners should be canceled, {@code false} otherwise. If it doesn't matter, use {@code false} (default).
     */
    boolean invoke(S source, T event);
}
