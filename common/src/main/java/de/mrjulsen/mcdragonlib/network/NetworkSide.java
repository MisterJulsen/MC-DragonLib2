package de.mrjulsen.mcdragonlib.network;

import de.mrjulsen.mcdragonlib.network.v2.NetworkFlow;
import de.mrjulsen.mcdragonlib.network.v2.PacketDirection;

/**
 * @deprecated Use {@link NetworkFlow} or {@link PacketDirection}.
 */
@Deprecated
public enum NetworkSide {
    S2C,
    C2S
}