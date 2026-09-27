package de.mrjulsen.mcdragonlib.net.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.event.EventNetworkChannel;

@OnlyIn(Dist.CLIENT)
public class ClientNetworkImpl {

    public static void registerChannel(EventNetworkChannel channel, ResourceLocation channelId) {
        channel.addListener(DLNetworkImpl.createHandler(NetworkEvent.ServerCustomPayloadEvent.class, channelId));
    }

    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
