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

package de.mrjulsen.mcdragonlib.network.internal;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.v2.ChannelSession;
import de.mrjulsen.mcdragonlib.network.v2.DLNetwork;
import de.mrjulsen.mcdragonlib.network.v2.NetworkError;
import de.mrjulsen.mcdragonlib.network.v2.PacketContext;
import de.mrjulsen.mcdragonlib.network.v2.PacketDefinition;
import de.mrjulsen.mcdragonlib.network.v2.SessionResource;
import de.mrjulsen.mcdragonlib.network.v2.codec.DLStreamCodec;
import de.mrjulsen.mcdragonlib.network.v2.stream.StreamPacket;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.network.NetworkProcessor.StreamProvider;
import de.mrjulsen.mcdragonlib.network.NetworkProcessor.StreamReceiver;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;

@Deprecated
public final class LegacyStreamPacket<I extends NetworkPacketData, O extends NetworkPacketData> extends PacketDefinition {

    private static final byte KIND_REQUEST = 0;
    private static final byte KIND_RESPONSE = 1;

    private record Outgoing<I extends NetworkPacketData, O extends NetworkPacketData>(StreamProvider<I, O> provider, Consumer<DLStatus> callback, NetworkDirection sender) {}

    private final DLStreamCodec<I> inputCodec;
    private final DLStreamCodec<O> outputCodec;
    private final Function<DLStatus, I> inputFactory;
    private final Function<DLStatus, O> outputFactory;
    private final Supplier<StreamReceiver<I, O>> receiverFactory;

    private final States fallback = new States();

    public LegacyStreamPacket(Settings settings, Function<DLStatus, I> inputFactory, Function<DLStatus, O> outputFactory, Supplier<StreamReceiver<I, O>> receiverFactory) {
        super(settings);
        this.inputCodec = LegacyBridge.dataCodec(inputFactory);
        this.outputCodec = LegacyBridge.dataCodec(outputFactory);
        this.inputFactory = Objects.requireNonNull(inputFactory, "inputFactory");
        this.outputFactory = Objects.requireNonNull(outputFactory, "outputFactory");
        this.receiverFactory = Objects.requireNonNull(receiverFactory, "receiverFactory");
    }

    public int getFallbackCount() {
        return fallback.outgoing.size() + fallback.incoming.size();
    }

    public void send(NetworkDirection sender, StreamProvider<I, O> provider, Consumer<DLStatus> finishCallback) {
        Connection connection = sender.getConnection();
        States states = states(connection);
        int correlationId = connection == null ? LegacyBridge.nextFallbackId() : getChannel().getSession(connection).nextCorrelationId();
        states.outgoing.put(correlationId, new Outgoing<>(provider, finishCallback, sender));

        I first;
        try {
            first = provider.execute(true, Optional.empty(), Optional.empty());
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not produce the first value of {}.", this, e);
            states.outgoing.remove(correlationId);
            finishCallback.accept(DLStatus.error(e));
            return;
        }
        sendChunk(sender, correlationId, KIND_REQUEST, buf -> inputCodec.encode(buf, first));
    }

    @Override
    protected void receive(PacketContext context, FriendlyByteBuf payload) {
        byte kind = payload.readByte();
        int correlationId = payload.readVarInt();
        if (kind == KIND_REQUEST) {
            receiveRequest(context, correlationId, payload);
        } else {
            receiveResponse(context, correlationId, payload);
        }
    }

    private void receiveRequest(PacketContext context, int correlationId, FriendlyByteBuf payload) {
        States states = states(context.getConnection());
        I in;
        try {
            in = inputCodec.decode(payload);
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not decode a chunk of {}.", this, e);
            states.incoming.remove(correlationId);
            respond(context, correlationId, outputFactory.apply(DLStatus.error(e)));
            return;
        }

        NetworkPacketContext legacyContext = LegacyBridge.adapt(context);
        LegacyBridge.handlerExecutionMode(this).run(context, () -> {
            O out;
            try {
                StreamReceiver<I, O> receiver = states.incoming.computeIfAbsent(correlationId, ignored -> receiverFactory.get());
                out = receiver.execute(in, legacyContext);
                if (isFinal(in.getStatus())) {
                    states.incoming.remove(correlationId);
                }
            } catch (Exception e) {
                DLNetwork.LOGGER.error("Receiver of {} failed.", this, e);
                states.incoming.remove(correlationId);
                out = outputFactory.apply(DLStatus.error(e));
            }
            respond(context, correlationId, out);
        });
    }

    private void receiveResponse(PacketContext context, int correlationId, FriendlyByteBuf payload) {
        States states = states(context.getConnection());
        Outgoing<I, O> outgoing = states.outgoing.get(correlationId);
        if (outgoing == null) {
            DLNetwork.LOGGER.debug("Discarding a chunk for a stream of {} that is no longer open.", this);
            return;
        }

        O in;
        try {
            in = outputCodec.decode(payload);
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not decode a chunk of {}.", this, e);
            states.outgoing.remove(correlationId);
            outgoing.callback().accept(DLStatus.error(e));
            return;
        }

        NetworkPacketContext legacyContext = LegacyBridge.adapt(context);
        LegacyBridge.handlerExecutionMode(this).run(context, () -> {
            I next;
            try {
                next = outgoing.provider().execute(false, Optional.of(in), Optional.of(legacyContext));
            } catch (Exception e) {
                DLNetwork.LOGGER.error("Provider of {} failed.", this, e);
                states.outgoing.remove(correlationId);
                sendChunk(outgoing.sender(), correlationId, KIND_REQUEST, buf -> inputCodec.encode(buf, inputFactory.apply(DLStatus.error(e))));
                outgoing.callback().accept(DLStatus.error(e));
                return;
            }

            if (!in.getStatus().isDone()) {
                sendChunk(outgoing.sender(), correlationId, KIND_REQUEST, buf -> inputCodec.encode(buf, next));
            }
            if (isFinal(in.getStatus())) {
                states.outgoing.remove(correlationId);
                outgoing.callback().accept(next.getStatus());
            }
        });
    }

    private void respond(PacketContext context, int correlationId, O result) {
        try {
            dispatch(context.reply(), buf -> {
                buf.writeByte(KIND_RESPONSE);
                buf.writeVarInt(correlationId);
                outputCodec.encode(buf, result);
            });
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not send a chunk of {}.", this, e);
        }
    }

    private void sendChunk(NetworkDirection sender, int correlationId, byte kind, Consumer<FriendlyByteBuf> writer) {
        try {
            LegacyBridge.send(getChannel(), this, sender, buf -> {
                buf.writeByte(kind);
                buf.writeVarInt(correlationId);
                writer.accept(buf);
            });
        } catch (Exception e) {
            DLNetwork.LOGGER.error("Could not send a chunk of {}.", this, e);
        }
    }

    private static boolean isFinal(DLStatus status) {
        return status.isDone() || status.isError() || status.isCancel();
    }

    private States states(Connection connection) {
        if (connection == null) {
            return fallback;
        }
        ChannelSession session = getChannel().getSession(connection);
        return session.getResource(this, States::new);
    }

    private final class States implements SessionResource {

        private final Map<Integer, Outgoing<I, O>> outgoing = new ConcurrentHashMap<>();
        private final Map<Integer, StreamReceiver<I, O>> incoming = new ConcurrentHashMap<>();

        @Override
        public void close(NetworkError.Reason reason) {
            for (Outgoing<I, O> open : outgoing.values()) {
                try {
                    open.callback().accept(DLStatus.error(new IllegalStateException(reason.toString())));
                } catch (Exception e) {
                    DLNetwork.LOGGER.error("Callback of {} failed while closing.", LegacyStreamPacket.this, e);
                }
            }
            outgoing.clear();
            incoming.clear();
        }
    }
}
