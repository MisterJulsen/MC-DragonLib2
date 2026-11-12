package de.mrjulsen.mcdragonlib.time.format;

import java.util.Locale;

import de.mrjulsen.mcdragonlib.time.TimeOfDay;

/**
 * Turns a clock reading into text. Use one of the constants or write your own.
 */
@FunctionalInterface
public interface ClockFormat {

    String format(TimeOfDay time);

    /** {@code 14:05} */
    ClockFormat H24 = t -> String.format(Locale.ROOT, "%02d:%02d", t.getHour(), t.getMinute());
    /** {@code 14:05:09} */
    ClockFormat H24_SECONDS = t -> String.format(Locale.ROOT, "%02d:%02d:%02d", t.getHour(), t.getMinute(), t.getSecond());
    /** {@code 14:05:09.250} */
    ClockFormat H24_MILLIS = t -> String.format(Locale.ROOT, "%02d:%02d:%02d.%03d", t.getHour(), t.getMinute(), t.getSecond(), t.getMillis());
    /** {@code 2:05 PM} */
    ClockFormat H12 = t -> String.format(Locale.ROOT, "%d:%02d %s", hour12(t), t.getMinute(), period(t));
    /** {@code 2:05:09 PM} */
    ClockFormat H12_SECONDS = t -> String.format(Locale.ROOT, "%d:%02d:%02d %s", hour12(t), t.getMinute(), t.getSecond(), period(t));

    private static int hour12(TimeOfDay t) {
        int hour = t.getHour() % 12;
        return hour == 0 ? 12 : hour;
    }

    private static String period(TimeOfDay t) {
        return t.getHour() < 12 ? "AM" : "PM";
    }
}
