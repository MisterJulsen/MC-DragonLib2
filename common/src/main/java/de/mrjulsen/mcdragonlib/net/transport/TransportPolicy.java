package de.mrjulsen.mcdragonlib.net.transport;

/**
 * The limits the transport enforces on incoming messages.
 *
 * <p>Reassembly allocates memory on behalf of a remote peer, so every inbound message is checked
 * against these limits before anything is reserved for it. Implementations are supplied by the
 * layer that knows the packet registry.
 */
public interface TransportPolicy {

    /**
     * Returns how large a fully reassembled message for the given packet may be.
     *
     * @param packetName the registered packet the message belongs to
     * @return the highest accepted payload size in bytes, or a negative value if the packet is unknown
     */
    int getMaxMessageBytes(String packetName);

    /**
     * Returns how many bytes all unfinished transfers of one connection may occupy together.
     *
     * @return the reassembly budget per connection in bytes
     */
    int getConnectionBudgetBytes();

    /**
     * Returns how long an unfinished transfer is kept before it is discarded.
     *
     * @return the reassembly timeout in milliseconds
     */
    long getReassemblyTimeoutMillis();
}
