package de.mrjulsen.mcdragonlib.util.time;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the time-of-day portion of a {@link DLTime} relative to a given {@link ITimeSystem}.
 *
 * <p>The instance stores the fractional tick position within a single day (0 .. ticksPerDay)
 * after applying the system's daytime offset. This type is intended for wrap-aware comparisons
 * (for example, checking whether a time falls between two daily times that may cross midnight).
 *
 * <p>All returned values may be fractional to preserve sub-tick precision.
 */
public final class DLTimeOfDay {

    private final double ticksInDay;

    /**
     * Construct a DLTimeOfDay from an absolute {@link DLTime} and an {@link ITimeSystem}.
     *
     * @param time absolute DLTime to convert (must not be null)
     * @param system the time system providing day length and offset (must not be null)
     */
    public DLTimeOfDay(@NotNull DLTime time, @NotNull ITimeSystem system) {
        double absoluteTicks = time.toTicks(system);
        long ticksPerDay = system.getTicksPerDay();
        double offset = system.getDaytimeOffset();
        this.ticksInDay = (absoluteTicks + offset) % ticksPerDay;
    }

    /**
     * Return the fractional tick-of-day (0 .. ticksPerDay).
     *
     * @return ticks within the current day according to the system used at construction time
     */
    public double getTicks() {
        return ticksInDay;
    }

    /**
     * Return the time-of-day as game-seconds.
     *
     * @param system the time system used to derive seconds from ticks (must not be null)
     * @return seconds since the beginning of the day implied by this instance
     */
    public double getSeconds(@NotNull ITimeSystem system) {
        return ticksInDay / (system.getTicksPerDay() / 86400.0);
    }

    /**
     * Return the time-of-day as game-minutes.
     *
     * @param system the time system used to derive minutes (must not be null)
     * @return minutes since the beginning of the day implied by this instance
     */
    public double getMinutes(@NotNull ITimeSystem system) {
        return getSeconds(system) / 60.0;
    }

    /**
     * Return the time-of-day as game-hours.
     *
     * @param system the time system used to derive hours (must not be null)
     * @return hours since the beginning of the day implied by this instance
     */
    public double getHours(@NotNull ITimeSystem system) {
        return getMinutes(system) / 60.0;
    }

    /**
     * Check whether this time-of-day lies between {@code start} and {@code end}, accounting for wrap-around.
     *
     * <p>If {@code start <= end} the check is inclusive within the interval [start, end]. If {@code start > end}
     * the interval crosses midnight and the method returns true when the time is {@code >=} start or {@code <=} end.
     *
     * @param start interval start (must not be null)
     * @param end interval end (must not be null)
     * @return true if this time-of-day is inside the interval (wrap-aware)
     */
    public boolean isBetween(@NotNull DLTimeOfDay start, @NotNull DLTimeOfDay end) {
        double s = start.getTicks();
        double e = end.getTicks();
        if (s <= e) {
            return ticksInDay >= s && ticksInDay <= e;
        } else {
            return ticksInDay >= s || ticksInDay <= e;
        }
    }
}
