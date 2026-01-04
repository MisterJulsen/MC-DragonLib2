package de.mrjulsen.mcdragonlib.util.time;

/**
 * Units used by the DLTime API to express durations and to convert between
 * real-world units and game-centric units.
 *
 * <p>Most enum constants carry a {@link #millis} factor for direct conversion to real milliseconds.
 * The {@code TICKS} constant is special: its conversion to ticks depends on the {@link ITimeSystem}
 * and therefore {@link #getTicks(ITimeSystem)} must be used for system-aware conversions.
 */
public enum DLTimeUnit {

    MILLIS(1.0),
    SECONDS(1000.0),
    MINUTES(SECONDS.millis * 60.0),
    HOURS(MINUTES.millis * 60.0),
    DAYS(HOURS.millis * 24.0),
    TICKS(0.0);

    /**
     * Number of real milliseconds represented by one unit instance for fixed (real) units.
     * For {@code TICKS} this value is 0 since ticks are converted via an {@link ITimeSystem}.
     */
    public final double millis;

    DLTimeUnit(double millis) {
        this.millis = millis;
    }

    /**
     * Convert a given amount expressed in this unit to real milliseconds.
     *
     * <p>For {@link #TICKS} this returns {@code amount * 0} and callers should instead
     * use {@link ITimeSystem#getRealMillisFromTicks(double, double)} when converting ticks.
     *
     * @param amount the amount in this unit
     * @return equivalent number of real milliseconds
     */
    public double toMillis(double amount) {
        return amount * millis;
    }

    /**
     * Return how many game ticks correspond to one unit of this enum value for the
     * provided {@link ITimeSystem}.
     *
     * <p>Examples:
     * - for {@code SECONDS} it returns {@code system.getTicksPerDay() / DAYS.getSeconds()}
     * - for {@code TICKS} it returns {@code 1.0}
     *
     * @param system the time system used to compute tick counts; must not be {@code null} for non-TICKS units
     * @return number of ticks corresponding to one unit in the given system
     */
    public double getTicks(ITimeSystem system) {
        return switch (this) {
            case TICKS -> 1.0;
            case SECONDS -> system.getTicksPerDay() / DAYS.getSeconds();
            case MINUTES -> system.getTicksPerDay() / (DAYS.getMinutes());
            case HOURS -> system.getTicksPerDay() / 24.0;
            case DAYS -> system.getTicksPerDay();
            default -> 0.0;
        };
    }

    /**
     * Return the length of this unit in seconds (for real-world units).
     *
     * @return seconds represented by this unit (millis / 1000.0)
     */
    public double getSeconds() {
        return millis / 1000.0;
    }

    /**
     * Return the length of this unit in minutes (for real-world units).
     *
     * @return minutes represented by this unit (getSeconds() / 60.0)
     */
    public double getMinutes() {
        return getSeconds() / 60.0;
    }
}
