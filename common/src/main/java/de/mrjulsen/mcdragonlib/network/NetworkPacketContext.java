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

package de.mrjulsen.mcdragonlib.network;

import de.mrjulsen.mcdragonlib.network.v2.PacketContext;
import dev.architectury.utils.Env;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

/**
 * @deprecated Use {@link PacketContext}.
 */
@Deprecated
public interface NetworkPacketContext {
    Player getPlayer();    
    void queue(Runnable runnable);    
    <O> O queueResult(Supplier<O> runnable);
    Env getEnvironment();

    default NetworkDirection buildDirection() {
        return getEnvironment() == Env.CLIENT ? NetworkDirection.toServer() : NetworkDirection.toPlayer((ServerPlayer)getPlayer());
    }
}