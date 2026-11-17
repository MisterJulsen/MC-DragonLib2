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

package de.mrjulsen.mcdragonlib.network.v2.transport;

/**
 * The limits the transport enforces on incoming messages.
 *
 * <p>Reassembly allocates memory on behalf of a remote peer, so every inbound message is checked
 * against these limits before anything is reserved for it. Implementations are supplied by the
 * layer that knows the packet registry.
 */
public interface TransportPolicy {

    /**
     * Returns how large a fully reassembled message for the given packet may be.
     *
     * @param packetName the registered packet the message belongs to
     * @return the highest accepted payload size in bytes, or a negative value if the packet is unknown
     */
    int getMaxMessageBytes(String packetName);

    /**
     * Returns how many bytes all unfinished transfers of one connection may occupy together.
     *
     * @return the reassembly budget per connection in bytes
     */
    int getConnectionBudgetBytes();

    /**
     * Returns how long an unfinished transfer is kept before it is discarded.
     *
     * @return the reassembly timeout in milliseconds
     */
    long getReassemblyTimeoutMillis();
}
