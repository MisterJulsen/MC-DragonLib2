package de.mrjulsen.mcdragonlib.network.fabric;

import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkSide;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class DLNetworkManagerImpl {
    
    public static void registerChannel(ResourceLocation channelId, String name, NetworkSide side, String protocolVersion) {
        CustomPacketPayload.Type<DLNetworkManager.BufCustomPacketPayload> type = new CustomPacketPayload.Type<>(DLUtils.resourceLocation(channelId.getNamespace(), channelId.getPath() + "/" + name));
        StreamCodec<? super RegistryFriendlyByteBuf, DLNetworkManager.BufCustomPacketPayload> codec = DLNetworkManager.BufCustomPacketPayload.streamCodec(type);
        PayloadTypeRegistry.playC2S().register(type, codec);
        PayloadTypeRegistry.playS2C().register(type, codec);

        ServerPlayNetworking.registerGlobalReceiver(type, (payload, ctx) -> {
            NetworkPacketContext context = context(ctx.player(), ctx.server(), false);
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(payload.payload()), ctx.player().registryAccess());
            DLNetworkManager.receiveData(channelId, buf, NetworkSide.S2C, context);
            buf.release();
        });

        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> ClientNetworkManager.registerChannel(channelId, name, protocolVersion));
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
    
    public static Packet<?> toPacket(ResourceLocation channelId, String name, NetworkSide side, FriendlyByteBuf buf) {
        ResourceLocation id = DLUtils.resourceLocation(channelId.getNamespace(), channelId.getPath() + "/" + name);
        if (side == NetworkSide.C2S) {
            return toC2SPacket(id, buf);
        } else if (side == NetworkSide.S2C) {
            return toS2CPacket(id, buf);
        }
        
        throw new IllegalArgumentException("Invalid side: " + side);
    }
    

    
    @Environment(EnvType.CLIENT)
    private static Packet<?> toC2SPacket(ResourceLocation id, FriendlyByteBuf buf) {
        CustomPacketPayload.Type<DLNetworkManager.BufCustomPacketPayload> type = new CustomPacketPayload.Type<>(id);
        DLNetworkManager.BufCustomPacketPayload payload = new DLNetworkManager.BufCustomPacketPayload(type, ByteBufUtil.getBytes(buf));
        return ClientPlayNetworking.createC2SPacket(payload);
    }
    
    private static Packet<?> toS2CPacket(ResourceLocation id, FriendlyByteBuf buf) {
        CustomPacketPayload.Type<DLNetworkManager.BufCustomPacketPayload> type = new CustomPacketPayload.Type<>(id);
        DLNetworkManager.BufCustomPacketPayload payload = new DLNetworkManager.BufCustomPacketPayload(type, ByteBufUtil.getBytes(buf));
        return ServerPlayNetworking.createS2CPacket(payload);
    }
}
