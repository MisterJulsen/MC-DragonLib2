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

package de.mrjulsen.mcdragonlib.util.time;

import java.util.List;

/**
 * Default time system implementing the vanilla Minecraft behaviour: a single zone spanning the whole day at 20 TPS.
 *
 * <p>Provides a singleton {@link #INSTANCE} for consumers that want the standard mapping.
 */
public final class VanillaTimeSystem implements ITimeSystem {

    public static final VanillaTimeSystem INSTANCE = new VanillaTimeSystem();

    private static final List<TimeZone> VANILLA_ZONES = List.of(
        new TimeZone(0, 24000L, 20D)
    );

    private VanillaTimeSystem() {
    }
    
    @Override
    public long getTicksPerDay() {
        return 24000L;
    }

    @Override
    public List<TimeZone> getTimeZones() {
        return VANILLA_ZONES;
    }
}
