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
 * The direction a single frame travels in.
 *
 * <p>This describes the transport, not the packet definition. A request and its response use
 * opposite flows. Which side may start an exchange is described by {@link PacketDirection}.
 */
public enum NetworkFlow {

    /** From the server to a client. */
    CLIENTBOUND(1048576),
    /** From a client to the server. */
    SERVERBOUND(32767);

    private final int maxPayloadBytes;

    private NetworkFlow(int maxPayloadBytes) {
        this.maxPayloadBytes = maxPayloadBytes;
    }

    /**
     * Returns the payload limit vanilla enforces for a custom payload packet in this direction.
     *
     * @return the highest number of bytes a single packet may carry
     */
    public int getMaxPayloadBytes() {
        return maxPayloadBytes;
    }

    /**
     * Returns the flow pointing the other way.
     *
     * @return the opposite flow
     */
    public NetworkFlow opposite() {
        return this == CLIENTBOUND ? SERVERBOUND : CLIENTBOUND;
    }
}
