package de.mrjulsen.mcdragonlib.net;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.zip.CRC32;

import de.mrjulsen.mcdragonlib.net.codec.DLStreamCodec;
import de.mrjulsen.mcdragonlib.net.packet.RequestPacketBuilder;
import de.mrjulsen.mcdragonlib.net.packet.SendPacketBuilder;
import de.mrjulsen.mcdragonlib.net.stream.StreamPacketBuilder;
import de.mrjulsen.mcdragonlib.net.transport.FrameHeader;
import de.mrjulsen.mcdragonlib.net.transport.OutboundMessage;
import de.mrjulsen.mcdragonlib.net.transport.TransportPolicy;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * A namespace for packets and the state they need per connection.
 *
 * <p>One channel maps to one Minecraft custom payload channel. A packet is identified on the wire
 * by the name it was registered under, so adding or removing packets never changes what the
 * others are called and a packet may be registered at any time, including after the channel has
 * already carried traffic.
 */
public final class DLChannel {

    private static final Map<ResourceLocation, DLChannel> CHANNELS = new ConcurrentHashMap<>();

    private final ResourceLocation id;
    private final String protocolVersion;
    private final Map<String, PacketDefinition> definitions = new ConcurrentSkipListMap<>();
    private final Map<Connection, ChannelSession> sessions = new ConcurrentHashMap<>();
    private final TransportPolicy transportPolicy;

    private DLChannel(ResourceLocation id, String protocolVersion) {
        this.id = id;
        this.protocolVersion = protocolVersion;
        this.transportPolicy = new TransportPolicy() {
            @Override
            public int getMaxMessageBytes(String packetName) {
                PacketDefinition definition = definitions.get(packetName);
                return definition == null ? -1 : definition.getMaxMessageBytes();
            }

            @Override
            public int getConnectionBudgetBytes() {
                return NetworkSettings.getReassemblyBudgetBytes();
            }

            @Override
            public long getReassemblyTimeoutMillis() {
                return NetworkSettings.getReassemblyTimeoutMillis();
            }
        };
    }

    /**
     * Creates a channel or returns the existing one for the same id.
     *
     * <p>Reusing an id is allowed and intended: several parts of a mod can add packets to the
     * same channel without having to share the instance. They have to agree on the protocol
     * version, because only one of them can be announced to the other side.
     *
     * @param id the custom payload channel to use
     * @param protocolVersion a version string checked against the other side on join
     * @return the channel for that id
     * @throws IllegalStateException if the channel exists with a different protocol version
     */
    public static DLChannel create(ResourceLocation id, String protocolVersion) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(protocolVersion, "protocolVersion");
        DLChannel channel = CHANNELS.computeIfAbsent(id, key -> {
            DLChannel created = new DLChannel(key, protocolVersion);
            DLNetwork.registerChannel(key, protocolVersion);
            return created;
        });
        if (!channel.protocolVersion.equals(protocolVersion)) {
            throw new IllegalStateException("Channel '" + id + "' already exists with protocol version '" + channel.protocolVersion + "', so it cannot also be created with '" + protocolVersion + "'. Everything sharing a channel must agree on its version.");
        }
        return channel;
    }

    /**
     * Looks up an already created channel.
     *
     * @param id the channel id
     * @return the channel, or an empty optional if nothing was registered for that id
     */
    public static Optional<DLChannel> find(ResourceLocation id) {
        return Optional.ofNullable(CHANNELS.get(id));
    }

    /**
     * Returns every channel that has been created.
     *
     * @return all known channels
     */
    public static Collection<DLChannel> getAll() {
        return CHANNELS.values();
    }

    /**
     * Returns the custom payload channel this instance uses.
     *
     * @return the channel id
     */
    public ResourceLocation getId() {
        return id;
    }

    /**
     * Returns the version string compared against the other side.
     *
     * @return the protocol version
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Starts registering a packet that is sent without expecting an answer.
     *
     * @param name the packet id, unique within this channel
     * @param codec the codec for the payload
     * @param <T> the payload type
     * @return a builder for the packet
     */
    public <T> SendPacketBuilder<T> send(String name, DLStreamCodec<T> codec) {
        return new SendPacketBuilder<>(this, name, codec);
    }

    /**
     * Starts registering a packet that answers every message with a result.
     *
     * @param name the packet id, unique within this channel
     * @param requestCodec the codec for the payload sent to the handler
     * @param responseCodec the codec for the payload sent back
     * @param <Q> the request type
     * @param <R> the response type
     * @return a builder for the packet
     */
    public <Q, R> RequestPacketBuilder<Q, R> request(String name, DLStreamCodec<Q> requestCodec, DLStreamCodec<R> responseCodec) {
        return new RequestPacketBuilder<>(this, name, requestCodec, responseCodec);
    }

    /**
     * Starts registering a packet that transfers an open-ended amount of data.
     *
     * @param name the packet id, unique within this channel
     * @param requestCodec the codec for the value describing what is being asked for
     * @param <Q> the request type
     * @return a builder for the packet
     */
    public <Q> StreamPacketBuilder<Q> stream(String name, DLStreamCodec<Q> requestCodec) {
        return new StreamPacketBuilder<>(this, name, requestCodec);
    }

    /**
     * Adds a packet to this channel.
     *
     * @param definition the packet to add
     * @param <T> the packet type
     * @return the packet that was passed in
     * @throws IllegalArgumentException if the name is empty, too long or already taken
     */
    public synchronized <T extends PacketDefinition> T register(T definition) {
        String name = definition.getName();
        if (name.isEmpty() || name.length() > FrameHeader.MAX_PACKET_NAME_LENGTH) {
            throw new IllegalArgumentException("Packet name '" + name + "' must be between 1 and " + FrameHeader.MAX_PACKET_NAME_LENGTH + " characters.");
        }
        if (definitions.containsKey(name)) {
            throw new IllegalArgumentException("A packet named '" + name + "' is already registered on channel '" + id + "'.");
        }
        definitions.put(name, definition);
        DLNetwork.LOGGER.debug("Registered {} on channel '{}'.", definition, id);
        return definition;
    }

    /**
     * Returns a checksum over the registered packet names, used to detect a mismatched peer.
     *
     * @return the registry digest
     */
    public synchronized long getRegistryDigest() {
        CRC32 digest = new CRC32();
        for (String name : definitions.keySet()) {
            digest.update(name.getBytes(StandardCharsets.UTF_8));
            digest.update(0);
        }
        return digest.getValue();
    }

    /**
     * Returns how many packets are registered on this channel.
     *
     * @return the packet count
     */
    public int getPacketCount() {
        return definitions.size();
    }

    /**
     * Looks up a packet by its name.
     *
     * @param name the packet id used at registration
     * @return the packet, or an empty optional if the name is unknown
     */
    public Optional<PacketDefinition> findByName(String name) {
        return Optional.ofNullable(definitions.get(name));
    }

    /**
     * Returns the limits this channel applies to incoming messages.
     *
     * @return the transport policy
     */
    public TransportPolicy getTransportPolicy() {
        return transportPolicy;
    }

    /**
     * Returns the per-connection state of this channel, creating it on first use.
     *
     * @param connection the connection to look up
     * @return the session for that connection
     */
    public ChannelSession getSession(Connection connection) {
        return sessions.computeIfAbsent(connection, key -> new ChannelSession(this, key));
    }

    /**
     * Returns the per-connection state only if it already exists.
     *
     * @param connection the connection to look up
     * @return the session, or {@code null} if this channel has not used that connection yet
     */
    public ChannelSession peekSession(Connection connection) {
        return sessions.get(connection);
    }

    /**
     * Splits an encoded payload into frames and sends it over every given connection.
     *
     * @param definition the packet the payload belongs to
     * @param flow the direction the message travels in
     * @param connections the recipients
     * @param payload the complete encoded payload, which stays owned by the caller
     */
    public void transmit(PacketDefinition definition, NetworkFlow flow, Collection<Connection> connections, FriendlyByteBuf payload) {
        for (Connection connection : connections) {
            if (!connection.isConnected()) {
                continue;
            }
            ChannelSession session = getSession(connection);
            FriendlyByteBuf copy = new FriendlyByteBuf(payload.retainedDuplicate());
            OutboundMessage message = OutboundMessage.of(flow, session.nextTransferId(), definition.getName(), copy);
            session.enqueue(message, flow);
        }
    }

    /**
     * Handles one received frame, dispatching the message once it is complete.
     *
     * @param frame the received frame
     * @param context what is known about the received message
     */
    public void receive(FriendlyByteBuf frame, PacketContext context) {
        ChannelSession session = getSession(context.getConnection());
        session.getAssembler().accept(frame, (packetName, payload) -> {
            PacketDefinition definition = definitions.get(packetName);
            if (definition == null) {
                DLNetwork.LOGGER.warn("Received a message for unknown packet '{}' on channel '{}'.", packetName, id);
                return;
            }
            definition.receive(context, payload);
        });
    }

    /**
     * Releases the state this channel holds for a connection.
     *
     * @param connection the connection that went away
     * @param reason why the state is being released
     */
    public void closeSession(Connection connection, NetworkError.Reason reason) {
        ChannelSession session = sessions.remove(connection);
        if (session != null) {
            session.close(reason);
        }
    }

    /**
     * Returns the connections this channel currently holds state for.
     *
     * @return the live connections
     */
    public Collection<Connection> getSessionConnections() {
        return sessions.keySet();
    }

    /**
     * Advances timeouts and pushes out queued frames for every connection of this channel.
     *
     * @param now the current wall-clock time in milliseconds
     */
    public void tick(long now) {
        for (Map.Entry<Connection, ChannelSession> entry : sessions.entrySet()) {
            if (!entry.getKey().isConnected()) {
                closeSession(entry.getKey(), NetworkError.Reason.DISCONNECTED);
                continue;
            }
            entry.getValue().tick(now);
        }
    }
}
