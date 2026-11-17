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

package de.mrjulsen.mcdragonlib.util.time.format;

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

/**
 * Simple formatter that renders a duration as ticks (e.g. "123t").
 *
 * <p>When formatting in INGAME context an {@link ITimeSystem} is required to resolve ticks.
 * In REAL context the method converts real milliseconds to ticks using a fixed 50ms/tick.
 */
public class TimeFormatTicks implements ITimeFormatter {
    
    public static final TimeFormatTicks INSTANCE = new TimeFormatTicks();

    @Override
    public String format(DLTime time, TimeContext context, @Nullable ITimeSystem system) {
        if (system == null && context == TimeContext.INGAME) {
            throw new IllegalArgumentException("ITimeSystem required for ticks");
        }
        long ticks = (context == TimeContext.REAL) ? (long) (time.toRealMillis() / 50.0) : (long) time.toTicks(system);
        return ticks + "t";
    }
}
