package de.mrjulsen.mcdragonlib.net;


import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;
import de.mrjulsen.mcdragonlib.DragonLib;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class DLNetworkManager {

    public static final int NETWORK_CALLBACK_TIMEOUT = 30000;

    public static <T extends BaseNetworkPacket<T>> void registerPackets(String modid, Collection<Class<? extends BaseNetworkPacket<?>>> c2s, Collection<Class<? extends BaseNetworkPacket<?>>> s2c) {         
        c2s.forEach(x -> {
            registerPacket(Side.C2S, modid, x);
        });
        s2c.forEach(x -> {
            registerPacket(Side.S2C, modid, x);
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends BaseNetworkPacket<T>> void registerPacket(Side side, String modid, Class<? extends BaseNetworkPacket<?>> c) {
        try {
            Class<T> clazz = (Class<T>)c;
            T packet = clazz.getConstructor().newInstance();
            String name = UUID.nameUUIDFromBytes(clazz.getName().getBytes(StandardCharsets.UTF_8)).toString().replace("-", "") + "_" + (side == Side.C2S ? "client" : "server");
            
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec = StreamCodec.of((buf, msg) -> {
                packet.encode(msg, buf);
            }, (buf) -> {
                return packet.decode(buf);
            });

            if (side == Side.C2S || Platform.getEnv() == EnvType.CLIENT) {
                NetworkManager.registerReceiver(side, packet.typeOf(side, modid, name), codec, (p, context) -> {
                    packet.handle(p, () -> context);
                });
            } else {
                NetworkManager.registerS2CPayloadType(packet.typeOf(side, modid, name), codec);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            DragonLib.LOGGER.error("Unable to register packet.", e);
        }
    }

    public static <T extends BaseNetworkPacket<T>> void sendToServer(T packet) {
        packet.prepareSendForSide(Side.C2S);
        NetworkManager.sendToServer(packet);
    }

    public static <T extends BaseNetworkPacket<T>> void sendToPlayer(ServerPlayer player, T packet) {
        packet.prepareSendForSide(Side.S2C);
        NetworkManager.sendToPlayer(player, packet);
    }

    public static <T extends BaseNetworkPacket<T>> void sendToPlayers(Iterable<ServerPlayer> players, T packet) {
        packet.prepareSendForSide(Side.S2C);
        NetworkManager.sendToPlayers(players, packet);
    }
}

