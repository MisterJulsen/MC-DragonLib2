/*
 * DragonLib 3 - 1.21.1
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

import de.mrjulsen.mcdragonlib.DragonLib;
import dev.architectury.utils.Env;
import net.minecraft.server.MinecraftServer;

/**
 * Reaches the game thread of whichever side is asking.
 *
 * <p>Used where no packet context is available, for example when a request gives up waiting and
 * its callback still has to run somewhere safe.
 */
public final class MainThread {

    private MainThread() {}

    /**
     * Runs a task on the game thread of the given side, or immediately if that side has no game
     * thread right now.
     *
     * @param environment the side whose game thread to use
     * @param task the work to perform
     */
    public static void run(Env environment, Runnable task) {
        if (environment == Env.CLIENT) {
            runOnClient(task);
            return;
        }
        MinecraftServer server = DragonLib.getCurrentServer().orElse(null);
        if (server == null || server.isSameThread()) {
            task.run();
        } else {
            server.execute(task);
        }
    }

    private static void runOnClient(Runnable task) {
        try {
            ClientNetworkHooks.getTaskQueue().execute(task);
        } catch (Throwable t) {
            task.run();
        }
    }
}
