package de.mrjulsen.mcdragonlib.network.forge;

import de.mrjulsen.mcdragonlib.net.NetworkFlow;
import de.mrjulsen.mcdragonlib.net.forge.DLNetworkImpl;
import de.mrjulsen.mcdragonlib.network.NetworkSide;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

/**
 * @deprecated Kept so the deprecated manager keeps linking. Delegates to {@link DLNetworkImpl}.
 */
@Deprecated
public class DLNetworkManagerImpl {

    public static void registerChannel(ResourceLocation channelId, String protocolVersion) {
        DLNetworkImpl.registerChannel(channelId, protocolVersion);
    }

    public static Packet<?> toPacket(ResourceLocation channelId, NetworkSide side, FriendlyByteBuf buffer) {
        return DLNetworkImpl.toPacket(channelId, side == NetworkSide.C2S ? NetworkFlow.SERVERBOUND : NetworkFlow.CLIENTBOUND, buffer);
    }
}
