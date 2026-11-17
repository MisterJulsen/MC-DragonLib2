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

package de.mrjulsen.mcdragonlib.network.v2.fabric;

import de.mrjulsen.mcdragonlib.network.v2.DLNetwork;
import de.mrjulsen.mcdragonlib.network.v2.NetworkFlow;
import de.mrjulsen.mcdragonlib.network.v2.PacketContext;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class ClientNetworkImpl {

    public static void registerChannel(ResourceLocation channelId) {
        ClientPlayNetworking.registerGlobalReceiver(channelId, (client, handler, buf, sender) ->
            DLNetwork.receive(channelId, buf, clientContext(client, handler)));
    }

    static PacketContext clientContext(Minecraft client, ClientPacketListener handler) {
        Connection connection = handler.getConnection();
        return new PacketContext() {
            @Override
            public Env getEnvironment() {
                return Env.CLIENT;
            }

            @Override
            public NetworkFlow getFlow() {
                return NetworkFlow.CLIENTBOUND;
            }

            @Override
            public Connection getConnection() {
                return connection;
            }

            @Override
            public Player getPlayer() {
                return client.player;
            }

            @Override
            public void queue(Runnable task) {
                client.execute(task);
            }
        };
    }
}
