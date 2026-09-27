package de.mrjulsen.mcdragonlib.net.internal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.player.Player;

/**
 * Client-only entry points used by the networking layer. Loaded lazily so that nothing here is
 * touched on a dedicated server.
 */
public final class ClientNetworkHooks {

    private ClientNetworkHooks() {}

    /**
     * Returns the connection to the server the client is currently playing on.
     *
     * @return the active connection, or {@code null} while not in a world
     */
    public static Connection getServerConnection() {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        return listener == null ? null : listener.getConnection();
    }

    /**
     * Returns the player this client controls.
     *
     * @return the local player, or {@code null} while not in a world
     */
    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    /**
     * Returns the task queue of the client, used to reach the render thread.
     *
     * @return the client instance, which is also its own task queue
     */
    public static Minecraft getTaskQueue() {
        return Minecraft.getInstance();
    }
}
