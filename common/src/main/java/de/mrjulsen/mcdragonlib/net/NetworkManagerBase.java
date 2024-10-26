package de.mrjulsen.mcdragonlib.net;


import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;
import de.mrjulsen.mcdragonlib.DragonLib;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class NetworkManagerBase {

    public static final int NETWORK_CALLBACK_TIMEOUT = 30000;

    @SuppressWarnings("unchecked")
    public <T extends BaseNetworkPacket<T>> NetworkManagerBase(String modid, String networkChannel, Collection<Class<? extends BaseNetworkPacket<?>>> c2s, Collection<Class<? extends BaseNetworkPacket<?>>> s2c) {         
        c2s.forEach(x -> {
            try {
                Class<T> clazz = (Class<T>)x;
                T packet = clazz.getConstructor().newInstance();
                String name = UUID.nameUUIDFromBytes(clazz.getName().getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
                
                StreamCodec<? super RegistryFriendlyByteBuf, T> codec = StreamCodec.of((buf, msg) -> {
                    packet.encode(msg, buf);
                }, (buf) -> {
                    return packet.decode(buf);
                });

                NetworkManager.registerReceiver(Side.C2S, packet.typeOf(modid, name), codec, (buf, context) -> {
                    packet.handle(packet, () -> context);
                });
                

            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                DragonLib.LOGGER.error("Unable to register packet.", e);
            }
        });
    }
}
