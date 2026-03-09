package de.mrjulsen.mcdragonlib.network.neoforge;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkSide;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import dev.architectury.platform.hooks.EventBusesHooks;
import dev.architectury.utils.Env;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

//@EventBusSubscriber(modid = DragonLib.MODID)
public class DLNetworkManagerImpl {

    public static <T extends CustomPacketPayload> void registerChannel(ResourceLocation channelId, String name, NetworkSide side, String protocolVersion) {
        CustomPacketPayload.Type<DLNetworkManager.BufCustomPacketPayload> type = new CustomPacketPayload.Type<>(DLUtils.resourceLocation(channelId.getNamespace(), channelId.getPath() + "/" + name));
        StreamCodec<? super RegistryFriendlyByteBuf, DLNetworkManager.BufCustomPacketPayload> codec = DLNetworkManager.BufCustomPacketPayload.streamCodec(type);

        EventBusesHooks.whenAvailable(DragonLib.MODID, bus -> {
            bus.<RegisterPayloadHandlersEvent>addListener(event -> {
                event.registrar(type.id().getNamespace()).optional().playBidirectional(type, codec, (payload, ctx) -> {
                    RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(payload.payload()), ctx.player().registryAccess());
                    Player player = ctx.player();
                    DLNetworkManager.receiveData(channelId, buf, side, context(player, ctx, false));
                    buf.release();
                });

                /*
                if (side == NetworkSide.C2S) {
                } else {
                    event.registrar(type.id().getNamespace()).optional().playToClient(type, codec, (payload, ctx) -> {
                        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(payload.payload()), ctx.player().registryAccess());
                        DLNetworkManager.receiveData(channelId, buf, NetworkSide.C2S, context(ctx.player(), ctx.player().getServer(), false));
                        buf.release();
                    });
                }

                if (FMLLoader.getDist().isClient()) {
                }
                //Env.unsafeRunWhenOn(Dist.CLIENT, () -> () -> )

                 */
            });
        });
    }

    static NetworkPacketContext context(Player player, IPayloadContext taskQueue, boolean client) {
        return new NetworkPacketContext() {
            @Override
            public Player getPlayer() {
                return player;
            }

            @Override
            public void queue(Runnable runnable) {
                taskQueue.enqueueWork(runnable);
            }

            @Override
            public <O> O queueResult(Supplier<O> supplier) {
                CompletableFuture<O> future = new CompletableFuture<>();
                taskQueue.enqueueWork(() -> {
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



    private static Packet<?> toC2SPacket(ResourceLocation id, FriendlyByteBuf buf) {
        CustomPacketPayload.Type<DLNetworkManager.BufCustomPacketPayload> type = new CustomPacketPayload.Type<>(id);
        DLNetworkManager.BufCustomPacketPayload payload = new DLNetworkManager.BufCustomPacketPayload(type, ByteBufUtil.getBytes(buf));
        return new ServerboundCustomPayloadPacket(payload);
    }

    private static Packet<?> toS2CPacket(ResourceLocation id, FriendlyByteBuf buf) {
        CustomPacketPayload.Type<DLNetworkManager.BufCustomPacketPayload> type = new CustomPacketPayload.Type<>(id);
        DLNetworkManager.BufCustomPacketPayload payload = new DLNetworkManager.BufCustomPacketPayload(type, ByteBufUtil.getBytes(buf));
        return new ClientboundCustomPayloadPacket(payload);
    }
}
