package de.mrjulsen.mcdragonlib.net;

import java.util.concurrent.Executor;

import de.mrjulsen.mcdragonlib.net.internal.MainThread;
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
