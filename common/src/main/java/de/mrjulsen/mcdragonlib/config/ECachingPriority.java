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

package de.mrjulsen.mcdragonlib.config;

import java.util.Arrays;

import net.minecraft.util.StringRepresentable;

/** The importance of caching data. */
public enum ECachingPriority implements StringRepresentable {
    /** Must always be cached, regardless of the settings the user has made. */
    ALWAYS(1, "always"),
    NORMAL(0, "normal"),
    LOW(-1, "low"),
    LOWEST(-2, "lowest");

    int idx;
    String name;

    ECachingPriority(int idx, String name) {
        this.idx = idx;
        this.name = name;
    }

    public int getIndex() {
        return idx;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static ECachingPriority getByIndex(int index) {
        return Arrays.stream(values()).filter(x -> x.getIndex() == index).findFirst().orElse(NORMAL);
    }

    public boolean shouldCache() {
        return getIndex() >= LOWEST.getIndex() + ModCommonConfig.CACHING.get().getIndex();
    }
}
