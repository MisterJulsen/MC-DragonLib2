package de.mrjulsen.mcdragonlib.net.stream;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;

import de.mrjulsen.mcdragonlib.net.codec.DLStreamCodec;

/**
 * Where the data of a stream comes from.
 *
 * <p>A source is asked for one chunk at a time and only while the receiver has room for it, so a
 * file of any size can be sent without ever holding more than a chunk in memory. Sources are used
 * by a single stream and are closed when it ends, fails or is cancelled.
 */
public interface StreamSource extends Closeable {

    /**
     * Produces the next chunk.
     *
     * @return the next chunk, or {@code null} once the data is exhausted
     * @throws IOException if the data could not be read
     */
    byte[] next() throws IOException;

    /**
     * Returns the total size of this source if it is known ahead of time, which lets the receiver
     * show real progress.
     *
     * @return the total number of bytes, or a negative value if unknown
     */
    default long getTotalBytes() {
        return -1L;
    }

    @Override
    default void close() throws IOException {}

    /**
     * Streams a file from disk.
     *
     * @param path the file to read
     * @param chunkSize how many bytes to read at a time
     * @return a source reading that file
     * @throws IOException if the file could not be opened
     */
    static StreamSource ofFile(Path path, int chunkSize) throws IOException {
        Objects.requireNonNull(path, "path");
        long size = Files.size(path);
        StreamSource delegate = ofInputStream(Files.newInputStream(path), chunkSize);
        return new StreamSource() {
            @Override
            public byte[] next() throws IOException {
                return delegate.next();
            }

            @Override
            public long getTotalBytes() {
                return size;
            }

            @Override
            public void close() throws IOException {
                delegate.close();
            }
        };
    }

    /**
     * Streams whatever an input stream produces. The stream is closed with the source.
     *
     * @param input the stream to read
     * @param chunkSize how many bytes to read at a time
     * @return a source reading that stream
     */
    static StreamSource ofInputStream(InputStream input, int chunkSize) {
        Objects.requireNonNull(input, "input");
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("The chunk size must be positive.");
        }
        return new StreamSource() {
            @Override
            public byte[] next() throws IOException {
                byte[] buffer = new byte[chunkSize];
                int read = 0;
                while (read < chunkSize) {
                    int count = input.read(buffer, read, chunkSize - read);
                    if (count < 0) {
                        break;
                    }
                    read += count;
                }
                if (read == 0) {
                    return null;
                }
                if (read == chunkSize) {
                    return buffer;
                }
                byte[] exact = new byte[read];
                System.arraycopy(buffer, 0, exact, 0, read);
                return exact;
            }

            @Override
            public void close() throws IOException {
                input.close();
            }
        };
    }

    /**
     * Streams an array that is already in memory.
     *
     * @param data the bytes to send
     * @param chunkSize how many bytes to send at a time
     * @return a source reading that array
     */
    static StreamSource ofBytes(byte[] data, int chunkSize) {
        Objects.requireNonNull(data, "data");
        StreamSource delegate = ofInputStream(new ByteArrayInputStream(data), chunkSize);
        return new StreamSource() {
            @Override
            public byte[] next() throws IOException {
                return delegate.next();
            }

            @Override
            public long getTotalBytes() {
                return data.length;
            }

            @Override
            public void close() throws IOException {
                delegate.close();
            }
        };
    }

    /**
     * Streams objects, one per chunk, so the receiver can start processing the first ones while
     * the rest are still being produced.
     *
     * @param values the objects to send
     * @param codec the codec encoding a single object
     * @param <T> the object type
     * @return a source producing one chunk per object
     */
    static <T> StreamSource ofIterator(Iterator<T> values, DLStreamCodec<T> codec) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(codec, "codec");
        return () -> values.hasNext() ? codec.toBytes(values.next()) : null;
    }
}
