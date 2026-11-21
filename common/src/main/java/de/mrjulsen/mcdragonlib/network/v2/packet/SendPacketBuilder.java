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

package de.mrjulsen.mcdragonlib.network.v2.packet;

import java.util.Objects;

import de.mrjulsen.mcdragonlib.network.v2.DLChannel;
import de.mrjulsen.mcdragonlib.network.v2.codec.DLStreamCodec;

/**
 * Collects the settings for a {@link SendPacket} and registers it.
 *
 * @param <T> the payload type
 */
public final class SendPacketBuilder<T> extends PacketBuilder<SendPacketBuilder<T>> {

    private final DLStreamCodec<T> codec;
    private SendPacket.Handler<T> handler;

    public SendPacketBuilder(DLChannel channel, String name, DLStreamCodec<T> codec) {
        super(channel, name);
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    /**
     * Sets what happens when a payload arrives.
     *
     * @param handler the receiving side of this packet
     * @return this builder
     */
    public SendPacketBuilder<T> handler(SendPacket.Handler<T> handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
        return this;
    }

    /**
     * Adds the finished packet to its channel.
     *
     * @return the registered packet
     */
    public SendPacket<T> register() {
        if (handler == null) {
            throw new IllegalStateException("Packet '" + name + "' needs a handler.");
        }
        return channel.register(new SendPacket<>(settings(), codec, handler));
    }
}
