package de.mrjulsen.mcdragonlib.network;

import java.util.Objects;

import de.mrjulsen.mcdragonlib.internal.ClientWrapper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

public sealed interface NetworkDirection permits NetworkDirection.C2S, NetworkDirection.S2C {
    
    @FunctionalInterface
    public static non-sealed interface C2S extends NetworkDirection {
        @Override
        default NetworkSide getDirection() {
            return NetworkSide.C2S;
        }
    }
    
    @FunctionalInterface
    public static non-sealed interface S2C extends NetworkDirection {        
        @Override
        default NetworkSide getDirection() {
            return NetworkSide.S2C;
        }
    }

    public static final C2S C2S = (packet) -> { throw new IllegalStateException("Please create your own and new instance of NetworkDirection."); };
    public static final S2C S2C = (packet) -> { throw new IllegalStateException("Please create your own and new instance of NetworkDirection."); };

    void send(Packet<?> packet);
    NetworkSide getDirection();
    

    public static S2C toPlayer(ServerPlayer player) {
        return packet -> Objects.requireNonNull(player, "Unable to send packet to a 'null' player!").connection.send(packet);
    }
    
    public static C2S toServer() {
        return ClientWrapper.toServer();
    }

    public static <N extends NetworkDirection> N forContext(N current, NetworkPacketContext context) {
        if (current instanceof S2C) {
            return (N)NetworkDirection.toPlayer((ServerPlayer)context.getPlayer());
        } else if (current instanceof C2S) {
            return (N)NetworkDirection.toServer();
        }
        return null;
    }



    
}
