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

package de.mrjulsen.mcdragonlib.network.v2;

/**
 * State a packet keeps for one connection.
 *
 * <p>Anything attached to a {@link ChannelSession} is ticked with it and released when the
 * connection goes away, so a packet type does not have to track connections itself. Stream
 * packets use this, and so can any pattern added later.
 */
public interface SessionResource {

    /**
     * Advances anything time based this resource holds.
     *
     * @param now the current wall-clock time in milliseconds
     */
    default void tick(long now) {}

    /**
     * Releases everything this resource holds.
     *
     * @param reason why the session is going away
     */
    void close(NetworkError.Reason reason);
}
