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

package de.mrjulsen.mcdragonlib.time.v1;

/**
 * A section of the day in which time runs at one constant speed.
 * <p>A {@link TimeSystem} splits its day into sections, for example a long day and a short night. Within
 * a section, {@code ticksPerSecond} day ticks pass per real second. Vanilla runs at 20 the whole day.</p>
 *
 * @param startTick first tick of the section within the day (inclusive)
 * @param endTick end of the section within the day (exclusive)
 * @param ticksPerSecond how many ticks pass per real second. Must be positive
 */
public record TimeSection(long startTick, long endTick, double ticksPerSecond) implements Comparable<TimeSection> {

    public TimeSection {
        if (startTick < 0) {
            throw new IllegalArgumentException("The start tick must not be negative: " + startTick);
        }
        if (endTick <= startTick) {
            throw new IllegalArgumentException("The end tick must be greater than the start tick: " + startTick + " - " + endTick);
        }
        if (ticksPerSecond <= 0 || Double.isInfinite(ticksPerSecond)) {
            throw new IllegalArgumentException("The speed must be a positive, finite number: " + ticksPerSecond);
        }
    }

    /**
     * Number of ticks this section spans.
     */
    public long lengthTicks() {
        return endTick - startTick;
    }

    /**
     * Real milliseconds one tick takes in this section.
     */
    public double millisPerTick() {
        return TimeSystem.MILLIS_PER_SECOND / ticksPerSecond;
    }

    /**
     * Real milliseconds the whole section takes.
     */
    public double lengthMillis() {
        return lengthTicks() * millisPerTick();
    }

    /**
     * Speed relative to vanilla: 1 is vanilla speed, 2 twice as fast, 0.5 half as fast and so on.
     */
    public double speed() {
        return ticksPerSecond / TimeSystem.VANILLA_TICKS_PER_SECOND;
    }

    /**
     * Whether the given tick within the day belongs to this section.
     */
    public boolean contains(double tickInDay) {
        return tickInDay >= startTick && tickInDay < endTick;
    }

    @Override
    public int compareTo(TimeSection o) {
        return Long.compare(startTick, o.startTick);
    }
}
