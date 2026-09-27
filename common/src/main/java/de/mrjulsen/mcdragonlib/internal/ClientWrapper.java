package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.net.internal.ClientNetworkHooks;
import de.mrjulsen.mcdragonlib.network.NetworkDirection.C2S;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.Level;

public class ClientWrapper {

    public static Level getClientLevel() {
        return Minecraft.getInstance().level;
    }

    public static C2S toServer() {
        return new C2S() {
            @Override
            public void send(Packet<?> packet) {
                if (Minecraft.getInstance().getConnection() != null) {
                    Minecraft.getInstance().getConnection().send(packet);
                } else {
                    throw new IllegalStateException("Unable to send packet to the server while not in game!");
                }
            }

            @Override
            public Connection getConnection() {
                return ClientNetworkHooks.getServerConnection();
            }
        };
    }
}
