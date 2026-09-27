package de.mrjulsen.mcdragonlib.net.fabric;

import de.mrjulsen.mcdragonlib.mixin.ServerGamePacketListenerAccessor;
import de.mrjulsen.mcdragonlib.net.DLNetwork;
import de.mrjulsen.mcdragonlib.net.NetworkFlow;
import de.mrjulsen.mcdragonlib.net.PacketContext;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;

public class DLNetworkImpl {

    public static void registerChannel(ResourceLocation channelId, String protocolVersion) {
        ServerPlayNetworking.registerGlobalReceiver(channelId, (server, player, handler, buf, sender) ->
            DLNetwork.receive(channelId, buf, serverContext(server, player, handler)));
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> ClientNetworkImpl.registerChannel(channelId));
    }

    public static Packet<?> toPacket(ResourceLocation channelId, NetworkFlow flow, FriendlyByteBuf frame) {
        if (flow == NetworkFlow.SERVERBOUND) {
            return toServerboundPacket(channelId, frame);
        }
        return ServerPlayNetworking.createS2CPacket(channelId, frame);
    }

    @Environment(EnvType.CLIENT)
    private static Packet<?> toServerboundPacket(ResourceLocation channelId, FriendlyByteBuf frame) {
        return ClientPlayNetworking.createC2SPacket(channelId, frame);
    }

    static PacketContext serverContext(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler) {
        Connection connection = ((ServerGamePacketListenerAccessor) handler).dragonlib$getConnection();
        return new PacketContext() {
            @Override
            public Env getEnvironment() {
                return Env.SERVER;
            }

            @Override
            public NetworkFlow getFlow() {
                return NetworkFlow.SERVERBOUND;
            }

            @Override
            public Connection getConnection() {
                return connection;
            }

            @Override
            public Player getPlayer() {
                return player;
            }

            @Override
            public void queue(Runnable task) {
                server.execute(task);
            }
        };
    }
}
