package de.mrjulsen.mcdragonlib.network.fabric;

import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkSide;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ClientNetworkManager {
    public static void registerChannel(ResourceLocation channelId, String name, String protocolVersion) {
        CustomPacketPayload.Type<DLNetworkManager.BufCustomPacketPayload> type = new CustomPacketPayload.Type<>(DLUtils.resourceLocation(channelId.getNamespace(), channelId.getPath() + "/" + name));

        ClientPlayNetworking.registerGlobalReceiver(type, new ClientPlayNetworking.PlayPayloadHandler() {
            @Override
            public void receive(CustomPacketPayload payload, ClientPlayNetworking.Context ctx) {
                NetworkPacketContext context = DLNetworkManagerImpl.context(ctx.player(), ctx.client(), true);
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(((DLNetworkManager.BufCustomPacketPayload)payload).payload()), ctx.player().registryAccess());
                DLNetworkManager.receiveData(channelId, buf, NetworkSide.C2S, context);
                buf.release();
            }
        });
    }
}
