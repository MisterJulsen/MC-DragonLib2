package de.mrjulsen.mcdragonlib.network.packet;

import net.minecraft.network.FriendlyByteBuf;

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
