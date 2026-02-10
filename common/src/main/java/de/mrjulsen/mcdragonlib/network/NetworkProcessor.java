package de.mrjulsen.mcdragonlib.network;

import java.util.Optional;

public final class NetworkProcessor {
    private NetworkProcessor() {}
    
    @FunctionalInterface
    public interface Send<I extends NetworkPacketData> {
        void execute(I data, NetworkPacketContext context);
    }
    
    @FunctionalInterface
    public interface Receive<O extends NetworkPacketData> {
        O execute(NetworkPacketContext context);
    }
        
    @FunctionalInterface
    public interface SendAndReceive<I extends NetworkPacketData, O extends NetworkPacketData> {
        O execute(I data, NetworkPacketContext context);
    }    
        
    @FunctionalInterface
    public interface StreamProvider<I extends NetworkPacketData, O extends NetworkPacketData> {
        /**
         * 
         * @param data is null on the first run
         */
        I execute(boolean initialCall, Optional<O> data, Optional<NetworkPacketContext> context);
    }

    @FunctionalInterface
    public interface StreamReceiver<I extends NetworkPacketData, O extends NetworkPacketData> {
        O execute(I data, NetworkPacketContext context);
    }
}
