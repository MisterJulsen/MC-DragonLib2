package de.mrjulsen.mcdragonlib.network.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import de.mrjulsen.mcdragonlib.network.DLNetworkManager;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkSide;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class NetworkPacker {

    public static List<Packet<?>> pack(ResourceLocation channelId, String name, PacketHeaderInfo info, NetworkSide side, CompoundTag rawData) {
        final int headerSize = SegmentedPacketHeaderInfo.getSize(info);
        
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeNbt(rawData);

        int maxSize = (side == NetworkSide.C2S ? 32767 : 1048576) - 20 - headerSize;        
        int partSize = maxSize - 4;
        int parts = (int)Math.ceil(buf.readableBytes() / (float) partSize);
        List<Packet<?>> packets = new ArrayList<>(parts);

        if (buf.readableBytes() <= maxSize) {
            FriendlyByteBuf stateBuf = new FriendlyByteBuf(Unpooled.buffer(1));
            SegmentedPacketHeaderInfo header = new SegmentedPacketHeaderInfo(info, SegmentType.SINGLE, parts, 0);
            header.writeBufferHeader(stateBuf);
            FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(stateBuf, buf));
            
            packets.add(DLNetworkManager.toPacket(channelId, name, side, packetBuffer));
        } else {
            for (int i = 0; i < parts; i++) {
                FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
                SegmentedPacketHeaderInfo header;
                if (i == 0) {
                    header = new SegmentedPacketHeaderInfo(info, SegmentType.START, parts, i);
                } else if (i == parts - 1) {
                    header = new SegmentedPacketHeaderInfo(info, SegmentType.END, parts, i);
                } else {
                    header = new SegmentedPacketHeaderInfo(info, SegmentType.PART, parts, i);
                }
                header.writeBufferHeader(packetBuffer);
                int next = Math.min(buf.readableBytes(), partSize);
                packetBuffer.writeBytes(buf.retainedSlice(buf.readerIndex(), next));
                buf.skipBytes(next);
                
                packets.add(DLNetworkManager.toPacket(channelId, name, side, packetBuffer));
            }
            buf.retain();
        }
        
        return packets;
    }


    private static final Map<NetworkReceiverData.Key, NetworkReceiverData> chunks = new ConcurrentHashMap<>();

    public static void cleanUp(SegmentedPacketHeaderInfo info, NetworkSide side) {
        NetworkReceiverData.Key key = new NetworkReceiverData.Key(info.type(), side);
        NetworkReceiverData  data = chunks.remove(key);
        if (data != null) {
            data.close();
        }
    }

    public static void unpack(SegmentedPacketHeaderInfo info, NetworkSide side, FriendlyByteBuf buf, NetworkPacketContext context, Consumer<FriendlyByteBuf> finishCallback) {
        NetworkReceiverData.Key key = new NetworkReceiverData.Key(info.type(), side);
        NetworkReceiverData data = null;
        switch (info.segment()) {
            case START:
                data = new NetworkReceiverData(key, info);
                if (chunks.put(key, data) != null) {
                    DLNetworkManager.LOGGER.warn("Received invalid START packet for SplitPacketTransformer with packet id " + key.info().name() + " for side " + side);
                }
                buf.retain();
                data.add(new NetworkReceiverData.Chunk(info.partNumber(), buf));
                break;
            case PART:
                if ((data = chunks.get(key)) == null) {
                    DLNetworkManager.LOGGER.warn("Received invalid PART packet for SplitPacketTransformer with packet id " + key.info().name() + " for side " + side);
                } else if (!data.getKey().equals(key)) {
                    DLNetworkManager.LOGGER.warn("Received invalid PART packet for SplitPacketTransformer with packet id " + key.info().name() + " for side " + side + ", id in cache is " + data.getKey().info().name());
                    data.close();
                    chunks.remove(key);
                } else {
                    buf.retain();
                    data.add(new NetworkReceiverData.Chunk(info.partNumber(), buf));
                }
                break;
            case END:
                if ((data = chunks.get(key)) == null) {
                    DLNetworkManager.LOGGER.warn("Received invalid END packet for SplitPacketTransformer with packet id " + key.info().name() + " for side " + side);
                } else if (!data.getKey().equals(key)) {
                    DLNetworkManager.LOGGER.warn("Received invalid END packet for SplitPacketTransformer with packet id " + key.info().name() + " for side " + side + ", id in cache is " + data.getKey().info().name());
                } else {
                    buf.retain();
                    data.add(new NetworkReceiverData.Chunk(info.partNumber(), buf));
                }
                if (data.chunksCount() != info.expectedParts()) {
                    DLNetworkManager.LOGGER.warn("Received invalid END packet for SplitPacketTransformer with packet id " + key.info().name() + " for side " + side + " with size " + data.chunksCount() + ", parts expected is " + info.expectedParts());
                } else {
                    FriendlyByteBuf byteBuf = data.mergeData();
                    finishCallback.accept(byteBuf);
                }
                data.close();
                chunks.remove(key);
                break;
            case SINGLE:
                finishCallback.accept(buf);
                break;
            default:
                throw new IllegalStateException("Illegal split packet header!");
        }
    }
    
}
