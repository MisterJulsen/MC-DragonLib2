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

package de.mrjulsen.mcdragonlib.network.v2.transport;

/**
 * Signals that received data does not form a valid DragonLib message.
 *
 * <p>Since the sender is remote, this is treated as bad input rather than a programming error:
 * the offending transfer is dropped and the connection stays usable.
 */
public class TransportException extends RuntimeException {

    /**
     * Creates an exception describing why a frame or message was rejected.
     *
     * @param message the reason the data was rejected
     */
    public TransportException(String message) {
        super(message);
    }

    /**
     * Creates an exception describing why a frame or message was rejected.
     *
     * @param message the reason the data was rejected
     * @param cause the underlying failure
     */
    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
