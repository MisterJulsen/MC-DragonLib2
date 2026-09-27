package de.mrjulsen.mcdragonlib.net.fabric;

import de.mrjulsen.mcdragonlib.net.DLNetwork;
import de.mrjulsen.mcdragonlib.net.NetworkFlow;
import de.mrjulsen.mcdragonlib.net.PacketContext;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class ClientNetworkImpl {

    public static void registerChannel(ResourceLocation channelId) {
        ClientPlayNetworking.registerGlobalReceiver(channelId, (client, handler, buf, sender) ->
            DLNetwork.receive(channelId, buf, clientContext(client, handler)));
    }

    static PacketContext clientContext(Minecraft client, ClientPacketListener handler) {
        Connection connection = handler.getConnection();
        return new PacketContext() {
            @Override
            public Env getEnvironment() {
                return Env.CLIENT;
            }

            @Override
            public NetworkFlow getFlow() {
                return NetworkFlow.CLIENTBOUND;
            }

            @Override
            public Connection getConnection() {
                return connection;
            }

            @Override
            public Player getPlayer() {
                return client.player;
            }

            @Override
            public void queue(Runnable task) {
                client.execute(task);
            }
        };
    }
}
