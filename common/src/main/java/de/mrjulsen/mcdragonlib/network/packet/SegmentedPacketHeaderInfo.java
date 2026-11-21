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

import de.mrjulsen.mcdragonlib.network.v2.transport.FrameHeader;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @deprecated Replaced by {@link FrameHeader}.
 */
@Deprecated
public record SegmentedPacketHeaderInfo(PacketHeaderInfo type, SegmentType segment, int expectedParts, int partNumber) {

    public void writeBufferHeader(FriendlyByteBuf buffer) {
        type.writeBufferHeader(buffer);
        buffer.writeByte(segment.getId());
        buffer.writeInt(expectedParts);
        buffer.writeInt(partNumber);
    }

    public static SegmentedPacketHeaderInfo readBufferHeader(FriendlyByteBuf buffer) {
        return new SegmentedPacketHeaderInfo(
            PacketHeaderInfo.readBufferHeader(buffer),
            SegmentType.getById(buffer.readByte()),
            buffer.readInt(),
            buffer.readInt()
        );
    }

    public static int getSize(PacketHeaderInfo info) {
        int size = 0;
        size += info.getSize();
        size += Byte.BYTES;
        size += Integer.BYTES;
        size += Integer.BYTES;
        return size;
    }

    
}
