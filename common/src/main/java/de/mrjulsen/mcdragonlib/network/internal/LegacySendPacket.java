/*
 * DragonLib 3 - 1.20.1
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

package de.mrjulsen.mcdragonlib.network.internal;

import java.util.Objects;
import java.util.function.Function;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.v2.DLNetwork;
import de.mrjulsen.mcdragonlib.network.v2.PacketContext;
import de.mrjulsen.mcdragonlib.network.v2.PacketDefinition;
import de.mrjulsen.mcdragonlib.network.v2.codec.DLStreamCodec;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.network.NetworkProcessor;
import net.minecraft.network.FriendlyByteBuf;

@Deprecated
public final class LegacySendPacket<I extends NetworkPacketData> extends PacketDefinition {

    private final DLStreamCodec<I> codec;
    private final NetworkProcessor.Send<I> processor;

    public LegacySendPacket(Settings settings, Function<DLStatus, I> factory, NetworkProcessor.Send<I> processor) {
        super(settings);
        this.codec = LegacyBridge.dataCodec(factory);
        this.processor = Objects.requireNonNull(processor, "processor");
    }

    public void send(NetworkDirection sender, I data) {
        LegacyBridge.send(getChannel(), this, sender, buf -> codec.encode(buf, data));
    }

    @Override
    protected void receive(PacketContext context, FriendlyByteBuf payload) {
        I data;
        try {
            data = codec.decode(payload);
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not decode {}.", this, e);
            return;
        }
        LegacyBridge.handlerExecutionMode(this).run(context, () -> {
            try {
                processor.execute(data, LegacyBridge.adapt(context));
            } catch (Exception e) {
                DLNetwork.LOGGER.error("Handler of {} failed.", this, e);
            }
        });
    }
}
