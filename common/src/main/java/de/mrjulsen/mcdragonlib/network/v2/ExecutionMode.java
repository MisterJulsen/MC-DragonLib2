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

package de.mrjulsen.mcdragonlib.network.v2;

import java.util.concurrent.Executor;

import de.mrjulsen.mcdragonlib.network.v2.internal.MainThread;
import dev.architectury.utils.Env;
import net.minecraft.Util;

/**
 * Where a packet handler is run.
 *
 * <p>Handlers arrive on a netty event loop that is shared between connections, so blocking there
 * stalls unrelated players. At the same time most handlers only touch the world, which is only
 * safe on the game thread. {@link #MAIN} is therefore the default and anything else is a
 * deliberate choice made by whoever writes the handler.
 */
public enum ExecutionMode {

    /** Runs on the game thread, where world, entity and inventory access is safe. */
    MAIN,
    /** Runs on Minecraft's shared background pool. Suitable for computation, not for blocking calls. */
    ASYNC,
    /** Runs on Minecraft's I/O pool. Suitable for disk access, compression and other blocking work. */
    BLOCKING,
    /** Runs directly on the netty event loop. Only safe for handlers that do nothing but move bytes. */
    NETTY;

    /**
     * Runs the given task according to this mode.
     *
     * @param context the context of the packet being handled, used to reach the game thread
     * @param task the work to perform
     */
    public void run(PacketContext context, Runnable task) {
        switch (this) {
            case MAIN -> context.queue(task);
            case ASYNC, BLOCKING -> executor().execute(task);
            case NETTY -> task.run();
        }
    }

    /**
     * Runs the given task according to this mode when no packet context is available, such as
     * when a request gives up waiting.
     *
     * @param environment the side whose game thread to use for {@link #MAIN}
     * @param task the work to perform
     */
    public void run(Env environment, Runnable task) {
        switch (this) {
            case MAIN -> MainThread.run(environment, task);
            case ASYNC, BLOCKING -> executor().execute(task);
            case NETTY -> task.run();
        }
    }

    /**
     * Returns the executor backing this mode, or {@code null} for modes that are not pool based.
     *
     * @return the backing executor or {@code null} for {@link #MAIN} and {@link #NETTY}
     */
    public Executor executor() {
        return switch (this) {
            case ASYNC -> Util.backgroundExecutor();
            case BLOCKING -> Util.ioPool();
            default -> null;
        };
    }
}
