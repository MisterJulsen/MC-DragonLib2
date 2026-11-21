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

package de.mrjulsen.mcdragonlib.internal;

import de.mrjulsen.mcdragonlib.network.v2.internal.ClientNetworkHooks;
import de.mrjulsen.mcdragonlib.network.NetworkDirection.C2S;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.Level;

public class ClientWrapper {

    public static Level getClientLevel() {
        return Minecraft.getInstance().level;
    }

    public static C2S toServer() {
        return new C2S() {
            @Override
            public void send(Packet<?> packet) {
                if (Minecraft.getInstance().getConnection() != null) {
                    Minecraft.getInstance().getConnection().send(packet);
                } else {
                    throw new IllegalStateException("Unable to send packet to the server while not in game!");
                }
            }

            @Override
            public Connection getConnection() {
                return ClientNetworkHooks.getServerConnection();
            }
        };
    }
}
