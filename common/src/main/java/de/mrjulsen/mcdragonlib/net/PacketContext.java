package de.mrjulsen.mcdragonlib.net;

import org.jetbrains.annotations.Nullable;

import dev.architectury.utils.Env;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * What a handler knows about the packet it is handling.
 *
 * <p>Handlers run wherever their {@link ExecutionMode} puts them, so the context is the only
 * reliable way back to the game thread and to the peer the packet came from.
 */
public interface PacketContext {

    /**
     * Returns the side this handler is running on.
     *
     * @return the local environment
     */
    Env getEnvironment();

    /**
     * Returns the direction the received message travelled in.
     *
     * @return the flow the message arrived with
     */
    NetworkFlow getFlow();

    /**
     * Returns the connection the message arrived on.
     *
     * @return the originating connection
     */
    Connection getConnection();

    /**
     * Returns the player on the other end.
     *
     * @return the sending player on the server, the local player on the client, or {@code null}
     *         if there is none
     */
    @Nullable
    Player getPlayer();

    /**
     * Returns the player that sent the message when running on the server.
     *
     * @return the sender, or {@code null} when running on the client
     */
    @Nullable
    default ServerPlayer getSender() {
        Player player = getPlayer();
        return player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    /**
     * Runs a task on the game thread.
     *
     * @param task the work to perform
     */
    void queue(Runnable task);

    /**
     * Returns whether the connection this message came from is still usable.
     *
     * @return {@code true} while the peer is still connected
     */
    default boolean isConnected() {
        return getConnection().isConnected();
    }

    /**
     * Returns a target pointing back at the sender of this message.
     *
     * @return a target for the originating connection
     */
    default PacketTarget reply() {
        return PacketTarget.connection(getConnection(), getFlow().opposite());
    }
}
