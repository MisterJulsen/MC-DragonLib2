package de.mrjulsen.mcdragonlib.network.forge;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkSide;
import dev.architectury.utils.Env;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = DragonLib.MODID)
public class DLNetworkManagerImpl {

    public static void registerChannel(ResourceLocation channelId, String protocolVersion) {
        EventNetworkChannel channel = NetworkRegistry.newEventChannel(channelId, () -> protocolVersion, version -> true, version -> true);
        channel.addListener(createPacketHandler(NetworkEvent.ClientCustomPayloadEvent.class, channelId));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientNetworkingManager.initClient(channel, channelId));
    }
    
    static <T extends NetworkEvent> Consumer<T> createPacketHandler(Class<T> clazz, ResourceLocation channelId) {
        final ResourceLocation id = channelId;
        return event -> {
            if (event.getClass() != clazz) return;
            NetworkEvent.Context context = event.getSource().get();
            if (context.getPacketHandled()) return;
            FriendlyByteBuf buffer = event.getPayload();
            if (buffer == null) return;
            
            NetworkSide side = context.getDirection().getReceptionSide() == LogicalSide.CLIENT ? NetworkSide.C2S : NetworkSide.S2C;
            NetworkPacketContext packetContext = new NetworkPacketContext() {
                @Override
                public Player getPlayer() {
                    return getEnvironment() == Env.CLIENT ? getClientPlayer() : context.getSender();
                }
                
                @Override
                public void queue(Runnable runnable) {
                    context.enqueueWork(runnable);
                }
                
                @Override
                public Env getEnvironment() {
                    return context.getDirection().getReceptionSide() == LogicalSide.CLIENT ? Env.CLIENT : Env.SERVER;
                }
                
                private Player getClientPlayer() {
                    return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> ClientNetworkingManager::getClientPlayer);
                }
            };
            DLNetworkManager.receiveData(id, buffer, side, packetContext);            
            context.setPacketHandled(true);
        };
    }
    
    public static Packet<?> toPacket(ResourceLocation channelId, NetworkSide side, FriendlyByteBuf buffer) {
        return (side == NetworkSide.C2S ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT).buildPacket(Pair.of(buffer, 0), channelId).getThis();
    }
}
