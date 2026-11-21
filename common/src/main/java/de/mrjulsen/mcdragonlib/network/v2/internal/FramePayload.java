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

package de.mrjulsen.mcdragonlib.network.v2.internal;

import org.jetbrains.annotations.ApiStatus;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * The custom payload that carries one frame of a channel.
 *
 * <p>Since 1.20.5 custom payloads need a registered type instead of a raw buffer. Every channel uses one type whose id is
 * the channel id, the frame header tells the packets of a channel apart. The frame is written as the remaining bytes of
 * the packet without a length prefix, so the payload limits are the same as with the raw buffers of older versions.
 *
 * <p>On a local connection the payload object is handed over without being encoded, so the receiver must not modify or
 * release {@link #data()}. Use {@link #frame()} to read it.
 *
 * @param type the payload type of the channel
 * @param data the encoded frame
 */
@ApiStatus.Internal
public record FramePayload(CustomPacketPayload.Type<FramePayload> type, ByteBuf data) implements CustomPacketPayload {

    /**
     * Returns the payload type of a channel.
     *
     * @param channelId the channel
     * @return the payload type, equal for equal channel ids
     */
    public static CustomPacketPayload.Type<FramePayload> type(ResourceLocation channelId) {
        return new CustomPacketPayload.Type<>(channelId);
    }

    /**
     * Creates the codec for the payload type of a channel.
     *
     * @param type the payload type of the channel
     * @return a codec that writes the frame as the remaining bytes of the packet
     */
    public static StreamCodec<FriendlyByteBuf, FramePayload> codec(CustomPacketPayload.Type<FramePayload> type) {
        return StreamCodec.of(
            (buf, payload) -> buf.writeBytes(payload.data(), payload.data().readerIndex(), payload.data().readableBytes()),
            buf -> {
                int length = buf.readableBytes();
                ByteBuf data = Unpooled.buffer(length, length);
                buf.readBytes(data, length);
                return new FramePayload(type, data);
            }
        );
    }

    /**
     * Returns a view of the frame with its own reader index, which leaves this payload untouched.
     *
     * @return the frame to hand to the networking layer
     */
    public FriendlyByteBuf frame() {
        return new FriendlyByteBuf(data.duplicate());
    }
}
