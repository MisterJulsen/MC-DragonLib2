package de.mrjulsen.mcdragonlib.net.internal;

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
