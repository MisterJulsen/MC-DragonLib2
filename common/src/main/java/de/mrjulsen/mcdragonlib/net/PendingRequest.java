package de.mrjulsen.mcdragonlib.net;

/**
 * An exchange that has been sent and is waiting for its answer.
 *
 * <p>The session keeps these so that a response can be matched to its request and so that every
 * outstanding exchange can still be completed when the connection or the game goes away.
 */
public interface PendingRequest {

    /**
     * Returns the packet this exchange belongs to.
     *
     * @return the originating packet definition
     */
    PacketDefinition getDefinition();

    /**
     * Returns when this exchange gives up waiting.
     *
     * @return the deadline as a wall-clock timestamp in milliseconds
     */
    long getDeadline();

    /**
     * Completes this exchange without a result.
     *
     * @param error why no result will arrive
     */
    void fail(NetworkError error);
}
