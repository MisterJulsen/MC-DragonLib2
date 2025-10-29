package de.mrjulsen.mcdragonlib.network.fabric;

import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkSide;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;

public class DLNetworkManagerImpl {
    
    public static void registerChannel(ResourceLocation channelId, String protocolVersion) {
        ServerPlayNetworking.registerGlobalReceiver(channelId, (server, player, handler, buf, sender) -> {
            NetworkPacketContext context = context(player, server, false);
            DLNetworkManager.receiveData(channelId, buf, NetworkSide.C2S, context);             
        });
        ClientPlayNetworking.registerGlobalReceiver(channelId, new ClientPlayNetworking.PlayChannelHandler() {
            @Override
            public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender) {
                NetworkPacketContext context = context(client.player, client, true);
                DLNetworkManager.receiveData(channelId, buf, NetworkSide.S2C, context);
            }
        });
    }
    
    
    private static NetworkPacketContext context(Player player, BlockableEventLoop<?> taskQueue, boolean client) {
        return new NetworkPacketContext() {
            @Override
            public Player getPlayer() {
                return player;
            }
            
            @Override
            public void queue(Runnable runnable) {
                taskQueue.execute(runnable);
            }
            
            @Override
            public Env getEnvironment() {
                return client ? Env.CLIENT : Env.SERVER;
            }
        };
    }
    
    public static Packet<?> toPacket(ResourceLocation channelId, NetworkSide side, FriendlyByteBuf buf) {
        if (side == NetworkSide.C2S) {
            return toC2SPacket(channelId, buf);
        } else if (side == NetworkSide.S2C) {
            return toS2CPacket(channelId, buf);
        }
        
        throw new IllegalArgumentException("Invalid side: " + side);
    }
    

    
    @Environment(EnvType.CLIENT)
    private static Packet<?> toC2SPacket(ResourceLocation channelId, FriendlyByteBuf buf) {
        return ClientPlayNetworking.createC2SPacket(channelId, buf);
    }
    
    private static Packet<?> toS2CPacket(ResourceLocation channelId, FriendlyByteBuf buf) {
        return ServerPlayNetworking.createS2CPacket(channelId, buf);
    }
}
