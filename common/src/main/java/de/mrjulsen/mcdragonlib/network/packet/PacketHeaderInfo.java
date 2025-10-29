package de.mrjulsen.mcdragonlib.network.packet;

import java.nio.charset.StandardCharsets;

import de.mrjulsen.mcdragonlib.network.CommunicationType;
import net.minecraft.network.FriendlyByteBuf;

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
