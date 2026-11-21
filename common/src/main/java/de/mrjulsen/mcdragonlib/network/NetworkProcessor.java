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

package de.mrjulsen.mcdragonlib.network;

import java.util.Optional;

/**
 * @deprecated Use the handler interfaces of the packet types in {@code de.mrjulsen.mcdragonlib.net.packet}.
 */
@Deprecated
public final class NetworkProcessor {
    private NetworkProcessor() {}
    
    @FunctionalInterface
    public interface Send<I extends NetworkPacketData> {
        void execute(I data, NetworkPacketContext context);
    }
    
    @FunctionalInterface
    public interface Receive<O extends NetworkPacketData> {
        O execute(NetworkPacketContext context);
    }
        
    @FunctionalInterface
    public interface SendAndReceive<I extends NetworkPacketData, O extends NetworkPacketData> {
        O execute(I data, NetworkPacketContext context);
    }    
        
    @FunctionalInterface
    public interface StreamProvider<I extends NetworkPacketData, O extends NetworkPacketData> {
        /**
         * 
         * @param data is null on the first run
         */
        I execute(boolean initialCall, Optional<O> data, Optional<NetworkPacketContext> context);
    }

    @FunctionalInterface
    public interface StreamReceiver<I extends NetworkPacketData, O extends NetworkPacketData> {
        O execute(I data, NetworkPacketContext context);
    }
}
