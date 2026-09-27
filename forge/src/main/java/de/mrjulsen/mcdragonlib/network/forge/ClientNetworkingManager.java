package de.mrjulsen.mcdragonlib.network.forge;

import de.mrjulsen.mcdragonlib.net.forge.ClientNetworkImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.event.EventNetworkChannel;

/**
 * @deprecated Kept for linking. Delegates to {@link ClientNetworkImpl}.
 */
@Deprecated
@OnlyIn(Dist.CLIENT)
public class ClientNetworkingManager {

    public static void initClient(EventNetworkChannel channel, ResourceLocation id) {
        ClientNetworkImpl.registerChannel(channel, id);
    }

    public static Player getClientPlayer() {
        return ClientNetworkImpl.getClientPlayer();
    }
}
