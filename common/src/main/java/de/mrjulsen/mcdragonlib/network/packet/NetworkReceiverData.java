/*
 * DragonLib 3 - 1.21.1
 * Copyright (c) 2026 MrJulsen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.mrjulsen.mcdragonlib.network.packet;

import java.util.concurrent.PriorityBlockingQueue;

import de.mrjulsen.mcdragonlib.network.v2.transport.MessageAssembler;
import de.mrjulsen.mcdragonlib.network.NetworkSide;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @deprecated Reassembly state lives in {@link MessageAssembler}.
 */
@Deprecated
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
    
    public record Key(PacketHeaderInfo info, NetworkSide side) {}
}
