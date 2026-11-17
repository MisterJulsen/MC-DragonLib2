/*
 * DragonLib 3 - 1.20.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.network.v2.internal;

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
