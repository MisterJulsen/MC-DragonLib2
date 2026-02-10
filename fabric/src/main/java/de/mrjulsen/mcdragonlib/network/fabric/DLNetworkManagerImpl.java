package de.mrjulsen.mcdragonlib.network.fabric;

import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkSide;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class DLNetworkManagerImpl {
    
    public static void registerChannel(ResourceLocation channelId, String protocolVersion) {
        ServerPlayNetworking.registerGlobalReceiver(channelId, (server, player, handler, buf, sender) -> {
            NetworkPacketContext context = context(player, server, false);
            DLNetworkManager.receiveData(channelId, buf, NetworkSide.S2C, context);             
        });
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> ClientNetworkManager.registerChannel(channelId, protocolVersion));
    }
    
    
    static NetworkPacketContext context(Player player, BlockableEventLoop<?> taskQueue, boolean client) {
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
            public <O> O queueResult(Supplier<O> supplier) {
                CompletableFuture<O> future = new CompletableFuture<>();
                taskQueue.execute(() -> {
                    try {
                        future.complete(supplier.get());
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
                return future.join();
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
