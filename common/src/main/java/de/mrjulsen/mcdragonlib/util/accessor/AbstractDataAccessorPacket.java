package de.mrjulsen.mcdragonlib.util.accessor;

import java.util.function.Supplier;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.UUID;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.Single.MutableSingle;
import de.mrjulsen.mcdragonlib.net.IPacketBase;
import de.mrjulsen.mcdragonlib.util.WorkerAsync;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractDataAccessorPacket<T extends AbstractDataAccessorPacket<T, I, C, O>, I, C, O> implements IPacketBase<T> {

    protected UUID requestId;
    protected I param;
    protected DataAccessorType<I, C, O> type;
    protected boolean sendToClient;

    public AbstractDataAccessorPacket() {}

    @SuppressWarnings("unchecked")
    public T setData(UUID id, I param, DataAccessorType<I, C, O> type, boolean sendToClient) {
        this.requestId = id;
        this.param = param;
        this.type = type;
        this.sendToClient = sendToClient;
        return (T)this;
    }

    @Override
    public void encode(T packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.requestId);
        CompoundTag nbt = new CompoundTag();
        packet.encodeParam(packet.param, nbt);
        buf.writeNbt(nbt);
        buf.writeResourceLocation(packet.type.getId());
        buf.writeBoolean(packet.sendToClient);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T decode(FriendlyByteBuf buf) {
        try {
            UUID id = buf.readUUID();
            CompoundTag nbt = buf.readNbt();
            DataAccessorType<I, C, O> type = (DataAccessorType<I, C, O>)DataAccessorType.get(buf.readResourceLocation()).get();
            boolean sendToClient = buf.readBoolean();
            return (T)getClass().getConstructor().newInstance().setData(id, decodeParam(nbt, type), type, sendToClient);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | NoSuchElementException e) {
            throw new IllegalStateException("Unable to decode network packet.", e);            
        }
    }

    @Override
    public void handle(T packet, Supplier<PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            WorkerAsync worker = DataAccessor.getWorker(packet.sendToClient);
            worker.queueTask(() -> {
                CompoundTag nbt;
                MutableSingle<Object> tempData = new MutableSingle<>(null);
                boolean hasMore = true;
                int iteration = 0;
                do {
                    hasMore = processServer(contextSupplier.get().getPlayer(), packet.param, packet.type, tempData, (nbt = new CompoundTag()), iteration);
                    DataAccessorResponsePacket newPacket = new DataAccessorResponsePacket(packet.requestId, hasMore, iteration, nbt);
                    if (packet.sendToClient) {
                        DragonLib.getDragonLibNetworkManager().CHANNEL.sendToPlayer((ServerPlayer)contextSupplier.get().getPlayer(), newPacket);
                    } else {
                        DragonLib.getDragonLibNetworkManager().CHANNEL.sendToServer(newPacket);
                    }
                    iteration++;
                } while (hasMore);
            });
        });
    }

    public abstract void encodeParam(I param, CompoundTag nbt);
    public abstract I decodeParam(CompoundTag nbt, DataAccessorType<I, C, O> type);
    public abstract boolean processServer(Player player, I param, DataAccessorType<I, C, O> type, MutableSingle<Object> temp, CompoundTag nbt, int iteration);
    public abstract C receiveChunk(boolean hasMore, C previous, int iteration, CompoundTag nbt);
    public abstract O processClient(C chunks);
    
}
