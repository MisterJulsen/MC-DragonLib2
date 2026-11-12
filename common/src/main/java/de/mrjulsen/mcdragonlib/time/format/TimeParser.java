package de.mrjulsen.mcdragonlib.time.format;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mrjulsen.mcdragonlib.time.TimeOfDay;
import de.mrjulsen.mcdragonlib.time.format.TimeParts.Field;

/**
 * Reads clock readings and durations from text, e.g. from text fields, commands or configs. Reads
 * everything {@link ClockFormat} and {@link DurationFormat} write, and a bit more.
 * <p>Usually used through {@code TimeOfDay.parse}, {@code GameDuration.parse} and
 * {@link #parseRealDuration(String)}. All methods throw {@link IllegalArgumentException} on invalid
 * input.</p>
 */
public final class TimeParser {

    private static final Pattern CLOCK = Pattern.compile("(\\d{1,2})(?::(\\d{2})(?::(\\d{2})(?:\\.(\\d{1,3}))?)?)?\\s*(am|pm|a\\.m\\.|p\\.m\\.)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern DIGITAL = Pattern.compile("(\\d+)((?::\\d{1,2}){1,3})(?:\\.(\\d{1,3}))?");
    private static final Pattern VERBOSE_PART = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(ms|d|h|m|s)(?![a-z])", Pattern.CASE_INSENSITIVE);

    private static final double NANOS_PER_MILLI = TimeUnit.MILLISECONDS.toNanos(1);

    private TimeParser() {}

    /**
     * Reads a clock reading in 24 or 12 hour notation: {@code 14:05}, {@code 14:05:09},
     * {@code 14:05:09.250}, {@code 2:05 PM}, {@code 2 pm}. A bare hour without AM/PM such as
     * {@code 14} is accepted too. {@code 24:00} is midnight.
     */
    public static TimeOfDay parseClock(String text) {
        String input = text == null ? "" : text.trim();
        Matcher m = CLOCK.matcher(input);
        if (!m.matches()) {
            throw invalid("clock reading", text);
        }
        int hour = Integer.parseInt(m.group(1));
        int minute = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
        int second = m.group(3) == null ? 0 : Integer.parseInt(m.group(3));
        int millis = m.group(4) == null ? 0 : Integer.parseInt(padMillis(m.group(4)));
        String period = m.group(5);

        if (period != null) {
            if (hour < 1 || hour > 12) {
                throw new IllegalArgumentException("The hour must be between 1 and 12 with AM/PM: '" + text + "'");
            }
            boolean pm = Character.toLowerCase(period.charAt(0)) == 'p';
            hour = hour % 12 + (pm ? 12 : 0);
        } else if (hour == 24 && minute == 0 && second == 0 && millis == 0) {
            hour = 0;
        }
        try {
            return TimeOfDay.of(hour, minute, second, millis);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid clock reading '" + text + "': " + e.getMessage(), e);
        }
    }

    /**
     * Reads a duration and returns it in milliseconds; whether they are game or real milliseconds is up
     * to the caller. Accepted, each with an optional leading {@code -}:
     * <ul>
     * <li>Verbose: {@code 1d 2h 3m 4s 500ms}, in any order, spaces optional, decimals allowed
     *     ({@code 1.5h}). A unit may only appear once.</li>
     * <li>Digital: {@code 1:02:03} (h:mm:ss), {@code 1:02:03:04} (d:hh:mm:ss), each optionally with
     *     {@code .SSS}. Two parts such as {@code 1:30} are read as h:mm, like a clock reading.</li>
     * <li>ISO 8601: {@code P1DT2H3M4.5S}.</li>
     * <li>A plain number of milliseconds, e.g. {@code 1500}.</li>
     * </ul>
     */
    public static double parseDurationMillis(String text) {
        String input = text == null ? "" : text.trim();
        if (input.isEmpty()) {
            throw invalid("duration", text);
        }
        boolean negative = false;
        String body = input;
        if (body.charAt(0) == '-' || body.charAt(0) == '+') {
            negative = body.charAt(0) == '-';
            body = body.substring(1).trim();
        }
        if (body.isEmpty()) {
            throw invalid("duration", text);
        }

        double millis;
        if (body.charAt(0) == 'P' || body.charAt(0) == 'p') {
            millis = parseIso(body, text);
        } else if (body.chars().allMatch(Character::isDigit)) {
            millis = Long.parseLong(body);
        } else if (body.indexOf(':') >= 0) {
            millis = parseDigital(body, text);
        } else {
            millis = parseVerbose(body, text);
        }
        return negative ? -millis : millis;
    }

    /**
     * Reads a real duration, see {@link #parseDurationMillis(String)} for the accepted formats.
     */
    public static Duration parseRealDuration(String text) {
        double millis = parseDurationMillis(text);
        long whole = (long)Math.floor(millis);
        long nanos = Math.round((millis - whole) * NANOS_PER_MILLI);
        return Duration.ofMillis(whole).plusNanos(nanos);
    }

    private static double parseIso(String body, String original) {
        try {
            Duration d = Duration.parse(body.toUpperCase(Locale.ROOT));
            return d.getSeconds() * (double)Field.SECONDS.millis() + d.getNano() / NANOS_PER_MILLI;
        } catch (DateTimeParseException e) {
            throw invalid("duration", original);
        }
    }

    private static double parseDigital(String body, String original) {
        Matcher m = DIGITAL.matcher(body);
        if (!m.matches()) {
            throw invalid("duration", original);
        }
        String[] rest = m.group(2).substring(1).split(":");
        long[] parts = new long[rest.length + 1];
        parts[0] = Long.parseLong(m.group(1));
        for (int i = 0; i < rest.length; i++) {
            parts[i + 1] = Long.parseLong(rest[i]);
        }
        long[] units = switch (parts.length) {
            case 2 -> new long[] { Field.HOURS.millis(), Field.MINUTES.millis() };
            case 3 -> new long[] { Field.HOURS.millis(), Field.MINUTES.millis(), Field.SECONDS.millis() };
            default -> new long[] { Field.DAYS.millis(), Field.HOURS.millis(), Field.MINUTES.millis(), Field.SECONDS.millis() };
        };
        if (m.group(3) != null && parts.length == 2) {
            throw new IllegalArgumentException("Milliseconds need seconds, e.g. 1:02:03.250: '" + original + "'");
        }
        double millis = 0;
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                long max = units[i - 1] / units[i] - 1; // e.g. 23 hours, 59 minutes
                if (parts[i] > max) {
                    throw new IllegalArgumentException("A field is out of range in '" + original + "': " + parts[i] + " > " + max);
                }
            }
            millis += parts[i] * units[i];
        }
        if (m.group(3) != null) {
            millis += Integer.parseInt(padMillis(m.group(3)));
        }
        return millis;
    }

    private static double parseVerbose(String body, String original) {
        Matcher m = VERBOSE_PART.matcher(body);
        double millis = 0;
        int position = 0;
        boolean[] seen = new boolean[5];
        while (m.find()) {
            if (!body.substring(position, m.start()).isBlank()) {
                throw invalid("duration", original);
            }
            String unit = m.group(2).toLowerCase(Locale.ROOT);
            int index;
            long factor;
            switch (unit) {
                case "d" -> { index = 0; factor = Field.DAYS.millis(); }
                case "h" -> { index = 1; factor = Field.HOURS.millis(); }
                case "m" -> { index = 2; factor = Field.MINUTES.millis(); }
                case "s" -> { index = 3; factor = Field.SECONDS.millis(); }
                default -> { index = 4; factor = Field.MILLIS.millis(); }
            }
            if (seen[index]) {
                throw new IllegalArgumentException("The unit '" + unit + "' appears twice in '" + original + "'");
            }
            seen[index] = true;
            millis += Double.parseDouble(m.group(1)) * factor;
            position = m.end();
        }
        if (position == 0 || !body.substring(position).isBlank()) {
            throw invalid("duration", original);
        }
        return millis;
    }

    private static String padMillis(String digits) {
        return (digits + "00").substring(0, 3);
    }

    private static IllegalArgumentException invalid(String what, String text) {
        return new IllegalArgumentException("Not a valid " + what + ": '" + text + "'");
    }
}
