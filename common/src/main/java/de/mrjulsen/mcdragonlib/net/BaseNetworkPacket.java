package de.mrjulsen.mcdragonlib.net;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import dev.architectury.networking.NetworkManager.PacketContext;
import dev.architectury.networking.NetworkManager.Side;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseNetworkPacket<T extends BaseNetworkPacket<T>> implements CustomPacketPayload {

    private static final Map<Class<? extends BaseNetworkPacket<?>>, Map<Side, Type<?>>> typesByClass = new HashMap<>();

    private Side selectedSide;

    @SuppressWarnings("unchecked")
    @Override
    public final Type<T> type() {
        final Side side = selectedSide;
        selectedSide = null;

        if (side == null) {
            throw new IllegalStateException("Cannot send DragonLib Network Packet for " + this + " since no side has been selected. Consider using the DLNetworkManager to send the packet, otherwise it won't work.");
        }

        try {
            return (Type<T>)typesByClass.get(getClass()).get(side);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to get DragonLib Network Packet for " + this + " on side " + side + ".", e);
        }
    }

    @SuppressWarnings("unchecked")
    public final Type<T> typeOf(Side side, String modid, String name) {
        try {
            return (Type<T>)typesByClass.computeIfAbsent((Class<? extends BaseNetworkPacket<?>>)this.getClass(), x -> new HashMap<>()).computeIfAbsent(side, x -> new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(modid, name)));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to register DragonLib Network Packet '" + modid + ":" + name + "' on side " + side + ".", e);
        }
    }

    public final void prepareSendForSide(Side side) {
        this.selectedSide = side;
    }

    public abstract void encode(T packet, RegistryFriendlyByteBuf buf); 
    public abstract T decode(RegistryFriendlyByteBuf buf); 
    public abstract void handle(T packet, Supplier<PacketContext> contextSupplier);
    
}
