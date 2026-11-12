package de.mrjulsen.mcdragonlib.time;

import java.time.LocalTime;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import de.mrjulsen.mcdragonlib.time.format.ClockFormat;
import de.mrjulsen.mcdragonlib.time.format.TimeParser;
import de.mrjulsen.mcdragonlib.time.format.TimeParts;

/**
 * A clock reading within a game day, such as 06:30, independent of any time system.
 * <p>Because a game day always has 24 game hours, "06:30" means the same in every time system; only
 * the matching tick differs, see {@link #toTicks(TimeSystem)}. Get the clock reading of a point in
 * time with {@link GameTime#timeOfDay()}.</p>
 * <p>Instances are immutable and have game millisecond precision.</p>
 */
public final class TimeOfDay implements Comparable<TimeOfDay> {

    private static final long MILLIS_PER_DAY = GameTimeUnit.DAYS.millis();

    public static final TimeOfDay MIDNIGHT = new TimeOfDay(0);
    public static final TimeOfDay NOON = new TimeOfDay(MILLIS_PER_DAY / 2);

    private final long millisOfDay;

    private TimeOfDay(long millisOfDay) {
        this.millisOfDay = millisOfDay;
    }

    public static TimeOfDay of(int hour, int minute) {
        return of(hour, minute, 0, 0);
    }

    public static TimeOfDay of(int hour, int minute, int second) {
        return of(hour, minute, second, 0);
    }

    /**
     * @throws IllegalArgumentException if a field is out of range (hour 0-23, minute and second 0-59,
     *         millis 0-999)
     */
    public static TimeOfDay of(int hour, int minute, int second, int millis) {
        check(hour, GameTimeUnit.HOURS, GameTimeUnit.DAYS);
        check(minute, GameTimeUnit.MINUTES, GameTimeUnit.HOURS);
        check(second, GameTimeUnit.SECONDS, GameTimeUnit.MINUTES);
        check(millis, GameTimeUnit.MILLIS, GameTimeUnit.SECONDS);
        return new TimeOfDay(hour * GameTimeUnit.HOURS.millis() + minute * GameTimeUnit.MINUTES.millis() + second * GameTimeUnit.SECONDS.millis() + millis * GameTimeUnit.MILLIS.millis());
    }

    /**
     * A clock reading from game milliseconds since midnight. Values outside one day are wrapped, so
     * this can also be used for clock arithmetic.
     */
    public static TimeOfDay ofMillisOfDay(double millis) {
        long rounded = Math.round(millis) % MILLIS_PER_DAY;
        return new TimeOfDay(rounded < 0 ? rounded + MILLIS_PER_DAY : rounded);
    }

    /**
     * The clock reading for the given ticks since midnight in the given time system. Values outside one
     * day are wrapped.
     */
    public static TimeOfDay ofTicks(double ticksSinceMidnight, TimeSystem system) {
        return ofMillisOfDay(ticksSinceMidnight / system.ticksPerDay() * MILLIS_PER_DAY);
    }

    /**
     * A real clock reading, so real times can use the same {@link ClockFormat}s as game times.
     */
    public static TimeOfDay of(LocalTime time) {
        return new TimeOfDay(TimeUnit.NANOSECONDS.toMillis(time.toNanoOfDay()));
    }

    /**
     * Reads a clock reading such as {@code 14:05}, {@code 14:05:09.250} or {@code 2:05 PM}. See
     * {@link TimeParser#parseClock(String)} for all accepted formats.
     *
     * @throws IllegalArgumentException if the text is not a valid clock reading
     */
    public static TimeOfDay parse(String text) {
        return TimeParser.parseClock(text);
    }

    /**
     * Like {@link #parse(String)}, but empty instead of throwing.
     */
    public static Optional<TimeOfDay> tryParse(String text) {
        try {
            return Optional.of(parse(text));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static void check(int value, GameTimeUnit unit, GameTimeUnit larger) {
        long max = larger.millis() / unit.millis() - 1;
        if (value < 0 || value > max) {
            String name = unit.name().toLowerCase(Locale.ROOT);
            throw new IllegalArgumentException("The " + name + " must be between 0 and " + max + ": " + value);
        }
    }

    public int getHour() {
        return field(GameTimeUnit.HOURS, GameTimeUnit.DAYS);
    }

    public int getMinute() {
        return field(GameTimeUnit.MINUTES, GameTimeUnit.HOURS);
    }

    public int getSecond() {
        return field(GameTimeUnit.SECONDS, GameTimeUnit.MINUTES);
    }

    public int getMillis() {
        return field(GameTimeUnit.MILLIS, GameTimeUnit.SECONDS);
    }

    private int field(GameTimeUnit unit, GameTimeUnit larger) {
        return (int)(millisOfDay % larger.millis() / unit.millis());
    }

    /**
     * Game milliseconds since midnight.
     */
    public long toMillisOfDay() {
        return millisOfDay;
    }

    /**
     * The same clock reading as a {@link LocalTime}.
     */
    public LocalTime toLocalTime() {
        return LocalTime.ofNanoOfDay(TimeUnit.MILLISECONDS.toNanos(millisOfDay));
    }

    /**
     * Time since midnight in the given unit, e.g. 6.5 for 06:30 in {@link GameTimeUnit#HOURS}.
     */
    public double get(GameTimeUnit unit, TimeSystem system) {
        return toTicks(system) / unit.ticks(system);
    }

    /**
     * Ticks since midnight in the given time system.
     */
    public double toTicks(TimeSystem system) {
        return (double)millisOfDay / MILLIS_PER_DAY * system.ticksPerDay();
    }

    /**
     * Ticks since midnight in the {@link TimeSystems#current() current} time system.
     */
    public double toTicks() {
        return toTicks(TimeSystems.current());
    }

    /**
     * This clock reading split into hours, minutes, seconds and milliseconds.
     */
    public TimeParts toParts() {
        return TimeParts.ofMillis(millisOfDay);
    }

    /**
     * The game time since midnight, e.g. 6.5 game hours for 06:30.
     */
    public GameDuration sinceMidnight(TimeSystem system) {
        return GameDuration.ofMillis(millisOfDay, system);
    }

    public GameDuration sinceMidnight() {
        return sinceMidnight(TimeSystems.current());
    }

    /**
     * The game time from this clock reading forward to {@code other}, wrapping around midnight: 22:00
     * until 06:00 is 8 hours, 06:00 until 22:00 is 16 hours. Always within {@code [0, 24h)}, so the same
     * clock reading gives zero.
     */
    public GameDuration until(TimeOfDay other, TimeSystem system) {
        long diff = other.millisOfDay - millisOfDay;
        return GameDuration.ofMillis(diff < 0 ? diff + MILLIS_PER_DAY : diff, system);
    }

    public GameDuration until(TimeOfDay other) {
        return until(other, TimeSystems.current());
    }

    /**
     * The clock reading the given game time later, wrapping around midnight. Negative durations go back.
     */
    public TimeOfDay add(GameDuration duration, TimeSystem system) {
        return ofMillisOfDay(millisOfDay + duration.toMillis(system) % MILLIS_PER_DAY);
    }

    public TimeOfDay add(GameDuration duration) {
        return add(duration, TimeSystems.current());
    }

    public TimeOfDay sub(GameDuration duration, TimeSystem system) {
        return ofMillisOfDay(millisOfDay - duration.toMillis(system) % MILLIS_PER_DAY);
    }

    public TimeOfDay sub(GameDuration duration) {
        return sub(duration, TimeSystems.current());
    }

    /**
     * The clock reading the given amount later. Clock units need no time system, ticks use the given one.
     */
    public TimeOfDay add(double amount, GameTimeUnit unit, TimeSystem system) {
        return ofMillisOfDay(millisOfDay + amount * unit.millis(system) % MILLIS_PER_DAY);
    }

    public TimeOfDay add(double amount, GameTimeUnit unit) {
        return unit.isClockUnit()
            ? ofMillisOfDay(millisOfDay + amount * unit.millis() % MILLIS_PER_DAY)
            : add(amount, unit, TimeSystems.current());
    }

    public TimeOfDay addHours(double hours) {
        return add(hours, GameTimeUnit.HOURS);
    }

    public TimeOfDay addMinutes(double minutes) {
        return add(minutes, GameTimeUnit.MINUTES);
    }

    public TimeOfDay addSeconds(double seconds) {
        return add(seconds, GameTimeUnit.SECONDS);
    }

    public boolean isBefore(TimeOfDay other) {
        return millisOfDay < other.millisOfDay;
    }

    public boolean isAfter(TimeOfDay other) {
        return millisOfDay > other.millisOfDay;
    }

    /**
     * Whether this clock reading lies in the range from {@code start} to {@code end}, both inclusive.
     * Ranges across midnight work too: 22:00 - 06:00 contains 23:00 and 05:00.
     */
    public boolean isBetween(TimeOfDay start, TimeOfDay end) {
        if (start.millisOfDay <= end.millisOfDay) {
            return millisOfDay >= start.millisOfDay && millisOfDay <= end.millisOfDay;
        }
        return millisOfDay >= start.millisOfDay || millisOfDay <= end.millisOfDay;
    }

    public String format(ClockFormat format) {
        return format.format(this);
    }

    @Override
    public int compareTo(TimeOfDay o) {
        return Long.compare(millisOfDay, o.millisOfDay);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TimeOfDay o && millisOfDay == o.millisOfDay;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(millisOfDay);
    }

    @Override
    public String toString() {
        return getMillis() == 0 ? ClockFormat.H24_SECONDS.format(this) : ClockFormat.H24_MILLIS.format(this);
    }
}
