package de.mrjulsen.mcdragonlib.time;

import java.time.Duration;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.time.format.ClockFormat;
import net.minecraft.world.level.Level;

/**
 * A point in game time, measured in absolute day time ticks like {@link Level#getDayTime()}.
 * <p>Ticks may be fractional, e.g. after adding a real duration, and negative. Everything that needs
 * the length of a day, the clock or the speed of time takes a {@link TimeSystem}; the variants without
 * one use {@link TimeSystems#current()}.</p>
 *
 * <pre>{@code
 * GameTime now = GameTime.now();
 * TimeOfDay clock = now.timeOfDay();                    // e.g. 14:05:30
 * GameTime evening = now.next(TimeOfDay.of(18, 0));     // the next 18:00
 * Duration wait = now.realUntil(evening);               // real time until then
 * GameTime later = now.plusReal(Duration.ofMinutes(5)); // game time in five real minutes
 * }</pre>
 *
 * <h2>Days</h2>
 * <p>{@link #day()} counts clock days, which start at midnight. Vanilla's day counter
 * ({@code dayTime / 24000}) starts at tick 0, which is 06:00, so the two differ in the early
 * morning.</p>
 */
public final class GameTime implements Comparable<GameTime> {

    public static final GameTime ZERO = new GameTime(0);

    private final double ticks;

    private GameTime(double ticks) {
        if (!Double.isFinite(ticks)) {
            throw new IllegalArgumentException("A point in time must be finite: " + ticks);
        }
        this.ticks = ticks;
    }

    public static GameTime ofTicks(double ticks) {
        return ticks == 0 ? ZERO : new GameTime(ticks);
    }

    /**
     * The current day time of the given level.
     */
    public static GameTime of(Level level) {
        return ofTicks(level.getDayTime());
    }

    /**
     * The current day time of the overworld. Works on both sides; on a client without a world, this is
     * {@link #ZERO}.
     */
    public static GameTime now() {
        return ofTicks(DragonLib.getCurrentWorldTime());
    }

    /**
     * The given clock reading on the given clock day, see {@link #day(TimeSystem)}.
     */
    public static GameTime of(long day, TimeOfDay time, TimeSystem system) {
        return ofTicks(day * (double)system.ticksPerDay() + time.toTicks(system) - system.clockOffset());
    }

    public static GameTime of(long day, TimeOfDay time) {
        return of(day, time, TimeSystems.current());
    }

    public double ticks() {
        return ticks;
    }

    /**
     * The clock reading at this point in time.
     */
    public TimeOfDay timeOfDay(TimeSystem system) {
        return TimeOfDay.ofTicks(system.clockTicksAt(ticks), system);
    }

    public TimeOfDay timeOfDay() {
        return timeOfDay(TimeSystems.current());
    }

    /**
     * The game clock at this point as text, e.g. {@code "14:05"} with {@link ClockFormat#H24}.
     */
    public String format(ClockFormat format, TimeSystem system) {
        return format.format(timeOfDay(system));
    }

    public String format(ClockFormat format) {
        return format(format, TimeSystems.current());
    }

    /**
     * The real time it takes the day time to get from tick 0 to this point, taking the speed of every
     * section passed into account. Negative before tick 0. This is not a wall clock: it only depends on
     * the ticks and the time system, never on when or where it is calculated.
     */
    public Duration realSinceOrigin(TimeSystem system) {
        return TimeSystem.toDuration(system.realMillisBetween(0, ticks));
    }

    public Duration realSinceOrigin() {
        return realSinceOrigin(TimeSystems.current());
    }

    /**
     * The point reached when the given real time has passed since tick 0. Exact inverse of
     * {@link #realSinceOrigin(TimeSystem)}.
     */
    public static GameTime ofRealSinceOrigin(Duration real, TimeSystem system) {
        return ofTicks(system.tickAfterRealMillis(0, TimeSystem.toMillis(real)));
    }

    public static GameTime ofRealSinceOrigin(Duration real) {
        return ofRealSinceOrigin(real, TimeSystems.current());
    }

    /**
     * {@link #realSinceOrigin(TimeSystem)} as a clock reading, wrapping every 24 real hours. In vanilla
     * tick 0 is {@code 00:00}, tick 24000 (one game day, 20 real minutes) is {@code 00:20}. This is not
     * a wall clock either, just real time in the format of a clock.
     */
    public TimeOfDay realTimeOfDay(TimeSystem system) {
        return TimeOfDay.ofMillisOfDay(system.realMillisBetween(0, ticks));
    }

    public TimeOfDay realTimeOfDay() {
        return realTimeOfDay(TimeSystems.current());
    }

    /**
     * {@link #realTimeOfDay(TimeSystem)} as text, e.g. {@code "00:20"} with {@link ClockFormat#H24}.
     */
    public String formatReal(ClockFormat format, TimeSystem system) {
        return format.format(realTimeOfDay(system));
    }

    public String formatReal(ClockFormat format) {
        return formatReal(format, TimeSystems.current());
    }

    /**
     * The clock day this point lies in, counted from midnight before tick 0 (day 0). See the class
     * description for how this differs from vanilla's day counter.
     */
    public long day(TimeSystem system) {
        return (long)system.dayIndex(ticks + system.clockOffset());
    }

    public long day() {
        return day(TimeSystems.current());
    }

    /**
     * The section of the day this point lies in.
     */
    public TimeSection section(TimeSystem system) {
        return system.sectionAt(ticks);
    }

    public TimeSection section() {
        return section(TimeSystems.current());
    }

    /**
     * How fast time runs at this point, in ticks per real second.
     */
    public double ticksPerSecond(TimeSystem system) {
        return system.ticksPerSecondAt(ticks);
    }

    public double ticksPerSecond() {
        return ticksPerSecond(TimeSystems.current());
    }

    public GameTime plus(GameDuration duration) {
        return ofTicks(ticks + duration.ticks());
    }

    public GameTime minus(GameDuration duration) {
        return ofTicks(ticks - duration.ticks());
    }

    public GameTime plusTicks(double ticks) {
        return ofTicks(this.ticks + ticks);
    }

    public GameTime plus(double amount, GameTimeUnit unit, TimeSystem system) {
        return plusTicks(amount * unit.ticks(system));
    }

    public GameTime plus(double amount, GameTimeUnit unit) {
        return plus(amount, unit, TimeSystems.current());
    }

    /**
     * The point in game time reached after the given real time, taking the speed of every section passed
     * into account. Negative durations go back.
     */
    public GameTime plusReal(Duration real, TimeSystem system) {
        return ofTicks(system.tickAfterRealMillis(ticks, TimeSystem.toMillis(real)));
    }

    public GameTime plusReal(Duration real) {
        return plusReal(real, TimeSystems.current());
    }

    public GameTime minusReal(Duration real, TimeSystem system) {
        return plusReal(real.negated(), system);
    }

    public GameTime minusReal(Duration real) {
        return minusReal(real, TimeSystems.current());
    }

    /**
     * The game time from this point to {@code other}; negative if {@code other} is earlier.
     */
    public GameDuration until(GameTime other) {
        return GameDuration.between(this, other);
    }

    /**
     * The real time from this point to {@code other}; negative if {@code other} is earlier.
     */
    public Duration realUntil(GameTime other, TimeSystem system) {
        return TimeSystem.toDuration(system.realMillisBetween(ticks, other.ticks));
    }

    public Duration realUntil(GameTime other) {
        return realUntil(other, TimeSystems.current());
    }

    /**
     * The given clock reading on the same clock day as this point.
     */
    public GameTime withTimeOfDay(TimeOfDay time, TimeSystem system) {
        return of(day(system), time, system);
    }

    public GameTime withTimeOfDay(TimeOfDay time) {
        return withTimeOfDay(time, TimeSystems.current());
    }

    /**
     * The next time the clock shows {@code time}, at or after this point.
     */
    public GameTime next(TimeOfDay time, TimeSystem system) {
        GameTime candidate = withTimeOfDay(time, system);
        return candidate.ticks < ticks ? candidate.plusTicks(system.ticksPerDay()) : candidate;
    }

    public GameTime next(TimeOfDay time) {
        return next(time, TimeSystems.current());
    }

    /**
     * The last time the clock showed {@code time}, at or before this point.
     */
    public GameTime previous(TimeOfDay time, TimeSystem system) {
        GameTime candidate = withTimeOfDay(time, system);
        return candidate.ticks > ticks ? candidate.plusTicks(-system.ticksPerDay()) : candidate;
    }

    public GameTime previous(TimeOfDay time) {
        return previous(time, TimeSystems.current());
    }

    public boolean isBefore(GameTime other) {
        return ticks < other.ticks;
    }

    public boolean isAfter(GameTime other) {
        return ticks > other.ticks;
    }

    @Override
    public int compareTo(GameTime o) {
        return Double.compare(ticks, o.ticks);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GameTime o && Double.compare(ticks, o.ticks) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(ticks);
    }

    @Override
    public String toString() {
        return "GameTime[" + ticks + "t]";
    }
}
