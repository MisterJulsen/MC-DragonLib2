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

    /**
     * Return the hour-of-day (0..23), derived from the system used.
     */
    public int getHourOfDay(@NotNull ITimeSystem system) {
        double hours = getHours(system);
        int h = (int) Math.floor(hours) % 24;
        if (h < 0) h += 24;
        return h;
    }

    /**
     * Return the minute within the current hour (0..59).
     */
    public int getMinuteOfHour(@NotNull ITimeSystem system) {
        double totalMinutes = getMinutes(system);
        int minute = (int) Math.floor(totalMinutes) % 60;
        if (minute < 0) minute += 60;
        return minute;
    }

    /**
     * Return the second within the current minute (0..59).
     */
    public int getSecondOfMinute(@NotNull ITimeSystem system) {
        double totalSeconds = getSeconds(system);
        int second = (int) Math.floor(totalSeconds) % 60;
        if (second < 0) second += 60;
        return second;
    }

    // Neue Methoden: Ticks-bezogene Zeitkomponenten (anstelle von Millisekunden)

    /**
     * Return the fractional number of ticks elapsed within the current game-second (0 .. ticksPerSecond).
     *
     * <p>Ticks are the native time unit used by the {@link ITimeSystem}. This method computes how many
     * (possibly fractional) ticks have passed since the beginning of the current second implied by this
     * DLTimeOfDay and the provided {@code system}. The value preserves sub-tick precision.
     *
     * @param system the time system used to convert day-ticks to seconds (must not be null)
     * @return fractional ticks since the start of the current second (range: [0, ticksPerSecond))
     */
    public double getTicksWithinSecond(@NotNull ITimeSystem system) {
        double ticksPerSecond = system.getTicksPerDay() / 86400.0;
        if (ticksPerSecond == 0.0) return 0.0;
        double rem = ticksInDay - Math.floor(ticksInDay / ticksPerSecond) * ticksPerSecond;
        if (rem < 0.0) rem += ticksPerSecond;
        return rem;
    }

    /**
     * Return the whole tick index within the current game-second (0 .. floor(ticksPerSecond)-1).
     *
     * <p>This returns the integer tick number that falls inside the current second. If the system defines
     * less than one tick per second, this method will return 0.
     *
     * @param system the time system used to convert day-ticks to seconds (must not be null)
     * @return integer tick index within the current second
     */
    public int getWholeTickOfSecond(@NotNull ITimeSystem system) {
        double fractional = getTicksWithinSecond(system);
        return (int) Math.floor(fractional);
    }

    /**
     * Return the whole tick index within the current game-minute (0 .. floor(ticksPerMinute)-1).
     *
     * <p>The minute-based tick index counts ticks from the start of the current minute implied by this
     * DLTimeOfDay and the provided {@code system}. Useful when you need the tick offset inside a minute.
     *
     * @param system the time system used to derive minute length in ticks (must not be null)
     * @return integer tick index within the current minute
     */
    public int getWholeTickOfMinute(@NotNull ITimeSystem system) {
        double ticksPerSecond = system.getTicksPerDay() / 86400.0;
        double ticksPerMinute = ticksPerSecond * 60.0;
        if (ticksPerMinute == 0.0) return 0;
        double rem = ticksInDay - Math.floor(ticksInDay / ticksPerMinute) * ticksPerMinute;
        if (rem < 0.0) rem += ticksPerMinute;
        return (int) Math.floor(rem);
    }
}
