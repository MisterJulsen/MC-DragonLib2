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

package de.mrjulsen.mcdragonlib.network.v2;

/**
 * An exchange that has been sent and is waiting for its answer.
 *
 * <p>The session keeps these so that a response can be matched to its request and so that every
 * outstanding exchange can still be completed when the connection or the game goes away.
 */
public interface PendingRequest {

    /**
     * Returns the packet this exchange belongs to.
     *
     * @return the originating packet definition
     */
    PacketDefinition getDefinition();

    /**
     * Returns when this exchange gives up waiting.
     *
     * @return the deadline as a wall-clock timestamp in milliseconds
     */
    long getDeadline();

    /**
     * Completes this exchange without a result.
     *
     * @param error why no result will arrive
     */
    void fail(NetworkError error);
}
