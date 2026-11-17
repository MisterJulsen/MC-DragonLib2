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

import net.minecraft.network.FriendlyByteBuf;

/**
 * Receives a fully reassembled message from the transport.
 *
 * <p>The payload buffer is only valid for the duration of the call and is released by the
 * transport afterwards, so anything needed later must be decoded or copied before returning.
 */
@FunctionalInterface
public interface MessageSink {

    /**
     * Handles one complete message.
     *
     * @param packetName the registered packet the message belongs to
     * @param payload the complete payload, valid only during this call
     */
    void accept(String packetName, FriendlyByteBuf payload);
}
