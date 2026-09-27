package de.mrjulsen.mcdragonlib.net;

import java.util.Objects;

import de.mrjulsen.mcdragonlib.net.codec.DLCodecs;
import de.mrjulsen.mcdragonlib.net.codec.DLStreamCodec;

/**
 * Why an exchange did not produce a result.
 *
 * <p>The reason is always meaningful to the receiving side. The detail text is not: it is only
 * filled for failures that carry no sensitive information, because anything sent to a client can
 * be read by that client. Server-side exception messages stay in the log.
 *
 * @param reason the category of the failure
 * @param detail a short description safe to send to the other side, possibly empty
 */
public record NetworkError(Reason reason, String detail) {

    /** Codec used to put an error on the wire. */
    public static final DLStreamCodec<NetworkError> CODEC = DLStreamCodec.composite(
        DLCodecs.enumByName(Reason.class), NetworkError::reason,
        DLCodecs.string(512), NetworkError::detail,
        NetworkError::new
    );

    /**
     * The categories a failure can fall into.
     */
    public enum Reason {
        /** The remote handler threw. Details are only in the remote log. */
        HANDLER_FAILED,
        /** No response arrived within the configured time. */
        TIMEOUT,
        /** The connection went away before the exchange finished. */
        DISCONNECTED,
        /** The server or client is shutting down. */
        SHUTDOWN,
        /** The payload was larger than the packet allows. */
        TOO_LARGE,
        /** The payload could not be read or written. */
        MALFORMED,
        /** The other side does not know this packet. */
        UNKNOWN_PACKET,
        /** The exchange was cancelled by one of the two sides. */
        CANCELLED
    }

    /**
     * Creates an error with a detail text.
     *
     * @param reason the category of the failure
     * @param detail a short description safe to send to the other side
     */
    public NetworkError {
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(detail, "detail");
    }

    /**
     * Creates an error without a detail text.
     *
     * @param reason the category of the failure
     * @return an error carrying only its reason
     */
    public static NetworkError of(Reason reason) {
        return new NetworkError(reason, "");
    }

    /**
     * Creates an error with a detail text.
     *
     * @param reason the category of the failure
     * @param detail a short description safe to send to the other side
     * @return an error carrying a reason and a description
     */
    public static NetworkError of(Reason reason, String detail) {
        return new NetworkError(reason, detail);
    }

    /**
     * Returns whether this error carries a description.
     *
     * @return {@code true} if the detail text is not empty
     */
    public boolean hasDetail() {
        return !detail.isEmpty();
    }

    @Override
    public String toString() {
        return hasDetail() ? reason + ": " + detail : reason.toString();
    }
}
