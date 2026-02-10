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


    private static volatile ExecutorService EXECUTOR;
    private static final ScheduledExecutorService WATCHDOG =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, DragonLib.MOD_NAME + " Network Task Watchdog");
                t.setDaemon(true);
                return t;
            });


    private NetworkThreadPool() {
    }

    public static synchronized void init() {
        if (EXECUTOR != null && !EXECUTOR.isShutdown()) {
            return;
        }

        int threadCount = ModCommonConfig.NETWORK_THREAD_COUNT.get() <= 0 ? THREAD_COUNT : ModCommonConfig.NETWORK_THREAD_COUNT.get();
        DLNetworkManager.LOGGER.info("Creating network thread pool with " + threadCount + " Threads.");
        EXECUTOR = new ThreadPoolExecutor(
                threadCount,
                threadCount,
                60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_LIMIT),
                new ThreadFactoryBuilder()
                        .setNameFormat(DragonLib.MOD_NAME + " Network Worker Thread #%d")
                        .setDaemon(true)
                        .build(),
                new ThreadPoolExecutor.DiscardPolicy()
        );
    }

    static ExecutorService executor() {
        ExecutorService ex = EXECUTOR;
        if (ex == null || ex.isShutdown()) {
            throw new IllegalStateException("NetworkThreadPool not initialized or already shut down.");
        }
        return ex;
    }

    public static <T> void executeWithResponse(NetworkPacketType<?, ?, ?> type, Supplier<T> supplier, Consumer<T> responder, Function<Throwable, T> errorFactory) {
        final int timeout = ModCommonConfig.NETWORK_THREAD_TIMEOUT.get();
        CompletableFuture
                .supplyAsync(supplier, executor())
                .completeOnTimeout(errorFactory.apply(new TimeoutException("Network task timed out because it blocked the network thread for too long (" + timeout + " seconds). [ChannelID: " + type.getChannelId() + ", Name: " + type.getName() + "]")), timeout, TimeUnit.SECONDS)
                .exceptionally(e -> errorFactory.apply(unwrapCompletionException(e)))
                .thenAccept(result -> {
                    try {
                        responder.accept(result);
                    } catch (Throwable t) {
                        DLNetworkManager.LOGGER.error("Error while sending network response.", t);
                    }
                });
    }

    public static void execute(NetworkPacketType<?, ?, ?> type, Runnable task, Consumer<Throwable> onError) {
        final int timeout = ModCommonConfig.NETWORK_THREAD_TIMEOUT.get();
        Supplier<Void> error = () -> {
            onError.accept(new TimeoutException("Network task timed out because it blocked the network thread for too long (" + timeout + " seconds). [ChannelID: " + type.getChannelId() + ", Name: " + type.getName() + "]"));
            return null;
        };

        CompletableFuture
                .runAsync(task, executor())
                .completeOnTimeout(error.get(), timeout, TimeUnit.SECONDS)
                .exceptionally(e -> {
                    onError.accept(unwrapCompletionException(e));
                    return null;
                });
    }

    private static Throwable unwrapCompletionException(Throwable t) {
        if (t instanceof CompletionException && t.getCause() != null) {
            return t.getCause();
        }
        return t;
    }

    public static synchronized void shutdown() {
        if (EXECUTOR == null) {
            return;
        }
        long time = System.currentTimeMillis();
        DLNetworkManager.LOGGER.info("Shutting down network threads. This may take some time. Please wait.");
        ExecutorService ex = EXECUTOR;
        EXECUTOR = null;
        try {
            ex.shutdown();
            if (!ex.awaitTermination(ModCommonConfig.NETWORK_THREAD_TIMEOUT.get(), TimeUnit.SECONDS)) {
                DLNetworkManager.LOGGER.warn("Network thread pool did not terminate in time, forcing shutdownNow.");
                ex.shutdownNow();
            }
        } catch (InterruptedException e) {
            DLNetworkManager.LOGGER.error("Interrupted while waiting for network thread shutdown.", e);
            ex.shutdownNow();
        } finally {
            DLNetworkManager.LOGGER.info("Network threads shut down. Took " + (System.currentTimeMillis() - time) + "ms.");
        }
    }
}
