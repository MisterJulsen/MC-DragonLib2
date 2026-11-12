package de.mrjulsen.mcdragonlib.time.v1.format;

import java.util.Locale;

import de.mrjulsen.mcdragonlib.time.v1.format.TimeParts.Field;

/**
 * Turns a duration into text. Works for real durations ({@code TimeParts.of(duration)}) and game
 * durations ({@code GameDuration#format}) alike.
 * <p>Fields below the smallest shown one are cut off, not rounded, so a timer never shows a second
 * before it has passed.</p>
 */
@FunctionalInterface
public interface DurationFormat {

    String format(TimeParts parts);

    /**
     * {@code 1:02:03} style. Hours, minutes and seconds are padded to two digits, milliseconds to
     * three and separated by a dot.
     *
     * @param largest the largest field; anything larger is folded into it, e.g. 26 hours instead of 1 day
     * @param smallest the smallest field shown
     */
    static DurationFormat digital(Field largest, Field smallest) {
        checkRange(largest, smallest);
        return parts -> {
            StringBuilder sb = new StringBuilder(parts.negative() ? "-" : "");
            for (Field field : Field.values()) {
                if (field.ordinal() < largest.ordinal() || field.ordinal() > smallest.ordinal()) {
                    continue;
                }
                long value = parts.get(field, largest);
                if (field == largest) {
                    sb.append(value);
                } else if (field == Field.MILLIS) {
                    sb.append('.').append(String.format(Locale.ROOT, "%03d", value));
                } else {
                    sb.append(':').append(String.format(Locale.ROOT, "%02d", value));
                }
            }
            return sb.toString();
        };
    }

    /**
     * {@code 1:02:03}: hours, minutes and seconds.
     */
    DurationFormat DIGITAL = digital(Field.HOURS, Field.SECONDS);

    /**
     * {@code 1d 2h 3m 4s} style. Fields that are zero are left out; a duration of zero is {@code 0s}
     * (or the unit of {@code smallest}).
     *
     * @param largest the largest field; anything larger is folded into it
     * @param smallest the smallest field shown
     */
    static DurationFormat verbose(Field largest, Field smallest) {
        checkRange(largest, smallest);
        return parts -> {
            StringBuilder sb = new StringBuilder();
            for (Field field : Field.values()) {
                if (field.ordinal() < largest.ordinal() || field.ordinal() > smallest.ordinal()) {
                    continue;
                }
                long value = parts.get(field, largest);
                if (value != 0) {
                    sb.append(sb.isEmpty() ? "" : " ").append(value).append(suffix(field));
                }
            }
            if (sb.isEmpty()) {
                return "0" + suffix(smallest);
            }
            return parts.negative() ? "-" + sb : sb.toString();
        };
    }

    /**
     * {@code 1d 2h 3m 4s}: days down to seconds.
     */
    DurationFormat VERBOSE = verbose(Field.DAYS, Field.SECONDS);


    private static String suffix(Field field) {
        return switch (field) {
            case DAYS -> "d";
            case HOURS -> "h";
            case MINUTES -> "m";
            case SECONDS -> "s";
            case MILLIS -> "ms";
        };
    }

    private static void checkRange(Field largest, Field smallest) {
        if (largest.ordinal() > smallest.ordinal()) {
            throw new IllegalArgumentException("The largest field " + largest + " is smaller than the smallest field " + smallest + ".");
        }
    }
}
