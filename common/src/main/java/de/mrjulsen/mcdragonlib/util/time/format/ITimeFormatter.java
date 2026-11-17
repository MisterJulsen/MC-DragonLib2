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
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

/**
 * Interface for formatting DLTime instances into strings.
 *
 * <p>Implementations must produce a textual representation for a given {@link DLTime}
 * and a {@link TimeContext}. For INGAME context a valid {@link ITimeSystem} should be supplied.
 */
public interface ITimeFormatter {
    /**
     * Format a DLTime for the given context and time system.
     *
     * @param time the DLTime to format
     * @param context REAL or INGAME interpretation
     * @param system ITimeSystem used when formatting INGAME values (may be null for REAL)
     * @return formatted string
     */
    String format(DLTime time, TimeContext context, ITimeSystem system);
}
