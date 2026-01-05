package de.mrjulsen.mcdragonlib.util.time.format;

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

/**
 * Formatter that renders a time-of-day in 24-hour "HH:mm" format.
 *
 * <p>For REAL context it interprets the DLTime's real milliseconds as wall-clock time.
 * For INGAME it maps ticks to a day using the provided {@link ITimeSystem}.
 */
public class TimeFormat24Hours implements ITimeFormatter {

    public static final TimeFormat24Hours INSTANCE = new TimeFormat24Hours();

    @Deprecated(forRemoval = true)
    public TimeFormat24Hours() {}

    @Override
    public String format(DLTime time, TimeContext context, @Nullable ITimeSystem system) {
        if (context == TimeContext.REAL) {
            double totalMillis = time.toRealMillis();
            long totalSeconds = (long) (totalMillis / 1000.0);
            long hour = (totalSeconds / 3600) % 24;
            long minute = (totalSeconds / 60) % 60;
            return String.format("%02d:%02d", hour, minute);
        } else {
            requireSystem(system);
            long ticksPerDay = system.getTicksPerDay();
            double t = (time.toTicks(system) + system.getDaytimeOffset()) % ticksPerDay;
            long hour = (long) ((t * 24) / ticksPerDay);
            long minute = (long) ((t * 24 * 60) % (ticksPerDay * 60) / ticksPerDay);
            return String.format("%02d:%02d", hour, minute);
        }
    }

    private static void requireSystem(@Nullable ITimeSystem system) {
        if (system == null) throw new IllegalArgumentException("ITimeSystem required for Ingame time");
    }
}
