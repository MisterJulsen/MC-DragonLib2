package de.mrjulsen.mcdragonlib.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class NetworkThreadPool {

    private static final int THREAD_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
    private static final int QUEUE_LIMIT = 100;

    private static volatile ThreadPoolExecutor EXECUTOR;

    private NetworkThreadPool() {}

    public static synchronized void init() {
        if (EXECUTOR != null && !EXECUTOR.isShutdown()) {
            return;
        }

        int threadCount = ModCommonConfig.NETWORK_THREAD_COUNT.get() <= 0
                ? THREAD_COUNT
                : ModCommonConfig.NETWORK_THREAD_COUNT.get();

        DLNetworkManager.LOGGER.info("Creating network thread pool with " + threadCount + " threads.");

        EXECUTOR = new ThreadPoolExecutor(
                threadCount,
                threadCount,
                60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_LIMIT),
                new ThreadFactoryBuilder()
                        .setNameFormat(DragonLib.MOD_NAME + " Network Worker Thread #%d")
                        .setDaemon(true)
                        .build(),
                (r, executor) -> DLNetworkManager.LOGGER.warn(
                        "Network task queue is full (limit: " + QUEUE_LIMIT + "). Task discarded.")
        );
    }

    static ThreadPoolExecutor executor() {
        ThreadPoolExecutor ex = EXECUTOR;
        if (ex == null || ex.isShutdown()) {
            throw new IllegalStateException("NetworkThreadPool not initialized or already shut down.");
        }
        return ex;
    }

    /**
     * Führt einen Task mit Rückgabewert aus.
     *
     * - Bricht den Thread per Interrupt ab, wenn möglich (z.B. bei blockierenden I/O-Operationen).
     * - Gibt eine Warnung aus, wenn der Thread nicht abbrechbar ist (z.B. Endlosschleife ohne sleep/wait).
     * - Ruft errorFactory und responder niemals doppelt auf.
     */
    public static <T> void executeWithResponse(NetworkPacketType<?, ?, ?> type, Supplier<T> supplier, Consumer<T> responder, Function<Throwable, T> errorFactory) {

        final int timeoutSeconds = ModCommonConfig.NETWORK_THREAD_TIMEOUT.get();
        final ThreadPoolExecutor ex = executor();

        Future<T> taskFuture = ex.submit(supplier::get);
        ex.submit(() -> {
            T result;
            try {
                result = taskFuture.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                boolean cancelled = taskFuture.cancel(true);
                TimeoutException timeout = new TimeoutException("Network task timed out after " + timeoutSeconds + "s. [ChannelID: " + type.getChannelId() + ", Name: " + type.getName() + "]");

                if (cancelled) {
                    DLNetworkManager.LOGGER.warn("Network task was cancelled due to timeout after " + timeoutSeconds + "s. [ChannelID: " + type.getChannelId() + ", Name: " + type.getName() + "]");
                } else {
                    DLNetworkManager.LOGGER.error("Network task timed out  after " + timeoutSeconds + "s but COULD NOT be cancelled! The worker thread is likely stuck in an uninterruptible loop. Check your packet handler for infinite loops or blocking native calls. [ChannelID: " + type.getChannelId() + ", Name: " + type.getName() + "]");
                }

                result = errorFactory.apply(timeout);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                result = errorFactory.apply(e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                DLNetworkManager.LOGGER.error("Network task threw an exception. [ChannelID: " + type.getChannelId() + ", Name: " + type.getName() + "]", cause);
                result = errorFactory.apply(cause);
            }

            try {
                responder.accept(result);
            } catch (Throwable t) {
                DLNetworkManager.LOGGER.error("Error while sending network response.", t);
            }
        });
    }

    public static void execute(NetworkPacketType<?, ?, ?> type, Runnable task, Consumer<Throwable> onError) {

        final int timeoutSeconds = ModCommonConfig.NETWORK_THREAD_TIMEOUT.get();
        final ThreadPoolExecutor ex = executor();

        Future<?> taskFuture = ex.submit(task);
        ex.submit(() -> {
            try {
                taskFuture.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                boolean cancelled = taskFuture.cancel(true);
                TimeoutException timeout = new TimeoutException("Network task timed out after " + timeoutSeconds + "s. [ChannelID: " + type.getChannelId() + ", Name: " + type.getName() + "]");

                if (cancelled) {
                    DLNetworkManager.LOGGER.warn("Network task was cancelled due to timeout after " + timeoutSeconds + "s. [ChannelID: " + type.getChannelId() + ", Name: " + type.getName() + "]");
                } else {
                    DLNetworkManager.LOGGER.error("Network task timed out  after " + timeoutSeconds + "s but COULD NOT be cancelled! The worker thread is likely stuck in an uninterruptible loop. Check your packet handler for infinite loops or blocking native calls. [ChannelID: " + type.getChannelId() + ", Name: " + type.getName() + "]");
                }

                try {
                    onError.accept(timeout);
                } catch (Throwable t) {
                    DLNetworkManager.LOGGER.error("Error in onError callback.", t);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                onError.accept(e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                DLNetworkManager.LOGGER.error("Network task threw an exception. [ChannelID: " + type.getChannelId() + ", Name: " + type.getName() + "]", cause);
                try {
                    onError.accept(cause);
                } catch (Throwable t) {
                    DLNetworkManager.LOGGER.error("Error in onError callback.", t);
                }
            }
        });
    }

    public static synchronized void shutdown() {
        if (EXECUTOR == null) return;

        long time = System.currentTimeMillis();
        DLNetworkManager.LOGGER.info("Shutting down network threads. Please wait...");
        ThreadPoolExecutor ex = EXECUTOR;
        EXECUTOR = null;

        try {
            ex.shutdown();
            if (!ex.awaitTermination(ModCommonConfig.NETWORK_THREAD_TIMEOUT.get(), TimeUnit.SECONDS)) {
                DLNetworkManager.LOGGER.warn("Network thread pool did not terminate in time, forcing shutdown.");
                ex.shutdownNow();
            }
        } catch (InterruptedException e) {
            DLNetworkManager.LOGGER.error("Interrupted while waiting for network thread shutdown.", e);
            ex.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            DLNetworkManager.LOGGER.info("Network threads shut down. Took " + (System.currentTimeMillis() - time) + "ms.");
        }
    }
}