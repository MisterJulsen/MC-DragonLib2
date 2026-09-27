package de.mrjulsen.mcdragonlib.net;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.mrjulsen.mcdragonlib.mixin.ServerGamePacketListenerAccessor;
import de.mrjulsen.mcdragonlib.net.internal.ClientNetworkHooks;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Where a packet is sent.
 *
 * <p>A target resolves to one or more connections at the moment it is used, so a target created
 * ahead of time still works after a reconnect. Exchanges that expect a response need a target
 * that resolves to exactly one connection.
 */
public interface PacketTarget {

    /**
     * Returns the direction packets sent to this target travel in.
     *
     * @return the flow of this target
     */
    NetworkFlow getFlow();

    /**
     * Resolves the connections this target currently covers.
     *
     * @return the connections to send to, possibly empty
     */
    Collection<Connection> getConnections();

    /**
     * Resolves this target to the single connection an exchange needs.
     *
     * @return the only connection this target covers
     * @throws IllegalStateException if the target covers no or more than one connection
     */
    default Connection requireSingleConnection() {
        Collection<Connection> connections = getConnections();
        if (connections.size() != 1) {
            throw new IllegalStateException("This packet expects a response and therefore needs exactly one recipient, but the target resolved to " + connections.size() + ".");
        }
        return connections.iterator().next();
    }

    /**
     * Targets the server the client is currently connected to.
     *
     * @return a target for the current server connection
     */
    static PacketTarget server() {
        return new PacketTarget() {
            @Override
            public NetworkFlow getFlow() {
                return NetworkFlow.SERVERBOUND;
            }

            @Override
            public Collection<Connection> getConnections() {
                Connection connection = ClientNetworkHooks.getServerConnection();
                return connection == null ? List.of() : List.of(connection);
            }
        };
    }

    /**
     * Targets a single player.
     *
     * @param player the recipient
     * @return a target for that player
     */
    static PacketTarget player(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return new PacketTarget() {
            @Override
            public NetworkFlow getFlow() {
                return NetworkFlow.CLIENTBOUND;
            }

            @Override
            public Collection<Connection> getConnections() {
                Connection connection = connectionOf(player);
                return connection == null ? List.of() : List.of(connection);
            }
        };
    }

    /**
     * Returns the connection a player is attached to.
     *
     * @param player the player to look up
     * @return the player's connection, or {@code null} if there is none
     */
    static Connection connectionOf(ServerPlayer player) {
        if (player.connection == null) {
            return null;
        }
        return ((ServerGamePacketListenerAccessor) player.connection).dragonlib$getConnection();
    }

    /**
     * Targets a fixed group of players.
     *
     * @param players the recipients
     * @return a target for those players
     */
    static PacketTarget players(Collection<ServerPlayer> players) {
        Objects.requireNonNull(players, "players");
        return of(NetworkFlow.CLIENTBOUND, () -> players);
    }

    /**
     * Targets every player currently on the server.
     *
     * @param server the server whose players to reach
     * @return a target for all online players
     */
    static PacketTarget all(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        return of(NetworkFlow.CLIENTBOUND, () -> server.getPlayerList().getPlayers());
    }

    /**
     * Targets every player in one level.
     *
     * @param level the level whose players to reach
     * @return a target for the players of that level
     */
    static PacketTarget level(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        return of(NetworkFlow.CLIENTBOUND, level::players);
    }

    /**
     * Targets every player within a radius of a position.
     *
     * @param level the level to search in
     * @param center the position to measure from
     * @param radius the maximum distance in blocks
     * @return a target for the players in range
     */
    static PacketTarget near(ServerLevel level, Vec3 center, double radius) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(center, "center");
        double radiusSqr = radius * radius;
        return of(NetworkFlow.CLIENTBOUND, () -> level.players().stream()
            .filter(player -> player.position().distanceToSqr(center) <= radiusSqr)
            .collect(Collectors.toList()));
    }

    /**
     * Targets one connection directly, which is mainly useful for replying to a received packet.
     *
     * @param connection the connection to send over
     * @param flow the direction packets take on that connection
     * @return a target for that connection
     */
    static PacketTarget connection(Connection connection, NetworkFlow flow) {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(flow, "flow");
        return new PacketTarget() {
            @Override
            public NetworkFlow getFlow() {
                return flow;
            }

            @Override
            public Collection<Connection> getConnections() {
                return connection.isConnected() ? List.of(connection) : List.of();
            }
        };
    }

    private static PacketTarget of(NetworkFlow flow, java.util.function.Supplier<Collection<ServerPlayer>> players) {
        return new PacketTarget() {
            @Override
            public NetworkFlow getFlow() {
                return flow;
            }

            @Override
            public Collection<Connection> getConnections() {
                return players.get().stream()
                    .map(PacketTarget::connectionOf)
                    .filter(connection -> connection != null && connection.isConnected())
                    .collect(Collectors.toList());
            }
        };
    }
}
