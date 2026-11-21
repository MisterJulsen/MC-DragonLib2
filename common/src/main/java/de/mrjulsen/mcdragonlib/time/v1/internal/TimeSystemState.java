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

package de.mrjulsen.mcdragonlib.time.v1.internal;

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.mcdragonlib.time.v1.TimeSystem;
import de.mrjulsen.mcdragonlib.time.v1.TimeSystems;

/**
 * The time systems that come from outside the code: the datapack one and the one a server sent.
 */
public final class TimeSystemState {

    private static volatile TimeSystem datapack;
    private static volatile TimeSystem synced;

    private TimeSystemState() {}

    @Nullable
    public static TimeSystem datapack() {
        return datapack;
    }

    @Nullable
    public static TimeSystem synced() {
        return synced;
    }

    static void setDatapack(@Nullable TimeSystem system) {
        datapack = system;
        TimeSystems.invalidate();
    }

    static void setSynced(@Nullable TimeSystem system) {
        synced = system;
    }
}
