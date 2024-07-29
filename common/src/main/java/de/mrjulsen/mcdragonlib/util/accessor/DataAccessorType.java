package de.mrjulsen.mcdragonlib.util.accessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import de.mrjulsen.mcdragonlib.util.accessor.BasicDataAccessorPacket.IChunkReceiver;
import de.mrjulsen.mcdragonlib.util.accessor.BasicDataAccessorPacket.IServerProcessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class DataAccessorType<I, C, O> {

    private static final Map<ResourceLocation, DataAccessorType<?, ?, ?>> values = new HashMap<>();

   
    public static final String DEFAULT_NBT_DATA = "Data";

    private ResourceLocation id;
    public final BiConsumer<I, CompoundTag> encodeRequest;
    public final Function<CompoundTag, I> decodeRequest;
    public final IServerProcessor<I> serverProcessor;
    public final IChunkReceiver<C> chunkProcessor;
    public final Function<C, O> output;

    public DataAccessorType(BiConsumer<I, CompoundTag> encodeRequest, Function<CompoundTag, I> decodeRequest, IServerProcessor<I> serverProcessor, IChunkReceiver<C> chunkProcessor, Function<C, O> output) {
        this.encodeRequest = encodeRequest;
        this.decodeRequest = decodeRequest;
        this.serverProcessor = serverProcessor;
        this.chunkProcessor = chunkProcessor;
        this.output = output;
    }

    public ResourceLocation getId() {
        return id;
    }

    public static <I, C, O> DataAccessorType<I, C, O> register(ResourceLocation id, DataAccessorType<I, C, O> o) {
        if (values.containsKey(id)) {
            throw new IllegalArgumentException("A DataAccessorType with this id already exists: " + id);
        }
        values.put(id, o);
        o.id = id;
        return o;
    }

    public static ImmutableList<DataAccessorType<?, ?, ?>> values() {
        return ImmutableList.copyOf(values.values());
    }

    @SuppressWarnings("unchecked")
    public static <I, C, O> Optional<DataAccessorType<I, C, O>> get(ResourceLocation id) {
        return Optional.ofNullable(values.containsKey(id) ? (DataAccessorType<I, C, O>)values.get(id) : null);
    }

    public static class Builder {
        public static <I, C, O> DataAccessorType<I, C, O> createChunked(BiConsumer<I, CompoundTag> encodeRequest, Function<CompoundTag, I> decodeRequest, IServerProcessor<I> serverProcessor, IChunkReceiver<C> chunkProcessor, Function<C, O> output) {
            return new DataAccessorType<>(encodeRequest, decodeRequest, serverProcessor, chunkProcessor, output);
        }

        public static <I, O> DataAccessorType<I, O, O> create(BiConsumer<I, CompoundTag> encodeRequest, Function<CompoundTag, I> decodeRequest, IServerProcessor<I> serverProcessor, IChunkReceiver<O> chunkProcessor) {
            return createChunked(encodeRequest, decodeRequest, serverProcessor, chunkProcessor, x -> x);
        }

        public static <I> DataAccessorType<I, Void, Void> createEmptyResponse(BiConsumer<I, CompoundTag> encodeRequest, Function<CompoundTag, I> decodeRequest, IServerProcessor<I> serverProcessor) {
            return new DataAccessorType<>(encodeRequest, decodeRequest, serverProcessor, (a, b, c, d) -> null, x -> x);
        }

        public static <C, O> DataAccessorType<Void, C, O> createNoInputChunked(IServerProcessor<Void> serverProcessor, IChunkReceiver<C> chunkProcessor, Function<C, O> output) {
            return new DataAccessorType<>((i, nbt) -> {}, nbt -> null, serverProcessor, chunkProcessor, output);
        }       

        public static <O> DataAccessorType<Void, O, O> createNoInput(IServerProcessor<Void> serverProcessor, IChunkReceiver<O> chunkProcessor) {
            return createNoInputChunked(serverProcessor, chunkProcessor, x -> x);
        }

        public static DataAccessorType<Void, Void, Void> createNoIO(IServerProcessor<Void> serverProcessor) {
            return createNoInput(serverProcessor, (a, b, c, d) -> null);
        }
    }
    
}
