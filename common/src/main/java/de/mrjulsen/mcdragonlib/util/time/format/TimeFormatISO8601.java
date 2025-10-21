package de.mrjulsen.mcdragonlib.util.time.format;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

public class TimeFormatISO8601 implements ITimeFormatter {

    public static final TimeFormatISO8601 INSTANCE = new TimeFormatISO8601();

    public TimeFormatISO8601() {}

    @Override
    public String format(DLTime time, TimeContext context) {
        ITimeSystem provider = time.getTimeSystem();
        long day;
        long hour;
        long minute;
        long second;

        if (context == TimeContext.REAL) {
            double totalMillis = time.toRealMillis();
            double millisPerDay = provider.getRealMillisFromTicks(provider.getTicksPerDay());
            day = (long) Math.floor(totalMillis / millisPerDay);

            double millisOfDay = totalMillis % millisPerDay;
            double totalSeconds = millisOfDay / 1000.0;
            hour = (long) ((totalSeconds / 3600) % 24);
            minute = (long) ((totalSeconds / 60) % 60);
            second = (long) (totalSeconds % 60);
        } else {
            long ticks = (long) time.getTicks();
            long ticksPerDay = provider.getTicksPerDay();
            day = Math.floorDiv(ticks, ticksPerDay);
            long dayTicks = ticks % ticksPerDay;

            hour = Math.floorDiv(dayTicks * 24, ticksPerDay);
            minute = Math.floorDiv((dayTicks * 24 * 60) % (ticksPerDay * 60), ticksPerDay);
            second = Math.floorDiv((dayTicks * 24 * 60 * 60) % (ticksPerDay * 60 * 60), ticksPerDay);
        }

        return String.format("%02dT%02d:%02d:%02dZ", day + 1, hour, minute, second);
    }
}

