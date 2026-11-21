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

package de.mrjulsen.mcdragonlib.network;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.network.v2.DLNetwork;
import de.mrjulsen.mcdragonlib.network.v2.ExecutionMode;
import net.minecraft.Util;

/**
 * Runs packet handlers of the deprecated networking API off the game thread.
 *
 * <p>This no longer owns a thread pool. Work goes to the executor Minecraft already maintains for
 * blocking tasks, which removes the lifecycle this class used to manage and the queue it used to
 * overflow. There is no task timeout either: a handler that legitimately takes a while is no
 * longer interrupted halfway through.
 *
 * @deprecated Choose an {@link ExecutionMode} on the packet instead.
 */
@Deprecated
public final class NetworkThreadPool {

    private NetworkThreadPool() {}

    /**
     * Does nothing. Kept so existing startup code keeps compiling.
     */
    public static void init() {}

    /**
     * Does nothing. Kept so existing shutdown code keeps compiling.
     */
    public static void shutdown() {}

    /**
     * Runs a task and passes its result to a responder.
     *
     * @param type the packet the task belongs to, used for logging
     * @param supplier the work to perform
     * @param responder receives the result, or the fallback if the task failed
     * @param errorFactory turns a failure into a fallback result
     * @param <T> the result type
     */
    public static <T> void executeWithResponse(NetworkPacketType<?, ?, ?> type, Supplier<T> supplier, Consumer<T> responder, Function<Throwable, T> errorFactory) {
        Util.ioPool().execute(() -> {
            T result;
            try {
                result = supplier.get();
            } catch (Throwable t) {
                DLNetwork.LOGGER.error("Network task failed. [ChannelID: {}, Name: {}]", type.getChannelId(), type.getName(), t);
                try {
                    result = errorFactory.apply(t);
                } catch (Throwable inner) {
                    DLNetwork.LOGGER.error("Error factory failed. [ChannelID: {}, Name: {}]", type.getChannelId(), type.getName(), inner);
                    return;
                }
            }
            try {
                responder.accept(result);
            } catch (Throwable t) {
                DLNetwork.LOGGER.error("Responder failed. [ChannelID: {}, Name: {}]", type.getChannelId(), type.getName(), t);
            }
        });
    }

    /**
     * Runs a task without a result.
     *
     * @param type the packet the task belongs to, used for logging
     * @param task the work to perform
     * @param onError called if the task failed
     */
    public static void execute(NetworkPacketType<?, ?, ?> type, Runnable task, Consumer<Throwable> onError) {
        Util.ioPool().execute(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                DLNetwork.LOGGER.error("Network task failed. [ChannelID: {}, Name: {}]", type.getChannelId(), type.getName(), t);
                try {
                    onError.accept(t);
                } catch (Throwable inner) {
                    DLNetwork.LOGGER.error("Error callback failed. [ChannelID: {}, Name: {}]", type.getChannelId(), type.getName(), inner);
                }
            }
        });
    }
}
