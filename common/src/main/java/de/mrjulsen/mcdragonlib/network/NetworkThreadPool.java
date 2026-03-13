package de.mrjulsen.mcdragonlib.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class NetworkThreadPool {

    private static final int DEFAULT_THREAD_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
    private static final int QUEUE_LIMIT = 100;

    private static volatile ThreadPoolExecutor EXECUTOR;
    private static volatile ScheduledExecutorService TIMEOUT_SCHEDULER;

    private NetworkThreadPool() {}

    public static synchronized void init() {
        if (EXECUTOR != null && !EXECUTOR.isShutdown()) {
            return;
        }

        int threadCount = ModCommonConfig.NETWORK_THREAD_COUNT.get() <= 0 ? DEFAULT_THREAD_COUNT : ModCommonConfig.NETWORK_THREAD_COUNT.get();
        DLNetworkManager.LOGGER.info("Creating network thread pool with {} threads.", threadCount);

        EXECUTOR = new ThreadPoolExecutor(
                threadCount,
                threadCount,
                60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_LIMIT),
                new ThreadFactoryBuilder()
                        .setNameFormat(DragonLib.MOD_NAME + " Network Worker #%d")
                        .setDaemon(true)
                        .build(),
                (r, executor) -> DLNetworkManager.LOGGER.warn("Network task queue is full (limit: {}). Task discarded.", QUEUE_LIMIT)
        );

        TIMEOUT_SCHEDULER = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat(DragonLib.MOD_NAME + " Network Timeout Watchdog")
                        .setDaemon(true)
                        .build()
        );
    }

    public static synchronized void shutdown() {
        if (EXECUTOR == null) return;

        long time = System.currentTimeMillis();
        DLNetworkManager.LOGGER.info("Shutting down network threads. Please wait...");

        ThreadPoolExecutor ex = EXECUTOR;
        ScheduledExecutorService scheduler = TIMEOUT_SCHEDULER;
        EXECUTOR = null;
        TIMEOUT_SCHEDULER = null;

        shutdownExecutor(ex, "Worker pool");
        shutdownExecutor(scheduler, "Timeout scheduler");

        DLNetworkManager.LOGGER.info("Network threads shut down. Took {}ms.", System.currentTimeMillis() - time);
    }

    private static void shutdownExecutor(ExecutorService service, String name) {
        try {
            // shutdownNow() is intentionally used here instead of shutdown().
            //
            // The TIMEOUT_SCHEDULER may still have pending watchdog tasks sitting in its
            // delay queue (scheduled e.g. 30s into the future) even after all actual work
            // is done. Calling shutdown() would cause awaitTermination() to block until
            // every such delayed task has been executed — effectively waiting the full
            // timeout duration on disconnect.
            //
            // shutdownNow() interrupts the scheduler thread immediately, causing
            // DelayedWorkQueue.take() to unblock at once. Pending watchdog tasks are
            // discarded, which is safe: by shutdown time all worker futures are already
            // done, so any watchdog that fires would only hit the `taskFuture.isDone()`
            // early-exit branch anyway.
            service.shutdownNow();
            if (!service.awaitTermination(ModCommonConfig.NETWORK_THREAD_TIMEOUT.get(), TimeUnit.SECONDS)) {
                DLNetworkManager.LOGGER.warn("{} did not terminate in time after shutdownNow.", name);
            }
        } catch (InterruptedException e) {
            DLNetworkManager.LOGGER.error("Interrupted while waiting for {} shutdown.", name, e);
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static ThreadPoolExecutor executor() {
        ThreadPoolExecutor ex = EXECUTOR;
        if (ex == null || ex.isShutdown()) {
            throw new IllegalStateException("NetworkThreadPool not initialized or already shut down.");
        }
        return ex;
    }

    private static ScheduledExecutorService scheduler() {
        ScheduledExecutorService s = TIMEOUT_SCHEDULER;
        if (s == null || s.isShutdown()) {
            throw new IllegalStateException("NetworkThreadPool not initialized or already shut down.");
        }
        return s;
    }

    /**
     * Schedules a timeout watchdog for the given future.
     * If the future is not done after {@code timeoutSeconds}, it will be cancelled and
     * {@code onTimeout} is called with a descriptive {@link TimeoutException}.
     */
    private static void scheduleTimeout(Future<?> taskFuture, int timeoutSeconds, NetworkPacketType<?, ?, ?> type, Consumer<TimeoutException> onTimeout) {
        scheduler().schedule(() -> {
            if (taskFuture.isDone()) return;

            boolean cancelled = taskFuture.cancel(true);
            String msg = String.format("Network task timed out after %ds. [ChannelID: %s, Name: %s]", timeoutSeconds, type.getChannelId(), type.getName());

            if (cancelled) {
                DLNetworkManager.LOGGER.warn("Network task cancelled due to timeout after {}s. [ChannelID: {}, Name: {}]", timeoutSeconds, type.getChannelId(), type.getName());
            } else {
                DLNetworkManager.LOGGER.error(
                        "Network task timed out after {}s! " +
                                "The worker thread is likely stuck in an uninterruptible loop. " +
                                "Check your packet handler for infinite loops or blocking native calls. " +
                                "[ChannelID: {}, Name: {}]",
                        timeoutSeconds, type.getChannelId(), type.getName()
                );
            }

            onTimeout.accept(new TimeoutException(msg));
        }, timeoutSeconds, TimeUnit.SECONDS);
    }

    /** Safely applies an error factory, catching and logging any exception it throws. */
    private static <T> T safeErrorFactory(Function<Throwable, T> errorFactory, Throwable cause, NetworkPacketType<?, ?, ?> type) {
        try {
            return errorFactory.apply(cause);
        } catch (Throwable t) {
            DLNetworkManager.LOGGER.error("errorFactory threw an exception. [ChannelID: {}, Name: {}]", type.getChannelId(), type.getName(), t);
            return null;
        }
    }

    /** Safely calls a responder/consumer, catching and logging any exception it throws. */
    private static <T> void safeRespond(Consumer<T> responder, T value, NetworkPacketType<?, ?, ?> type) {
        try {
            responder.accept(value);
        } catch (Throwable t) {
            DLNetworkManager.LOGGER.error("Responder threw an exception. [ChannelID: {}, Name: {}]", type.getChannelId(), type.getName(), t);
        }
    }

    /**
     * Executes a task that produces a result and sends it back via {@code responder}.
     *
     * <ul>
     *   <li>The timeout watchdog runs on a dedicated scheduler thread – it does NOT
     *       consume a worker slot while waiting.</li>
     *   <li>On timeout, the task is interrupted if possible. A warning is logged when
     *       the interrupt succeeds; an error is logged when it does not (stuck thread).</li>
     *   <li>{@code errorFactory} and {@code responder} are always called exactly once,
     *       even in error cases.</li>
     * </ul>
     *
     * @param type         the packet type, used for logging context
     * @param supplier     the task to execute; its return value is passed to {@code responder}
     * @param responder    called with the task result (or the error-factory result on failure)
     * @param errorFactory maps a {@link Throwable} to a fallback result for {@code responder}
     */
    public static <T> void executeWithResponse(NetworkPacketType<?, ?, ?> type, Supplier<T> supplier, Consumer<T> responder, Function<Throwable, T> errorFactory) {
        final int timeoutSeconds = ModCommonConfig.NETWORK_THREAD_TIMEOUT.get();
        final ThreadPoolExecutor ex = executor();
        Future<T> taskFuture = ex.submit(supplier::get);

        scheduleTimeout(taskFuture, timeoutSeconds, type, timeout -> {
            T errorResult = safeErrorFactory(errorFactory, timeout, type);
            safeRespond(responder, errorResult, type);
        });

        ex.submit(() -> {
            try {
                T result = taskFuture.get();
                safeRespond(responder, result, type);
            } catch (CancellationException e) {
                // Already handled by the timeout watchdog – do nothing
            } catch (ExecutionException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                DLNetworkManager.LOGGER.error("Network task threw an exception. [ChannelID: {}, Name: {}]", type.getChannelId(), type.getName(), cause);
                safeRespond(responder, safeErrorFactory(errorFactory, cause, type), type);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                safeRespond(responder, safeErrorFactory(errorFactory, e, type), type);
            }
        });
    }

    /**
     * Executes a fire-and-forget task with timeout supervision.
     *
     * <ul>
     *   <li>On timeout, the task is interrupted if possible and {@code onError} is invoked.</li>
     *   <li>On any other failure, the exception is logged and {@code onError} is invoked.</li>
     *   <li>{@code onError} is always called at most once.</li>
     * </ul>
     *
     * @param type    the packet type, used for logging context
     * @param task    the task to execute
     * @param onError called with the causing {@link Throwable} on failure
     */
    public static void execute(NetworkPacketType<?, ?, ?> type, Runnable task, Consumer<Throwable> onError) {
        final int timeoutSeconds = ModCommonConfig.NETWORK_THREAD_TIMEOUT.get();
        final ThreadPoolExecutor ex = executor();
        Future<?> taskFuture = ex.submit(task);

        scheduleTimeout(taskFuture, timeoutSeconds, type, timeout ->
                safeOnError(onError, timeout, type)
        );

        ex.submit(() -> {
            try {
                taskFuture.get();
            } catch (CancellationException e) {
                // Already handled by the timeout watchdog – do nothing
            } catch (ExecutionException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                DLNetworkManager.LOGGER.error("Network task threw an exception. [ChannelID: {}, Name: {}]", type.getChannelId(), type.getName(), cause);
                safeOnError(onError, cause, type);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                safeOnError(onError, e, type);
            }
        });
    }

    private static void safeOnError(Consumer<Throwable> onError, Throwable cause, NetworkPacketType<?, ?, ?> type) {
        try {
            onError.accept(cause);
        } catch (Throwable t) {
            DLNetworkManager.LOGGER.error("onError callback threw an exception. [ChannelID: {}, Name: {}]", type.getChannelId(), type.getName(), t);
        }
    }
}