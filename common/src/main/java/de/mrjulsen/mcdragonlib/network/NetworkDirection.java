package de.mrjulsen.mcdragonlib.network;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.mcdragonlib.internal.ClientWrapper;
import de.mrjulsen.mcdragonlib.net.PacketTarget;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

/**
 * @deprecated Use {@link de.mrjulsen.mcdragonlib.net.PacketTarget}.
 */
@Deprecated
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

    /**
     * Returns the connection this instance sends over, if it knows one.
     *
     * <p>The instances handed out by {@link #toPlayer(ServerPlayer)} and {@link #toServer()} do.
     * A hand written implementation does not, in which case the networking layer falls back to
     * routing purely through {@link #send(Packet)}.
     *
     * @return the connection, or {@code null} if this instance only knows how to send
     */
    @Nullable
    default Connection getConnection() {
        return null;
    }
    

    public static S2C toPlayer(ServerPlayer player) {
        Objects.requireNonNull(player, "Unable to send packet to a 'null' player!");
        return new S2C() {
            @Override
            public void send(Packet<?> packet) {
                player.connection.send(packet);
            }

            @Override
            public Connection getConnection() {
                return PacketTarget.connectionOf(player);
            }
        };
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
