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

package de.mrjulsen.mcdragonlib.network.packet;

import de.mrjulsen.mcdragonlib.network.v2.transport.FrameType;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @deprecated Replaced by {@link FrameType}.
 */
@Deprecated
public enum SegmentType {
    SINGLE((byte)0x0),
    START((byte)0x1),
    PART((byte)0x2),
    END((byte)0x3);

    private final byte id;
    private static final Map<Byte, SegmentType> TYPES_BY_ID = Arrays.stream(values()).collect(Collectors.toMap(x -> x.getId(), x -> x));

    private SegmentType(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public static SegmentType getById(byte id) {
        if (TYPES_BY_ID.containsKey(id)) {
            return TYPES_BY_ID.get(id);
        }
        return SINGLE;
    }
}
