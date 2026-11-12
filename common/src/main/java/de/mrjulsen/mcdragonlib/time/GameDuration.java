package de.mrjulsen.mcdragonlib.time;

import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mrjulsen.mcdragonlib.time.format.DurationFormat;
import de.mrjulsen.mcdragonlib.time.format.TimeParser;
import de.mrjulsen.mcdragonlib.time.format.TimeParts;

/**
 * An amount of game time, measured in day time ticks, the same unit as {@link GameTime}. May be
 * negative.
 * <p>Because both are ticks, arithmetic between points and durations ({@link GameTime#plus},
 * {@link GameTime#until}, {@link #between}) is exact. Game units such as hours are converted with a
 * {@link TimeSystem} when the duration is created or read; the variants without one use
 * {@link TimeSystems#current()}. A game hour is {@code ticksPerDay / 24} ticks, so if the length of the
 * day changes, a duration created as "2 hours" keeps its ticks and then is a different number of hours.
 * To keep a value such as a configured waiting time in game units, store it in game units and convert
 * it when it is used.</p>
 * <p>How long a game duration takes in real time depends on when it starts if the day has sections of
 * different speeds. That is why converting to and from real time needs a start:
 * {@link #toReal(GameTime)} and {@link #ofReal(Duration, GameTime)}. For an amount of real time, use
 * {@link Duration}.</p>
 */
public final class GameDuration implements Comparable<GameDuration> {

    public static final GameDuration ZERO = new GameDuration(0);

    private static final Pattern TICKS_TEXT = Pattern.compile("([+-]?\\d+(?:\\.\\d+)?)\\s*(?:t|ticks?)", Pattern.CASE_INSENSITIVE);

    private final double ticks;

    private GameDuration(double ticks) {
        if (!Double.isFinite(ticks)) {
            throw new IllegalArgumentException("A duration must be finite: " + ticks);
        }
        this.ticks = ticks;
    }

    public static GameDuration ofTicks(double ticks) {
        return ticks == 0 ? ZERO : new GameDuration(ticks);
    }

    /**
     * E.g. {@code GameDuration.of(2, GameTimeUnit.HOURS, system)} for two game hours.
     */
    public static GameDuration of(double amount, GameTimeUnit unit, TimeSystem system) {
        return ofTicks(amount * unit.ticks(system));
    }

    public static GameDuration of(double amount, GameTimeUnit unit) {
        return unit == GameTimeUnit.TICKS ? ofTicks(amount) : of(amount, unit, TimeSystems.current());
    }

    /**
     * The duration of the given game milliseconds, e.g. 3600 for one tick in vanilla.
     */
    public static GameDuration ofMillis(double gameMillis, TimeSystem system) {
        return of(gameMillis, GameTimeUnit.MILLIS, system);
    }

    public static GameDuration ofMillis(double gameMillis) {
        return ofMillis(gameMillis, TimeSystems.current());
    }

    /**
     * The game time that passes from {@code start} to {@code end}.
     */
    public static GameDuration between(GameTime start, GameTime end) {
        return ofTicks(end.ticks() - start.ticks());
    }

    /**
     * The game time that passes within the given real time, starting at {@code start}.
     */
    public static GameDuration ofReal(Duration real, GameTime start, TimeSystem system) {
        return between(start, start.plusReal(real, system));
    }

    public static GameDuration ofReal(Duration real, GameTime start) {
        return ofReal(real, start, TimeSystems.current());
    }

    /**
     * Reads a game duration such as {@code 1h 30m} or {@code 1:30:00}, converted to
     * ticks with the given time system. See {@link TimeParser#parseDurationMillis(String)} for all
     * accepted formats; a plain number means game milliseconds. Ticks can be given directly as
     * {@code 100t} or {@code 100 ticks}.
     *
     * @throws IllegalArgumentException if the text is not a valid duration
     */
    public static GameDuration parse(String text, TimeSystem system) {
        if (text != null) {
            Matcher m = TICKS_TEXT.matcher(text.trim());
            if (m.matches()) {
                return ofTicks(Double.parseDouble(m.group(1)));
            }
        }
        return ofMillis(TimeParser.parseDurationMillis(text), system);
    }

    public static GameDuration parse(String text) {
        return parse(text, TimeSystems.current());
    }

    /**
     * Like {@link #parse(String, TimeSystem)}, but empty instead of throwing.
     */
    public static Optional<GameDuration> tryParse(String text, TimeSystem system) {
        try {
            return Optional.of(parse(text, system));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static Optional<GameDuration> tryParse(String text) {
        return tryParse(text, TimeSystems.current());
    }



    public double ticks() {
        return ticks;
    }

    /**
     * This duration in the given unit, e.g. 1.5 for 1500 ticks in vanilla {@link GameTimeUnit#HOURS}.
     */
    public double to(GameTimeUnit unit, TimeSystem system) {
        return ticks / unit.ticks(system);
    }

    public double to(GameTimeUnit unit) {
        return unit == GameTimeUnit.TICKS ? ticks : to(unit, TimeSystems.current());
    }

    /**
     * Game milliseconds, e.g. 3600 for one tick in vanilla.
     */
    public double toMillis(TimeSystem system) {
        return to(GameTimeUnit.MILLIS, system);
    }

    public double toMillis() {
        return toMillis(TimeSystems.current());
    }

    /**
     * The real time this duration takes when it starts at {@code start}.
     */
    public Duration toReal(GameTime start, TimeSystem system) {
        return start.realUntil(start.plus(this), system);
    }

    public Duration toReal(GameTime start) {
        return toReal(start, TimeSystems.current());
    }

    /**
     * This duration split into game days, hours, minutes, seconds and milliseconds, e.g. for
     * {@link DurationFormat}.
     */
    public TimeParts toParts(TimeSystem system) {
        return TimeParts.ofMillis(toMillis(system));
    }

    public TimeParts toParts() {
        return toParts(TimeSystems.current());
    }

    public String format(DurationFormat format, TimeSystem system) {
        return format.format(toParts(system));
    }

    public String format(DurationFormat format) {
        return format(format, TimeSystems.current());
    }

    /**
     * Formats the real time this duration takes when it starts at {@code start}. In vanilla, 24000
     * ticks give "20m" here, while {@link #format(DurationFormat)} gives "1d".
     */
    public String formatReal(DurationFormat format, GameTime start, TimeSystem system) {
        return format.format(TimeParts.of(toReal(start, system)));
    }

    public String formatReal(DurationFormat format, GameTime start) {
        return formatReal(format, start, TimeSystems.current());
    }

    public boolean isZero() {
        return ticks == 0;
    }

    public boolean isNegative() {
        return ticks < 0;
    }




    public GameDuration add(GameDuration other) {
        return ofTicks(ticks + other.ticks);
    }

    public GameDuration sub(GameDuration other) {
        return ofTicks(ticks - other.ticks);
    }

    public GameDuration add(double ticks) {
        return ofTicks(this.ticks + ticks);
    }

    public GameDuration add(double amount, GameTimeUnit unit, TimeSystem system) {
        return add(of(amount, unit, system));
    }

    public GameDuration add(double amount, GameTimeUnit unit) {
        return add(of(amount, unit));
    }

    public GameDuration mul(double factor) {
        return ofTicks(ticks * factor);
    }

    public GameDuration div(double divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Cannot divide a duration by zero.");
        }
        return ofTicks(ticks / divisor);
    }

    /**
     * How many times {@code other} fits into this duration, e.g. 4 for 2h / 30m.
     */
    public double div(GameDuration other) {
        if (other.ticks == 0) {
            throw new ArithmeticException("Cannot divide by a zero duration.");
        }
        return ticks / other.ticks;
    }

    public GameDuration negated() {
        return ofTicks(-ticks);
    }

    public GameDuration abs() {
        return ticks < 0 ? negated() : this;
    }




    @Override
    public int compareTo(GameDuration o) {
        return Double.compare(ticks, o.ticks);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GameDuration o && Double.compare(ticks, o.ticks) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(ticks);
    }

    @Override
    public String toString() {
        return "GameDuration[" + ticks + "t]";
    }
}
