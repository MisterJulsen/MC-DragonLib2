package de.mrjulsen.mcdragonlib.time.format;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * A duration or clock reading split into days, hours, minutes, seconds and milliseconds, as input for
 * {@link DurationFormat} and {@link ClockFormat}.
 * <p>Works the same for real and game time; game durations are split into game units, see
 * {@code GameDuration#toParts}. All fields are non-negative, the sign is kept separately.</p>
 */
public record TimeParts(boolean negative, long days, int hours, int minutes, int seconds, int millis) {

    /**
     * The fields of {@link TimeParts}, from largest to smallest.
     */
    public enum Field {
        DAYS(TimeUnit.DAYS.toMillis(1)),
        HOURS(TimeUnit.HOURS.toMillis(1)),
        MINUTES(TimeUnit.MINUTES.toMillis(1)),
        SECONDS(TimeUnit.SECONDS.toMillis(1)),
        MILLIS(TimeUnit.MILLISECONDS.toMillis(1));

        private final long millis;

        Field(long millis) {
            this.millis = millis;
        }

        public long millis() {
            return millis;
        }

        Field larger() {
            return this == DAYS ? null : values()[ordinal() - 1];
        }

        boolean isValid(long value) {
            return value >= 0 && (this == DAYS || value < larger().millis / millis);
        }
    }

    public TimeParts {
        if (!Field.DAYS.isValid(days) || !Field.HOURS.isValid(hours) || !Field.MINUTES.isValid(minutes) || !Field.SECONDS.isValid(seconds) || !Field.MILLIS.isValid(millis)) {
            throw new IllegalArgumentException("Fields out of range: " + days + "d " + hours + "h " + minutes + "m " + seconds + "s " + millis + "ms");
        }
    }

    /**
     * Splits the given milliseconds, rounded to whole milliseconds.
     */
    public static TimeParts ofMillis(double millis) {
        long total = Math.round(Math.abs(millis));
        return new TimeParts(millis < 0 && total != 0,
            total / Field.DAYS.millis(),
            (int)split(total, Field.HOURS),
            (int)split(total, Field.MINUTES),
            (int)split(total, Field.SECONDS),
            (int)split(total, Field.MILLIS));
    }

    private static long split(long total, Field field) {
        return total % field.larger().millis / field.millis;
    }

    public static TimeParts of(Duration duration) {
        return ofMillis(duration.getSeconds() * (double)Field.SECONDS.millis() + duration.getNano() / (double)TimeUnit.MILLISECONDS.toNanos(1));
    }

    /**
     * The total, unsigned, in milliseconds.
     */
    public long totalMillis() {
        return days * Field.DAYS.millis() + hours * Field.HOURS.millis() + minutes * Field.MINUTES.millis() + seconds * Field.SECONDS.millis() + millis;
    }

    /**
     * The value of a field when nothing larger than {@code largest} is shown: everything above is folded
     * into it. With {@code largest = HOURS}, 1 day and 2 hours gives 26 hours.
     */
    public long get(Field field, Field largest) {
        long total = totalMillis();
        if (field == largest) {
            return total / field.millis;
        }
        if (field.ordinal() < largest.ordinal()) {
            return 0;
        }
        return split(total, field);
    }

    public long get(Field field) {
        return get(field, Field.DAYS);
    }
}
