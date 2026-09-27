package de.mrjulsen.mcdragonlib.network.fabric;

import de.mrjulsen.mcdragonlib.net.fabric.ClientNetworkImpl;
import net.minecraft.resources.ResourceLocation;

/**
 * @deprecated Kept for linking. Delegates to {@link ClientNetworkImpl}.
 */
@Deprecated
public class ClientNetworkManager {

    public static void registerChannel(ResourceLocation channelId, String protocolVersion) {
        ClientNetworkImpl.registerChannel(channelId);
    }
}
