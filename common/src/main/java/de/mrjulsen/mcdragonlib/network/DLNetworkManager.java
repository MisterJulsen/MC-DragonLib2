package de.mrjulsen.mcdragonlib.network;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.net.DLChannel;
import de.mrjulsen.mcdragonlib.net.DLNetwork;
import de.mrjulsen.mcdragonlib.net.NetworkFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

/**
 * Registry for packets of the deprecated networking API.
 *
 * <p>An instance is a thin view onto a {@link DLChannel} with the same id, so packets registered
 * here and packets registered through the current API share one channel, one wire format and one
 * set of limits. Two managers created for the same channel id now cooperate instead of silently
 * shadowing each other.
 *
 * @deprecated Use {@link DLChannel} instead.
 */
@Deprecated
public final class DLNetworkManager {

    /** Logger of the networking layer. */
    public static final Logger LOGGER = DLNetwork.LOGGER;

    private final ResourceLocation channelId;
    private final String protocolVersion;
    private final DLChannel channel;
    private final Map<String, NetworkPacketType<?, ?, ?>> packets = new ConcurrentHashMap<>();

    /**
     * Creates a view onto the channel with the given id, creating that channel if needed.
     *
     * @param channelId the channel to use
     * @param protocolVersion the version string announced to the other side
     */
    public DLNetworkManager(ResourceLocation channelId, String protocolVersion) {
        this.channelId = channelId;
        this.protocolVersion = protocolVersion;
        this.channel = DLChannel.create(channelId, protocolVersion);
    }

    /**
     * Returns the channel backing this manager.
     *
     * @return the underlying channel
     */
    public DLChannel getChannel() {
        return channel;
    }

    /**
     * Returns the version string announced to the other side.
     *
     * @return the protocol version
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Registers a packet that is sent without expecting an answer.
     *
     * @param name the packet id, unique within the channel
     * @param direction which side starts the exchange
     * @param processor handles a received payload
     * @param factory creates a blank payload instance
     * @param <N> the direction type
     * @param <I> the payload type
     * @param <S> the processor type
     * @return the registered packet
     */
    public <N extends NetworkDirection, I extends NetworkPacketData, S extends NetworkProcessor.Send<I>> NetworkPacketType.Send<N, I> registerSendOnlyPacket(String name, N direction, S processor, Function<DLStatus, I> factory) {
        return register(new NetworkPacketType.Send<>(channelId, name, direction, processor, factory));
    }

    /**
     * Registers a packet that asks the other side for a value without sending one.
     *
     * @param name the packet id, unique within the channel
     * @param direction which side starts the exchange
     * @param processor produces the answer
     * @param factory creates a blank answer instance
     * @param <N> the direction type
     * @param <O> the response type
     * @param <S> the processor type
     * @return the registered packet
     */
    public <N extends NetworkDirection, O extends NetworkPacketData, S extends NetworkProcessor.Receive<O>> NetworkPacketType.Receive<N, O> registerReceiveOnlyPacket(String name, N direction, S processor, Function<DLStatus, O> factory) {
        return register(new NetworkPacketType.Receive<>(channelId, name, direction, processor, factory));
    }

    /**
     * Registers a packet that sends a value and receives one back.
     *
     * @param name the packet id, unique within the channel
     * @param direction which side starts the exchange
     * @param processor turns a request into a response
     * @param inputFactory creates a blank request instance
     * @param outputFactory creates a blank response instance
     * @param <N> the direction type
     * @param <I> the request type
     * @param <O> the response type
     * @param <S> the processor type
     * @return the registered packet
     */
    public <N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData, S extends NetworkProcessor.SendAndReceive<I, O>> NetworkPacketType.SendAndReceive<N, I, O> registerSendAndReceivePacket(String name, N direction, S processor, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory) {
        return register(new NetworkPacketType.SendAndReceive<>(channelId, name, direction, processor, inputFactory, outputFactory));
    }

    /**
     * Registers a packet that exchanges a value per round trip.
     *
     * @param name the packet id, unique within the channel
     * @param direction which side starts the exchange
     * @param factory creates the receiver for a new stream
     * @param inputFactory creates a blank instance of the initiating payload
     * @param outputFactory creates a blank instance of the answering payload
     * @param <N> the direction type
     * @param <I> the type sent by the initiating side
     * @param <O> the type sent back per chunk
     * @return the registered packet
     */
    public <N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData> NetworkPacketType.Stream<N, I, O> registerStreamPacket(String name, N direction, Supplier<NetworkProcessor.StreamReceiver<I, O>> factory, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory) {
        return register(new NetworkPacketType.Stream<>(channelId, name, direction, factory, inputFactory, outputFactory));
    }

    private <T extends NetworkPacketType<?, ?, ?>> T register(T packet) {
        if (isPacketRegistered(packet.getName())) {
            throw new IllegalArgumentException("A packet with id '" + packet.getName() + "' has already been registered for '" + channelId + "'.");
        }
        channel.register(packet.getDelegate());
        packets.put(packet.getName(), packet);
        LOGGER.info("Registering {} network packet of type {} with id '{}' in '{}'.", packet.getDirection(), packet.getType(), packet.getName(), channelId);
        return packet;
    }

    /**
     * Returns whether a packet with the given name is registered here.
     *
     * @param name the packet id
     * @return {@code true} if it is registered
     */
    public boolean isPacketRegistered(String name) {
        return packets.containsKey(name);
    }

    /**
     * Looks up a registered packet.
     *
     * @param name the packet id
     * @param side the side the packet is expected to start from
     * @return the packet, or an empty optional if nothing matches
     */
    public Optional<NetworkPacketType<?, ?, ?>> getRegisteredPacket(String name, NetworkSide side) {
        NetworkPacketType<?, ?, ?> type = packets.get(name);
        if (type == null || type.getDirection() != side) {
            return Optional.empty();
        }
        return Optional.of(type);
    }

    /**
     * Registers the underlying custom payload channel.
     *
     * @param channelId the channel to register
     * @param protocolVersion the version string announced to the other side
     * @deprecated Creating a {@link DLChannel} already does this.
     */
    @Deprecated
    public static void registerChannel(ResourceLocation channelId, String protocolVersion) {
        DLChannel.create(channelId, protocolVersion);
    }

    /**
     * Wraps a buffer into the Minecraft packet that carries it.
     *
     * @param channelId the channel the data belongs to
     * @param side the direction the data travels in
     * @param buffer the data to wrap
     * @return a packet ready to be sent
     * @deprecated Use {@link DLNetwork#toPacket(ResourceLocation, NetworkFlow, FriendlyByteBuf)}.
     */
    @Deprecated
    public static Packet<?> toPacket(ResourceLocation channelId, NetworkSide side, FriendlyByteBuf buffer) {
        return DLNetwork.toPacket(channelId, side == NetworkSide.C2S ? NetworkFlow.SERVERBOUND : NetworkFlow.CLIENTBOUND, buffer);
    }

    /**
     * No longer used. Received data is dispatched by the current transport.
     *
     * @param channelId the channel the data arrived on
     * @param buf the received data
     * @param side the direction the data travelled in
     * @param context the context of the received message
     * @deprecated Received data reaches packets through {@link DLNetwork} now.
     */
    @Deprecated
    public static void receiveData(ResourceLocation channelId, FriendlyByteBuf buf, NetworkSide side, NetworkPacketContext context) {
        LOGGER.warn("Ignoring a call to the removed legacy receive path for channel '{}'.", channelId);
    }
}
