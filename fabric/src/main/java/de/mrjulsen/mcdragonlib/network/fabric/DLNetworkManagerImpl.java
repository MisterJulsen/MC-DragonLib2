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

package de.mrjulsen.mcdragonlib.network.fabric;

import de.mrjulsen.mcdragonlib.network.v2.NetworkFlow;
import de.mrjulsen.mcdragonlib.network.v2.fabric.DLNetworkImpl;
import de.mrjulsen.mcdragonlib.network.NetworkSide;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

/**
 * @deprecated Kept so the deprecated manager keeps linking. Delegates to {@link DLNetworkImpl}.
 */
@Deprecated
public class DLNetworkManagerImpl {

    public static void registerChannel(ResourceLocation channelId, String protocolVersion) {
        DLNetworkImpl.registerChannel(channelId, protocolVersion);
    }

    public static Packet<?> toPacket(ResourceLocation channelId, NetworkSide side, FriendlyByteBuf buf) {
        return DLNetworkImpl.toPacket(channelId, side == NetworkSide.C2S ? NetworkFlow.SERVERBOUND : NetworkFlow.CLIENTBOUND, buf);
    }
}
