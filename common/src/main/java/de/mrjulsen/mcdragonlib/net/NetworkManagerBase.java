package de.mrjulsen.mcdragonlib.net;


import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import de.mrjulsen.mcdragonlib.DragonLib;
import dev.architectury.networking.NetworkChannel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class NetworkManagerBase {

    public static final int NETWORK_CALLBACK_TIMEOUT = 30000;
    public final NetworkChannel CHANNEL;

    @SuppressWarnings("unchecked")
    public <T extends IPacketBase<T>> NetworkManagerBase(String modid, String networkChannel, Collection<Class<? extends IPacketBase<?>>> classes) {
        CHANNEL = NetworkChannel.create(new ResourceLocation(modid, networkChannel));
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


    private static record NetworkCallback(long creationTime, BiConsumer<CompoundTag, Long> callback) {}
    private static final Map<UUID, NetworkCallback> networkCallbacks = new HashMap<>();

    public <T extends AbstractIdentifiableRequestPacket<T>>void sendAndAwait(T requestPacket, BiConsumer<CompoundTag, Long> callback) {
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (networkCallbacks.containsKey(id));

        networkCallbacks.put(id, new NetworkCallback(System.currentTimeMillis(), callback));
        requestPacket.id = id;
        CHANNEL.sendToServer(requestPacket);
    }

    public static void executeCallback(UUID id, CompoundTag nbt, long time) {
        if (networkCallbacks.containsKey(id)) {
            networkCallbacks.remove(id).callback().accept(nbt, time);
        }
    }

    public void clearCallbacks() {
        networkCallbacks.clear();
    }

    public static void callbackListenerTick() {
        networkCallbacks.entrySet().removeIf(t -> t.getValue().creationTime() < System.currentTimeMillis() - NETWORK_CALLBACK_TIMEOUT);
    }
}
