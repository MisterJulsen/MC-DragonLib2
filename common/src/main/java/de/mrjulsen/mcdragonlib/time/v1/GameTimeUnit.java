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

package de.mrjulsen.mcdragonlib.time.v1;

import java.util.concurrent.TimeUnit;

/**
 * Units of game time. Apart from {@link #TICKS} they are clock units: a game day always has 24 game
 * hours, so they have a fixed length in game milliseconds, while the number of ticks they span depends
 * on the length of the day in the {@link TimeSystem}.
 * <p>For real time use {@link java.time.Duration} instead.</p>
 */
public enum GameTimeUnit {
    TICKS(0),
    MILLIS(TimeUnit.MILLISECONDS.toMillis(1)),
    SECONDS(TimeUnit.SECONDS.toMillis(1)),
    MINUTES(TimeUnit.MINUTES.toMillis(1)),
    HOURS(TimeUnit.HOURS.toMillis(1)),
    DAYS(TimeUnit.DAYS.toMillis(1));

    private final long millis;

    GameTimeUnit(long millis) {
        this.millis = millis;
    }

    /**
     * Whether this is a clock unit, i.e. anything but {@link #TICKS}.
     */
    public boolean isClockUnit() {
        return this != TICKS;
    }

    /**
     * How many game milliseconds one of this unit spans. Independent of the time system, except for
     * {@link #TICKS}, which needs {@link #millis(TimeSystem)}.
     *
     * @throws UnsupportedOperationException for {@link #TICKS}
     */
    public long millis() {
        if (this == TICKS) {
            throw new UnsupportedOperationException("The length of a tick in game milliseconds depends on the time system.");
        }
        return millis;
    }

    /**
     * How many game milliseconds one of this unit spans in the given time system. In vanilla a tick is
     * 3600 game milliseconds.
     */
    public double millis(TimeSystem system) {
        return this == TICKS ? (double)DAYS.millis / system.ticksPerDay() : millis;
    }

    /**
     * How many ticks one of this unit spans in the given time system. In vanilla a game hour is 1000
     * ticks and a game second only 0.2778 ticks.
     */
    public double ticks(TimeSystem system) {
        return this == TICKS ? 1D : millis * (double)system.ticksPerDay() / DAYS.millis;
    }
}
