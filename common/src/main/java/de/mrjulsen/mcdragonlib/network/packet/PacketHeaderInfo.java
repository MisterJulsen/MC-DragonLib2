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

package de.mrjulsen.mcdragonlib.network.packet;

import java.nio.charset.StandardCharsets;

import de.mrjulsen.mcdragonlib.network.v2.transport.FrameHeader;
import de.mrjulsen.mcdragonlib.network.CommunicationType;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @deprecated Replaced by {@link FrameHeader}.
 */
@Deprecated
public record PacketHeaderInfo(PacketType type, CommunicationType communication, long requestId, String name) {

    public void writeBufferHeader(FriendlyByteBuf buffer) {
        buffer.writeByte(type.getId());
        buffer.writeByte(communication.getId());
        buffer.writeLong(requestId);
        buffer.writeUtf(name);
    }

    public static PacketHeaderInfo readBufferHeader(FriendlyByteBuf buffer) {
        return new PacketHeaderInfo(
            PacketType.getById(buffer.readByte()),
            CommunicationType.getById(buffer.readByte()),
            buffer.readLong(),
            buffer.readUtf()
        );
    }

    public int getSize() {
        int size = 0;
        size += Byte.BYTES;
        size += Byte.BYTES;
        size += Long.BYTES;
        size += name.getBytes(StandardCharsets.UTF_8).length;
        return size;
    }
    
}
