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

package de.mrjulsen.mcdragonlib.client.gui.v2;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.BitflagEnum;
import de.mrjulsen.mcdragonlib.data.IIterableEnum;
import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;

/**
 * Edges a component sticks to when its parent is resized. Used as a bitflag set, see
 * {@link GuiComponent#anchor}.
 * <p>Anchoring both edges of an axis stretches the component on that axis, anchoring neither keeps
 * it at its relative center.</p>
 */
public enum Anchor implements BitflagEnum, ITranslatableEnum, IIterableEnum<Anchor> {
    TOP("top", 0b0001, true),
    LEFT("left", 0b0010, false),
    RIGHT("right", 0b0100, false),
    BOTTOM("bottom", 0b1000, true);

    private final String name;
    private final long bit;
    private final boolean vertical;

    private Anchor(String name, long bit, boolean vertical) {
        this.name = name;
        this.bit = bit;
        this.vertical = vertical;
    }

    public String getName() {
        return name;
    }

    @Override
    public long getBit() {
        return bit;
    }

    /**
     * Whether this anchor sits on the vertical axis, i.e. TOP or BOTTOM.
     */
    public boolean isVertical() {
        return vertical;
    }

    public boolean isHorizontal() {
        return !vertical;
    }

    @Override
    public Data getTranslationData() {
        return new Data(DragonLib.MODID, "anchor", name);
    }

    @Override
    public Anchor[] getValues() {
        return values();
    }

    @Override
    public String toString() {
        return name;
    }
}
