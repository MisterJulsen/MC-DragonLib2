package de.mrjulsen.mcdragonlib.network.packet;

import java.util.concurrent.PriorityBlockingQueue;

import dev.architectury.networking.NetworkManager.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public class NetworkReceiverData {

    private final Key key;
    private final PriorityBlockingQueue<Chunk> chunks;

    public NetworkReceiverData(Key key, SegmentedPacketHeaderInfo info) {
        this.key = key;
        this.chunks = new PriorityBlockingQueue<>(info.expectedParts());
    }

    public final Key getKey() {
        return key;
    }

    public void add(Chunk chunk) {
        this.chunks.add(chunk);
    }

    public int chunksCount() {
        return chunks.size();
    }

    public FriendlyByteBuf mergeData() {
        ByteBuf[] buffers = new ByteBuf[chunksCount()];
        int i = 0;
        for (Chunk chunk : chunks) {
            buffers[i] = chunk.buffer;
            i++;
        }
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(buffers));
    }

    public void close() {
        while (!chunks.isEmpty()) {
            Chunk chunk = chunks.poll();
            chunk.close();
        }
    }


    public static class Chunk implements Comparable<Chunk> {

        private final int number;
        private final FriendlyByteBuf buffer;
        private boolean closed = false;

        public Chunk(int number, FriendlyByteBuf data) {
            this.number = number;
            this.buffer = data;
        }

        @Override
        public int compareTo(Chunk o) {
            return Integer.compare(number, o.number);
        }

        public void close() {
            if (closed) return;
            buffer.release();
            closed = true;
        }
    }
    
    public record Key(PacketHeaderInfo info, Side side) {}
}
