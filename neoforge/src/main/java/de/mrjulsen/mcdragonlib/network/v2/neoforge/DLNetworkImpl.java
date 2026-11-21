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

package de.mrjulsen.mcdragonlib.network.v2.neoforge;

import java.util.LinkedHashMap;
import java.util.Map;

import de.mrjulsen.mcdragonlib.network.v2.DLNetwork;
import de.mrjulsen.mcdragonlib.network.v2.NetworkFlow;
import de.mrjulsen.mcdragonlib.network.v2.PacketContext;
import de.mrjulsen.mcdragonlib.network.v2.internal.FramePayload;
import dev.architectury.utils.Env;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class DLNetworkImpl {

    /** Version of the payload registration. Channel versions are checked by the handshake of the networking layer. */
    private static final String PAYLOAD_VERSION = "1";

    private static final Map<ResourceLocation, CustomPacketPayload.Type<FramePayload>> CHANNELS = new LinkedHashMap<>();
    private static boolean payloadsRegistered = false;

    /**
     * Hooks into the payload registration of NeoForge. Must be called from the mod constructor.
     *
     * @param modBus the event bus of DragonLib
     */
    public static void init(IEventBus modBus) {
        modBus.addListener(DLNetworkImpl::onRegisterPayloads);
    }

    public static synchronized void registerChannel(ResourceLocation channelId, String protocolVersion) {
        if (payloadsRegistered) {
            DLNetwork.LOGGER.error("The channel '{}' was created after NeoForge registered all network payloads, so it cannot send or receive anything. Create channels while mods are constructed.", channelId);
            return;
        }
        CHANNELS.putIfAbsent(channelId, FramePayload.type(channelId));
    }

    private static synchronized void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        // Optional, so players can join servers that do not know a channel. The networking layer reports that itself.
        // Handled on the network thread like the event channels of older versions, the networking layer moves work to the game thread itself.
        PayloadRegistrar registrar = event.registrar(PAYLOAD_VERSION).optional().executesOn(HandlerThread.NETWORK);
        for (Map.Entry<ResourceLocation, CustomPacketPayload.Type<FramePayload>> entry : CHANNELS.entrySet()) {
            ResourceLocation channelId = entry.getKey();
            CustomPacketPayload.Type<FramePayload> type = entry.getValue();
            registrar.playBidirectional(type, FramePayload.codec(type), (payload, context) ->
                DLNetwork.receive(channelId, payload.frame(), context(context)));
        }
        payloadsRegistered = true;
    }

    public static Packet<?> toPacket(ResourceLocation channelId, NetworkFlow flow, FriendlyByteBuf frame) {
        FramePayload payload = new FramePayload(FramePayload.type(channelId), frame);
        return flow == NetworkFlow.SERVERBOUND ? new ServerboundCustomPayloadPacket(payload) : new ClientboundCustomPayloadPacket(payload);
    }

    private static PacketContext context(IPayloadContext source) {
        boolean client = source.flow() == PacketFlow.CLIENTBOUND;
        Connection connection = source.connection();
        return new PacketContext() {
            @Override
            public Env getEnvironment() {
                return client ? Env.CLIENT : Env.SERVER;
            }

            @Override
            public NetworkFlow getFlow() {
                return client ? NetworkFlow.CLIENTBOUND : NetworkFlow.SERVERBOUND;
            }

            @Override
            public Connection getConnection() {
                return connection;
            }

            @Override
            public Player getPlayer() {
                return source.player();
            }

            @Override
            public void queue(Runnable task) {
                source.enqueueWork(task);
            }
        };
    }
}
