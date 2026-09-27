package de.mrjulsen.mcdragonlib.network.internal;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.net.DLChannel;
import de.mrjulsen.mcdragonlib.net.DLNetwork;
import de.mrjulsen.mcdragonlib.net.NetworkFlow;
import de.mrjulsen.mcdragonlib.net.PacketContext;
import de.mrjulsen.mcdragonlib.net.PacketDefinition;
import de.mrjulsen.mcdragonlib.net.PacketDirection;
import de.mrjulsen.mcdragonlib.net.codec.DLStreamCodec;
import de.mrjulsen.mcdragonlib.net.transport.OutboundMessage;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.network.NetworkSide;
import dev.architectury.utils.Env;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

/**
 * Glue between the deprecated networking API and the current one.
 *
 * <p>Everything a mod built against the old API relies on is expressed in terms of the new
 * transport here, so there is only one wire format and one set of limits to maintain.
 */
@Deprecated
public final class LegacyBridge {

    private static final AtomicInteger FALLBACK_IDS = new AtomicInteger();
    private static final AtomicInteger FALLBACK_TRANSFERS = new AtomicInteger();

    private LegacyBridge() {}

    /**
     * Wraps the NBT serialization of a legacy payload type as a codec.
     *
     * @param factory creates a blank instance before deserialization
     * @param <T> the payload type
     * @return a codec delegating to the payload's own NBT methods
     */
    public static <T extends NetworkPacketData> DLStreamCodec<T> dataCodec(Function<DLStatus, T> factory) {
        Objects.requireNonNull(factory, "factory");
        return DLStreamCodec.of(
            (buf, value) -> buf.writeNbt(value.serializeNbt()),
            buf -> {
                T value = Objects.requireNonNull(factory.apply(DLStatus.EMPTY), "packet data factory returned null");
                CompoundTag nbt = buf.readAnySizeNbt();
                value.deserializeNbt(nbt == null ? new CompoundTag() : nbt);
                return value;
            }
        );
    }

    /**
     * Presents a current packet context through the deprecated context interface.
     *
     * @param context the context of the received message
     * @return the same context seen through the old interface
     */
    public static NetworkPacketContext adapt(PacketContext context) {
        return new NetworkPacketContext() {
            @Override
            public net.minecraft.world.entity.player.Player getPlayer() {
                return context.getPlayer();
            }

            @Override
            public void queue(Runnable runnable) {
                context.queue(runnable);
            }

            @Override
            public <O> O queueResult(Supplier<O> supplier) {
                CompletableFuture<O> future = new CompletableFuture<>();
                context.queue(() -> {
                    try {
                        future.complete(supplier.get());
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
                return future.join();
            }

            @Override
            public Env getEnvironment() {
                return context.getEnvironment();
            }
        };
    }

    /**
     * Translates the side a legacy packet was registered for.
     *
     * @param side the legacy side
     * @return the matching direction
     */
    public static PacketDirection direction(NetworkSide side) {
        return side == NetworkSide.C2S ? PacketDirection.TO_SERVER : PacketDirection.TO_CLIENT;
    }

    /**
     * Returns the direction a legacy sender transmits in.
     *
     * @param sender the legacy sender
     * @return the flow its packets travel in
     */
    public static NetworkFlow flowOf(NetworkDirection sender) {
        return sender.getDirection() == NetworkSide.C2S ? NetworkFlow.SERVERBOUND : NetworkFlow.CLIENTBOUND;
    }

    /**
     * Reserves a correlation id for an exchange whose connection is unknown.
     *
     * <p>These ids are negative so they can never be confused with the per-connection ids handed
     * out by a session.
     *
     * @return a negative correlation id
     */
    public static int nextFallbackId() {
        return -FALLBACK_IDS.incrementAndGet();
    }

    /**
     * Sends an encoded payload through a legacy sender.
     *
     * <p>When the sender knows its connection the message takes the normal path, including
     * pacing and per-connection bookkeeping. A hand written sender only offers
     * {@link NetworkDirection#send(Packet)}, so the frames are pushed through that instead.
     *
     * @param channel the channel the packet belongs to
     * @param definition the packet being sent
     * @param sender the legacy sender
     * @param writer writes the payload
     */
    public static void send(DLChannel channel, PacketDefinition definition, NetworkDirection sender, Consumer<FriendlyByteBuf> writer) {
        NetworkFlow flow = flowOf(sender);
        FriendlyByteBuf payload = new FriendlyByteBuf(Unpooled.buffer());
        try {
            writer.accept(payload);
            if (payload.readableBytes() > definition.getMaxMessageBytes()) {
                throw new IllegalStateException("Payload of " + definition + " is " + payload.readableBytes() + " bytes, which exceeds its limit of " + definition.getMaxMessageBytes() + ".");
            }
            Connection connection = sender.getConnection();
            if (connection != null) {
                channel.transmit(definition, flow, List.of(connection), payload);
            } else {
                sendDirect(channel, definition, sender, flow, payload);
            }
        } finally {
            payload.release();
        }
    }

    private static void sendDirect(DLChannel channel, PacketDefinition definition, NetworkDirection sender, NetworkFlow flow, FriendlyByteBuf payload) {
        OutboundMessage message = OutboundMessage.of(flow, FALLBACK_TRANSFERS.incrementAndGet(), definition.getName(), new FriendlyByteBuf(payload.retainedDuplicate()));
        try {
            while (message.hasNextFrame()) {
                sender.send(DLNetwork.toPacket(channel.getId(), flow, message.nextFrame()));
            }
        } finally {
            message.release();
        }
    }
}
