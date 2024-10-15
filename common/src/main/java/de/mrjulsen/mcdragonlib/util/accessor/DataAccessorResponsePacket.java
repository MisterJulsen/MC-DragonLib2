package de.mrjulsen.mcdragonlib.util.accessor;

import java.util.function.Supplier;
import java.util.UUID;

import de.mrjulsen.mcdragonlib.net.IPacketBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class DataAccessorResponsePacket implements IPacketBase<DataAccessorResponsePacket> {

    private UUID requestId;
    private boolean hasMore;
    private int iteration;
    private CompoundTag nbt;

    public DataAccessorResponsePacket() {}

    public DataAccessorResponsePacket(UUID requestId, boolean hasMore, int iteration, CompoundTag nbt) {
        this.requestId = requestId;
        this.hasMore = hasMore;
        this.nbt = nbt;
        this.iteration = iteration;
    }

    @Override
    public void encode(DataAccessorResponsePacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.requestId);
        buf.writeBoolean(packet.hasMore);
        buf.writeInt(packet.iteration);
        buf.writeNbt(packet.nbt);
    }

    @Override
    public DataAccessorResponsePacket decode(FriendlyByteBuf buf) {
        return new DataAccessorResponsePacket(buf.readUUID(), buf.readBoolean(), buf.readInt(), buf.readNbt());
    }

    @Override
    public void handle(DataAccessorResponsePacket packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            DataAccessor.run(packet.requestId, packet.hasMore, packet.iteration, packet.nbt);
        });
    }
    
}
