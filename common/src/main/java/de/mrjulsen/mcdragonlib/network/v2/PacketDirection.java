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
 * Describes which side may start an exchange for a registered packet.
 *
 * <p>A response always travels the opposite way, so this only fixes the initiating side. The
 * direction an individual frame takes on the wire is described by {@link NetworkFlow}.
 */
public enum PacketDirection {

    /** The client starts the exchange and the server handles it. */
    TO_SERVER(NetworkFlow.SERVERBOUND),
    /** The server starts the exchange and the client handles it. */
    TO_CLIENT(NetworkFlow.CLIENTBOUND);

    private final NetworkFlow requestFlow;

    private PacketDirection(NetworkFlow requestFlow) {
        this.requestFlow = requestFlow;
    }

    /**
     * Returns the flow a request of this packet travels in.
     *
     * @return the flow used for requests
     */
    public NetworkFlow getRequestFlow() {
        return requestFlow;
    }

    /**
     * Returns the flow a response to this packet travels in.
     *
     * @return the flow used for responses
     */
    public NetworkFlow getResponseFlow() {
        return requestFlow.opposite();
    }
}
