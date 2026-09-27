package de.mrjulsen.mcdragonlib.net.stream;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.net.NetworkError;
import de.mrjulsen.mcdragonlib.net.Response;

/**
 * A running transfer, seen from the side that asked for it.
 *
 * <p>Listeners can be attached at any time: one that is added after the stream already finished
 * is called right away, so there is no window in which an outcome can be missed.
 */
public final class StreamHandle {

    /**
     * Reports how far a transfer has come.
     */
    @FunctionalInterface
    public interface ProgressListener {

        /**
         * Called after each processed chunk.
         *
         * @param transferred how many bytes have been handled so far
         * @param total the expected total, or a negative value if the sender did not announce one
         */
        void onProgress(long transferred, long total);
    }

    private final Runnable cancelAction;
    private final CompletableFuture<Response<Void>> future = new CompletableFuture<>();

    private ProgressListener progressListener;
    private Runnable completeListener;
    private Consumer<NetworkError> errorListener;

    private long transferred;
    private long total = -1L;
    private boolean finished;
    private NetworkError error;

    StreamHandle(Runnable cancelAction) {
        this.cancelAction = cancelAction;
    }

    /**
     * Registers a listener for transfer progress.
     *
     * @param listener called after each processed chunk
     * @return this handle
     */
    public synchronized StreamHandle onProgress(ProgressListener listener) {
        this.progressListener = Objects.requireNonNull(listener, "listener");
        if (transferred > 0) {
            listener.onProgress(transferred, total);
        }
        return this;
    }

    /**
     * Registers a listener for successful completion.
     *
     * @param listener called once the last chunk has been handled
     * @return this handle
     */
    public synchronized StreamHandle onComplete(Runnable listener) {
        this.completeListener = Objects.requireNonNull(listener, "listener");
        if (finished && error == null) {
            listener.run();
        }
        return this;
    }

    /**
     * Registers a listener for failure and cancellation.
     *
     * @param listener called with the reason the stream ended early
     * @return this handle
     */
    public synchronized StreamHandle onError(Consumer<NetworkError> listener) {
        this.errorListener = Objects.requireNonNull(listener, "listener");
        if (finished && error != null) {
            listener.accept(error);
        }
        return this;
    }

    /**
     * Returns a future that completes with the outcome of the transfer.
     *
     * @return a future holding an empty success or the failure reason
     */
    public CompletableFuture<Response<Void>> future() {
        return future;
    }

    /**
     * Asks both sides to stop the transfer. Has no effect once it has finished.
     */
    public void cancel() {
        cancelAction.run();
    }

    /**
     * Returns whether the transfer has ended, successfully or not.
     *
     * @return {@code true} once an outcome is known
     */
    public synchronized boolean isDone() {
        return finished;
    }

    /**
     * Returns how many bytes have been handled so far.
     *
     * @return the transferred byte count
     */
    public synchronized long getTransferredBytes() {
        return transferred;
    }

    /**
     * Returns the announced total size of the transfer.
     *
     * @return the total byte count, or a negative value if unknown
     */
    public synchronized long getTotalBytes() {
        return total;
    }

    synchronized void setTotal(long total) {
        this.total = total;
    }

    synchronized void advance(long bytes) {
        transferred += bytes;
        if (progressListener != null) {
            progressListener.onProgress(transferred, total);
        }
    }

    synchronized void complete() {
        if (finished) {
            return;
        }
        finished = true;
        if (completeListener != null) {
            completeListener.run();
        }
        future.complete(Response.success(null));
    }

    synchronized void fail(NetworkError reason) {
        if (finished) {
            return;
        }
        finished = true;
        error = reason;
        if (errorListener != null) {
            errorListener.accept(reason);
        }
        future.complete(Response.failure(reason));
    }
}
