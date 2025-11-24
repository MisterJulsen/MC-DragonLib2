package de.mrjulsen.mcdragonlib.network.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.event.EventNetworkChannel;

@OnlyIn(Dist.CLIENT)
public class ClientNetworkingManager {
    
    public static void initClient(EventNetworkChannel channel, ResourceLocation id) {
        channel.addListener(DLNetworkManagerImpl.createPacketHandler(NetworkEvent.ServerCustomPayloadEvent.class, id));
        MinecraftForge.EVENT_BUS.register(ClientNetworkingManager.class);
    }
    
    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
