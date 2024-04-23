package de.mrjulsen.mcdragonlib.net.builtin;

import java.util.UUID;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.IPacketBase;
import de.mrjulsen.mcdragonlib.net.NetworkManagerBase;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class IdentifiableResponsePacketBase implements IPacketBase<IdentifiableResponsePacketBase> {

    protected UUID id;
    protected CompoundTag nbt;
    protected long gameTime;

    public IdentifiableResponsePacketBase() {}

    public IdentifiableResponsePacketBase(UUID id, CompoundTag nbt, long gameTime) {
        this.id = id;
        this.nbt = nbt;
        this.gameTime = gameTime;
    }

    @Override
    public final void encode(IdentifiableResponsePacketBase packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.id);
        buf.writeNbt(packet.nbt);
        buf.writeLong(packet.gameTime);
    }

    @Override
    public IdentifiableResponsePacketBase decode(FriendlyByteBuf buf) {        
        return new IdentifiableResponsePacketBase(buf.readUUID(), buf.readNbt(), buf.readLong());
    }

    @Override
    public void handle(IdentifiableResponsePacketBase packet, Supplier<PacketContext> contextSupplier) {
        NetworkManagerBase.executeCallback(packet.id, packet.nbt, packet.gameTime);
    }
    
}