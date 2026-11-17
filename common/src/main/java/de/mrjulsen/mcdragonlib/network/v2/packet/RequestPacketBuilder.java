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

package de.mrjulsen.mcdragonlib.network.v2.packet;

import java.time.Duration;
import java.util.Objects;

import de.mrjulsen.mcdragonlib.network.v2.DLChannel;
import de.mrjulsen.mcdragonlib.network.v2.NetworkSettings;
import de.mrjulsen.mcdragonlib.network.v2.codec.DLStreamCodec;

/**
 * Collects the settings for a {@link RequestPacket} and registers it.
 *
 * @param <Q> the request type
 * @param <R> the response type
 */
public final class RequestPacketBuilder<Q, R> extends PacketBuilder<RequestPacketBuilder<Q, R>> {

    private final DLStreamCodec<Q> requestCodec;
    private final DLStreamCodec<R> responseCodec;
    private RequestPacket.Handler<Q, R> handler;
    private long timeoutMillis = NetworkSettings.getResponseTimeoutMillis();

    public RequestPacketBuilder(DLChannel channel, String name, DLStreamCodec<Q> requestCodec, DLStreamCodec<R> responseCodec) {
        super(channel, name);
        this.requestCodec = Objects.requireNonNull(requestCodec, "requestCodec");
        this.responseCodec = Objects.requireNonNull(responseCodec, "responseCodec");
    }

    /**
     * Sets how long a request waits before it gives up.
     *
     * <p>This only affects the waiting side. A handler that is still running is never interrupted
     * by it.
     *
     * @param timeout how long to wait for an answer
     * @return this builder
     */
    public RequestPacketBuilder<Q, R> timeout(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("The timeout must be positive.");
        }
        this.timeoutMillis = timeout.toMillis();
        return this;
    }

    /**
     * Sets what happens when a request arrives.
     *
     * @param handler turns a request into a response
     * @return this builder
     */
    public RequestPacketBuilder<Q, R> handler(RequestPacket.Handler<Q, R> handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
        return this;
    }

    /**
     * Adds the finished packet to its channel.
     *
     * @return the registered packet
     */
    public RequestPacket<Q, R> register() {
        if (handler == null) {
            throw new IllegalStateException("Packet '" + name + "' needs a handler.");
        }
        return channel.register(new RequestPacket<>(settings(), requestCodec, responseCodec, handler, timeoutMillis));
    }
}
