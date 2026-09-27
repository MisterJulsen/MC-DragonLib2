package de.mrjulsen.mcdragonlib.net;

/**
 * State a packet keeps for one connection.
 *
 * <p>Anything attached to a {@link ChannelSession} is ticked with it and released when the
 * connection goes away, so a packet type does not have to track connections itself. Stream
 * packets use this, and so can any pattern added later.
 */
public interface SessionResource {

    /**
     * Advances anything time based this resource holds.
     *
     * @param now the current wall-clock time in milliseconds
     */
    default void tick(long now) {}

    /**
     * Releases everything this resource holds.
     *
     * @param reason why the session is going away
     */
    void close(NetworkError.Reason reason);
}
