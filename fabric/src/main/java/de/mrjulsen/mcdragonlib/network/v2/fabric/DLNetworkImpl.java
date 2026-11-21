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

package de.mrjulsen.mcdragonlib.network.v2.fabric;

import de.mrjulsen.mcdragonlib.mixin.common.ServerCommonPacketListenerAccessor;
import de.mrjulsen.mcdragonlib.network.v2.DLNetwork;
import de.mrjulsen.mcdragonlib.network.v2.NetworkFlow;
import de.mrjulsen.mcdragonlib.network.v2.PacketContext;
import de.mrjulsen.mcdragonlib.network.v2.internal.FramePayload;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class DLNetworkImpl {

    public static void registerChannel(ResourceLocation channelId, String protocolVersion) {
        CustomPacketPayload.Type<FramePayload> type = FramePayload.type(channelId);
        StreamCodec<FriendlyByteBuf, FramePayload> codec = FramePayload.codec(type);
        PayloadTypeRegistry.playC2S().register(type, codec);
        PayloadTypeRegistry.playS2C().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
            DLNetwork.receive(channelId, payload.frame(), serverContext(context.server(), context.player())));
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> ClientNetworkImpl.registerChannel(channelId));
    }

    public static Packet<?> toPacket(ResourceLocation channelId, NetworkFlow flow, FriendlyByteBuf frame) {
        FramePayload payload = new FramePayload(FramePayload.type(channelId), frame);
        if (flow == NetworkFlow.SERVERBOUND) {
            return toServerboundPacket(payload);
        }
        return ServerPlayNetworking.createS2CPacket(payload);
    }

    @Environment(EnvType.CLIENT)
    private static Packet<?> toServerboundPacket(FramePayload payload) {
        return ClientPlayNetworking.createC2SPacket(payload);
    }

    static PacketContext serverContext(MinecraftServer server, ServerPlayer player) {
        Connection connection = ((ServerCommonPacketListenerAccessor) player.connection).dragonlib$getConnection();
        return new PacketContext() {
            @Override
            public Env getEnvironment() {
                return Env.SERVER;
            }

            @Override
            public NetworkFlow getFlow() {
                return NetworkFlow.SERVERBOUND;
            }

            @Override
            public Connection getConnection() {
                return connection;
            }

            @Override
            public Player getPlayer() {
                return player;
            }

            @Override
            public void queue(Runnable task) {
                server.execute(task);
            }
        };
    }
}
