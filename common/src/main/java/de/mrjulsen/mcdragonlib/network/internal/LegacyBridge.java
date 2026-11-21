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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.DLStatus;
import de.mrjulsen.mcdragonlib.network.v2.ExecutionMode;
import de.mrjulsen.mcdragonlib.util.DependencyVersionChecker;
import de.mrjulsen.mcdragonlib.network.v2.DLChannel;
import de.mrjulsen.mcdragonlib.network.v2.DLNetwork;
import de.mrjulsen.mcdragonlib.network.v2.NetworkFlow;
import de.mrjulsen.mcdragonlib.network.v2.PacketContext;
import de.mrjulsen.mcdragonlib.network.v2.PacketDefinition;
import de.mrjulsen.mcdragonlib.network.v2.PacketDirection;
import de.mrjulsen.mcdragonlib.network.v2.codec.DLStreamCodec;
import de.mrjulsen.mcdragonlib.network.v2.transport.OutboundMessage;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.network.NetworkPacketContext;
import de.mrjulsen.mcdragonlib.network.NetworkPacketData;
import de.mrjulsen.mcdragonlib.network.NetworkSide;
import dev.architectury.utils.Env;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

@Deprecated
public final class LegacyBridge {

    private static final AtomicInteger FALLBACK_IDS = new AtomicInteger();
    private static final AtomicInteger FALLBACK_TRANSFERS = new AtomicInteger();

    /**
     * The first version that runs packet handlers off the game thread. Every 1.21 release of DragonLib 3 already did that,
     * so there is no mod that depends on the old behaviour and no version check is needed.
     */
    private static final String FIRST_ASYNC_HANDLER_VERSION = null;
    private static final Map<String, Boolean> MAIN_THREAD_HANDLERS = new ConcurrentHashMap<>();

    private LegacyBridge() {}

    public static ExecutionMode handlerExecutionMode(PacketDefinition definition) {
        String modid = definition.getChannel().getId().getNamespace();
        boolean mainThread = MAIN_THREAD_HANDLERS.computeIfAbsent(modid, LegacyBridge::requiresMainThreadHandlers);
        return mainThread ? ExecutionMode.MAIN : definition.getHandlerExecutionMode();
    }

    private static boolean requiresMainThreadHandlers(String modid) {
        if (FIRST_ASYNC_HANDLER_VERSION == null) {
            return false;
        }
        boolean result;
        try {
            result = DependencyVersionChecker.checkDependencies(modid.replace("wiresapi", "pantographsandwires"), DragonLib.MODID, FIRST_ASYNC_HANDLER_VERSION)
                .map(r -> r.relation() == DependencyVersionChecker.VersionRelation.IS_OLDER)
                .orElse(false);
        } catch (Exception e) {
            DLNetwork.LOGGER.warn("Could not determine which DragonLib version {} was built with. Assuming a current one.", modid, e);
            result = false;
        }
        if (result) {
            DLNetwork.LOGGER.warn("{} was built with an older version of DragonLib's networking system. For compatibility, its packet handlers run on the game thread.", modid);
        }
        return result;
    }

    public static <T extends NetworkPacketData> DLStreamCodec<T> dataCodec(Function<DLStatus, T> factory) {
        Objects.requireNonNull(factory, "factory");
        return DLStreamCodec.of(
            (buf, value) -> buf.writeNbt(value.serializeNbt()),
            buf -> {
                T value = Objects.requireNonNull(factory.apply(DLStatus.EMPTY), "packet data factory returned null");
                CompoundTag nbt = buf.readNbt();
                value.deserializeNbt(nbt == null ? new CompoundTag() : nbt);
                return value;
            }
        );
    }

    public static NetworkPacketContext adapt(PacketContext context) {
        return new NetworkPacketContext() {
            @Override
            public net.minecraft.world.entity.player.Player getPlayer() {
                return context.getPlayer();
            }

            @Override
            public void queue(Runnable runnable) {
                context.queue(runnable);
            }

            @Override
            public <O> O queueResult(Supplier<O> supplier) {
                CompletableFuture<O> future = new CompletableFuture<>();
                context.queue(() -> {
                    try {
                        future.complete(supplier.get());
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
                return future.join();
            }

            @Override
            public Env getEnvironment() {
                return context.getEnvironment();
            }
        };
    }

    public static PacketDirection direction(NetworkSide side) {
        return side == NetworkSide.C2S ? PacketDirection.TO_SERVER : PacketDirection.TO_CLIENT;
    }

    public static NetworkFlow flowOf(NetworkDirection sender) {
        return sender.getDirection() == NetworkSide.C2S ? NetworkFlow.SERVERBOUND : NetworkFlow.CLIENTBOUND;
    }

    public static int nextFallbackId() {
        return -FALLBACK_IDS.incrementAndGet();
    }

    public static void send(DLChannel channel, PacketDefinition definition, NetworkDirection sender, Consumer<FriendlyByteBuf> writer) {
        NetworkFlow flow = flowOf(sender);
        FriendlyByteBuf payload = new FriendlyByteBuf(Unpooled.buffer());
        try {
            writer.accept(payload);
            if (payload.readableBytes() > definition.getMaxMessageBytes()) {
                throw new IllegalStateException("Payload of " + definition + " is " + payload.readableBytes() + " bytes, which exceeds its limit of " + definition.getMaxMessageBytes() + ".");
            }
            Connection connection = sender.getConnection();
            if (connection != null) {
                channel.transmit(definition, flow, List.of(connection), payload);
            } else {
                sendDirect(channel, definition, sender, flow, payload);
            }
        } finally {
            payload.release();
        }
    }

    private static void sendDirect(DLChannel channel, PacketDefinition definition, NetworkDirection sender, NetworkFlow flow, FriendlyByteBuf payload) {
        OutboundMessage message = OutboundMessage.of(flow, FALLBACK_TRANSFERS.incrementAndGet(), definition.getName(), new FriendlyByteBuf(payload.retainedDuplicate()));
        try {
            while (message.hasNextFrame()) {
                sender.send(DLNetwork.toPacket(channel.getId(), flow, message.nextFrame()));
            }
        } finally {
            message.release();
        }
    }
}
