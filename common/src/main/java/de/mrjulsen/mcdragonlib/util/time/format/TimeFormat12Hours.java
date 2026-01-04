package de.mrjulsen.mcdragonlib.util.time.format;

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

/**
 * Formatter that renders a time-of-day in 12-hour "hh:mm AM/PM" format.
 *
 * <p>Behaves similarly to {@link TimeFormat24Hours} but converts to AM/PM notation.
 */
public class TimeFormat12Hours implements ITimeFormatter {

    public static final TimeFormat12Hours INSTANCE = new TimeFormat12Hours();

    private TimeFormat12Hours() {}

    @Override
    public String format(DLTime time, TimeContext context, @Nullable ITimeSystem system) {
        int hour;
        int minute;
        String period;
        if (context == TimeContext.REAL) {
            double totalMillis = time.toRealMillis();
            long totalSeconds = (long) (totalMillis / 1000.0);
            hour = (int) ((totalSeconds / 3600) % 24);
            minute = (int) ((totalSeconds / 60) % 60);
        } else {
            requireSystem(system);
            long ticksPerDay = system.getTicksPerDay();
            double t = (time.toTicks(system) + system.getDaytimeOffset()) % ticksPerDay;
            hour = (int) ((t * 24) / ticksPerDay);
            minute = (int) ((t * 24 * 60) % (ticksPerDay * 60) / ticksPerDay);
        }
        period = (hour >= 12) ? "PM" : "AM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        return String.format("%02d:%02d %s", displayHour, minute, period);
    }

    private static void requireSystem(@Nullable ITimeSystem system) {
        if (system == null) throw new IllegalArgumentException("ITimeSystem required for Ingame time");
    }
}
