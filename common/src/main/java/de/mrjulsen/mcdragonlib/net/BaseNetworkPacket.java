package de.mrjulsen.mcdragonlib.net;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseNetworkPacket<T extends BaseNetworkPacket<T>> implements CustomPacketPayload {

    private static final Map<Class<? extends BaseNetworkPacket<?>>, Type<?>> typesByClass = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public final Type<T> type() {
        try {
            return (Type<T>)typesByClass.get(getClass());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to get DragonLib Network Packet for " + this + ".", e);
        }
    }

    @SuppressWarnings("unchecked")
    public final Type<T> typeOf(String modid, String name) {
        try {
            return (Type<T>)typesByClass.computeIfAbsent((Class<? extends BaseNetworkPacket<?>>)this.getClass(), x -> new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(modid, name)));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to register DragonLib Network Packet '" + modid + ":" + name + "'.", e);
        }
    }

    public abstract void encode(T packet, RegistryFriendlyByteBuf buf); 
    public abstract T decode(RegistryFriendlyByteBuf buf); 
    public abstract void handle(T packet, Supplier<PacketContext> contextSupplier);
    
}
