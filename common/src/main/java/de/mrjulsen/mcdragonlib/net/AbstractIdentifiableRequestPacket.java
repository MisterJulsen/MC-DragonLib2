package de.mrjulsen.mcdragonlib.net;

import java.util.UUID;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.net.builtin.IdentifiableResponsePacketBase;
import dev.architectury.networking.NetworkChannel;
import dev.architectury.networking.NetworkManager.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public abstract class AbstractIdentifiableRequestPacket<T extends AbstractIdentifiableRequestPacket<T>> implements IPacketBase<AbstractIdentifiableRequestPacket<T>> {

    public UUID id;

    @SuppressWarnings("unchecked")
    @Override
    public final void encode(AbstractIdentifiableRequestPacket<T> packet, FriendlyByteBuf buf) {
        encodeImpl((T)packet, buf);
        buf.writeUUID(packet.id);
    }

    @Override
    public final AbstractIdentifiableRequestPacket<T> decode(FriendlyByteBuf buf) {
        AbstractIdentifiableRequestPacket<T> t = decodeImpl(buf);
        t.id = buf.readUUID();
        return t;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void handle(AbstractIdentifiableRequestPacket<T> packet, Supplier<PacketContext> contextSupplier) {
        final long gameTime = contextSupplier.get().getPlayer().level().getDayTime();
        IdentifiableResponseData result = handleImpl((T)packet, contextSupplier);
        result.channel().sendToPlayer((ServerPlayer)contextSupplier.get().getPlayer(), new IdentifiableResponsePacketBase(packet.id, result.nbt(), gameTime));
    }

    public abstract void encodeImpl(T packet, FriendlyByteBuf buf);
    public abstract T decodeImpl(FriendlyByteBuf buf);
    public abstract IdentifiableResponseData handleImpl(T packet, Supplier<PacketContext> contextSupplier);

    public static record IdentifiableResponseData(NetworkChannel channel, CompoundTag nbt) {}
    
}