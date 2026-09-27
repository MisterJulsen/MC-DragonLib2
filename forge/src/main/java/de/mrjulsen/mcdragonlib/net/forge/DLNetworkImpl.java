package de.mrjulsen.mcdragonlib.net.forge;

import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.net.DLNetwork;
import de.mrjulsen.mcdragonlib.net.NetworkFlow;
import de.mrjulsen.mcdragonlib.net.PacketContext;
import dev.architectury.utils.Env;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
import org.apache.commons.lang3.tuple.Pair;

public class DLNetworkImpl {

    public static void registerChannel(ResourceLocation channelId, String protocolVersion) {
        EventNetworkChannel channel = NetworkRegistry.newEventChannel(channelId, () -> protocolVersion, version -> true, version -> true);
        channel.addListener(createHandler(NetworkEvent.ClientCustomPayloadEvent.class, channelId));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientNetworkImpl.registerChannel(channel, channelId));
    }

    public static Packet<?> toPacket(ResourceLocation channelId, NetworkFlow flow, FriendlyByteBuf frame) {
        NetworkDirection direction = flow == NetworkFlow.SERVERBOUND ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT;
        return direction.buildPacket(Pair.of(frame, 0), channelId).getThis();
    }

    static <T extends NetworkEvent> Consumer<T> createHandler(Class<T> type, ResourceLocation channelId) {
        return event -> {
            if (event.getClass() != type) {
                return;
            }
            NetworkEvent.Context context = event.getSource().get();
            if (context.getPacketHandled()) {
                return;
            }
            FriendlyByteBuf payload = event.getPayload();
            if (payload == null) {
                return;
            }
            DLNetwork.receive(channelId, payload, context(context));
            context.setPacketHandled(true);
        };
    }

    private static PacketContext context(NetworkEvent.Context source) {
        boolean client = source.getDirection().getReceptionSide() == LogicalSide.CLIENT;
        Connection connection = source.getNetworkManager();
        return new PacketContext() {
            @Override
            public Env getEnvironment() {
                return client ? Env.CLIENT : Env.SERVER;
            }

            @Override
            public NetworkFlow getFlow() {
                return client ? NetworkFlow.CLIENTBOUND : NetworkFlow.SERVERBOUND;
            }

            @Override
            public Connection getConnection() {
                return connection;
            }

            @Override
            public Player getPlayer() {
                return client ? DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> ClientNetworkImpl::getClientPlayer) : source.getSender();
            }

            @Override
            public void queue(Runnable task) {
                source.enqueueWork(task);
            }
        };
    }
}
