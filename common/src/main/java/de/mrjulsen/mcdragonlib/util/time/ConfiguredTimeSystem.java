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

package de.mrjulsen.mcdragonlib.util.time;

import java.util.List;

import de.mrjulsen.mcdragonlib.config.ModCommonConfig;

/**
 * Time system built from mod configuration values.
 *
 * <p>It uses configuration entries for ticks-per-day, default TPS and a daytime shift factor.
 * Exposes a singleton {@link #INSTANCE}.
 */
public class ConfiguredTimeSystem implements ITimeSystem {

    public static final ConfiguredTimeSystem INSTANCE = new ConfiguredTimeSystem();

    @Deprecated(forRemoval = true)
    public ConfiguredTimeSystem() {
    }
    
    @Override
    public long getTicksPerDay() {
        return ModCommonConfig.TIME_TICKS_PER_DAY.get();
    }

    @Override
    public List<TimeZone> getTimeZones() {
        return List.of(new TimeZone(0, getTicksPerDay(), ModCommonConfig.TIME_DEFAULT_TPS.get()));
    }

    @Override
    public double getDaytimeOffset() {
        return (double)getTicksPerDay() * ModCommonConfig.TIME_DAYTIME_SHIFT_FACTOR.get();
    }
}
