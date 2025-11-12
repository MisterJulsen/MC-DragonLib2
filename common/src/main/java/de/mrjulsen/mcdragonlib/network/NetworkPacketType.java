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

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData.Empty;
import de.mrjulsen.mcdragonlib.network.NetworkProcessor.StreamReceiver;
import de.mrjulsen.mcdragonlib.network.NetworkProcessor.StreamProvider;
import de.mrjulsen.mcdragonlib.network.packet.NetworkPacker;
import de.mrjulsen.mcdragonlib.network.packet.PacketHeaderInfo;
import de.mrjulsen.mcdragonlib.network.packet.PacketType;
import de.mrjulsen.mcdragonlib.util.DLStatistics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public abstract class NetworkPacketType<N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData> {
    private final ResourceLocation channelId;
    private final String name;
    private final PacketType type;
    private final NetworkSide direction;
    private final Function<DLStatus, I> sendFactory;
    private final Function<DLStatus, O> responseFactory;

    public NetworkPacketType(ResourceLocation channelId, PacketType type, NetworkDirection direction, String name, Function<DLStatus, I> sendFactory, Function<DLStatus, O> responseFactory) {
        this.channelId = channelId;
        this.type = type;
        this.name = name;
        this.direction = direction.getDirection();
        this.sendFactory = sendFactory;
        this.responseFactory = responseFactory;
    }
    
    abstract void receiveRequest(PacketHeaderInfo info, NetworkPacketContext context, I in);
    abstract void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, O in);

    public final ResourceLocation getChannelId() {
        return channelId;
    }

    public final String getName() {
        return name;
    }

    public final PacketType getType() {
        return type;
    }

    public final NetworkSide getDirection() {
        return direction;
    }

    public final PacketHeaderInfo getInfo(CommunicationType communication, long requestId) {
        return new PacketHeaderInfo(getType(), communication, requestId, getName());
    }
    
    protected void receive(PacketHeaderInfo info, NetworkPacketContext context, CompoundTag nbt, CommunicationType communication) {
        switch (communication) {
            case REQUEST -> receiveInternal(info, context, nbt);
            case RESPONSE -> receiveResponseInternal(info, context, nbt);
            default -> {}
        }
    }

    protected I createEmptyInputData(DLStatus status) {
        return sendFactory.apply(status);
    }

    protected O createEmptyOutputData(DLStatus status) {
        return responseFactory.apply(status);
    }

    protected void sendInternal(long requestId, N sender, @Nullable CompoundTag nbt) {
        PacketHeaderInfo info = getInfo(CommunicationType.REQUEST, requestId);
        List<Packet<?>> mcPackets = NetworkPacker.pack(getChannelId(), info, sender.getDirection(), nbt);
        for (Packet<?> packet : mcPackets) {
            sender.send(packet);
        }
    }

    protected void receiveInternal(PacketHeaderInfo info, NetworkPacketContext context, CompoundTag nbt) {
        I instance = sendFactory.apply(DLStatus.EMPTY);
        instance.deserializeNbt(nbt);
        receiveRequest(info, context, instance);
    }
    

    protected void respondInternal(PacketHeaderInfo header, NetworkPacketContext context, @Nullable CompoundTag nbt) {
        NetworkDirection sender = getDirection() == NetworkSide.C2S ? NetworkDirection.toServer() : NetworkDirection.toPlayer((ServerPlayer)context.getPlayer());
        PacketHeaderInfo info = getInfo(CommunicationType.RESPONSE, header.requestId());
        List<Packet<?>> mcPackets = NetworkPacker.pack(getChannelId(), info, sender.getDirection(), nbt);
        for (Packet<?> packet : mcPackets) {
            sender.send(packet);
        }
    }

    protected void receiveResponseInternal(PacketHeaderInfo info, NetworkPacketContext context, CompoundTag nbt) {
        O instance = responseFactory.apply(DLStatus.EMPTY);
        instance.deserializeNbt(nbt);
        receiveResponse(info, context, instance);
    }




    public static class Send<N extends NetworkDirection, I extends NetworkPacketData> extends NetworkPacketType<N, I, NetworkPacketData.Empty> {

        protected final NetworkProcessor.Send<I> handler;

        public Send(ResourceLocation channelId, String name, NetworkDirection direction, NetworkProcessor.Send<I> handler, Function<DLStatus, I> factory) {
            super(channelId, PacketType.SEND, direction, name, factory, NetworkPacketData.Empty::new);
            this.handler = handler;
        }

        @Override
        void receiveRequest(PacketHeaderInfo info, NetworkPacketContext context, I in) {
            handler.execute(in, context);
        }

        @Override
        void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, Empty in) {}
        
        public void send(N sender, I data) {
            long requestId = System.nanoTime();
            sendInternal(requestId, sender, data.serializeNbt());
        }
    }



    
    public static class Receive<N extends NetworkDirection, O extends NetworkPacketData> extends NetworkPacketType<N, NetworkPacketData.Empty, O> {

        protected static final Map<Long, CompletableFuture<?>> callbacks = new ConcurrentHashMap<>();
        private final NetworkProcessor.Receive<O> handler;

        public Receive(ResourceLocation channelId, String name, NetworkDirection direction, NetworkProcessor.Receive<O> handler, Function<DLStatus, O> factory) {
            super(channelId, PacketType.RECEIVE, direction, name, NetworkPacketData.Empty::new, factory);
            this.handler = handler;
        }

        public static DLStatistics debug_getStats() {
            DLStatistics.Group group = new DLStatistics.Group("callbacks", "Callbacks");
            DLStatistics stats = new DLStatistics("Network Receive Packets", List.of(
                new DLStatistics.Stat(group, "Result Callbacks", callbacks.size())
            ));
            return stats;
        }

        @Override
        void receiveRequest(PacketHeaderInfo info, NetworkPacketContext context, NetworkPacketData.Empty in) {
            try {
                O data = handler.execute(context);
                respondInternal(info, context, data.serializeNbt());
            } catch (Exception e) {
                respondInternal(info, context, createEmptyOutputData(DLStatus.error(e)).serializeNbt());
            }
        }

        @Override
        void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, O in) {
            if (callbacks.containsKey(info.requestId())) {
                ((CompletableFuture<O>)callbacks.remove(info.requestId())).complete(in);
            }
        }
        
        
        public void send(N sender, Consumer<O> responseCallback, Runnable errorCallback) {
            CompletableFuture<O> future = new CompletableFuture<>();
            future.thenAccept(responseCallback).exceptionally(ex -> {
                DLNetworkManager.LOGGER.error("Error while waiting for response.", ex);
                errorCallback.run();
                return null;
            }).orTimeout(60, TimeUnit.SECONDS);
            send(sender, future);
        }
        
        public void send(N sender, CompletableFuture<O> responseCallback) {
            long requestId = System.nanoTime();
            callbacks.put(requestId, responseCallback);
            sendInternal(requestId, sender, new CompoundTag());
        }
    } 



    
    public static class SendAndReceive<N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData> extends NetworkPacketType<N, I, O> {

        protected static final Map<Long, CompletableFuture<?>> callbacks = new ConcurrentHashMap<>();
        private final NetworkProcessor.SendAndReceive<I, O> handler;

        public SendAndReceive(ResourceLocation channelId, String name, NetworkDirection direction, NetworkProcessor.SendAndReceive<I, O> handler, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory) {
            super(channelId, PacketType.SEND_AND_RECEIVE, direction, name, inputFactory, outputFactory);
            this.handler = handler;
        }
        
        public static DLStatistics debug_getStats() {
            DLStatistics.Group group = new DLStatistics.Group("callbacks", "Callbacks");
            DLStatistics stats = new DLStatistics("Network Send and Receive Packets", List.of(
                new DLStatistics.Stat(group, "Result Callbacks", callbacks.size())
            ));
            return stats;
        }

        @Override
        void receiveRequest(PacketHeaderInfo info, NetworkPacketContext context, I in) {
            try {
                O data = handler.execute((I)in, context);
                respondInternal(info, context, data.serializeNbt());
            } catch (Exception e) {
                respondInternal(info, context, createEmptyOutputData(DLStatus.error(e)).serializeNbt());
            }
        }

        @Override
        void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, O in) {
            if (callbacks.containsKey(info.requestId())) {
                ((CompletableFuture<O>)callbacks.remove(info.requestId())).complete(in);
            }
        }
        
        
        public void send(N sender, I data, Consumer<O> responseCallback, Runnable errorCallback) {
            CompletableFuture<O> future = new CompletableFuture<>();
            future.thenAccept(responseCallback).exceptionally(ex -> {
                DLNetworkManager.LOGGER.error("Error while waiting for response.", ex);
                errorCallback.run();
                return null;
            }).orTimeout(60, TimeUnit.SECONDS);
            send(sender, data, future);
        }
        
        public void send(N sender, I data, CompletableFuture<O> responseCallback) {
            long requestId = System.nanoTime();
            callbacks.put(requestId, responseCallback);
            sendInternal(requestId, sender, data.serializeNbt());
        }
    }



    
    public static class Stream<N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData> extends NetworkPacketType<N, I, O> {

        private record RequestData<N extends NetworkDirection, O extends NetworkPacketData>(N sender, long requestId, Consumer<DLStatus> callback) {}

        protected static final Map<Long, RequestData<?, ?>> callbacks = new ConcurrentHashMap<>();
        protected static final Map<Long, StreamProvider<?, ?>> inputCache = new ConcurrentHashMap<>();
        protected static final Map<Long, StreamReceiver<?, ?>> outputCache = new ConcurrentHashMap<>();

        private final Supplier<StreamReceiver<I, O>> receiverFactory;


        public Stream(ResourceLocation channelId, String name, NetworkDirection direction, Supplier<StreamReceiver<I, O>> receiverFactory, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory) {
            super(channelId, PacketType.STREAM, direction, name, inputFactory, outputFactory);
            this.receiverFactory = receiverFactory;
        }
        
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
        }

        @Override
        void receiveResponse(PacketHeaderInfo info, NetworkPacketContext context, O in) {
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
        }
        
        
        public void send(N sender, StreamProvider<I, O> handler, Consumer<DLStatus> finishCallback) {
            long requestId = System.nanoTime();
            callbacks.put(requestId, new RequestData<>(sender, requestId, finishCallback));
            inputCache.put(requestId, handler);
            sendInternal(requestId, sender, handler.execute(true, Optional.empty(), Optional.empty()).serializeNbt());
        }
    }
}
