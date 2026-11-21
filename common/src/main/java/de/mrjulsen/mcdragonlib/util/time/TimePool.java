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

import org.jetbrains.annotations.Nullable;

/**
 * Mutable container representing a remaining budget of time stored as real milliseconds.
 *
 * <p>TimePool is initialized either from a {@link DLTime} or directly with a real-millisecond value.
 * The primary operations allow querying the remaining amount and extracting whole units
 * (either real-world units or game units via an {@link ITimeSystem}). Extraction removes the
 * extracted amount from the remaining budget.
 *
 * <p>Note: TimePool is not thread-safe.
 */
public final class TimePool {

    private double remainingMillis;

    /**
     * Initialize the pool from a {@link DLTime} instance by using its real-millisecond representation.
     *
     * @param time the starting time used to seed the pool; must not be null
     */
    public TimePool(DLTime time) {
        this.remainingMillis = time.toRealMillis();
    }

    /**
     * Initialize the pool with a raw real-millisecond amount.
     *
     * @param realMillis initial amount in real milliseconds
     */
    public TimePool(double realMillis) {
        this.remainingMillis = realMillis;
    }

    /**
     * Return the remaining amount expressed in real milliseconds.
     *
     * @return remaining milliseconds (double)
     */
    public double remainingRealMillis() {
        return remainingMillis;
    }

    /**
     * Check whether the pool is empty (no remaining milliseconds or negative).
     *
     * @return true if remaining millis {@code <= 0}
     */
    public boolean isEmpty() {
        return remainingMillis <= 0.0;
    }

    /**
     * Extract a whole-count of {@code unit} values from the remaining pool, depending on {@code context}.
     *
     * <p>If {@code context} is {@link TimeContext#REAL} the method uses {@link DLTimeUnit#millis}.
     * If {@code context} is {@link TimeContext#INGAME} the method requires an {@link ITimeSystem} to
     * convert the unit to real milliseconds and then computes how many whole units fit into the remaining budget.
     *
     * <p>The pool is mutated by subtracting the extracted amount.
     *
     * @param unit the unit to extract (e.g. MILLIS, SECONDS, TICKS)
     * @param context whether to treat the extraction as real or in-game
     * @param system the time system required for INGAME extraction (may be null for REAL)
     * @return the number of whole units extracted (long)
     * @throws IllegalArgumentException if context is INGAME and {@code system} is null
     */
    public long extract(DLTimeUnit unit, TimeContext context, @Nullable ITimeSystem system) {
        double unitMillis;
        if (context == TimeContext.REAL) {
            unitMillis = unit.millis;
        } else {
            requireSystem(system);
            double ticks = unit.getTicks(system);
            unitMillis = system.getRealMillisFromTicks(ticks, 0);
        }

        long count = (long) (remainingMillis / unitMillis);
        remainingMillis -= count * unitMillis;
        return count;
    }

    /**
     * Convenience extraction methods for common real-world units.
     */
    public long extractMillis() {
        return extract(DLTimeUnit.MILLIS, TimeContext.REAL, null);
    }

    public long extractSeconds() {
        return extract(DLTimeUnit.SECONDS, TimeContext.REAL, null);
    }

    public long extractMinutes() {
        return extract(DLTimeUnit.MINUTES, TimeContext.REAL, null);
    }

    public long extractHours() {
        return extract(DLTimeUnit.HOURS, TimeContext.REAL, null);
    }

    public long extractDays() {
        return extract(DLTimeUnit.DAYS, TimeContext.REAL, null);
    }

    /**
     * Convenience extraction methods for game units requiring an ITimeSystem.
     */
    public long extractGameTicks(ITimeSystem system) {
        return extract(DLTimeUnit.TICKS, TimeContext.INGAME, system);
    }

    public long extractGameSeconds(ITimeSystem system) {
        return extract(DLTimeUnit.SECONDS, TimeContext.INGAME, system);
    }

    public long extractGameMinutes(ITimeSystem system) {
        return extract(DLTimeUnit.MINUTES, TimeContext.INGAME, system);
    }

    public long extractGameHours(ITimeSystem system) {
        return extract(DLTimeUnit.HOURS, TimeContext.INGAME, system);
    }

    public long extractGameDays(ITimeSystem system) {
        return extract(DLTimeUnit.DAYS, TimeContext.INGAME, system);
    }

    private static void requireSystem(ITimeSystem system) {
        if (system == null)
            throw new IllegalArgumentException("ITimeSystem is required for INGAME extraction.");
    }
}
