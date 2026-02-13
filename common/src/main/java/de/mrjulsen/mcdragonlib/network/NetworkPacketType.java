package de.mrjulsen.mcdragonlib.network;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;
import de.mrjulsen.mcdragonlib.util.Cache;
import de.mrjulsen.mcdragonlib.util.DependencyVersionChecker;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData.Empty;
import de.mrjulsen.mcdragonlib.network.NetworkProcessor.StreamReceiver;
import de.mrjulsen.mcdragonlib.network.NetworkProcessor.StreamProvider;
import de.mrjulsen.mcdragonlib.network.packet.NetworkPacker;
import de.mrjulsen.mcdragonlib.network.packet.PacketHeaderInfo;
import de.mrjulsen.mcdragonlib.network.packet.PacketType;
import de.mrjulsen.mcdragonlib.util.DLStatistics;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Represents a typed network packet definition for a specific channel and packet semantics.
 *
 * <p>This abstract base class encapsulates common behavior and metadata used by concrete
 * packet type variants such as {@link Send}, {@link Receive}, {@link SendAndReceive} and {@link Stream}.
 * Each instance carries factories for input/output payload containers and is responsible for
 * creating header information, packing outgoing data and dispatching incoming payloads to
 * the appropriate handlers.
 *
 * @param <N> the {@link NetworkDirection} type describing the sender/receiver direction
 * @param <I> input packet data type, implementing {@link NetworkPacketData}
 * @param <O> output/response packet data type, implementing {@link NetworkPacketData}
 */
public abstract class NetworkPacketType<N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData> {

    private final ResourceLocation channelId;
    private final String name;
    private final PacketType type;
    private final NetworkSide direction;
    private final Function<DLStatus, I> sendFactory;
    private final Function<DLStatus, O> responseFactory;

    final Cache<Boolean> shouldUseOldNetworkSystem;

    public NetworkPacketType(ResourceLocation channelId, PacketType type, NetworkDirection direction, String name, Function<DLStatus, I> sendFactory, Function<DLStatus, O> responseFactory) {
        this.channelId = channelId;
        this.type = type;
        this.name = name;
        this.direction = direction.getDirection();
        this.sendFactory = sendFactory;
        this.responseFactory = responseFactory;

        this.shouldUseOldNetworkSystem = new Cache<>(() -> {
            boolean result =  DependencyVersionChecker.checkDependencies(channelId.getNamespace().replace("wiresapi", "pantographsandwires"), DragonLib.MODID, "1.20.1-3.0.20-beta").map(r -> {
                if (ModCommonConfig.DEBUG_NETWORKING.get()) {
                    DLNetworkManager.LOGGER.info("Check Network System Version: " + r);
                }
                return r.relation() == DependencyVersionChecker.VersionRelation.IS_OLDER;
            }).orElse(false);
            if (result) {
                DLNetworkManager.LOGGER.warn(channelId.getNamespace() + " was built with an older version of DragonLib's networking system. For compatibility, it uses the old system.");
            }
            return result;
        });
    }

    /**
     * Runs the supplied action in a "safe" manner depending on the environment.
     *
     * <p>On client environments this will schedule the action using {@link EnvExecutor#runInEnv},
     * otherwise it executes immediately. The action is provided as a {@link Supplier<Runnable>}
     * to defer creation until the target environment is known.
     *
     * @param environment the target environment the action should run in
     * @param action supplier producing the runnable action
     */
    protected static void runSafe(Env environment, Supplier<Runnable> action) {
        if (environment == Env.CLIENT) {
            EnvExecutor.runInEnv(environment, action);
        } else {
            action.get().run();
        }
    }

    static <T extends NetworkPacketData> void runAndRespondAsync(NetworkPacketType<?, ?, ?> type, Supplier<T> task, Consumer<CompoundTag> response, Function<Throwable, T> errorFactory) {
        NetworkThreadPool.executeWithResponse(type, () -> {
                    try {
                        return task.get();
                    } catch (Exception e) {
                        DLNetworkManager.LOGGER.error("Could not handle network task.", e);
                        return errorFactory.apply(e);
                    }
                },
                (result) -> {
                    CompoundTag nbt;
                    try {
                        nbt = result.serializeNbt();
                    } catch (Exception e) {
                        Exception exception = e;
                        if (result != null) {
                            DLStatus previousStatus = result.getStatus();
                            if (previousStatus != null && !previousStatus.noIssues()) {
                                Exception previousException = new Exception("Caused by [Flag: " + previousStatus.flag() + ", Code: " + previousStatus.code() + "]: " + result.getStatus().message());
                                String combinedMessage = exception.getMessage() + "; " + previousException.getMessage();
                                RuntimeException ex = new RuntimeException(combinedMessage, exception);
                                ex.addSuppressed(previousException);
                                exception = ex;
                            }
                        }
                        DLNetworkManager.LOGGER.error("Could not serialize response. Please check the response data factory.", exception);
                        nbt = NetworkPacketData.DEFAULT_INSTANCE.apply(DLStatus.error(exception)).serializeNbt();
                    }
                    response.accept(nbt);
                },
                errorFactory
        );
    }

    static <T extends NetworkPacketData> void runAsync(NetworkPacketType<?, ?, ?> type, Runnable task, Consumer<Throwable> onError) {
        NetworkThreadPool.execute(type, () -> {
                    try {
                        task.run();
                    } catch (Exception e) {
                        DLNetworkManager.LOGGER.error("Could not handle network task.", e);
                        onError.accept(e);
                    }
                },
                (e) -> {
                    DLNetworkManager.LOGGER.error("Could not handle network task.", e);
                    onError.accept(e);
                });
    }
    
    /**
     * Called when a request is received for this packet type.
     *
     * <p>Concrete implementations must implement how to handle the deserialized input data.
     *
     * @param info header info describing packet type and request id
     * @param context platform-specific network context for this packet dispatch
     * @param in deserialized input data instance
     */
    abstract void receiveRequest(PacketHeaderInfo info, NetworkPacketContext context, I in);

    /**
     * Called when a response is received for this packet type.
     *
     * <p>Concrete implementations must implement how to handle the deserialized response data.
     *
     * @param info header info describing packet type and request id
     * @param context platform-specific network context for this packet dispatch
     * @param in deserialized response data instance
     */
    abstract void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, O in);

    /**
     * Returns the channel id this packet type belongs to.
     *
     * @return the {@link ResourceLocation} channel id
     */
    public final ResourceLocation getChannelId() {
        return channelId;
    }

    /**
     * Returns the unique name (identifier) of this packet inside its channel.
     *
     * @return short packet id / name
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the configured {@link PacketType} of this packet (e.g. SEND, RECEIVE, STREAM).
     *
     * @return the packet type enum value
     */
    public final PacketType getType() {
        return type;
    }

    /**
     * Returns the {@link NetworkSide} direction this packet is associated with.
     *
     * <p>Note: direction indicates which logical side this packet is defined for (e.g. client-to-server).
     *
     * @return the packet's network side
     */
    public final NetworkSide getDirection() {
        return direction;
    }

    /**
     * Builds a {@link PacketHeaderInfo} instance for the provided communication type and request id.
     *
     * @param communication whether this header describes a REQUEST or RESPONSE
     * @param requestId unique request id used for matching responses/streams
     * @return a new {@link PacketHeaderInfo} instance
     */
    public final PacketHeaderInfo getInfo(CommunicationType communication, long requestId) {
        return new PacketHeaderInfo(getType(), communication, requestId, getName());
    }
    
    /**
     * Internal dispatcher that invokes either request or response handling based on the {@link CommunicationType}.
     *
     * @param info header information already read from the incoming buffer
     * @param context contextual information for the network dispatch
     * @param nbt compound tag containing the serialized payload
     * @param communication the communication direction (REQUEST or RESPONSE)
     */
    protected void receive(PacketHeaderInfo info, NetworkPacketContext context, CompoundTag nbt, CommunicationType communication) {
        switch (communication) {
            case REQUEST -> receiveInternal(info, context, nbt);
            case RESPONSE -> receiveResponseInternal(info, context, nbt);
            default -> {}
        }
    }

    /**
     * Creates an empty input instance using the configured send factory and the provided status.
     *
     * @param status the {@link DLStatus} to attach to the created data instance
     * @return new input instance of type {@code I}
     */
    protected I createEmptyInputData(DLStatus status) {
        return sendFactory.apply(status);
    }

    /**
     * Creates an empty output/response instance using the configured response factory and the provided status.
     *
     * @param status the {@link DLStatus} to attach to the created data instance
     * @return new output instance of type {@code O}
     */
    protected O createEmptyOutputData(DLStatus status) {
        return responseFactory.apply(status);
    }

    /**
     * Packs and sends a REQUEST for this packet type using the provided sender direction.
     *
     * @param requestId unique request identifier used to match responses or stream segments
     * @param sender the {@link NetworkDirection} that will actually transmit the generated {@link Packet} instances
     * @param nbt optional serialized payload (may be {@code null} for empty payloads)
     */
    protected void sendInternal(long requestId, N sender, @Nullable CompoundTag nbt) {
        PacketHeaderInfo info = getInfo(CommunicationType.REQUEST, requestId);
        List<Packet<?>> mcPackets = NetworkPacker.pack(getChannelId(), getName(), info, sender.getDirection(), nbt);
        for (Packet<?> packet : mcPackets) {
            sender.send(packet);
        }
    }

    /**
     * Internal helper that deserializes incoming REQUEST payload and forwards to {@link #receiveRequest}.
     *
     * @param info header information for the incoming packet
     * @param context network dispatch context
     * @param nbt serialized request payload
     */
    protected void receiveInternal(PacketHeaderInfo info, NetworkPacketContext context, CompoundTag nbt) {
        I instance = sendFactory.apply(DLStatus.EMPTY);
        instance.deserializeNbt(nbt);
        receiveRequest(info, context, instance);
    }
    

    /**
     * Sends a RESPONSE for a received request header using the opposite direction of this packet type.
     *
     * <p>For server-targeted packets this will create a player-direction sender, and vice versa.
     *
     * @param header original request header to respond to
     * @param context network context for resolving player/sender information
     * @param nbt optional serialized payload for the response
     */
    protected void respondInternal(PacketHeaderInfo header, NetworkPacketContext context, @Nullable CompoundTag nbt) {
        NetworkDirection sender = getDirection() == NetworkSide.C2S ? NetworkDirection.toPlayer((ServerPlayer)context.getPlayer()) : NetworkDirection.toServer();
        PacketHeaderInfo info = getInfo(CommunicationType.RESPONSE, header.requestId());
        List<Packet<?>> mcPackets = NetworkPacker.pack(getChannelId(), getName(), info, sender.getDirection(), nbt);
        for (Packet<?> packet : mcPackets) {
            sender.send(packet);
        }
    }

    /**
     * Internal helper that deserializes incoming RESPONSE payload and forwards to {@link #receiveResponse}.
     *
     * @param info header information for the incoming response
     * @param context network dispatch context
     * @param nbt serialized response payload
     */
    protected void receiveResponseInternal(PacketHeaderInfo info, NetworkPacketContext context, CompoundTag nbt) {
        O instance = responseFactory.apply(DLStatus.EMPTY);
        instance.deserializeNbt(nbt);
        receiveResponse(info, context, instance);
    }




    /**
     * Packet type used for send-only semantics (no response expected).
     *
     * @param <N> direction type
     * @param <I> input data type
     */
    public static class Send<N extends NetworkDirection, I extends NetworkPacketData> extends NetworkPacketType<N, I, NetworkPacketData.Empty> {

        /**
         * Handler invoked when a matching packet REQUEST arrives.
         *
         * <p>Protected so subclasses/outer classes can access it when implementing custom logic.
         */
        protected final NetworkProcessor.Send<I> handler;

        public Send(ResourceLocation channelId, String name, NetworkDirection direction, NetworkProcessor.Send<I> handler, Function<DLStatus, I> factory) {
            super(channelId, PacketType.SEND, direction, name, factory, NetworkPacketData.Empty::new);
            this.handler = handler;
        }

        @Override
        void receiveRequest(PacketHeaderInfo info, NetworkPacketContext context, I in) {
            if (shouldUseOldNetworkSystem.get()) {
                context.queue(() -> {
                    runSafe(context.getEnvironment(), () -> () -> {
                        handler.execute(in, context);
                    });
                });
                return;
            }
            NetworkPacketType.runAsync(this, () -> handler.execute(in, context), (e) -> {});
        }

        @Override
        void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, Empty in) {}
        
        /**
         * Sends the provided data as a request from the given sender.
         *
         * @param sender network direction that will perform the transmission
         * @param data payload instance to serialize and send
         */
        public void send(N sender, I data) {
            long requestId = System.nanoTime();
            sendInternal(requestId, sender, data.serializeNbt());
        }
    }



    
    /**
     * Packet type used for receive-request semantics where a request invokes server-side logic
     * and a response is expected by the caller.
     *
     * <p>This variant stores a static callback map that matches request ids to {@link CompletableFuture}
     * instances that will be completed when the response arrives.
     *
     * @param <N> direction type
     * @param <O> output data type
     */
    public static class Receive<N extends NetworkDirection, O extends NetworkPacketData> extends NetworkPacketType<N, NetworkPacketData.Empty, O> {

        /**
         * Map of outstanding request ids to their associated {@link CompletableFuture} callbacks.
         * The map is {@link java.util.concurrent.ConcurrentHashMap} backed for thread-safety.
         */
        protected static final Map<Long, CompletableFuture<?>> callbacks = new ConcurrentHashMap<>();
        private final NetworkProcessor.Receive<O> handler;

        public Receive(ResourceLocation channelId, String name, NetworkDirection direction, NetworkProcessor.Receive<O> handler, Function<DLStatus, O> factory) {
            super(channelId, PacketType.RECEIVE, direction, name, NetworkPacketData.Empty::new, factory);
            this.handler = handler;
        }

        /**
         * Returns debug statistics about active callbacks for monitoring purposes.
         *
         * @return a {@link de.mrjulsen.mcdragonlib.util.DLStatistics} instance representing callback counts
         */
        public static DLStatistics debug_getStats() {
            DLStatistics.Group group = new DLStatistics.Group("callbacks", "Callbacks");
            DLStatistics stats = new DLStatistics("Network Receive Packets", List.of(
                new DLStatistics.Stat(group, "Result Callbacks", callbacks.size())
            ));
            return stats;
        }

        @Override
        void receiveRequest(PacketHeaderInfo info, NetworkPacketContext context, NetworkPacketData.Empty in) {
            if (shouldUseOldNetworkSystem.get()) {
                context.queue(() -> {
                    runSafe(context.getEnvironment(), () -> () -> {
                        try {
                            O data = handler.execute(context);
                            respondInternal(info, context, data.serializeNbt());
                        } catch (Exception e) {
                            respondInternal(info, context, createEmptyOutputData(DLStatus.error(e)).serializeNbt());
                        }
                    });
                });
                return;
            }

            NetworkPacketType.runAndRespondAsync(
                    this,
                    () -> handler.execute(context),
                    (nbt) -> respondInternal(info, context, nbt),
                    (ex) -> createEmptyOutputData(DLStatus.error(ex))
            );
        }

        @Override
        void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, O in) {
            if (callbacks.containsKey(info.requestId())) {
                ((CompletableFuture<O>)callbacks.remove(info.requestId())).complete(in);
            }
        }
        
        
        /**
         * Sends a request and provides callbacks for success and error.
         *
         * @param sender the network sender direction
         * @param responseCallback consumer invoked when a response arrives
         * @param errorCallback runnable invoked if the request times out or fails
         */
        public void send(N sender, Consumer<O> responseCallback, Runnable errorCallback) {
            CompletableFuture<O> future = new CompletableFuture<>();
            future.thenAccept(responseCallback).exceptionally(ex -> {
                DLNetworkManager.LOGGER.error("Error while waiting for response. [ChannelID: " + getChannelId() + ", Name: " + getName() + "]", ex);
                errorCallback.run();
                return null;
            }).orTimeout(60, TimeUnit.SECONDS);
            send(sender, future);
        }
        
        /**
         * Sends a request and registers the provided {@link CompletableFuture} to be completed on response.
         *
         * @param sender the network sender direction
         * @param responseCallback future to complete when response arrives
         */
        public void send(N sender, CompletableFuture<O> responseCallback) {
            long requestId = System.nanoTime();
            callbacks.put(requestId, responseCallback);
            sendInternal(requestId, sender, new CompoundTag());
        }
    } 



    
    /**
     * Packet type supporting send-and-receive (request/response) semantics where an input payload is sent
     * and a typed output is expected.
     *
     * <p>Maintains its own static callback map similar to {@link Receive}.
     *
     * @param <N> direction type
     * @param <I> input data type
     * @param <O> output data type
     */
    public static class SendAndReceive<N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData> extends NetworkPacketType<N, I, O> {

        /**
         * Map holding outstanding request callbacks keyed by request id.
         */
        protected static final Map<Long, CompletableFuture<?>> callbacks = new ConcurrentHashMap<>();
        private final NetworkProcessor.SendAndReceive<I, O> handler;

        public SendAndReceive(ResourceLocation channelId, String name, NetworkDirection direction, NetworkProcessor.SendAndReceive<I, O> handler, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory) {
            super(channelId, PacketType.SEND_AND_RECEIVE, direction, name, inputFactory, outputFactory);
            this.handler = handler;
        }
        
        /**
         * Returns debug statistics about active callbacks for monitoring purposes.
         *
         * @return a {@link de.mrjulsen.mcdragonlib.util.DLStatistics} instance representing callback counts
         */
        public static DLStatistics debug_getStats() {
            DLStatistics.Group group = new DLStatistics.Group("callbacks", "Callbacks");
            DLStatistics stats = new DLStatistics("Network Send and Receive Packets", List.of(
                new DLStatistics.Stat(group, "Result Callbacks", callbacks.size())
            ));
            return stats;
        }

        @Override
        void receiveRequest(PacketHeaderInfo info, NetworkPacketContext context, I in) {
            if (shouldUseOldNetworkSystem.get()) {
                context.queue(() -> {
                    runSafe(context.getEnvironment(), () -> () -> {
                        try {
                            O data = handler.execute((I)in, context);
                            respondInternal(info, context, data.serializeNbt());
                        } catch (Exception e) {
                            respondInternal(info, context, createEmptyOutputData(DLStatus.error(e)).serializeNbt());
                        }
                    });
                });
                return;
            }

            NetworkPacketType.runAndRespondAsync(
                    this,
                    () -> handler.execute(in, context),
                    (nbt) -> respondInternal(info, context, nbt),
                    (ex) -> createEmptyOutputData(DLStatus.error(ex))
            );
        }

        @Override
        void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, O in) {
            if (callbacks.containsKey(info.requestId())) {
                ((CompletableFuture<O>)callbacks.remove(info.requestId())).complete(in);
            }
        }

        /**
         * Sends a request with the given input payload and registers success/error handlers.
         *
         * @param sender network direction used to send
         * @param data input payload to serialize and send
         * @param responseCallback invoked when response arrives
         * @param errorCallback invoked if the request times out or fails
         */
        public void send(N sender, I data, Consumer<O> responseCallback, Runnable errorCallback) {
            send(sender, data, ModCommonConfig.NETWORK_RESPONSE_TIMEOUT.get(), responseCallback, errorCallback);
        }
        
        /**
         * Sends a request with the given input payload and registers success/error handlers.
         *
         * @param sender network direction used to send
         * @param data input payload to serialize and send
         * @param timeout The time in seconds before the system stops waiting for a response and returns an error
         * @param responseCallback invoked when response arrives
         * @param errorCallback invoked if the request times out or fails
         */
        public void send(N sender, I data, int timeout, Consumer<O> responseCallback, Runnable errorCallback) {
            CompletableFuture<O> future = new CompletableFuture<>();
            future
                    .orTimeout(timeout, TimeUnit.SECONDS)
                    .thenAccept(responseCallback)
                    .exceptionally(ex -> {
                        DLNetworkManager.LOGGER.error("Error while waiting for response [ChannelID: " + getChannelId() + ", Name: " + getName() + "]: " + ex.getMessage());
                            errorCallback.run();
                            return null;
                    }
            );
            send(sender, data, future);
        }
        
        /**
         * Sends a request with the given input payload and registers a {@link CompletableFuture}
         * that will be completed when the response arrives.
         *
         * @param sender network direction used to send
         * @param data input payload to serialize and send
         * @param responseCallback future to complete when response arrives
         */
        public void send(N sender, I data, CompletableFuture<O> responseCallback) {
            long requestId = System.nanoTime();
            callbacks.put(requestId, responseCallback);
            sendInternal(requestId, sender, data.serializeNbt());
        }
    }



    
    /**
     * Packet type used for streaming scenarios where multiple segments may be exchanged for a single request id.
     *
     * <p>This class manages several static caches:
     * <ul>
     *   <li>{@code callbacks} maps request ids to finish callbacks</li>
     *   <li>{@code inputCache} stores providers that supply the next input segment</li>
     *   <li>{@code outputCache} stores receivers that process incoming segments</li>
     * </ul>
     *
     * @param <N> direction type
     * @param <I> input data type for stream segments
     * @param <O> output data type for stream segments
     */
    public static class Stream<N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData> extends NetworkPacketType<N, I, O> {

        /**
         * Internal record that stores information associated with an active stream request.
         *
         * @param sender original sender direction
         * @param requestId unique id for the stream
         * @param callback consumer invoked with the final {@link DLStatus} when the stream finishes
         * @param <N> direction type
         * @param <O> output packet type for the stream receiver
         */
        private record RequestData<N extends NetworkDirection, O extends NetworkPacketData>(N sender, long requestId, Consumer<DLStatus> callback) {}

        /**
         * Map of outstanding stream request ids to their finish callbacks and metadata.
         */
        protected static final Map<Long, RequestData<?, ?>> callbacks = new ConcurrentHashMap<>();

        /**
         * Cache of input providers used to produce subsequent input segments for active streams.
         */
        protected static final Map<Long, StreamProvider<?, ?>> inputCache = new ConcurrentHashMap<>();

        /**
         * Cache of output receivers used to aggregate/process incoming stream segments.
         */
        protected static final Map<Long, StreamReceiver<?, ?>> outputCache = new ConcurrentHashMap<>();

        private final Supplier<StreamReceiver<I, O>> receiverFactory;


        public Stream(ResourceLocation channelId, String name, NetworkDirection direction, Supplier<StreamReceiver<I, O>> receiverFactory, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory) {
            super(channelId, PacketType.STREAM, direction, name, inputFactory, outputFactory);
            this.receiverFactory = receiverFactory;
        }
        
        /**
         * Returns debug statistics about active callbacks for monitoring purposes.
         *
         * @return a {@link de.mrjulsen.mcdragonlib.util.DLStatistics} instance representing callback counts
         */
        public static DLStatistics debug_getStats() {
            DLStatistics.Group group = new DLStatistics.Group("callbacks", "Callbacks");
            DLStatistics stats = new DLStatistics("Network Stream Packets", List.of(
                new DLStatistics.Stat(group, "Result Callbacks", callbacks.size()),
                new DLStatistics.Stat(group, "Input Data Providers", inputCache.size()),
                new DLStatistics.Stat(group, "Output Data Providers", outputCache.size())
            ));
            return stats;
        }

        @Override
        void receiveRequest(PacketHeaderInfo info, NetworkPacketContext context, I in) {
            if (shouldUseOldNetworkSystem.get()) {
                context.queue(() -> {
                    runSafe(context.getEnvironment(), () -> () -> {
                        try {
                            O data = ((StreamReceiver<I, O>)outputCache.computeIfAbsent(info.requestId(), l -> receiverFactory.get())).execute((I)in, context);
                            if (in.getStatus().isDone() || in.getStatus().isError() || in.getStatus().isCancel()) {
                                outputCache.remove(info.requestId());
                            }
                            respondInternal(info, context, data.serializeNbt());
                        } catch (Exception e) {
                            respondInternal(info, context, createEmptyOutputData(DLStatus.error(e)).serializeNbt());
                            outputCache.remove(info.requestId());
                        }
                    });
                });
                return;
            }

            NetworkPacketType.runAndRespondAsync(
                    this,
                    () -> {
                        StreamReceiver<I, O> receiver = (StreamReceiver<I, O>) outputCache.computeIfAbsent(info.requestId(), id -> receiverFactory.get());
                        O data = receiver.execute(in, context);

                        if (in.getStatus().isDone() || in.getStatus().isError() || in.getStatus().isCancel()) {
                            outputCache.remove(info.requestId());
                        }
                        return data;
                    },
                    (nbt) -> respondInternal(info, context, nbt),
                    (ex) -> {
                        outputCache.remove(info.requestId());
                        return createEmptyOutputData(DLStatus.error(ex));
                    }
            );
        }

        @Override
        void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, O in) {
            if (shouldUseOldNetworkSystem.get()) {
                context.queue(() -> {
                    runSafe(context.getEnvironment(), () -> () -> {
                        try {
                            I data = ((StreamProvider<I, O>)inputCache.get(info.requestId())).execute(false, Optional.of(in), Optional.of(context));
                            RequestData<N, O> requestData = ((RequestData<N, O>)callbacks.get(info.requestId()));
                            if (!in.getStatus().isDone()) {
                                sendInternal(requestData.requestId(), NetworkDirection.forContext(requestData.sender(), context), data.serializeNbt());
                            }
                            if (in.getStatus().isDone() || in.getStatus().isError() || in.getStatus().isCancel()) {
                                callbacks.remove(info.requestId()).callback().accept(data.getStatus());
                                inputCache.remove(info.requestId());
                            }
                        } catch (Exception e) {
                            sendInternal(info.requestId(), (N)context.buildDirection(), createEmptyInputData(DLStatus.error(e)).serializeNbt());
                            callbacks.remove(info.requestId()).callback().accept(DLStatus.error(e));
                            inputCache.remove(info.requestId());
                        }
                    });
                });
                return;
            }

            runAsync(this, () -> {
                StreamProvider<I, O> provider = (StreamProvider<I, O>) inputCache.get(info.requestId());
                I data = provider.execute(false, Optional.of(in), Optional.of(context));
                RequestData<N, O> requestData = (RequestData<N, O>) callbacks.get(info.requestId());

                if (!in.getStatus().isDone()) {
                    sendInternal(requestData.requestId(), NetworkDirection.forContext(requestData.sender(), context), data.serializeNbt());
                }

                if (in.getStatus().isDone() || in.getStatus().isError() || in.getStatus().isCancel()) {
                    callbacks.remove(info.requestId()).callback().accept(data.getStatus());
                    inputCache.remove(info.requestId());
                }
            }, e -> {
                sendInternal(info.requestId(), (N) context.buildDirection(), createEmptyInputData(DLStatus.error(e)).serializeNbt());
                callbacks.remove(info.requestId()).callback().accept(DLStatus.error(e));
                inputCache.remove(info.requestId());
            });
        }

        /**
         * Sends the initial stream request and registers the provided {@link StreamProvider} as the input source.
         *
         * @param sender the network direction that will transmit the stream
         * @param handler the {@link StreamProvider} responsible for generating input segments
         * @param finishCallback consumer that will be invoked with the final {@link DLStatus} when the stream ends
         */
        public void send(N sender, StreamProvider<I, O> handler, Consumer<DLStatus> finishCallback) {
            long requestId = System.nanoTime();
            callbacks.put(requestId, new RequestData<>(sender, requestId, finishCallback));
            inputCache.put(requestId, handler);
            sendInternal(requestId, sender, handler.execute(true, Optional.empty(), Optional.empty()).serializeNbt());
        }
    }
}
