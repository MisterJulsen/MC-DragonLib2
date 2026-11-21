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

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @deprecated The current transport does not use this classification.
 */
@Deprecated
public enum CommunicationType {
    NONE((byte)0x0),
    REQUEST((byte)0x1),
    RESPONSE((byte)0x2);

    private final byte id;
    private static final Map<Byte, CommunicationType> TYPES_BY_ID = Arrays.stream(values()).collect(Collectors.toMap(x -> x.getId(), x -> x));

    private CommunicationType(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public static CommunicationType getById(byte id) {
        if (TYPES_BY_ID.containsKey(id)) {
            return TYPES_BY_ID.get(id);
        }
        return NONE;
    }
}
