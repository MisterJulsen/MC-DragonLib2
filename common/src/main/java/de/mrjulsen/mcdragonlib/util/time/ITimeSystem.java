package de.mrjulsen.mcdragonlib.util.time;

import java.util.List;

/**
 * Abstraction describing how "game time" maps to real-world time for a particular time model.
 *
 * <p>An ITimeSystem defines:
 * <ul>
 * <li>the number of ticks that form a full in-game day ({@link #getTicksPerDay()}),</li>
 * <li>a sequence of {@link TimeZone} entries that describe variable tick durations during a day,</li>
 * <li>and optionally an offset to align the in-game "midnight" with a particular tick value.</li>
 * </ul>
 *
 * <p>Implementations must provide the basic parameters; the default methods provide
 * robust conversion routines between real milliseconds and game ticks that honour
 * multi-zone/day-length models. These default conversions assume that {@link TimeZone}s
 * are non-overlapping and collectively cover a day (implementations should ensure validity).
 */
public interface ITimeSystem {
    /**
     * Return the number of game ticks that constitute a complete in-game day for this system.
     *
     * @return ticks per day, typically > 0 (e.g. 24000 for vanilla Minecraft)
     */
    long getTicksPerDay();

    /**
     * Return an ordered or unordered collection of {@link TimeZone} entries describing
     * how many real milliseconds each tick consumes across segments of the in-game day.
     *
     * <p>Implementations may return an immutable list. The default conversion routines
     * will sort the list internally.
     *
     * @return list of TimeZone objects describing the day's zones
     */
    List<TimeZone> getTimeZones();

    /**
     * Optional daytime offset that shifts which tick is considered the start of the "day".
     *
     * <p>The default implementation returns {@code getTicksPerDay() / 4.0} which mirrors
     * the conventional Minecraft offset (so that tick 0 corresponds to 6:00).
     *
     * @return fractional tick offset to add when converting absolute ticks to a daily position
     */
    default double getDaytimeOffset() {
        return getTicksPerDay() / 4.0;
    }
    
    /**
     * Convert a number of game ticks to the equivalent real-world milliseconds, starting
     * conversion at an arbitrary fractional {@code startTick}.
     *
     * <p>This default implementation:
     * <ul>
     * <li>handles wrap-around across the remainder of the current day,</li>
     * <li>consumes whole full-days using a precomputed milliseconds-per-full-day factor,</li>
     * <li>and computes any final partial-day contribution by iterating over {@link TimeZone}s.</li>
     * </ul>
     * <p>Parameters:
     * @param totalTicks total number of ticks to convert (must be >= 0; values &le; 0 produce 0)
     * @param startTick fractional starting tick position in the current day (may be fractional)
     * @return equivalent number of real milliseconds for the supplied tick span
     */
    default double getRealMillisFromTicks(double totalTicks, double startTick) {
        if (totalTicks <= 0) return 0;

        long ticksPerDay = getTicksPerDay();
        List<TimeZone> zones = getTimeZones().stream().sorted().toList();
        double millisPerFullDay = calculateMillisPerFullDay(zones);

        double remainingTicks = totalTicks;
        double totalRealMillis = 0;
        double currentTickInDay = Math.floorMod((long)startTick, ticksPerDay) + (startTick - Math.floor(startTick));

        double ticksUntilEndOfDay = ticksPerDay - currentTickInDay;
        if (remainingTicks <= ticksUntilEndOfDay) {
            return getMillisInPartialDay(remainingTicks, currentTickInDay, zones);
        }

        totalRealMillis += getMillisInPartialDay(ticksUntilEndOfDay, currentTickInDay, zones);
        remainingTicks -= ticksUntilEndOfDay;

        long fullDays = (long) (remainingTicks / ticksPerDay);
        totalRealMillis += fullDays * millisPerFullDay;
        remainingTicks -= (double) fullDays * ticksPerDay;

        if (remainingTicks > 1e-9) {
            totalRealMillis += getMillisInPartialDay(remainingTicks, 0, zones);
        }

        return totalRealMillis;
    }

    /**
     * Convert a real-world millisecond span into game ticks starting at an arbitrary
     * fractional {@code startTick} position.
     *
     * <p>The returned value is a double to preserve fractional ticks when necessary.
     *
     * @param totalMillis total real milliseconds to convert (must be >= 0; values &le; 0 produce 0)
     * @param startTick fractional starting tick position in the current day (may be fractional)
     * @return equivalent number of game ticks (double)
     */
    default double getTicksFromRealMillis(double totalMillis, double startTick) {
        if (totalMillis <= 0) return 0;

        long ticksPerDay = getTicksPerDay();
        List<TimeZone> zones = getTimeZones().stream().sorted().toList();
        double millisPerFullDay = calculateMillisPerFullDay(zones);

        double remainingMillis = totalMillis;
        double totalTicks = 0;
        double currentTickInDay = Math.floorMod((long)startTick, ticksPerDay) + (startTick - Math.floor(startTick));

        double millisUntilEndOfDay = getMillisInPartialDay(ticksPerDay - currentTickInDay, currentTickInDay, zones);
        if (remainingMillis <= millisUntilEndOfDay) {
            return getTicksInPartialDay(remainingMillis, currentTickInDay, zones);
        }

        totalTicks += (ticksPerDay - currentTickInDay);
        remainingMillis -= millisUntilEndOfDay;

        long fullDays = (long) (remainingMillis / millisPerFullDay);
        totalTicks += (double) fullDays * ticksPerDay;
        remainingMillis -= (double) fullDays * millisPerFullDay;

        if (remainingMillis > 1e-9) {
            totalTicks += getTicksInPartialDay(remainingMillis, 0, zones);
        }

        return totalTicks;
    }

    // Private helper methods below are used by the default implementations and are intentionally not part
    // of the public contract beyond the behavior produced by the public default methods.
    private double calculateMillisPerFullDay(List<TimeZone> zones) {
        return zones.stream().mapToDouble(z -> (z.endTick() - z.startTick()) * z.getRealMillisPerTick()).sum();
    }

    private double getMillisInPartialDay(double ticks, double startTickInDay, List<TimeZone> zones) {
        double remaining = ticks;
        double millis = 0;
        double current = startTickInDay;

        for (TimeZone zone : zones) {
            if (remaining <= 0) break;
            double effectiveStart = Math.max(zone.startTick(), current);
            if (effectiveStart < zone.endTick()) {
                double ticksInZone = Math.min(remaining, zone.endTick() - effectiveStart);
                millis += ticksInZone * zone.getRealMillisPerTick();
                remaining -= ticksInZone;
                current = effectiveStart + ticksInZone;
            }
        }
        return millis;
    }

    private double getTicksInPartialDay(double millis, double startTickInDay, List<TimeZone> zones) {
        double remaining = millis;
        double ticks = 0;
        double current = startTickInDay;

        for (TimeZone zone : zones) {
            if (remaining <= 0) break;
            double effectiveStart = Math.max(zone.startTick(), current);
            if (effectiveStart < zone.endTick()) {
                double zoneMsPerTick = zone.getRealMillisPerTick();
                double maxTicksInZone = zone.endTick() - effectiveStart;
                double maxMillisInZone = maxTicksInZone * zoneMsPerTick;

                if (remaining >= maxMillisInZone) {
                    ticks += maxTicksInZone;
                    remaining -= maxMillisInZone;
                    current = zone.endTick();
                } else {
                    ticks += remaining / zoneMsPerTick;
                    remaining = 0;
                }
            }
        }
        return ticks;
    }
}