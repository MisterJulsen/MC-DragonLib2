package de.mrjulsen.mcdragonlib.network;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.packet.NetworkPacker;
import de.mrjulsen.mcdragonlib.network.packet.SegmentedPacketHeaderInfo;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public final class DLNetworkManager {
    private DLNetworkManager() {}

    public static final Logger LOGGER = LogUtils.getLogger();

    private static final ConcurrentHashMap<ResourceLocation, NetworkPacketType<?, ?, ?>> packets = new ConcurrentHashMap<>();
    

    public static <N extends NetworkDirection, I extends NetworkPacketData, S extends NetworkProcessor.Send<I>> NetworkPacketType.Send<N, I> registerSendOnlyPacket(ResourceLocation id, N direction, S processor, Function<DLStatus, I> factory) {
        return register(new NetworkPacketType.Send<>(id, direction, processor, factory));
    }

    public static <N extends NetworkDirection, O extends NetworkPacketData, S extends NetworkProcessor.Receive<O>> NetworkPacketType.Receive<N, O> registerReceiveOnlyPacket(ResourceLocation id, N direction, S processor, Function<DLStatus, O> factory) {
        return register(new NetworkPacketType.Receive<>(id, direction, processor, factory));
    }

    public static <N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData, S extends NetworkProcessor.SendAndReceive<I, O>> NetworkPacketType.SendAndReceive<N, I, O> registerSendAndReceivePacket(ResourceLocation id, N direction, S processor, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory) {
        return register(new NetworkPacketType.SendAndReceive<>(id, direction, processor, inputFactory, outputFactory));
    }
    
    public static <N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData> NetworkPacketType.Stream<N, I, O> registerStreamPacket(ResourceLocation id, N direction, Supplier<NetworkProcessor.StreamReceiver<I, O>> factory, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory) {
        return register(new NetworkPacketType.Stream<>(id, direction, factory, inputFactory, outputFactory));
    }
    
    private static <T extends NetworkPacketType<?, ?, ?>> T register(T packet) {
        if (isPacketRegistered(packet.getId())) {
            throw new IllegalArgumentException("A packet with id '" + packet.getId() + "' has already been registered.");
        }
        packets.put(packet.getId(), packet);
        LOGGER.info("Registering {} network packet of type {} with id {}", packet.getDirection(), packet.getType(), packet.getId());
        return packet;
    }

    public static boolean isPacketRegistered(ResourceLocation id) {
        return packets.containsKey(id);
    }

    public static Optional<NetworkPacketType<?, ?, ?>> getRegisteredPacket(ResourceLocation id, NetworkManager.Side side) {
        if (!isPacketRegistered(id)) {
            return Optional.empty();
        }
        NetworkPacketType<?, ?, ?> type = packets.get(id);
        if (type == null || type.getDirection() != side) {
            return Optional.empty();
        }
        return Optional.of(type);
    }

    @ExpectPlatform
    public static Packet<?> toPacket(NetworkManager.Side side, FriendlyByteBuf buffer) {
        throw new AssertionError();
    }    
        
    public static void receiveData(FriendlyByteBuf buf, NetworkManager.Side side, NetworkPacketContext context) {
        SegmentedPacketHeaderInfo header = SegmentedPacketHeaderInfo.readBufferHeader(buf);

        CommunicationType communication = header.type().communication();
        NetworkManager.Side fSide;
        if (communication == CommunicationType.RESPONSE) {
            fSide = side;
        } else {
            fSide = side;
        }

        getRegisteredPacket(header.type().id(), fSide).ifPresentOrElse(x -> {
            NetworkPacker.unpack(header, fSide, buf, context, (rawData) -> {
                CompoundTag nbt = rawData.readAnySizeNbt();
                x.receive(header.type(), context, nbt, communication);
                NetworkPacker.cleanUp(header, fSide);
            });
        }, () -> {
            LOGGER.warn("There is no {} packet registered with ID {}", fSide, header.type().id());
            NetworkPacker.cleanUp(header, fSide);
        });
    }

}
