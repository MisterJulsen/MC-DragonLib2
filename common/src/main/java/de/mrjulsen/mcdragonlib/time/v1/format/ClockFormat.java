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

package de.mrjulsen.mcdragonlib.time.v1.format;

import java.util.Locale;

import de.mrjulsen.mcdragonlib.time.v1.TimeOfDay;

/**
 * Turns a clock reading into text. Use one of the constants or write your own.
 */
@FunctionalInterface
public interface ClockFormat {

    String format(TimeOfDay time);

    /** {@code 14:05} */
    ClockFormat H24 = t -> String.format(Locale.ROOT, "%02d:%02d", t.getHour(), t.getMinute());
    /** {@code 14:05:09} */
    ClockFormat H24_SECONDS = t -> String.format(Locale.ROOT, "%02d:%02d:%02d", t.getHour(), t.getMinute(), t.getSecond());
    /** {@code 14:05:09.250} */
    ClockFormat H24_MILLIS = t -> String.format(Locale.ROOT, "%02d:%02d:%02d.%03d", t.getHour(), t.getMinute(), t.getSecond(), t.getMillis());
    /** {@code 2:05 PM} */
    ClockFormat H12 = t -> String.format(Locale.ROOT, "%d:%02d %s", hour12(t), t.getMinute(), period(t));
    /** {@code 2:05:09 PM} */
    ClockFormat H12_SECONDS = t -> String.format(Locale.ROOT, "%d:%02d:%02d %s", hour12(t), t.getMinute(), t.getSecond(), period(t));

    private static int hour12(TimeOfDay t) {
        int hour = t.getHour() % 12;
        return hour == 0 ? 12 : hour;
    }

    private static String period(TimeOfDay t) {
        return t.getHour() < 12 ? "AM" : "PM";
    }
}
