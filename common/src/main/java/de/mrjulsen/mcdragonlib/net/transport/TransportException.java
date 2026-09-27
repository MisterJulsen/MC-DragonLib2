package de.mrjulsen.mcdragonlib.net.transport;

/**
 * Signals that received data does not form a valid DragonLib message.
 *
 * <p>Since the sender is remote, this is treated as bad input rather than a programming error:
 * the offending transfer is dropped and the connection stays usable.
 */
public class TransportException extends RuntimeException {

    /**
     * Creates an exception describing why a frame or message was rejected.
     *
     * @param message the reason the data was rejected
     */
    public TransportException(String message) {
        super(message);
    }

    /**
     * Creates an exception describing why a frame or message was rejected.
     *
     * @param message the reason the data was rejected
     * @param cause the underlying failure
     */
    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
