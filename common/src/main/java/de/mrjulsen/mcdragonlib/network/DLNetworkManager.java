package de.mrjulsen.mcdragonlib.network;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.architectury.impl.NetworkAggregator;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.packet.NetworkPacker;
import de.mrjulsen.mcdragonlib.network.packet.SegmentedPacketHeaderInfo;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

/**
 * Central manager for network channels and packet registration used by DragonLib.
 *
 * <p>This class holds a per-channel registry of {@link NetworkPacketType} instances,
 * provides convenience registration methods for common packet patterns (send-only,
 * receive-only, send-and-receive, streaming), and dispatches incoming raw buffers
 * to the appropriate packet decoder/handler via {@link NetworkPacker}.
 *
 * <p>Usage:
 * <ul>
 *   <li>Create one manager per channel (ResourceLocation) with a protocol version.</li>
 *   <li>Register packets with {@code registerSendOnlyPacket}, {@code registerReceiveOnlyPacket},
 *       {@code registerSendAndReceivePacket} or {@code registerStreamPacket}.</li>
 * </ul>
 *
 * <p>Note: channel registration and packet &lt;-&gt; platform {@code Packet<?>} translation are
 * provided by platform-specific implementations.
 */
public final class DLNetworkManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(DragonLib.MOD_NAME + " Networking System");
    private static final ConcurrentHashMap<ResourceLocation, DLNetworkManager> managers = new ConcurrentHashMap<>();

    private final ResourceLocation channelId;
    private final String protocolVersion;
    private final ConcurrentHashMap<String, NetworkPacketType<?, ?, ?>> packets = new ConcurrentHashMap<>();

    /**
     * Creates a network manager instance for the given channel identifier and protocol version.
     *
     * @param channelId unique channel {@link ResourceLocation} used to separate message namespaces
     * @param protocolVersion string protocol version used for compatibility checks
     */
    public DLNetworkManager(ResourceLocation channelId, String protocolVersion) {
        this.channelId = channelId;
        this.protocolVersion = protocolVersion;
    }
   

    /**
     * Register a send-only packet type.
     *
     * <p>This registers a packet type that can only be sent (no response expected). The
     * {@code processor} handles sending logic and {@code factory} creates the packet payload instance.
     *
     * @param name packet id (unique per channel)
     * @param direction the {@link NetworkDirection} describing which side can send/receive
     * @param processor network processor handling send operations
     * @param factory function that produces an input data instance given a {@link de.mrjulsen.mcdragonlib.data.DLStatus}
     * @param <N> NetworkDirection type
     * @param <I> input data type (implements {@link NetworkPacketData})
     * @param <S> processor type extending {@link NetworkProcessor.Send}
     * @return registered {@link NetworkPacketType.Send} instance
     * @throws IllegalArgumentException if a packet with the same name is already registered
     */
    public <N extends NetworkDirection, I extends NetworkPacketData, S extends NetworkProcessor.Send<I>> NetworkPacketType.Send<N, I> registerSendOnlyPacket(String name, N direction, S processor, Function<DLStatus, I> factory) {
        return register(new NetworkPacketType.Send<>(channelId, name, direction, processor, factory));
    }

    /**
     * Register a receive-only packet type.
     *
     * <p>The {@code processor} handles incoming data for this packet and {@code factory}
     * is used to create the expected data container.
     *
     * @param name packet id (unique per channel)
     * @param direction direction this packet is associated with
     * @param processor processor handling receive events
     * @param factory factory for creating receive-side data instances
     * @param <N> NetworkDirection type
     * @param <O> output data type (implements {@link NetworkPacketData})
     * @param <S> processor type extending {@link NetworkProcessor.Receive}
     * @return registered {@link NetworkPacketType.Receive} instance
     * @throws IllegalArgumentException if a packet with the same name is already registered
     */
    public <N extends NetworkDirection, O extends NetworkPacketData, S extends NetworkProcessor.Receive<O>> NetworkPacketType.Receive<N, O> registerReceiveOnlyPacket(String name, N direction, S processor, Function<DLStatus, O> factory) {
        return register(new NetworkPacketType.Receive<>(channelId, name, direction, processor, factory));
    }

    /**
     * Register a packet type that supports both sending and receiving (request/response).
     *
     * @param name packet id (unique per channel)
     * @param direction direction this packet is associated with
     * @param processor processor implementing {@link NetworkProcessor.SendAndReceive}
     * @param inputFactory factory for creating input instances
     * @param outputFactory factory for creating output instances
     * @param <N> direction type
     * @param <I> input data type
     * @param <O> output data type
     * @param <S> processor type
     * @return registered {@link NetworkPacketType.SendAndReceive} instance
     * @throws IllegalArgumentException on duplicate registration
     */
    public <N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData, S extends NetworkProcessor.SendAndReceive<I, O>> NetworkPacketType.SendAndReceive<N, I, O> registerSendAndReceivePacket(String name, N direction, S processor, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory) {
        return register(new NetworkPacketType.SendAndReceive<>(channelId, name, direction, processor, inputFactory, outputFactory));
    }
    
    /**
     * Register a streaming packet type. Streaming packets allow multiple segments and
     * use a {@link NetworkProcessor.StreamReceiver} to manage incremental data.
     *
     * @param name packet id (unique per channel)
     * @param direction packet direction
     * @param factory supplier that creates a new StreamReceiver for each stream
     * @param inputFactory factory for input data instances per segment
     * @param outputFactory factory for output data
     * @param <N> direction type
     * @param <I> input data type
     * @param <O> output data type
     * @return registered {@link NetworkPacketType.Stream} instance
     */
    public <N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData> NetworkPacketType.Stream<N, I, O> registerStreamPacket(String name, N direction, Supplier<NetworkProcessor.StreamReceiver<I, O>> factory, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory) {
        return register(new NetworkPacketType.Stream<>(channelId, name, direction, factory, inputFactory, outputFactory));
    }
    
    /**
     * Internal helper that performs registration and ensures the channel is registered
     * once per {@link ResourceLocation}. Throws on duplicate packet names.
     *
     * @param packet packet type to register
     * @param <T> concrete {@link NetworkPacketType} subtype
     * @return the same packet instance passed in
     * @throws IllegalArgumentException if a packet with the same name already exists for this channel
     */
    private <T extends NetworkPacketType<?, ?, ?>> T register(T packet) {
        if (isPacketRegistered(packet.getName())) {
            throw new IllegalArgumentException("A packet with id '" + packet.getName() + "' has already been registered for '" + channelId + "'.");
        }
        managers.computeIfAbsent(channelId, x -> {
            //registerChannel(channelId, protocolVersion);
            return this;
        });

        registerChannel(channelId, packet.getName(), packet.getDirection(), protocolVersion);
        packets.put(packet.getName(), packet);
        LOGGER.info("Registering {} network packet of type {} with id '{}' in '{}'.", packet.getDirection(), packet.getType(), packet.getName(), channelId);
        return packet;
    }

    /**
     * Returns whether a packet with the given name is already registered for this manager.
     *
     * @param name packet id
     * @return true if registered, false otherwise
     */
    public boolean isPacketRegistered(String name) {
        return packets.containsKey(name);
    }

    /**
     * Retrieves a registered packet by name if it exists and matches the requested side.
     *
     * @param name packet id
     //* @param side {@link NetworkSide} expected side for the packet
     * @return an {@link Optional} containing the {@link NetworkPacketType} if found and matching side
     */
    public Optional<NetworkPacketType<?, ?, ?>> getRegisteredPacket(String name/*, NetworkSide side*/) {
        if (!isPacketRegistered(name)) {
            return Optional.empty();
        }
        NetworkPacketType<?, ?, ?> type = packets.get(name);
        if (type == null/* || type.getDirection() != side*/) {
            return Optional.empty();
        }
        return Optional.of(type);
    }
    
    /**
     * Platform entry point to register the underlying network channel.
     *
     * <p>Platform-specific implementations must bind a listener such that inbound buffers
     * are forwarded to {@link #receiveData(ResourceLocation, FriendlyByteBuf, NetworkSide, NetworkPacketContext)}.
     *
     * @param channelId channel identifier (ResourceLocation)
     * @param protocolVersion textual protocol version used for compatibility checks
     * @throws AssertionError when called on non-platform implementation stub
     * @see dev.architectury.injectables.annotations.ExpectPlatform
     */
    @ExpectPlatform
    public static void registerChannel(ResourceLocation channelId, String name, NetworkSide side, String protocolVersion) {
        throw new AssertionError();
    }

    /**
     * Platform-specific conversion from a raw {@link FriendlyByteBuf} to a network {@code Packet<?>}
     * that the Minecraft networking stack can send.
     *
     * <p>Platform implementations must serialize the provided buffer into the correct packet
     * instance for the given {@code channelId} and {@code side}.
     *
     * @param channelId channel identifier
     * @param side side information
     * @param buffer buffer containing already-packed packet payload
     * @return a platform {@link Packet} ready to be sent
     * @throws AssertionError when called on non-platform implementation stub
     */
    @ExpectPlatform
    public static Packet<?> toPacket(ResourceLocation channelId, String name, NetworkSide side, FriendlyByteBuf buffer) {
        throw new AssertionError();
    }    
        
    /**
     * Entry point called by the platform networking layer when raw data arrives for a channel.
     *
     * <p>This method:
     * <ol>
     *   <li>reads a {@link SegmentedPacketHeaderInfo} header from the buffer</li>
     *   <li>determines the communication direction and resolves the registered packet</li>
     *   <li>delegates deserialization to {@link NetworkPacker} and invokes the packet handler</li>
     * </ol>
     *
     * <p>If the packet is unknown the buffer will be cleaned up and a warning logged.
     *
     * @param channelId channel identifier the data was received on
     * @param buf raw {@link FriendlyByteBuf} containing header + payload
     * @param side side the buffer was received from (client/server)
     * @param context contextual information provided by the platform when dispatching the buffer
     */
    public static void receiveData(ResourceLocation channelId, FriendlyByteBuf buf, NetworkSide side, NetworkPacketContext context) {
        if (!managers.containsKey(channelId)) return;

        SegmentedPacketHeaderInfo header = SegmentedPacketHeaderInfo.readBufferHeader(buf);
        CommunicationType communication = header.type().communication();
        NetworkSide fSide;
        if (communication == CommunicationType.RESPONSE) {
            fSide = side;
        } else {
            fSide = side == NetworkSide.S2C ? NetworkSide.C2S : NetworkSide.S2C;
        }

        managers.get(channelId).getRegisteredPacket(header.type().name()/*, fSide*/).ifPresentOrElse(x -> {
            NetworkPacker.unpack(header, fSide, buf, context, (rawData) -> {
                CompoundTag nbt = rawData.readNbt();
                x.receive(header.type(), context, nbt, communication);
                NetworkPacker.cleanUp(header, fSide);
            });
        }, () -> {
            LOGGER.warn("There is no {} packet registered with ID '{}' in '{}'.", fSide, header.type().name(), channelId);
            NetworkPacker.cleanUp(header, fSide);
        });
    }

    public record BufCustomPacketPayload(Type<BufCustomPacketPayload> _type, byte[] payload) implements CustomPacketPayload {
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return this._type();
        }

        public static StreamCodec<ByteBuf, BufCustomPacketPayload> streamCodec(Type<BufCustomPacketPayload> type) {
            return ByteBufCodecs.BYTE_ARRAY.map(bytes -> new BufCustomPacketPayload(type, bytes), BufCustomPacketPayload::payload);
        }
    }

}
