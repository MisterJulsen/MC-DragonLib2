package de.mrjulsen.mcdragonlib.time.v1;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Describes how game time relates to real time: how long a day is, at what speed it runs during
 * which part of the day, and which tick the clock shows as midnight.
 * <p>Time systems are immutable and validated when they are built. Their day is completely covered by
 * {@link TimeSection}s without gaps or overlaps, so every conversion is defined everywhere. Create one with
 * {@link #builder(long)} or {@link #uniform(long, double, double)}; the one currently in use is
 * available through {@link TimeSystems#current()}.</p>
 *
 * <h2>Ticks</h2>
 * <p>"Ticks" always means day time ticks, the unit of {@code Level#getDayTime()}. They are not server
 * ticks: in a system running at 10 ticks per second the server still ticks 20 times per second, but
 * the day time only advances by 10.</p>
 *
 * <h2>Clock</h2>
 * <p>A game day always has 24 game hours, no matter how many ticks it is long. The
 * {@link #clockOffset() clock offset} says what the clock shows at tick 0; in vanilla that is 6000
 * ticks, i.e. 06:00.</p>
 */
public final class TimeSystem {

    public static final long VANILLA_TICKS_PER_DAY = 24000L;
    public static final double VANILLA_TICKS_PER_SECOND = 20.0;
    public static final double VANILLA_CLOCK_OFFSET = VANILLA_TICKS_PER_DAY / 4.0;

    /** Real milliseconds per real second, for speeds given in ticks per second. */
    static final double MILLIS_PER_SECOND = TimeUnit.SECONDS.toMillis(1);
    private static final double NANOS_PER_MILLI = TimeUnit.MILLISECONDS.toNanos(1);

    /** The vanilla time system: 24000 ticks per day at 20 ticks per second, tick 0 is 06:00. */
    public static final TimeSystem VANILLA = uniform(VANILLA_TICKS_PER_DAY, VANILLA_TICKS_PER_SECOND, VANILLA_CLOCK_OFFSET);

    private final long ticksPerDay;
    private final double clockOffset;
    private final List<TimeSection> sections;
    private final long[] sectionStarts;
    private final double[] sectionStartMillis;
    private final double millisPerDay;

    private TimeSystem(long ticksPerDay, double clockOffset, List<TimeSection> sections) {
        this.ticksPerDay = ticksPerDay;
        this.clockOffset = clockOffset;
        this.sections = Collections.unmodifiableList(sections);
        this.sectionStarts = new long[sections.size()];
        this.sectionStartMillis = new double[sections.size()];
        double millis = 0;
        for (int i = 0; i < sections.size(); i++) {
            sectionStarts[i] = sections.get(i).startTick();
            sectionStartMillis[i] = millis;
            millis += sections.get(i).lengthMillis();
        }
        this.millisPerDay = millis;
    }

    /**
     * A time system that runs at one speed the whole day.
     *
     * @param ticksPerDay length of a day in ticks
     * @param ticksPerSecond speed in ticks per real second
     * @param clockOffset what the clock shows at tick 0, in ticks
     */
    public static TimeSystem uniform(long ticksPerDay, double ticksPerSecond, double clockOffset) {
        return builder(ticksPerDay).defaultSpeed(ticksPerSecond).clockOffset(clockOffset).build();
    }

    /**
     * Starts building a time system.
     *
     * @param ticksPerDay length of a day in ticks, must be positive
     */
    public static Builder builder(long ticksPerDay) {
        return new Builder(ticksPerDay);
    }

    /**
     * Length of a day in ticks.
     */
    public long ticksPerDay() {
        return ticksPerDay;
    }

    /**
     * What the clock shows at tick 0, in ticks within {@code [0, ticksPerDay)}. Vanilla: 6000 (06:00).
     */
    public double clockOffset() {
        return clockOffset;
    }

    /**
     * The sections of the day, sorted and covering the whole day without gaps.
     */
    public List<TimeSection> sections() {
        return sections;
    }

    /**
     * Whether the whole day runs at one speed.
     */
    public boolean isUniform() {
        return sections.size() == 1;
    }

    /**
     * Ticks per game unit. A game hour, for example, is {@code ticksPerDay / 24} ticks.
     */
    public double ticksPer(GameTimeUnit unit) {
        return unit.ticks(this);
    }

    /**
     * How long a whole day takes in real time.
     */
    public Duration realDayLength() {
        return toDuration(millisPerDay);
    }

    /**
     * Average speed over the whole day, in ticks per real second.
     */
    public double averageTicksPerSecond() {
        return ticksPerDay * MILLIS_PER_SECOND / millisPerDay;
    }

    /**
     * The section the given absolute tick lies in.
     */
    public TimeSection sectionAt(double tick) {
        return sections.get(sectionIndex(tickInDay(tick)));
    }

    /**
     * Speed at the given absolute tick, in ticks per real second.
     */
    public double ticksPerSecondAt(double tick) {
        return sectionAt(tick).ticksPerSecond();
    }

    /**
     * Speed at the given absolute tick relative to vanilla, see {@link TimeSection#speed()}.
     */
    public double speedAt(double tick) {
        return sectionAt(tick).speed();
    }

    /**
     * Real milliseconds that pass while the day time goes from {@code fromTick} to {@code toTick}.
     * Negative if {@code toTick} lies before {@code fromTick}.
     */
    public double realMillisBetween(double fromTick, double toTick) {
        double fromDay = dayIndex(fromTick);
        double toDay = dayIndex(toTick);
        return (toDay - fromDay) * millisPerDay + millisIntoDay(tickInDay(toTick, toDay)) - millisIntoDay(tickInDay(fromTick, fromDay));
    }

    /**
     * The absolute tick reached when {@code realMillis} real milliseconds pass starting at
     * {@code fromTick}. Negative values go back in time. Exact inverse of
     * {@link #realMillisBetween(double, double)}.
     */
    public double tickAfterRealMillis(double fromTick, double realMillis) {
        double fromDay = dayIndex(fromTick);
        double millis = millisIntoDay(tickInDay(fromTick, fromDay)) + realMillis;
        double days = Math.floor(millis / millisPerDay);
        double rest = millis - days * millisPerDay;
        if (rest >= millisPerDay) {
            rest -= millisPerDay;
            days++;
        } else if (rest < 0) {
            rest = 0;
        }
        return (fromDay + days) * ticksPerDay + tickIntoDay(rest);
    }

    /**
     * The clock reading at the given absolute tick, in ticks since midnight.
     */
    public double clockTicksAt(double tick) {
        return tickInDay(tick + clockOffset);
    }

    /**
     * Fractional real milliseconds as a {@link Duration}, with nanosecond precision.
     */
    static Duration toDuration(double millis) {
        if (!Double.isFinite(millis)) {
            throw new ArithmeticException("Cannot convert " + millis + "ms to a duration.");
        }
        double seconds = Math.floor(millis / MILLIS_PER_SECOND);
        long nanos = Math.round((millis - seconds * MILLIS_PER_SECOND) * NANOS_PER_MILLI);
        return Duration.ofSeconds((long)seconds, nanos);
    }

    /**
     * A {@link Duration} as fractional real milliseconds.
     */
    static double toMillis(Duration duration) {
        return duration.getSeconds() * MILLIS_PER_SECOND + duration.getNano() / NANOS_PER_MILLI;
    }

    double dayIndex(double tick) {
        double day = Math.floor(tick / ticksPerDay);
        double rest = tick - day * ticksPerDay;
        if (rest >= ticksPerDay) {
            day++;
        } else if (rest < 0) {
            day--;
        }
        return day;
    }

    double tickInDay(double tick) {
        return tickInDay(tick, dayIndex(tick));
    }

    private double tickInDay(double tick, double day) {
        double rest = tick - day * ticksPerDay;
        return rest < 0 ? 0 : Math.min(rest, Math.nextDown((double)ticksPerDay));
    }

    private int sectionIndex(double tickInDay) {
        int index = Arrays.binarySearch(sectionStarts, (long)Math.floor(tickInDay));
        return index >= 0 ? index : -index - 2;
    }

    private double millisIntoDay(double tickInDay) {
        int i = sectionIndex(tickInDay);
        return sectionStartMillis[i] + (tickInDay - sectionStarts[i]) * sections.get(i).millisPerTick();
    }

    private double tickIntoDay(double millisInDay) {
        int index = Arrays.binarySearch(sectionStartMillis, millisInDay);
        int i = index >= 0 ? index : -index - 2;
        TimeSection section = sections.get(Math.max(0, i));
        double tick = section.startTick() + (millisInDay - sectionStartMillis[Math.max(0, i)]) / section.millisPerTick();
        return Math.min(tick, section.endTick());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof TimeSystem o
            && ticksPerDay == o.ticksPerDay
            && Double.compare(clockOffset, o.clockOffset) == 0
            && sections.equals(o.sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticksPerDay, clockOffset, sections);
    }

    @Override
    public String toString() {
        return "TimeSystem[ticksPerDay=" + ticksPerDay + ", clockOffset=" + clockOffset + ", sections=" + sections + "]";
    }

    /**
     * Builds a {@link TimeSystem}. Parts of the day not covered by a section run at the
     * {@link #defaultSpeed(double) default speed}, so a system without any sections is uniform.
     */
    public static final class Builder {

        private final long ticksPerDay;
        private double clockOffset;
        private double defaultSpeed = VANILLA_TICKS_PER_SECOND;
        private final List<TimeSection> sections = new ArrayList<>();

        private Builder(long ticksPerDay) {
            if (ticksPerDay <= 0) {
                throw new IllegalArgumentException("A day must be at least one tick long: " + ticksPerDay);
            }
            this.ticksPerDay = ticksPerDay;
            this.clockOffset = ticksPerDay / 4.0;
        }

        /**
         * What the clock shows at tick 0, in ticks. Defaults to a quarter of the day, so tick 0 is 06:00
         * like in vanilla, whatever the length of the day; values outside the day are wrapped.
         */
        public Builder clockOffset(double ticks) {
            if (!Double.isFinite(ticks)) {
                throw new IllegalArgumentException("The clock offset must be finite: " + ticks);
            }
            this.clockOffset = ticks;
            return this;
        }

        /**
         * Speed for every part of the day not covered by a section. Defaults to the vanilla 20.
         */
        public Builder defaultSpeed(double ticksPerSecond) {
            if (!(ticksPerSecond > 0) || Double.isInfinite(ticksPerSecond)) {
                throw new IllegalArgumentException("The speed must be a positive, finite number: " + ticksPerSecond);
            }
            this.defaultSpeed = ticksPerSecond;
            return this;
        }

        /**
         * Adds a section. Sections must not overlap and must lie within the day.
         */
        public Builder section(long startTick, long endTick, double ticksPerSecond) {
            return section(new TimeSection(startTick, endTick, ticksPerSecond));
        }

        /**
         * Adds a section. Sections must not overlap and must lie within the day.
         */
        public Builder section(TimeSection section) {
            if (section.endTick() > ticksPerDay) {
                throw new IllegalArgumentException("The section " + section + " ends after the day (" + ticksPerDay + " ticks).");
            }
            sections.add(section);
            return this;
        }

        /**
         * @throws IllegalArgumentException if sections overlap
         */
        public TimeSystem build() {
            List<TimeSection> sorted = new ArrayList<>(sections);
            Collections.sort(sorted);
            List<TimeSection> complete = new ArrayList<>();
            long position = 0;
            for (TimeSection section : sorted) {
                if (section.startTick() < position) {
                    throw new IllegalArgumentException("The section " + section + " overlaps the previous one.");
                }
                if (section.startTick() > position) {
                    complete.add(new TimeSection(position, section.startTick(), defaultSpeed));
                }
                complete.add(section);
                position = section.endTick();
            }
            if (position < ticksPerDay) {
                complete.add(new TimeSection(position, ticksPerDay, defaultSpeed));
            }
            double offset = clockOffset % ticksPerDay;
            if (offset < 0) {
                offset += ticksPerDay;
            }
            return new TimeSystem(ticksPerDay, offset, merge(complete));
        }

        private static List<TimeSection> merge(List<TimeSection> sections) {
            List<TimeSection> merged = new ArrayList<>();
            for (TimeSection section : sections) {
                if (!merged.isEmpty()) {
                    TimeSection last = merged.get(merged.size() - 1);
                    if (last.endTick() == section.startTick() && Double.compare(last.ticksPerSecond(), section.ticksPerSecond()) == 0) {
                        merged.set(merged.size() - 1, new TimeSection(last.startTick(), section.endTick(), last.ticksPerSecond()));
                        continue;
                    }
                }
                merged.add(section);
            }
            return merged;
        }
    }
}
