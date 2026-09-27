package de.mrjulsen.mcdragonlib.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.net.transport.TransportException;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * The entry points the platform layers talk to.
 *
 * <p>Everything loader specific stops here: registering a custom payload channel and turning a
 * finished frame into a Minecraft packet. Everything above works the same on every loader.
 */
public final class DLNetwork {

    /** Logger shared by the whole networking layer. */
    public static final Logger LOGGER = LoggerFactory.getLogger(DragonLib.MOD_NAME + " Networking");

    private DLNetwork() {}

    /**
     * Registers the underlying custom payload channel with the loader.
     *
     * <p>The implementation has to forward every received buffer to
     * {@link #receive(ResourceLocation, FriendlyByteBuf, PacketContext)}.
     *
     * @param channelId the channel to register
     * @param protocolVersion the version string announced to the other side
     */
    @ExpectPlatform
    public static void registerChannel(ResourceLocation channelId, String protocolVersion) {
        throw new AssertionError();
    }

    /**
     * Wraps a finished frame into the Minecraft packet that carries it.
     *
     * @param channelId the channel the frame belongs to
     * @param flow the direction the frame travels in
     * @param frame the frame to wrap, ownership of which passes to the returned packet
     * @return a packet ready to be handed to a connection
     */
    @ExpectPlatform
    public static Packet<?> toPacket(ResourceLocation channelId, NetworkFlow flow, FriendlyByteBuf frame) {
        throw new AssertionError();
    }

    /**
     * Handles a frame that arrived on a channel.
     *
     * <p>Malformed input is dropped with a warning rather than propagated, because it comes from
     * a remote peer and must not be able to break the connection handler.
     *
     * @param channelId the channel the frame arrived on
     * @param frame the received frame
     * @param context what is known about the received message
     */
    public static void receive(ResourceLocation channelId, FriendlyByteBuf frame, PacketContext context) {
        DLChannel channel = DLChannel.find(channelId).orElse(null);
        if (channel == null) {
            LOGGER.warn("Received data on unregistered channel '{}'.", channelId);
            return;
        }
        try {
            channel.receive(frame, context);
        } catch (TransportException e) {
            LOGGER.warn("Dropping malformed data on channel '{}' from {}: {}", channelId, describe(context), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Could not handle data on channel '{}' from {}.", channelId, describe(context), e);
        }
    }

    /**
     * Releases everything every channel holds for a connection and fails its open exchanges.
     *
     * @param connection the connection that went away
     */
    public static void onDisconnect(Connection connection) {
        for (DLChannel channel : DLChannel.getAll()) {
            channel.closeSession(connection, NetworkError.Reason.DISCONNECTED);
        }
    }

    /**
     * Advances timeouts and pushes out queued frames. Called once per tick on each side.
     */
    public static void tick() {
        long now = System.currentTimeMillis();
        for (DLChannel channel : DLChannel.getAll()) {
            try {
                channel.tick(now);
            } catch (Exception e) {
                LOGGER.error("Could not tick channel '{}'.", channel.getId(), e);
            }
        }
    }

    /**
     * Fails every open exchange because the game is stopping. Queued outgoing data is discarded,
     * since nothing is left to receive it.
     */
    public static void shutdown() {
        for (DLChannel channel : DLChannel.getAll()) {
            for (Connection connection : java.util.List.copyOf(channel.getSessionConnections())) {
                channel.closeSession(connection, NetworkError.Reason.SHUTDOWN);
            }
        }
    }

    private static String describe(PacketContext context) {
        Player player = context.getPlayer();
        return player == null ? "the server" : player.getGameProfile().getName();
    }
}
