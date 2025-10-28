package de.mrjulsen.mcdragonlib.network.packet;

import java.nio.charset.StandardCharsets;

import de.mrjulsen.mcdragonlib.network.CommunicationType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record PacketHeaderInfo(PacketType type, CommunicationType communication, long requestId, ResourceLocation id) {

    public void writeBufferHeader(FriendlyByteBuf buffer) {
        buffer.writeByte(type.getId());
        buffer.writeByte(communication.getId());
        buffer.writeLong(requestId);
        buffer.writeResourceLocation(id);
    }

    public static PacketHeaderInfo readBufferHeader(FriendlyByteBuf buffer) {
        return new PacketHeaderInfo(
            PacketType.getById(buffer.readByte()),
            CommunicationType.getById(buffer.readByte()),
            buffer.readLong(),
            buffer.readResourceLocation()
        );
    }

    public int getSize() {
        int size = 0;
        size += Byte.BYTES;
        size += Byte.BYTES;
        size += Long.BYTES;
        size += id.toString().getBytes(StandardCharsets.UTF_8).length;
        return size;
    }
    
}
