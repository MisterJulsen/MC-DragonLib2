package de.mrjulsen.mcdragonlib.net;

/**
 * The direction a single frame travels in.
 *
 * <p>This describes the transport, not the packet definition. A request and its response use
 * opposite flows. Which side may start an exchange is described by {@link PacketDirection}.
 */
public enum NetworkFlow {

    /** From the server to a client. */
    CLIENTBOUND(1048576),
    /** From a client to the server. */
    SERVERBOUND(32767);

    private final int maxPayloadBytes;

    private NetworkFlow(int maxPayloadBytes) {
        this.maxPayloadBytes = maxPayloadBytes;
    }

    /**
     * Returns the payload limit vanilla enforces for a custom payload packet in this direction.
     *
     * @return the highest number of bytes a single packet may carry
     */
    public int getMaxPayloadBytes() {
        return maxPayloadBytes;
    }

    /**
     * Returns the flow pointing the other way.
     *
     * @return the opposite flow
     */
    public NetworkFlow opposite() {
        return this == CLIENTBOUND ? SERVERBOUND : CLIENTBOUND;
    }
}
