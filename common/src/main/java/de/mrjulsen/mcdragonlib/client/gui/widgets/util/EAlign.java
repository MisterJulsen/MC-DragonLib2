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

package de.mrjulsen.mcdragonlib.client.gui.widgets.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.IIterableEnum;
import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;

public enum EAlign implements BitflagEnum, ITranslatableEnum, IIterableEnum<EAlign> {
    TOP((byte)0, "top", 0b0001, AffectedTransformtion.POSITION, true),
    LEFT((byte)1,"left", 0b0010, AffectedTransformtion.POSITION, false),
    RIGHT((byte)3, "right", 0b0100, AffectedTransformtion.SIZE, false),
    BOTTOM((byte)2, "bottom", 0b1000, AffectedTransformtion.SIZE, true);

    private final byte order;
    private final String name;
    private final int bit;
    private final AffectedTransformtion transform;
    private final boolean isVertical;

    private static final ImmutableMap<Long, EAlign> valueMap;
    static {
        Map<Long, EAlign> map = new HashMap<>();
        for (EAlign value : values()) {
            map.put(value.getBit(), value);
        }
        valueMap = ImmutableMap.copyOf(map);
    }

    private EAlign(byte order, String name, int bit, AffectedTransformtion transform, boolean isVertical) {
        this.order = order;
        this.name = name;
        this.bit = bit;
        this.transform = transform;
        this.isVertical = isVertical;
    }

    public byte getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    public long getBit() {
        return bit;
    }

    public AffectedTransformtion getAffectedTransformation() {
        return transform;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Optional<EAlign> getByBit(long bit) {
        return Optional.ofNullable(valueMap.containsKey(bit) ? valueMap.get(bit) : null);
    }

    public boolean isVertical() {
        return isVertical;
    }

    public boolean isHorizontal() {
        return !isVertical();
    }

    public static enum AffectedTransformtion {
        NONE,
        POSITION,
        SIZE;
    }

    @Override
    public Data getTranslationData() {
        return new Data(DragonLib.MODID, "align", getName());
    }

    @Override
    public EAlign[] getValues() {
        return values();
    }
}
