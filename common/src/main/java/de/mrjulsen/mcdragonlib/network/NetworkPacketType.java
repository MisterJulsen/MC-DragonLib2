package de.mrjulsen.mcdragonlib.network;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.mcdragonlib.config.ModCommonConfig;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.net.DLChannel;
import de.mrjulsen.mcdragonlib.net.ExecutionMode;
import de.mrjulsen.mcdragonlib.net.NetworkSettings;
import de.mrjulsen.mcdragonlib.net.PacketDefinition;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData.Empty;
import de.mrjulsen.mcdragonlib.network.NetworkProcessor.StreamProvider;
import de.mrjulsen.mcdragonlib.network.NetworkProcessor.StreamReceiver;
import de.mrjulsen.mcdragonlib.network.internal.LegacyBridge;
import de.mrjulsen.mcdragonlib.network.internal.LegacyRequestPacket;
import de.mrjulsen.mcdragonlib.network.internal.LegacySendPacket;
import de.mrjulsen.mcdragonlib.network.internal.LegacyStreamPacket;
import de.mrjulsen.mcdragonlib.network.packet.PacketHeaderInfo;
import de.mrjulsen.mcdragonlib.network.packet.PacketType;
import de.mrjulsen.mcdragonlib.util.DLStatistics;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * A typed packet definition of the deprecated networking API.
 *
 * <p>Every packet registered through this class is carried by the current networking layer, so
 * old and new packets share one wire format, one set of limits and one place where connections
 * are cleaned up. Handlers keep running off the game thread, as they did before.
 *
 * @param <N> the direction type
 * @param <I> the type sent by the initiating side
 * @param <O> the type sent back
 * @deprecated Use {@link de.mrjulsen.mcdragonlib.net.DLChannel} and the packet builders in
 *             {@code de.mrjulsen.mcdragonlib.net.packet} instead.
 */
@Deprecated
public abstract class NetworkPacketType<N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData> {

    private final ResourceLocation channelId;
    private final String name;
    private final PacketType type;
    private final NetworkSide direction;
    private final Function<DLStatus, I> sendFactory;
    private final Function<DLStatus, O> responseFactory;

    /**
     * Creates a packet definition.
     *
     * @param channelId the channel the packet belongs to
     * @param type the exchange pattern
     * @param direction which side starts the exchange
     * @param name the packet id, unique within its channel
     * @param sendFactory creates a blank instance of the initiating payload
     * @param responseFactory creates a blank instance of the answering payload
     */
    public NetworkPacketType(ResourceLocation channelId, PacketType type, NetworkDirection direction, String name, Function<DLStatus, I> sendFactory, Function<DLStatus, O> responseFactory) {
        this.channelId = channelId;
        this.type = type;
        this.name = name;
        this.direction = direction.getDirection();
        this.sendFactory = sendFactory;
        this.responseFactory = responseFactory;
    }

    /**
     * Runs an action, loading it lazily on the client so that client-only code is never touched
     * on a dedicated server.
     *
     * @param environment the side to run on
     * @param action supplies the work to perform
     */
    protected static void runSafe(Env environment, Supplier<Runnable> action) {
        if (environment == Env.CLIENT) {
            EnvExecutor.runInEnv(environment, action);
        } else {
            action.get().run();
        }
    }

    /**
     * Returns the channel this packet belongs to.
     *
     * @return the channel id
     */
    public final ResourceLocation getChannelId() {
        return channelId;
    }

    /**
     * Returns the packet id.
     *
     * @return the packet name
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the exchange pattern of this packet.
     *
     * @return the packet type
     */
    public final PacketType getType() {
        return type;
    }

    /**
     * Returns which side starts an exchange of this packet.
     *
     * @return the initiating side
     */
    public final NetworkSide getDirection() {
        return direction;
    }

    /**
     * Builds the header the old wire format used.
     *
     * @param communication whether this is a request or a response
     * @param requestId the exchange id
     * @return a header describing this packet
     */
    public final PacketHeaderInfo getInfo(CommunicationType communication, long requestId) {
        return new PacketHeaderInfo(getType(), communication, requestId, getName());
    }

    /**
     * Creates a blank instance of the initiating payload.
     *
     * @param status the status to start from
     * @return a new payload instance
     */
    protected I createEmptyInputData(DLStatus status) {
        return sendFactory.apply(status);
    }

    /**
     * Creates a blank instance of the answering payload.
     *
     * @param status the status to start from
     * @return a new payload instance
     */
    protected O createEmptyOutputData(DLStatus status) {
        return responseFactory.apply(status);
    }

    /**
     * Returns the settings the delegate of this packet is built from.
     *
     * @return the settings for the underlying packet definition
     */
    protected final PacketDefinition.Settings settings() {
        DLChannel channel = DLChannel.find(channelId).orElseThrow(() ->
            new IllegalStateException("Channel '" + channelId + "' must be created before packet '" + name + "' is registered."));
        return new PacketDefinition.Settings(channel, name, LegacyBridge.direction(direction), ExecutionMode.BLOCKING, ExecutionMode.BLOCKING, NetworkSettings.getMaxMessageBytes());
    }

    abstract PacketDefinition getDelegate();

    /**
     * No longer used. The current transport dispatches received data directly to the delegate.
     *
     * @param info the header that was read
     * @param context the context of the received message
     * @param nbt the payload
     * @param communication whether this is a request or a response
     */
    protected void receive(PacketHeaderInfo info, NetworkPacketContext context, CompoundTag nbt, CommunicationType communication) {}

    /**
     * No longer used. Sending goes through the delegate.
     *
     * @param requestId the exchange id
     * @param sender where the message goes
     * @param nbt the payload
     */
    protected void sendInternal(long requestId, N sender, @Nullable CompoundTag nbt) {}

    /**
     * No longer used. Sending goes through the delegate.
     *
     * @param header the header of the message being answered
     * @param context the context of the received message
     * @param nbt the payload
     */
    protected void respondInternal(PacketHeaderInfo header, NetworkPacketContext context, @Nullable CompoundTag nbt) {}

    /**
     * No longer used. Decoding goes through the delegate.
     *
     * @param info the header that was read
     * @param context the context of the received message
     * @param nbt the payload
     */
    protected void receiveInternal(PacketHeaderInfo info, NetworkPacketContext context, CompoundTag nbt) {}

    /**
     * No longer used. Decoding goes through the delegate.
     *
     * @param info the header that was read
     * @param context the context of the received message
     * @param nbt the payload
     */
    protected void receiveResponseInternal(PacketHeaderInfo info, NetworkPacketContext context, CompoundTag nbt) {}

    abstract void receiveRequest(PacketHeaderInfo info, NetworkPacketContext context, I in);

    abstract void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, O in);

    private static <O> void awaitResponse(NetworkPacketType<?, ?, ?> packet, CompletableFuture<O> future, Consumer<O> responseCallback, Runnable errorCallback) {
        future
            .orTimeout(ModCommonConfig.NETWORK_RESPONSE_TIMEOUT.get(), TimeUnit.SECONDS)
            .thenAccept(responseCallback)
            .exceptionally(ex -> {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                if (cause instanceof TimeoutException) {
                    DLNetworkManager.LOGGER.error("Timeout while waiting for response [ChannelID: " + packet.getChannelId() + ", Name: " + packet.getName() + "]: " + cause.getMessage());
                } else {
                    DLNetworkManager.LOGGER.error("Error while waiting for response [ChannelID: " + packet.getChannelId() + ", Name: " + packet.getName() + "]", cause);
                }
                errorCallback.run();
                return null;
            });
    }

    /**
     * A packet that is sent without expecting an answer.
     *
     * @param <N> the direction type
     * @param <I> the payload type
     * @deprecated Use {@link de.mrjulsen.mcdragonlib.net.packet.SendPacket} instead.
     */
    @Deprecated
    public static class Send<N extends NetworkDirection, I extends NetworkPacketData> extends NetworkPacketType<N, I, NetworkPacketData.Empty> {

        protected final NetworkProcessor.Send<I> handler;
        private final LegacySendPacket<I> delegate;

        public Send(ResourceLocation channelId, String name, NetworkDirection direction, NetworkProcessor.Send<I> handler, Function<DLStatus, I> factory) {
            super(channelId, PacketType.SEND, direction, name, factory, NetworkPacketData.Empty::new);
            this.handler = handler;
            this.delegate = new LegacySendPacket<>(settings(), factory, handler);
        }

        /**
         * Sends a payload.
         *
         * @param sender where the payload goes
         * @param data the value to send
         */
        public void send(N sender, I data) {
            delegate.send(sender, data);
        }

        @Override
        LegacySendPacket<I> getDelegate() {
            return delegate;
        }

        @Override
        void receiveRequest(PacketHeaderInfo info, NetworkPacketContext context, I in) {}

        @Override
        void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, Empty in) {}
    }

    /**
     * A packet that asks the other side for a value without sending one.
     *
     * @param <N> the direction type
     * @param <O> the response type
     * @deprecated Use {@link de.mrjulsen.mcdragonlib.net.packet.RequestPacket} instead.
     */
    @Deprecated
    public static class Receive<N extends NetworkDirection, O extends NetworkPacketData> extends NetworkPacketType<N, NetworkPacketData.Empty, O> {

        protected static final Map<Long, CompletableFuture<?>> callbacks = new ConcurrentHashMap<>();

        private final NetworkProcessor.Receive<O> handler;
        private final LegacyRequestPacket<NetworkPacketData.Empty, O> delegate;

        public Receive(ResourceLocation channelId, String name, NetworkDirection direction, NetworkProcessor.Receive<O> handler, Function<DLStatus, O> factory) {
            super(channelId, PacketType.RECEIVE, direction, name, NetworkPacketData.Empty::new, factory);
            this.handler = handler;
            this.delegate = new LegacyRequestPacket<>(settings(), NetworkPacketData.Empty::new, factory, (in, ctx) -> handler.execute(ctx));
        }

        /**
         * Returns counters about exchanges that are still waiting.
         *
         * @return the current statistics
         */
        public static DLStatistics debug_getStats() {
            DLStatistics.Group group = new DLStatistics.Group("callbacks", "Callbacks");
            return new DLStatistics("Network Receive Packets", List.of(
                new DLStatistics.Stat(group, "Result Callbacks", LegacyRequestPacket.getFallbackCount())
            ));
        }

        /**
         * Asks the other side for a value.
         *
         * @param sender where the request goes
         * @param responseCallback receives the answer
         * @param errorCallback called if no answer arrives
         */
        public void send(N sender, Consumer<O> responseCallback, Runnable errorCallback) {
            CompletableFuture<O> future = new CompletableFuture<>();
            awaitResponse(this, future, responseCallback, errorCallback);
            send(sender, future);
        }

        /**
         * Asks the other side for a value and completes a future with the answer.
         *
         * @param sender where the request goes
         * @param responseCallback completed with the answer
         */
        public void send(N sender, CompletableFuture<O> responseCallback) {
            delegate.send(sender, new NetworkPacketData.Empty(DLStatus.OK), responseCallback);
        }

        @Override
        LegacyRequestPacket<NetworkPacketData.Empty, O> getDelegate() {
            return delegate;
        }

        @Override
        void receiveRequest(PacketHeaderInfo info, NetworkPacketContext context, NetworkPacketData.Empty in) {}

        @Override
        void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, O in) {}
    }

    /**
     * A packet that sends a value and receives one back.
     *
     * @param <N> the direction type
     * @param <I> the request type
     * @param <O> the response type
     * @deprecated Use {@link de.mrjulsen.mcdragonlib.net.packet.RequestPacket} instead.
     */
    @Deprecated
    public static class SendAndReceive<N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData> extends NetworkPacketType<N, I, O> {

        protected static final Map<Long, CompletableFuture<?>> callbacks = new ConcurrentHashMap<>();

        private final NetworkProcessor.SendAndReceive<I, O> handler;
        private final LegacyRequestPacket<I, O> delegate;

        public SendAndReceive(ResourceLocation channelId, String name, NetworkDirection direction, NetworkProcessor.SendAndReceive<I, O> handler, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory) {
            super(channelId, PacketType.SEND_AND_RECEIVE, direction, name, inputFactory, outputFactory);
            this.handler = handler;
            this.delegate = new LegacyRequestPacket<>(settings(), inputFactory, outputFactory, handler::execute);
        }

        /**
         * Returns counters about exchanges that are still waiting.
         *
         * @return the current statistics
         */
        public static DLStatistics debug_getStats() {
            DLStatistics.Group group = new DLStatistics.Group("callbacks", "Callbacks");
            return new DLStatistics("Network Send and Receive Packets", List.of(
                new DLStatistics.Stat(group, "Result Callbacks", LegacyRequestPacket.getFallbackCount())
            ));
        }

        /**
         * Sends a value and waits for the answer.
         *
         * @param sender where the request goes
         * @param data the value to send
         * @param responseCallback receives the answer
         * @param errorCallback called if no answer arrives
         */
        public void send(N sender, I data, Consumer<O> responseCallback, Runnable errorCallback) {
            CompletableFuture<O> future = new CompletableFuture<>();
            awaitResponse(this, future, responseCallback, errorCallback);
            send(sender, data, future);
        }

        /**
         * Sends a value and completes a future with the answer.
         *
         * @param sender where the request goes
         * @param data the value to send
         * @param responseCallback completed with the answer
         */
        public void send(N sender, I data, CompletableFuture<O> responseCallback) {
            delegate.send(sender, data, responseCallback);
        }

        @Override
        LegacyRequestPacket<I, O> getDelegate() {
            return delegate;
        }

        @Override
        void receiveRequest(PacketHeaderInfo info, NetworkPacketContext context, I in) {}

        @Override
        void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, O in) {}
    }

    /**
     * A packet that exchanges a value per round trip until one side reports that it is done.
     *
     * @param <N> the direction type
     * @param <I> the type sent by the initiating side
     * @param <O> the type sent back per chunk
     * @deprecated Use {@link de.mrjulsen.mcdragonlib.net.stream.StreamPacket} instead, which keeps
     *             several chunks in flight rather than one per round trip.
     */
    @Deprecated
    public static class Stream<N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData> extends NetworkPacketType<N, I, O> {

        protected static final Map<Long, Object> callbacks = new ConcurrentHashMap<>();
        protected static final Map<Long, StreamProvider<?, ?>> inputCache = new ConcurrentHashMap<>();
        protected static final Map<Long, StreamReceiver<?, ?>> outputCache = new ConcurrentHashMap<>();

        private final LegacyStreamPacket<I, O> delegate;

        public Stream(ResourceLocation channelId, String name, NetworkDirection direction, Supplier<StreamReceiver<I, O>> receiverFactory, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory) {
            super(channelId, PacketType.STREAM, direction, name, inputFactory, outputFactory);
            this.delegate = new LegacyStreamPacket<>(settings(), inputFactory, outputFactory, receiverFactory);
        }

        /**
         * Returns counters about streams that are still open.
         *
         * @return the current statistics
         */
        public static DLStatistics debug_getStats() {
            DLStatistics.Group group = new DLStatistics.Group("callbacks", "Callbacks");
            return new DLStatistics("Network Stream Packets", List.of(
                new DLStatistics.Stat(group, "Result Callbacks", callbacks.size()),
                new DLStatistics.Stat(group, "Input Data Providers", inputCache.size()),
                new DLStatistics.Stat(group, "Output Data Providers", outputCache.size())
            ));
        }

        /**
         * Starts a stream.
         *
         * @param sender where the stream goes
         * @param handler produces the next value from the last one received
         * @param finishCallback called once with the final status
         */
        public void send(N sender, StreamProvider<I, O> handler, Consumer<DLStatus> finishCallback) {
            delegate.send(sender, handler, finishCallback);
        }

        @Override
        LegacyStreamPacket<I, O> getDelegate() {
            return delegate;
        }

        @Override
        void receiveRequest(PacketHeaderInfo info, NetworkPacketContext context, I in) {}

        @Override
        void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, O in) {}
    }
}
