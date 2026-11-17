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

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.DLTimeUnit;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

import org.jetbrains.annotations.Nullable;

/**
 * Formatter producing an ISO-8601 duration string in the form "P{d}DT{h}H{m}M{s}S".
 *
 * <p>For INGAME context the conversion uses the provided {@link ITimeSystem}.
 */
public class TimeFormatISO8601 implements ITimeFormatter {

    public static final TimeFormatISO8601 INSTANCE = new TimeFormatISO8601();

    @Override
    public String format(DLTime time, TimeContext context, @Nullable ITimeSystem system) {
        double totalSeconds = (context == TimeContext.REAL) ? time.toReal(DLTimeUnit.SECONDS) : time.toGameSeconds(system);
        long days = (long) (totalSeconds / 86400);
        long hours = (long) ((totalSeconds % 86400) / 3600);
        long minutes = (long) ((totalSeconds % 3600) / 60);
        long seconds = (long) (totalSeconds % 60);
        return String.format("P%dDT%dH%dM%dS", days, hours, minutes, seconds);
    }
}
