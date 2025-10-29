package de.mrjulsen.mcdragonlib.network;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.packet.NetworkPacker;
import de.mrjulsen.mcdragonlib.network.packet.SegmentedPacketHeaderInfo;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public final class DLNetworkManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(DragonLib.MOD_NAME + " Networking System");
    private static final ConcurrentHashMap<ResourceLocation, DLNetworkManager> managers = new ConcurrentHashMap<>();

    private final ResourceLocation channelId;
    private final String protocolVersion;
    private final ConcurrentHashMap<String, NetworkPacketType<?, ?, ?>> packets = new ConcurrentHashMap<>();

    public DLNetworkManager(ResourceLocation channelId, String protocolVersion) {
        this.channelId = channelId;
        this.protocolVersion = protocolVersion;
    }
   

    public <N extends NetworkDirection, I extends NetworkPacketData, S extends NetworkProcessor.Send<I>> NetworkPacketType.Send<N, I> registerSendOnlyPacket(String name, N direction, S processor, Function<DLStatus, I> factory) {
        return register(new NetworkPacketType.Send<>(channelId, name, direction, processor, factory));
    }

    public <N extends NetworkDirection, O extends NetworkPacketData, S extends NetworkProcessor.Receive<O>> NetworkPacketType.Receive<N, O> registerReceiveOnlyPacket(String name, N direction, S processor, Function<DLStatus, O> factory) {
        return register(new NetworkPacketType.Receive<>(channelId, name, direction, processor, factory));
    }

    public <N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData, S extends NetworkProcessor.SendAndReceive<I, O>> NetworkPacketType.SendAndReceive<N, I, O> registerSendAndReceivePacket(String name, N direction, S processor, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory) {
        return register(new NetworkPacketType.SendAndReceive<>(channelId, name, direction, processor, inputFactory, outputFactory));
    }
    
    public <N extends NetworkDirection, I extends NetworkPacketData, O extends NetworkPacketData> NetworkPacketType.Stream<N, I, O> registerStreamPacket(String name, N direction, Supplier<NetworkProcessor.StreamReceiver<I, O>> factory, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory) {
        return register(new NetworkPacketType.Stream<>(channelId, name, direction, factory, inputFactory, outputFactory));
    }
    
    private <T extends NetworkPacketType<?, ?, ?>> T register(T packet) {
        if (isPacketRegistered(packet.getName())) {
            throw new IllegalArgumentException("A packet with id '" + packet.getName() + "' has already been registered for '" + channelId + "'.");
        }
        managers.computeIfAbsent(channelId, x -> {
            registerChannel(channelId, protocolVersion);
            return this;
        });
        packets.put(packet.getName(), packet);
        LOGGER.info("Registering {} network packet of type {} with id '{}' in '{}'.", packet.getDirection(), packet.getType(), packet.getName(), channelId);
        return packet;
    }

    public boolean isPacketRegistered(String name) {
        return packets.containsKey(name);
    }

    public Optional<NetworkPacketType<?, ?, ?>> getRegisteredPacket(String name, NetworkSide side) {
        if (!isPacketRegistered(name)) {
            return Optional.empty();
        }
        NetworkPacketType<?, ?, ?> type = packets.get(name);
        if (type == null || type.getDirection() != side) {
            return Optional.empty();
        }
        return Optional.of(type);
    }
    
    @ExpectPlatform
    public static void registerChannel(ResourceLocation channelId, String protocolVersion) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Packet<?> toPacket(ResourceLocation channelId, NetworkSide side, FriendlyByteBuf buffer) {
        throw new AssertionError();
    }    
        
    public static void receiveData(ResourceLocation channelId, FriendlyByteBuf buf, NetworkSide side, NetworkPacketContext context) {
        if (!managers.containsKey(channelId)) return;

        SegmentedPacketHeaderInfo header = SegmentedPacketHeaderInfo.readBufferHeader(buf);

        CommunicationType communication = header.type().communication();
        NetworkSide fSide;
        if (communication == CommunicationType.RESPONSE) {
            fSide = side;
        } else {
            fSide = side;
        }

        managers.get(channelId).getRegisteredPacket(header.type().name(), fSide).ifPresentOrElse(x -> {
            NetworkPacker.unpack(header, fSide, buf, context, (rawData) -> {
                CompoundTag nbt = rawData.readAnySizeNbt();
                x.receive(header.type(), context, nbt, communication);
                NetworkPacker.cleanUp(header, fSide);
            });
        }, () -> {
            LOGGER.warn("There is no {} packet registered with ID '{}' in '{}'.", fSide, header.type().name(), channelId);
            NetworkPacker.cleanUp(header, fSide);
        });
    }

}
