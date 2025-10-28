package de.mrjulsen.mcdragonlib.net;


import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import de.mrjulsen.mcdragonlib.DragonLib;
import dev.architectury.networking.NetworkChannel;
import net.minecraft.resources.ResourceLocation;

public class NetworkManagerBase {

    public final NetworkChannel CHANNEL;

    public <T extends IPacketBase<T>> NetworkManagerBase(ResourceLocation channelId, Collection<Class<? extends IPacketBase<?>>> classes) {
        CHANNEL = NetworkChannel.create(channelId);
        classes.forEach(c -> {
            try {
                Class<T> clazz = (Class<T>)c;
                T packet = clazz.getConstructor().newInstance();
                CHANNEL.register(clazz, packet::encode, packet::decode, packet::handle);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                DragonLib.LOGGER.error("Unable to register packet.", e);
            }
        });
    }
}
