package de.mrjulsen.mcdragonlib.net.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.net.DLChannel;
import de.mrjulsen.mcdragonlib.net.DLNetwork;
import de.mrjulsen.mcdragonlib.net.ExecutionMode;
import de.mrjulsen.mcdragonlib.net.PacketTarget;
import de.mrjulsen.mcdragonlib.net.codec.DLCodecs;
import de.mrjulsen.mcdragonlib.net.codec.DLStreamCodec;
import de.mrjulsen.mcdragonlib.net.packet.SendPacket;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Compares the registered channels of both sides when a player joins.
 *
 * <p>A packet is routed by name, so a side knowing packets the other does not is harmless on its
 * own. What is not harmless is a channel only one side has, or the same channel declared with
 * different protocol versions: in both cases messages disappear with no obvious cause. Those are
 * logged as errors on both sides, differing packet sets only as a note.
 */
public final class NetworkHandshake {

    /**
     * What one side knows about one channel.
     *
     * @param channel the channel id
     * @param protocolVersion the version string the channel was created with
     * @param digest a checksum over the registered packet names
     * @param packetCount how many packets are registered
     */
    public record ChannelInfo(ResourceLocation channel, String protocolVersion, long digest, int packetCount) {

        public static final DLStreamCodec<ChannelInfo> CODEC = DLStreamCodec.composite(
            DLCodecs.RESOURCE_LOCATION, ChannelInfo::channel,
            DLCodecs.STRING, ChannelInfo::protocolVersion,
            DLCodecs.LONG, ChannelInfo::digest,
            DLCodecs.VAR_INT, ChannelInfo::packetCount,
            ChannelInfo::new
        );
    }

    private static final DLStreamCodec<List<ChannelInfo>> LIST_CODEC = ChannelInfo.CODEC.list(4096);
    private static final DLStreamCodec<List<String>> PROBLEM_CODEC = DLCodecs.STRING.list(4096);

    private static final DLChannel CHANNEL = DLChannel.create(DLUtils.resourceLocation(DragonLib.MODID, "handshake"), "1");

    private static final SendPacket<List<ChannelInfo>> ANNOUNCE = CHANNEL.send("announce", LIST_CODEC)
        .toClient()
        .on(ExecutionMode.NETTY)
        .handler((channels, context) -> verify(channels, context.reply()))
        .register();

    private static final SendPacket<List<String>> REPORT = CHANNEL.send("report", PROBLEM_CODEC)
        .toServer()
        .on(ExecutionMode.NETTY)
        .handler((problems, context) -> {
            String name = context.getPlayer() == null ? "a client" : context.getPlayer().getGameProfile().getName();
            for (String problem : problems) {
                DLNetwork.LOGGER.error("Network mismatch reported by {}: {}", name, problem);
            }
        })
        .register();

    private NetworkHandshake() {}

    /**
     * Makes sure the handshake channel exists before any player can join.
     */
    public static void init() {}

    /**
     * Tells a joining player which channels this side has registered.
     *
     * @param player the player that joined
     */
    public static void announce(ServerPlayer player) {
        try {
            ANNOUNCE.send(PacketTarget.player(player), snapshot());
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not announce the networking state to {}.", player.getGameProfile().getName(), e);
        }
    }

    private static List<ChannelInfo> snapshot() {
        List<ChannelInfo> channels = new ArrayList<>();
        for (DLChannel channel : DLChannel.getAll()) {
            channels.add(new ChannelInfo(channel.getId(), channel.getProtocolVersion(), channel.getRegistryDigest(), channel.getPacketCount()));
        }
        return channels;
    }

    private static void verify(List<ChannelInfo> remote, PacketTarget back) {
        Map<ResourceLocation, ChannelInfo> local = snapshot().stream().collect(Collectors.toMap(ChannelInfo::channel, info -> info));
        List<String> problems = new ArrayList<>();
        List<String> notes = new ArrayList<>();

        for (ChannelInfo info : remote) {
            ChannelInfo mine = local.remove(info.channel());
            if (mine == null) {
                problems.add("Channel '" + info.channel() + "' exists on the server but not on the client, so nothing sent on it arrives.");
            } else if (!mine.protocolVersion().equals(info.protocolVersion())) {
                problems.add("Channel '" + info.channel() + "' is version '" + info.protocolVersion() + "' on the server and '" + mine.protocolVersion() + "' on the client.");
            } else if (mine.digest() != info.digest()) {
                notes.add("Channel '" + info.channel() + "' registers " + info.packetCount() + " packets on the server and " + mine.packetCount() + " on the client. Packets both sides know still work; one that only exists here is reported by name when it arrives.");
            }
        }
        for (ChannelInfo missing : local.values()) {
            problems.add("Channel '" + missing.channel() + "' exists on the client but not on the server, so nothing sent on it arrives.");
        }

        for (String note : notes) {
            DLNetwork.LOGGER.info("Network note: {}", note);
        }

        if (problems.isEmpty()) {
            DLNetwork.LOGGER.info("Networking matches the server across {} channels{}.", remote.size(), notes.isEmpty() ? "" : " (" + notes.size() + " with differing packet sets)");
            return;
        }
        for (String problem : problems) {
            DLNetwork.LOGGER.error("Network mismatch: {}", problem);
        }
        DLNetwork.LOGGER.error("Client and server need the same mods for these channels to work.");
        try {
            REPORT.send(back, problems);
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not report the mismatch back to the server.", e);
        }
    }
}
