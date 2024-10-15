package de.mrjulsen.mcdragonlib.util.accessor;

import de.mrjulsen.mcdragonlib.data.Single.MutableSingle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class BasicDataAccessorPacket<I, C, O> extends AbstractDataAccessorPacket<BasicDataAccessorPacket<I, C, O>, I, C, O> {

    public BasicDataAccessorPacket() {}

    @Override
    public void encodeParam(I param, CompoundTag nbt) {
        type.encodeRequest.accept(param, nbt);
    }

    @Override
    public I decodeParam(CompoundTag nbt, DataAccessorType<I, C, O> type) {
        return type.decodeRequest.apply(nbt);
    }

    @Override
    public boolean processServer(Player player, I param, DataAccessorType<I, C, O> type, MutableSingle<Object> temp, CompoundTag nbt, int iteration) {
        return type.serverProcessor.run(player, param, temp, nbt, iteration);
    }

    @Override
    public C receiveChunk(boolean hasMore, C previous, int iteration, CompoundTag nbt) {
        return type.chunkProcessor.run(hasMore, previous, iteration, nbt);
    }

    @Override
    public O processClient(C chunks) {
        return type.output.apply(chunks);
    }

    @FunctionalInterface
    public static interface IServerProcessor<I> {
        boolean run(Player player, I param, MutableSingle<Object> temp, CompoundTag nbt, int iteration);
    }

    @FunctionalInterface
    public static interface IChunkReceiver<C> {
        C run(boolean hasMore, C previous, int iteration, CompoundTag nbt);
    }
    
}
