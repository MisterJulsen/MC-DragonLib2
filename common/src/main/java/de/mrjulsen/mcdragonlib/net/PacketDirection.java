package de.mrjulsen.mcdragonlib.net;

/**
 * Describes which side may start an exchange for a registered packet.
 *
 * <p>A response always travels the opposite way, so this only fixes the initiating side. The
 * direction an individual frame takes on the wire is described by {@link NetworkFlow}.
 */
public enum PacketDirection {

    /** The client starts the exchange and the server handles it. */
    TO_SERVER(NetworkFlow.SERVERBOUND),
    /** The server starts the exchange and the client handles it. */
    TO_CLIENT(NetworkFlow.CLIENTBOUND);

    private final NetworkFlow requestFlow;

    private PacketDirection(NetworkFlow requestFlow) {
        this.requestFlow = requestFlow;
    }

    /**
     * Returns the flow a request of this packet travels in.
     *
     * @return the flow used for requests
     */
    public NetworkFlow getRequestFlow() {
        return requestFlow;
    }

    /**
     * Returns the flow a response to this packet travels in.
     *
     * @return the flow used for responses
     */
    public NetworkFlow getResponseFlow() {
        return requestFlow.opposite();
    }
}
