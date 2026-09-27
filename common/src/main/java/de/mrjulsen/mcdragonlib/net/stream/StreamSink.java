package de.mrjulsen.mcdragonlib.net.stream;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.net.NetworkError;
import de.mrjulsen.mcdragonlib.net.codec.DLStreamCodec;

/**
 * Where the data of a stream goes.
 *
 * <p>Chunks arrive in the order they were sent and one at a time. The sender is allowed to run
 * ahead by a fixed number of chunks, no further, so a slow sink automatically slows the sender
 * down instead of filling memory. Throwing from {@link #accept} cancels the stream.
 */
public interface StreamSink extends Closeable {

    /**
     * Handles the next chunk.
     *
     * @param chunk the received bytes, which may be kept
     * @throws IOException if the chunk could not be processed, which cancels the stream
     */
    void accept(byte[] chunk) throws IOException;

    /**
     * Called once after the last chunk has been handled.
     *
     * @throws IOException if finishing failed
     */
    default void finish() throws IOException {}

    /**
     * Called instead of {@link #finish()} when the stream did not complete.
     *
     * @param error why the stream ended early
     */
    default void abort(NetworkError error) {}

    @Override
    default void close() throws IOException {}

    /**
     * Writes the stream to a file, replacing anything already there.
     *
     * @param path the file to write
     * @return a sink writing that file
     * @throws IOException if the file could not be opened
     */
    static StreamSink toFile(Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        return toOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
    }

    /**
     * Writes the stream to an output stream, which is closed with the sink.
     *
     * @param output the stream to write to
     * @return a sink writing to that stream
     */
    static StreamSink toOutputStream(OutputStream output) {
        Objects.requireNonNull(output, "output");
        return new StreamSink() {
            @Override
            public void accept(byte[] chunk) throws IOException {
                output.write(chunk);
            }

            @Override
            public void finish() throws IOException {
                output.flush();
            }

            @Override
            public void close() throws IOException {
                output.close();
            }
        };
    }

    /**
     * Hands every chunk to a callback.
     *
     * @param consumer called once per chunk, in order
     * @return a sink forwarding to that callback
     */
    static StreamSink forEach(Consumer<byte[]> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        return consumer::accept;
    }

    /**
     * Decodes every chunk as a single object and hands it to a callback. Pairs with
     * {@link StreamSource#ofIterator(java.util.Iterator, DLStreamCodec)}.
     *
     * @param codec the codec decoding a single object
     * @param consumer called once per object, in order
     * @param <T> the object type
     * @return a sink decoding and forwarding each chunk
     */
    static <T> StreamSink forEachObject(DLStreamCodec<T> codec, Consumer<T> consumer) {
        Objects.requireNonNull(codec, "codec");
        Objects.requireNonNull(consumer, "consumer");
        return chunk -> consumer.accept(codec.fromBytes(chunk));
    }

    /**
     * Collects the whole stream in memory, which defeats the point of streaming and is therefore
     * capped.
     *
     * @param maxBytes the largest total size accepted
     * @param consumer called once with the complete data
     * @return a sink buffering the stream
     */
    static StreamSink collecting(int maxBytes, Consumer<byte[]> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        return new StreamSink() {
            @Override
            public void accept(byte[] chunk) throws IOException {
                if (buffer.size() + chunk.length > maxBytes) {
                    throw new IOException("The stream exceeds the collect limit of " + maxBytes + " bytes.");
                }
                buffer.write(chunk);
            }

            @Override
            public void finish() {
                consumer.accept(buffer.toByteArray());
            }
        };
    }
}
