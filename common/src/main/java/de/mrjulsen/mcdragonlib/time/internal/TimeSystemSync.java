package de.mrjulsen.mcdragonlib.time.internal;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.network.v2.DLChannel;
import de.mrjulsen.mcdragonlib.network.v2.ExecutionMode;
import de.mrjulsen.mcdragonlib.network.v2.PacketTarget;
import de.mrjulsen.mcdragonlib.network.v2.codec.DLCodecs;
import de.mrjulsen.mcdragonlib.network.v2.codec.DLStreamCodec;
import de.mrjulsen.mcdragonlib.network.v2.packet.SendPacket;
import de.mrjulsen.mcdragonlib.time.TimeSection;
import de.mrjulsen.mcdragonlib.time.TimeSystem;
import de.mrjulsen.mcdragonlib.time.TimeSystems;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Keeps the time system of connected clients equal to the server's.
 * <p>The server sends its system when a player joins, and again whenever it changes, e.g. after a
 * datapack reload or when a time mod changes its settings. Clients use it until they disconnect.</p>
 */
public final class TimeSystemSync {

    private static final int CHECK_INTERVAL_TICKS = 20;

    private static final DLStreamCodec<TimeSection> section_CODEC = DLStreamCodec.composite(
        DLCodecs.VAR_LONG, TimeSection::startTick,
        DLCodecs.VAR_LONG, TimeSection::endTick,
        DLCodecs.DOUBLE, TimeSection::ticksPerSecond,
        TimeSection::new
    );

    private static final DLStreamCodec<TimeSystem> SYSTEM_CODEC = DLStreamCodec.composite(
        DLCodecs.VAR_LONG, TimeSystem::ticksPerDay,
        DLCodecs.DOUBLE, TimeSystem::clockOffset,
        section_CODEC.list(4096), TimeSystem::sections,
        (ticksPerDay, clockOffset, sections) -> {
            TimeSystem.Builder builder = TimeSystem.builder(ticksPerDay).clockOffset(clockOffset);
            sections.forEach(builder::section);
            return builder.build();
        }
    );

    private static final DLChannel CHANNEL = DLChannel.create(DLUtils.resourceLocation(DragonLib.MODID, "time"), "1");

    private static final SendPacket<TimeSystem> SYNC = CHANNEL.send("system", SYSTEM_CODEC)
        .toClient()
        .on(ExecutionMode.MAIN)
        .handler((system, context) -> TimeSystemState.setSynced(system))
        .register();

    private static TimeSystem lastSent;
    private static int ticksUntilCheck;

    private TimeSystemSync() {}

    public static void init() {
        TimeSystemDatapack.register();

        PlayerEvent.PLAYER_JOIN.register(TimeSystemSync::sendTo);
        TickEvent.Server.SERVER_POST.register(TimeSystemSync::checkForChanges);
        LifecycleEvent.SERVER_STOPPED.register(server -> {
            lastSent = null;
            TimeSystemState.setDatapack(null);
        });

        if (Platform.getEnv() == EnvType.CLIENT) {
            ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> TimeSystemState.setSynced(null));
        }
    }

    private static void sendTo(ServerPlayer player) {
        try {
            SYNC.send(PacketTarget.player(player), TimeSystems.resolveLocal());
        } catch (Exception e) {
            DragonLib.LOGGER.error("Could not send the time system to {}.", player.getGameProfile().getName(), e);
        }
    }

    private static void checkForChanges(MinecraftServer server) {
        if (--ticksUntilCheck > 0) {
            return;
        }
        ticksUntilCheck = CHECK_INTERVAL_TICKS;
        TimeSystem current = TimeSystems.resolveLocal();
        if (current.equals(lastSent)) {
            return;
        }
        lastSent = current;
        try {
            SYNC.send(PacketTarget.all(server), current);
        } catch (Exception e) {
            DragonLib.LOGGER.error("Could not send the changed time system to the players.", e);
        }
    }
}
