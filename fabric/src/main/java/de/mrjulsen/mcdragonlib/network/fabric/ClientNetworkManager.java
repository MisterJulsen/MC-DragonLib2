package de.mrjulsen.mcdragonlib.network.fabric;

import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkSide;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ClientNetworkManager {
    public static void registerChannel(ResourceLocation channelId, String protocolVersion) {
        ClientPlayNetworking.registerGlobalReceiver(channelId, new ClientPlayNetworking.PlayChannelHandler() {
            @Override
            public void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender) {
                NetworkPacketContext context = DLNetworkManagerImpl.context(client.player, client, true);
                DLNetworkManager.receiveData(channelId, buf, NetworkSide.C2S, context);
            }
        });
    }
}
