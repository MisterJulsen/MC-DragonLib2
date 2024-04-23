package de.mrjulsen.mcdragonlib.net;

import java.util.function.Supplier;

import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

public interface IPacketBase<T extends IPacketBase<T>> {
    void encode(T packet, FriendlyByteBuf buf); 
    T decode(FriendlyByteBuf buf); 
    void handle(T packet, Supplier<PacketContext> contextSupplier);
}