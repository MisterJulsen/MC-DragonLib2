package de.mrjulsen.mcdragonlib.net;

import java.util.function.Supplier;

import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseNetworkPacket<T extends BaseNetworkPacket<T>> implements CustomPacketPayload {

    private CustomPacketPayload.Type<T> type = null;

    @Override
    public final Type<T> type() {
        return type;
    }

    public final Type<T> typeOf(String modid, String name) {
        return type = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(modid, name));
    }

    public abstract void encode(T packet, RegistryFriendlyByteBuf buf); 
    public abstract T decode(RegistryFriendlyByteBuf buf); 
    public abstract void handle(T packet, Supplier<PacketContext> contextSupplier);
    
}
