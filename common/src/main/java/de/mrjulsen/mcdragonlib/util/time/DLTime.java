package de.mrjulsen.mcdragonlib.util.time;

import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.config.ModCommonConfig;
import de.mrjulsen.mcdragonlib.config.ModServerConfig;
import de.mrjulsen.mcdragonlib.util.TimeCache;
import de.mrjulsen.mcdragonlib.util.time.datapack.TimeSystemDatapackLoader;
import de.mrjulsen.mcdragonlib.util.time.format.ITimeFormatter;
import net.minecraft.world.level.Level;

/**
 * Represents an absolute point in time expressed internally as "real" milliseconds.
 *
 * <p>This class provides a neutral representation of time that can be converted to and
 * from different notions of time used in Minecraft and the host environment. Internally
 * the primary stored value is {@code realMillis}, a double describing milliseconds in the
 * real-world sense. Conversions to game-specific units (ticks, game seconds/minutes/hours/days)
 * are performed using an {@link ITimeSystem} which encapsulates the mapping between real time
 * and the game's time progression model.
 *
 * <p>Instances of {@code DLTime} are immutable. Common operations create and return new
 * {@code DLTime} instances. The class implements {@link Comparable} to allow natural ordering
 * based on the underlying real-millisecond value.
 *
 * <p>Use the static factory methods or {@link Builder} to create instances in a descriptive
 * manner (for example {@link #fromGameSeconds(double, ITimeSystem)} or {@link #fromReal(double, DLTimeUnit)}).
 */
public final class DLTime implements Comparable<DLTime> {
    
    private static final Supplier<TimeCache<ITimeSystem>> timeSystemCache = Suppliers.memoize(() -> new TimeCache<>(() -> {
        ITimeSystem fallback = ModServerConfig.USE_CUSTOM.get() ? ConfiguredTimeSystem.INSTANCE : VanillaTimeSystem.INSTANCE;
        return TimeSystemDatapackLoader.getTimeSystem().orElseGet(() -> {
            if (ModServerConfig.AUTO_DETECT_TIME_SYSTEM.get()) {
                return DLTimeApi.getCurrentCompat().orElse(fallback);
            }
            return fallback;
        });
    }, ModCommonConfig.TIME_SYSTEM_CACHE_TTL.get()));

    /**
     * Return the currently resolved default {@link ITimeSystem}.
     *
     * <p>The returned time system is determined by a TTL-cached loader which can
     * consider server configuration, datapack-provided definitions and automatic
     * compatibility detection. The method never returns {@code null}.
     *
     * @return the effective default time system used for conversions when no explicit
     *         {@link ITimeSystem} is provided.
     */
    public static ITimeSystem defaultTimeSystem() {
        return timeSystemCache.get().get();
    }


    private final double realMillis;

    @Deprecated(forRemoval = true)
    private final ITimeSystem initialSystem;

    DLTime(double realMillis) {
        this.realMillis = realMillis;
        this.initialSystem = VanillaTimeSystem.INSTANCE;
    }

    /**
     * Create a new {@code DLTime} that represents an absolute duration/value given in a
     * real-world unit.
     *
     * <p>Example: {@code new DLTime(1.5, DLTimeUnit.SECONDS)} creates a DLTime representing
     * 1.5 real seconds.
     *
     * @param amount the amount in the provided unit
     * @param unit the real-world unit used to interpret {@code amount}; must not be {@code null}
     */
    public DLTime(double amount, DLTimeUnit unit) {
        this.realMillis = unit.toMillis(amount);
        this.initialSystem = VanillaTimeSystem.INSTANCE;
    }

    /**
     * Create a new {@code DLTime} from a number of game ticks using the provided time system.
     *
     * <p>The {@code system} parameter defines how many real milliseconds correspond to a
     * given number of game ticks and is used to compute the internal real-millisecond value.
     *
     * @param ticks the amount in game ticks
     * @param system the time system that maps ticks to real milliseconds; must not be {@code null}
     */
    public DLTime(double ticks, ITimeSystem system) {
        Objects.requireNonNull(system);
        this.realMillis = system.getRealMillisFromTicks(ticks, 0);
        this.initialSystem = system;
    }

    /**
     * Create a new {@code DLTime} from a {@link Level}'s day time using the specified time system.
     *
     * <p>This constructor reads {@link Level#getDayTime()} and interprets that value as game ticks
     * according to the supplied {@code system}. It is a convenience constructor for converting
     * level time to the library's absolute representation.
     *
     * @param level the Minecraft level whose day time will be used; must not be {@code null}
     * @param system the time system used to interpret the level's day time; must not be {@code null}
     */
    public DLTime(Level level, ITimeSystem system) {
        this(level.getDayTime(), system);
    }

    /**
     * Create a {@code DLTime} from a real-world value given in the specified unit.
     *
     * @param amount the amount in {@code unit}
     * @param unit the real-world time unit to interpret {@code amount}; must not be {@code null}
     * @return a new {@code DLTime} representing the requested real time
     * @see #DLTime(double, DLTimeUnit)
     */
    public static DLTime fromReal(double amount, DLTimeUnit unit) {
        return new DLTime(amount, unit);
    }

    /**
     * Create a {@code DLTime} from a number of game ticks interpreted by {@code system}.
     *
     * @param ticks the number of game ticks
     * @param system the time system used to convert ticks to real milliseconds; must not be {@code null}
     * @return a new {@code DLTime} corresponding to {@code ticks} in the provided system
     */
    public static DLTime fromGameTicks(double ticks, ITimeSystem system) {
        return new DLTime(ticks, system);
    }

    /**
     * Create a {@code DLTime} from a number of game seconds interpreted by {@code system}.
     *
     * <p>The method uses the conversion factor {@link DLTimeUnit#SECONDS} for the provided system
     * to compute the underlying tick count and then converts that to real milliseconds.
     *
     * @param seconds number of game seconds
     * @param system the time system that defines how many ticks equal a game second; must not be {@code null}
     * @return a new {@code DLTime} representing the given game seconds
     */
    public static DLTime fromGameSeconds(double seconds, ITimeSystem system) {
        return new DLTime(seconds * DLTimeUnit.SECONDS.getTicks(system), system);
    }

    /**
     * Create a {@code DLTime} from a number of game minutes interpreted by {@code system}.
     *
     * @param minutes number of game minutes
     * @param system the time system used for conversion; must not be {@code null}
     * @return a new {@code DLTime} representing the given game minutes
     */
    public static DLTime fromGameMinutes(double minutes, ITimeSystem system) {
        return fromGameSeconds(minutes * 60.0, system);
    }

    /**
     * Create a {@code DLTime} from a number of game hours interpreted by {@code system}.
     *
     * @param hours number of game hours
     * @param system the time system used for conversion; must not be {@code null}
     * @return a new {@code DLTime} representing the given game hours
     */
    public static DLTime fromGameHours(double hours, ITimeSystem system) {
        return fromGameSeconds(hours * 3600.0, system);
    }

    /**
     * Create a {@code DLTime} from a number of game days interpreted by {@code system}.
     *
     * <p>The conversion uses {@link ITimeSystem#getTicksPerDay()} to determine how many ticks
     * constitute a game day in {@code system}.
     *
     * @param days number of game days
     * @param system the time system used for conversion; must not be {@code null}
     * @return a new {@code DLTime} representing the given number of game days
     */
    public static DLTime fromGameDays(double days, ITimeSystem system) {
        return fromGameTicks(days * system.getTicksPerDay(), system);
    }


    /**
     * Obtain a {@link Builder} to incrementally construct a {@code DLTime}.
     *
     * <p>The {@link Builder} supports adding multiple components (real time and game-based
     * increments) and produces a single aggregated {@code DLTime}.
     *
     * @return a fresh {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Return the stored time expressed as real-world milliseconds.
     *
     * <p>This is the canonical internal representation used by this class.
     *
     * @return the time in real milliseconds (double precision)
     */
    public double toRealMillis() {
        return realMillis;
    }

    /**
     * Convert this {@code DLTime} to the given real-world {@link DLTimeUnit}.
     *
     * @param unit the unit to convert to; must not be {@code null}
     * @return the amount expressed in the requested unit
     */
    public double toReal(DLTimeUnit unit) {
        return realMillis / unit.millis;
    }

    /**
     * Convert this {@code DLTime} to game ticks using the provided {@link ITimeSystem}.
     *
     * <p>The {@code system} parameter defines the mapping from real milliseconds
     * to game ticks; if {@code null}, an {@link IllegalArgumentException} is thrown.
     *
     * @param system the time system to use for conversion; must not be {@code null}
     * @return the equivalent number of game ticks as a double
     * @throws IllegalArgumentException if {@code system} is {@code null}
     */
    public double toTicks(ITimeSystem system) {
        requireSystem(system);
        return system.getTicksFromRealMillis(realMillis, 0);
    }

    /**
     * Convert this {@code DLTime} to game seconds using the supplied {@link ITimeSystem}.
     *
     * @param system the time system to use; must not be {@code null}
     * @return the equivalent number of game seconds
     */
    public double toGameSeconds(ITimeSystem system) {
        return toTicks(system) / DLTimeUnit.SECONDS.getTicks(system);
    }

    /**
     * Convert this {@code DLTime} to game minutes using the supplied {@link ITimeSystem}.
     *
     * @param system the time system to use; must not be {@code null}
     * @return the equivalent number of game minutes
     */
    public double toGameMinutes(ITimeSystem system) {
        return toGameSeconds(system) / 60.0;
    }

    /**
     * Convert this {@code DLTime} to game hours using the supplied {@link ITimeSystem}.
     *
     * @param system the time system to use; must not be {@code null}
     * @return the equivalent number of game hours
     */
    public double toGameHours(ITimeSystem system) {
        return toGameMinutes(system) / 60.0;
    }

    /**
     * Convert this {@code DLTime} to game days using the supplied {@link ITimeSystem}.
     *
     * @param system the time system to use; must not be {@code null}
     * @return the equivalent number of game days
     */
    public double toGameDays(ITimeSystem system) {
        return toTicks(system) / system.getTicksPerDay();
    }

    /**
     * Retrieve an amount of this {@code DLTime} expressed in the requested unit and context.
     *
     * <p>If {@code context} is {@link TimeContext#REAL} then the result is based on real-world units.
     * If {@code context} is a game context, the {@code system} parameter must be supplied and will be
     * used to interpret ticks/seconds/minutes/hours/days. When the requested {@code unit} is {@code TICKS}
     * and {@code context} is {@code REAL}, the {@code system} is required as well since ticks are game-specific.
     *
     * @param unit the unit to express the amount in; must not be {@code null}
     * @param context whether to treat the retrieval as real or game-contextual
     * @param system the time system used for conversions when a game context or ticks are requested; may be {@code null}
     * @return the computed amount in the requested unit
     * @throws IllegalArgumentException if a game-based unit is requested without a {@code system}
     */
    public double getAmount(DLTimeUnit unit, TimeContext context, @Nullable ITimeSystem system) {
        if (context == TimeContext.REAL) {
            if (unit == DLTimeUnit.TICKS) {
                requireSystem(system);
                return toTicks(system);
            }
            return toReal(unit);
        } else {
            requireSystem(system);
            return switch (unit) {
                case TICKS -> toTicks(system);
                case SECONDS -> toGameSeconds(system);
                case MINUTES -> toGameMinutes(system);
                case HOURS -> toGameHours(system);
                case DAYS -> toGameDays(system);
                case MILLIS -> toRealMillis();
            };
        }
    }

    /**
     * Add another {@code DLTime} to this one and return the sum as a new instance.
     *
     * @param other the time to add; must not be {@code null}
     * @return a new {@code DLTime} representing the sum
     */
    public DLTime add(DLTime other) {
        return new DLTime(this.realMillis + other.realMillis);
    }

    /**
     * Subtract another {@code DLTime} from this one and return the difference as a new instance.
     *
     * @param other the time to subtract; must not be {@code null}
     * @return a new {@code DLTime} representing the difference
     */
    public DLTime sub(DLTime other) {
        return new DLTime(this.realMillis - other.realMillis);
    }

    /**
     * Add an amount of real-world time to this {@code DLTime} and return the result.
     *
     * @param amount the amount in the provided real unit
     * @param unit the real unit used to interpret {@code amount}; must not be {@code null}
     * @return a new {@code DLTime} with the added real time
     */
    public DLTime addReal(double amount, DLTimeUnit unit) {
        return new DLTime(this.realMillis + unit.toMillis(amount));
    }

    /**
     * Add a number of game ticks (interpreted using {@code system}) to this time and return the result.
     *
     * <p>The method takes into account the current tick position (computed via {@link #toTicks})
     * so that time systems which have non-linear or stateful tick durations can be handled correctly.
     *
     * @param ticks the number of game ticks to add
     * @param system the time system used for conversion; must not be {@code null}
     * @return a new {@code DLTime} representing the sum
     */
    public DLTime addGameTicks(double ticks, ITimeSystem system) {
        requireSystem(system);
        double startTick = toTicks(system);
        double addedMillis = system.getRealMillisFromTicks(ticks, startTick);
        return new DLTime(this.realMillis + addedMillis);
    }

    /**
     * Add game seconds to this time using {@code system}.
     *
     * @param seconds number of game seconds to add
     * @param system the time system used for conversion; must not be {@code null}
     * @return a new {@code DLTime} with the given seconds added
     */
    public DLTime addGameSeconds(double seconds, ITimeSystem system) {
        return addGameTicks(seconds * DLTimeUnit.SECONDS.getTicks(system), system);
    }

    /**
     * Add game minutes to this time using {@code system}.
     *
     * @param minutes number of game minutes to add
     * @param system the time system used for conversion; must not be {@code null}
     * @return a new {@code DLTime} with the given minutes added
     */
    public DLTime addGameMinutes(double minutes, ITimeSystem system) {
        return addGameSeconds(minutes * 60.0, system);
    }

    /**
     * Add game hours to this time using {@code system}.
     *
     * @param hours number of game hours to add
     * @param system the time system used for conversion; must not be {@code null}
     * @return a new {@code DLTime} with the given hours added
     */
    public DLTime addGameHours(double hours, ITimeSystem system) {
        return addGameMinutes(hours * 60.0, system);
    }

    /**
     * Add game days to this time using {@code system}.
     *
     * @param days number of game days to add
     * @param system the time system used for conversion; must not be {@code null}
     * @return a new {@code DLTime} with the given days added
     */
    public DLTime addGameDays(double days, ITimeSystem system) {
        return addGameTicks(days * system.getTicksPerDay(), system);
    }

    /**
     * Convert this {@code DLTime} into a {@link TimePool}.
     *
     * <p>A {@link TimePool} is a container that may represent a time budget or accumulator
     * measured by this starting value — consult {@link TimePool} for its semantics.
     *
     * @return a new {@link TimePool} initialized with this time
     */
    public TimePool asPool() {
        return new TimePool(this);
    }

    /**
     * Format this {@code DLTime} using a custom {@link ITimeFormatter}.
     *
     * <p>The formatter receives this instance along with a {@link TimeContext} and the
     * {@link ITimeSystem} to use for interpreting game-relative values.
     *
     * @param formatter the formatter to produce a string representation; must not be {@code null}
     * @param context whether formatting should treat the value as real or game-contextual
     * @param system the time system to use for any game-context conversions; must not be {@code null}
     * @return the formatted string
     */
    public String format(ITimeFormatter formatter, TimeContext context, ITimeSystem system) {
        return formatter.format(this, context, system);
    }

    /**
     * Check whether this time-of-day is between {@code start} and {@code end} when both are
     * interpreted as daily times (wrap-around aware).
     *
     * <p>The method internally converts each {@code DLTime} to a {@link DLTimeOfDay} using the
     * provided {@code system} and delegates wrap-aware comparison logic to that type.
     *
     * @param start the start time of the daily interval; must not be {@code null}
     * @param end the end time of the daily interval; must not be {@code null}
     * @param system the time system used to interpret daily times; must not be {@code null}
     * @return {@code true} if this time-of-day falls between {@code start} and {@code end} (inclusive behavior follows DLTimeOfDay)
     */
    public boolean isBetweenDaily(DLTime start, DLTime end, ITimeSystem system) {
        DLTimeOfDay now = new DLTimeOfDay(this, system);
        DLTimeOfDay s = new DLTimeOfDay(start, system);
        DLTimeOfDay e = new DLTimeOfDay(end, system);
        return now.isBetween(s, e);
    }


    /**
     * Ensure that a non-null {@link ITimeSystem} is provided for operations that require it.
     *
     * @param system the system to check
     * @throws IllegalArgumentException if {@code system} is {@code null}
     */
    private static void requireSystem(ITimeSystem system) {
        if (system == null)
            throw new IllegalArgumentException("ITimeSystem is required for this operation.");
    }

    /**
     * Compare this {@code DLTime} with another by their internal real-millisecond value.
     *
     * <p>The comparison is stable and consistent with {@link #equals(Object)} for identical
     * millisecond values represented as doubles.
     *
     * @param o the other {@code DLTime} to compare with; must not be {@code null}
     * @return a negative integer, zero, or a positive integer as this object is less than,
     *         equal to, or greater than the specified object
     */
    @Override
    public int compareTo(DLTime o) {
        return Double.compare(realMillis, o.realMillis);
    }

    /**
     * Compute a hash code for this {@code DLTime}.
     *
     * <p>The implementation uses a simple mixing of the stored milliseconds. Note that because
     * the class stores a {@code double} some precision nuances apply; the hash is primarily
     * intended for general-purpose use in hash-based collections.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return (int)(31 * realMillis);
    }

    /**
     * Equality check based on the internal real-millisecond value.
     *
     * <p>Two {@code DLTime} instances are considered equal when their {@code realMillis}
     * values are exactly equal (primitive double equality).
     *
     * @param obj the other object to compare
     * @return {@code true} if the other object is a {@code DLTime} with the same real-millisecond value
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DLTime o) {
            return realMillis == o.realMillis;
        }
        return false;
    }

    /**
     * Returns a concise debug string showing the class name and internal real-millisecond value.
     *
     * @return a string representation intended for debugging/logging
     */
    @Override
    public String toString() {
        return String.format("%s[%st]", getClass().getSimpleName(), realMillis);
    }



    /**
     * Builder for constructing complex {@code DLTime} values by summing several components.
     *
     * <p>Typical usage:
     * <pre>
     * DLTime t = DLTime.builder()
     *                   .real(500, DLTimeUnit.MILLIS)
     *                   .gameMinutes(3, someSystem)
     *                   .build();
     * </pre>
     *
     * The builder accumulates a real-millisecond total that becomes the internal value of
     * the returned {@code DLTime}. The builder is not thread-safe and is intended for single-threaded use.
     */
    public static final class Builder {

        private double millis;

        private Builder() {
        }

        /**
         * Add an amount of real-world time to the builder.
         *
         * @param amount the amount in the given unit
         * @param unit the real-world unit; must not be {@code null}
         * @return this builder for method chaining
         */
        public Builder real(double amount, DLTimeUnit unit) {
            this.millis += unit.toMillis(amount);
            return this;
        }

        /**
         * Add game ticks as interpreted by {@code system} to the builder.
         *
         * @param ticks the number of game ticks to add
         * @param system the time system used for conversion; must not be {@code null}
         * @return this builder for method chaining
         */
        public Builder gameTicks(double ticks, ITimeSystem system) {
            this.millis += system.getRealMillisFromTicks(ticks, 0);
            return this;
        }

        /**
         * Add game seconds (converted to ticks) using {@code system}.
         *
         * @param seconds the number of game seconds to add
         * @param system the time system used for conversion; must not be {@code null}
         * @return this builder for method chaining
         */
        public Builder gameSeconds(double seconds, ITimeSystem system) {
            return gameTicks(seconds * DLTimeUnit.SECONDS.getTicks(system), system);
        }

        /**
         * Add game minutes using {@code system}.
         *
         * @param minutes the number of game minutes to add
         * @param system the time system used for conversion; must not be {@code null}
         * @return this builder for method chaining
         */
        public Builder gameMinutes(double minutes, ITimeSystem system) {
            return gameSeconds(minutes * 60.0, system);
        }

        /**
         * Add game hours using {@code system}.
         *
         * @param hours the number of game hours to add
         * @param system the time system used for conversion; must not be {@code null}
         * @return this builder for method chaining
         */
        public Builder gameHours(double hours, ITimeSystem system) {
            return gameMinutes(hours * 60.0, system);
        }

        /**
         * Add game days using {@code system}.
         *
         * @param days the number of game days to add
         * @param system the time system used for conversion; must not be {@code null}
         * @return this builder for method chaining
         */
        public Builder gameDays(double days, ITimeSystem system) {
            return gameTicks(days * system.getTicksPerDay(), system);
        }

        /**
         * Build a {@code DLTime} from the accumulated components.
         *
         * @return a new {@code DLTime} representing the accumulated time
         */
        public DLTime build() {
            return new DLTime(millis);
        }
    }

    /**
     * Returns the ticks-per-second (TPS) of the currently active {@link TimeZone}
     * based on the current world time.
     *
     * <p>The current world time is obtained via
     * {@code DragonLib.getCurrentWorldTime()}, which returns the number of game
     * ticks since {@code /time set 0}. This tick value is mapped into the current
     * in-game day of the active {@link ITimeSystem}, and the corresponding
     * {@link TimeZone} is selected.</p>
     *
     * <p>The TPS is derived from the TimeZone's real-time tick duration:</p>
     *
     * <pre>
     * TPS = 1000 / realMillisPerTick
     * </pre>
     *
     * @return the TPS of the currently active TimeZone
     * @throws IllegalStateException if no matching TimeZone can be found
     */
    public static double getCurrentTimeZoneTPS() {
        ITimeSystem system = DLTime.defaultTimeSystem();
        long ticksPerDay = system.getTicksPerDay();

        double worldTick = DragonLib.getCurrentWorldTime();
        double tickInDay = Math.floorMod((long) worldTick, ticksPerDay) + (worldTick - Math.floor(worldTick));

        return system.getTimeZones().stream()
                .sorted()
                .filter(z -> tickInDay >= z.startTick() && tickInDay < z.endTick())
                .findFirst()
                .map(z -> 1000.0 / z.getRealMillisPerTick())
                .orElse(0.0);
    }

    /**
     * Calculates the ratio between vanilla Minecraft TPS (20) and the TPS of the
     * currently active {@link TimeZone}.
     *
     * <p>The returned value describes how much faster or slower game time progresses
     * compared to vanilla Minecraft. For example:</p>
     *
     * <ul>
     *   <li>Current TimeZone TPS = 1.25</li>
     *   <li>Result = {@code 20 / 1.25 = 16}</li>
     * </ul>
     *
     * <p>This means that while one world-time tick passes in the current TimeZone,
     * the game has already advanced by 16 vanilla ticks.</p>
     *
     * @return the ratio {@code 20 / currentTimeZoneTPS}
     */
    public static double getCurrentTimeZoneVanillaTpsRatio() {
        double timeZoneTps = getCurrentTimeZoneTPS();
        return 20.0 / timeZoneTps;
    }



    /**
     * @deprecated use {@link #fromGameTicks(double, ITimeSystem)} instead.
     */
    @Deprecated(forRemoval = true)
    public static DLTime fromTicks(double ticks, ITimeSystem provider) {
        return DLTime.fromGameTicks(ticks, provider);
    }

    /**
     * @deprecated use {@link #DLTime(Level, ITimeSystem)} or {@link #fromGameTicks(double, ITimeSystem)} instead.
     */
    @Deprecated(forRemoval = true)
    public static DLTime fromLevelTime(Level level, ITimeSystem provider) {
        return new DLTime(level, provider);
    }

    /**
     * @deprecated This method uses the current system clock and then maps it to game ticks using the provided time system.
     *             Prefer constructing time values explicitly (e.g. {@link #fromReal(double, DLTimeUnit)} or other factories).
     */
    @Deprecated(forRemoval = true)
    public static DLTime now(ITimeSystem provider) {
        long millis = System.currentTimeMillis();
        double ticks = provider.getTicksFromRealMillis(millis, 0);
        return DLTime.fromGameTicks(ticks, provider);
    }

    /**
     * @deprecated use {@link #toReal(DLTimeUnit)} with {@link DLTimeUnit#SECONDS} instead.
     */
    @Deprecated(forRemoval = true)
    public double toRealSeconds() {
        return toReal(DLTimeUnit.SECONDS);
    }

    /**
     * @deprecated use {@link #toReal(DLTimeUnit)} with {@link DLTimeUnit#MINUTES} instead.
     */
    @Deprecated(forRemoval = true)
    public double toRealMinutes() {
        return toReal(DLTimeUnit.MINUTES);
    }

    /**
     * @deprecated use {@link #toReal(DLTimeUnit)} with {@link DLTimeUnit#HOURS} instead.
     */
    @Deprecated(forRemoval = true)
    public double toRealHours() {
        return toReal(DLTimeUnit.HOURS);
    }

    /**
     * @deprecated use {@link #toReal(DLTimeUnit)} with {@link DLTimeUnit#DAYS} instead.
     */
    @Deprecated(forRemoval = true)
    public double toRealDays() {
        return toReal(DLTimeUnit.DAYS);
    }

    /**
     * @deprecated use {@link #addGameDays(double, ITimeSystem)} instead.
     */
    @Deprecated(forRemoval = true)
    public DLTime addGameDays(long days, ITimeSystem provider) {
        return addGameDays((double) days, provider);
    }

    /**
     * @deprecated use {@link #addGameHours(double, ITimeSystem)} instead.
     */
    @Deprecated(forRemoval = true)
    public DLTime addGameHours(long hours, ITimeSystem provider) {
        return addGameHours((double) hours, provider);
    }

    /**
     * @deprecated use {@link #addGameMinutes(double, ITimeSystem)} instead.
     */
    @Deprecated(forRemoval = true)
    public DLTime addGameMinutes(long minutes, ITimeSystem provider) {
        return addGameMinutes((double) minutes, provider);
    }

    /**
     * @deprecated use {@link #addGameSeconds(double, ITimeSystem)} instead.
     */
    @Deprecated(forRemoval = true)
    public DLTime addGameSeconds(long seconds, ITimeSystem provider) {
        return addGameSeconds((double) seconds, provider);
    }

    /**
     * @deprecated use {@link #addReal(double, DLTimeUnit)} with {@link DLTimeUnit#MILLIS} instead.
     */
    @Deprecated(forRemoval = true)
    public DLTime addRealMilliseconds(long ms) {
        return addReal(ms, DLTimeUnit.MILLIS);
    }

    /**
     * @deprecated use {@link #addReal(double, DLTimeUnit)} with {@link DLTimeUnit#SECONDS} instead.
     */
    @Deprecated(forRemoval = true)
    public DLTime addRealSeconds(long seconds) {
        return addReal(seconds, DLTimeUnit.SECONDS);
    }

    /**
     * @deprecated use {@link #addReal(double, DLTimeUnit)} with {@link DLTimeUnit#MINUTES} instead.
     */
    @Deprecated(forRemoval = true)
    public DLTime addRealMinutes(long minutes) {
        return addReal(minutes, DLTimeUnit.MINUTES);
    }

    /**
     * @deprecated use {@link #addReal(double, DLTimeUnit)} with {@link DLTimeUnit#HOURS} instead.
     */
    @Deprecated(forRemoval = true)
    public DLTime addRealHours(long hours) {
        return addReal(hours, DLTimeUnit.HOURS);
    }

    /**
     * @deprecated use {@link #addReal(double, DLTimeUnit)} with {@link DLTimeUnit#DAYS} instead.
     */
    @Deprecated(forRemoval = true)
    public DLTime addRealDays(long days) {
        return addReal(days, DLTimeUnit.DAYS);
    }

    /**
     * @deprecated use {@link #compareTo(DLTime)} or {@link #equals(Object)} for comparisons.
     */
    @Deprecated(forRemoval = true)
    public boolean isBefore(DLTime other) {
        return this.compareTo(other) < 0;
    }

    /**
     * @deprecated use {@link #compareTo(DLTime)} or {@link #equals(Object)} for comparisons.
     */
    @Deprecated(forRemoval = true)
    public boolean isAfter(DLTime other) {
        return this.compareTo(other) > 0;
    }

    /**
     * @deprecated Prefer {@link #isBetweenDaily(DLTime, DLTime, ITimeSystem)} for daily wrap-aware checks
     *             or perform explicit comparisons using {@link #compareTo(DLTime)} for non-daily intervals.
     */
    @Deprecated(forRemoval = true)
    public boolean isBetween(DLTime start, DLTime end) {
        double now = this.realMillis;
        double a = start.realMillis;
        double b = end.realMillis;
        return a <= b ? now >= a && now <= b : now >= b && now <= a;
    }

    /**
     * @deprecated use {@link #format(ITimeFormatter, TimeContext, ITimeSystem)} and provide the desired time system.
     */
    @Deprecated(forRemoval = true)
    public String format(ITimeFormatter formatter, TimeContext context) {
        return formatter.format(this, context, VanillaTimeSystem.INSTANCE);
    }

    @Deprecated(forRemoval = true)
    public static DLTime fromIngame(long days, int hours, int minutes, int seconds, ITimeSystem system) {
        requireSystem(system);
        double ticksPerSecond = DLTimeUnit.SECONDS.getTicks(system);
        double totalTicks = 0.0;
        totalTicks += (double) days * system.getTicksPerDay();
        totalTicks += (double) hours * 3600.0 * ticksPerSecond;
        totalTicks += (double) minutes * 60.0 * ticksPerSecond;
        totalTicks += (double) seconds * ticksPerSecond;
        return new DLTime(totalTicks, system);
    }

    @Deprecated(forRemoval = true)
    public static DLTime fromReal(long days, int hours, int minutes, int seconds, int millis, ITimeSystem system) {
        double totalMillis = 0.0;
        totalMillis += (double) millis;
        totalMillis += (double) seconds * 1_000.0;
        totalMillis += (double) minutes * 60_000.0;
        totalMillis += (double) hours * 3_600_000.0;
        totalMillis += (double) days * 86_400_000.0;
        return new DLTime(totalMillis);
    }

    @Deprecated(forRemoval = true)
    public double toTicks() {
        return toTicks(initialSystem);
    }

    @Deprecated(forRemoval = true)
    public double toGameSeconds() {
        return toGameSeconds(initialSystem);
    }

    @Deprecated(forRemoval = true)
    public double toGameMinutes() {
        return toGameMinutes(initialSystem);
    }

    @Deprecated(forRemoval = true)
    public double toGameHours() {
        return toGameHours(initialSystem);
    }

    @Deprecated(forRemoval = true)
    public double toGameDays() {
        return toGameDays(initialSystem);
    }    

    @Deprecated(forRemoval = true)
    public double getTicks() {
        return toTicks(initialSystem);
    }

    @Deprecated(forRemoval = true)
    public TimeSnapshot decomposeGameTime() {
        TimePool pool = asPool();
        return new TimeSnapshot(
            (int)pool.extractGameDays(initialSystem),
            (int)pool.extractGameHours(initialSystem),
            (int)pool.extractGameMinutes(initialSystem),
            (int)pool.extractGameSeconds(initialSystem),
            (int)pool.extractGameTicks(initialSystem)
        );
    }

    @Deprecated(forRemoval = true)
    public TimeSnapshot decomposeRealTime() {
        TimePool pool = asPool();
        return new TimeSnapshot(
            (int)pool.extractDays(),
            (int)pool.extractHours(),
            (int)pool.extractMinutes(),
            (int)pool.extractSeconds(),
            (int)pool.extractMillis()
        );
    }


    @Deprecated(forRemoval = true)
    public record TimeSnapshot(long days, int hours, int minutes, int seconds, int millis) {}

}

