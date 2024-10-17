package de.mrjulsen.mcdragonlib.util.accessor;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.Single.MutableSingle;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.WorkerAsync;
import dev.architectury.platform.Platform;

import java.util.UUID;

import net.fabricmc.api.EnvType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class DataAccessor {
    private static final Map<UUID, IChunkProcessor> callbacks = new HashMap<>();

    private static WorkerAsync serverWorker;
    private static WorkerAsync clientWorker;

    public static void startServerWorker() {
        stopServerWorker();
        serverWorker = new WorkerAsync("DragonLib Network Manager (Server)", DragonLib.LOGGER);
        serverWorker.start();
    }
    
    public static void startClientWorker() {
        stopClientWorker();
        clientWorker = new WorkerAsync("DragonLib Network Manager (Client)", DragonLib.LOGGER);
        clientWorker.start();
    }
    
    public static void stopServerWorker() {
        DLUtils.doIfNotNull(serverWorker, WorkerAsync::stop);
    }
    
    public static void stopClientWorker() {
        DLUtils.doIfNotNull(clientWorker, WorkerAsync::stop);
    }

    public static WorkerAsync getServerWorker() {
        return serverWorker;
    }

    public static WorkerAsync getClientWorker() {
        return clientWorker;
    }

    public static WorkerAsync getWorker(boolean preferServer) {
        if (clientWorker == null && serverWorker == null) {
            startServerWorker();
        }
        boolean useClient = (!preferServer && Platform.getEnv() == EnvType.CLIENT && clientWorker != null) || (serverWorker == null);
        return useClient ? clientWorker : serverWorker;
    }



    public static UUID addCallback(IChunkProcessor callback) {
        synchronized (callbacks) {
            UUID id;
            do {
                id = UUID.randomUUID();
            } while (callbacks.containsKey(id));
            callbacks.put(id, callback);
            return id;
        }
    }

    public static void run(UUID id, boolean hasMore, int iteration, CompoundTag nbt) {
        synchronized (callbacks) {
            if (callbacks.containsKey(id)) {
                (hasMore ? callbacks.get(id) : callbacks.remove(id)).run(hasMore, iteration, nbt);
            }
        }
    }

    /**
     * Example
     * <pre>
     * (in, nbt) -> {
     * 
     * }, (nbt) -> {
     *     return data;
     * }, (player, in, temp, nbt, iteration) -> {
     *     return hasMore;
     * }, (IChunkReceiver<Collection<String>>)(hasMore, list, iteration, nbt) -> {
     *     return list;
     * }, (chunks) -> {
     *     return chunks;
     * }
     * </pre>
     * @param <I> The Input data type
     * @param <C> The data type of the output chunks.
     * @param <O> The final data type for the end result.
     * @param param The input parameter.
     * @param type The DataAccessorType which contains all the functions.
     * @param output The final result.
     */
    public static <I, C, O> void getFromServer(I param, DataAccessorType<I, C, O> type, Consumer<O> output) {     
        BasicDataAccessorPacket<I, C, O> instance = new BasicDataAccessorPacket<>();
        MutableSingle<C> chunks = new MutableSingle<C>(null);
        UUID id = addCallback((hasMore, iteration, nbt) -> {           
            chunks.setFirst(instance.receiveChunk(hasMore, chunks.getFirst(), iteration, nbt));
            if (!hasMore) {
                output.accept(instance.processClient(chunks.getFirst()));
            }
        });
        instance.setData(id, param, type, true);
        DragonLib.getDragonLibNetworkManager().CHANNEL.sendToServer(instance);
    }
    
    /**
     * Example
     * <pre>
     * (in, nbt) -> {
     * 
     * }, (nbt) -> {
     *     return data;
     * }, (player, in, temp, nbt, iteration) -> {
     *     return hasMore;
     * }, (IChunkReceiver<Collection<String>>)(hasMore, list, iteration, nbt) -> {
     *     return list;
     * }, (chunks) -> {
     *     return chunks;
     * }
     * </pre>
     * @param <I> The Input data type
     * @param <C> The data type of the output chunks.
     * @param <O> The final data type for the end result.
     * @param param The input parameter.
     * @param type The DataAccessorType which contains all the functions.
     * @param output The final result.
     */
    public static <I, C, O> void getFromClient(ServerPlayer player, I param, DataAccessorType<I, C, O> type, Consumer<O> output) {     
        BasicDataAccessorPacket<I, C, O> instance = new BasicDataAccessorPacket<>();
        MutableSingle<C> chunks = new MutableSingle<C>(null);
        UUID id = addCallback((hasMore, iteration, nbt) -> {
            chunks.setFirst(instance.receiveChunk(hasMore, chunks.getFirst(), iteration, nbt));
            if (!hasMore) {
                output.accept(instance.processClient(chunks.getFirst()));
            }
        });
        instance.setData(id, param, type, false);
        DragonLib.getDragonLibNetworkManager().CHANNEL.sendToPlayer(player, instance);
    }

    @FunctionalInterface
    public static interface IChunkProcessor {
        void run(boolean hasMore, int iteration, CompoundTag nbt);
    }
}
