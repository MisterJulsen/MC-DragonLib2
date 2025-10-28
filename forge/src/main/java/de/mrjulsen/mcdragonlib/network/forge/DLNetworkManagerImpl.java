package de.mrjulsen.mcdragonlib.network.forge;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
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
    
    private static final ResourceLocation CHANNEL_ID = new ResourceLocation(DragonLib.MODID, "network");
    static final EventNetworkChannel CHANNEL = NetworkRegistry.newEventChannel(CHANNEL_ID, () -> Platform.getMod(DragonLib.MODID).getVersion(), version -> true, version -> true);
    
    static {
        CHANNEL.addListener(createPacketHandler(NetworkEvent.ClientCustomPayloadEvent.class));        
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientNetworkingManager::initClient);
    }
    
    static <T extends NetworkEvent> Consumer<T> createPacketHandler(Class<T> clazz) {
        return event -> {
            if (event.getClass() != clazz) return;
            NetworkEvent.Context context = event.getSource().get();
            if (context.getPacketHandled()) return;
            FriendlyByteBuf buffer = event.getPayload();
            if (buffer == null) return;
            
            NetworkManager.Side side = context.getDirection().getReceptionSide() == LogicalSide.CLIENT ? NetworkManager.Side.S2C : NetworkManager.Side.C2S;
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
            DLNetworkManager.receiveData(buffer, side, packetContext);            
            context.setPacketHandled(true);
        };
    }
    
    public static Packet<?> toPacket(NetworkManager.Side side, FriendlyByteBuf buffer) {
        return (side == NetworkManager.Side.C2S ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT).buildPacket(Pair.of(buffer, 0), CHANNEL_ID).getThis();
    }
}
