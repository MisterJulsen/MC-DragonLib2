package de.mrjulsen.mcdragonlib.util.time.format;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

public class TimeFormatRFC3339 implements ITimeFormatter {

    public static final TimeFormatRFC3339 INSTANCE = new TimeFormatRFC3339();

    public TimeFormatRFC3339() {}

    @Override
    public String format(DLTime time, TimeContext context) {
        ITimeSystem provider = time.getTimeSystem();
        long day;
        long hour;
        long minute;
        long second;
        long millis;

        if (context == TimeContext.REAL) {
            double totalMillis = time.toRealMillis();
            double millisPerDay = provider.getRealMillisFromTicks(provider.getTicksPerDay());
            day = (long) Math.floor(totalMillis / millisPerDay);

            double millisOfDay = totalMillis % millisPerDay;
            double totalSeconds = millisOfDay / 1000.0;
            hour = (long) ((totalSeconds / 3600) % 24);
            minute = (long) ((totalSeconds / 60) % 60);
            second = (long) (totalSeconds % 60);
            millis = (long) (millisOfDay % 1000);
        } else {
            long ticks = (long) time.getTicks();
            long ticksPerDay = provider.getTicksPerDay();
            day = Math.floorDiv(ticks, ticksPerDay);
            long dayTicks = ticks % ticksPerDay;

            hour = Math.floorDiv(dayTicks * 24, ticksPerDay);
            minute = Math.floorDiv((dayTicks * 24 * 60) % (ticksPerDay * 60), ticksPerDay);
            second = Math.floorDiv((dayTicks * 24 * 60 * 60) % (ticksPerDay * 60 * 60), ticksPerDay);
            millis = Math.floorDiv((dayTicks * 24 * 60 * 60 * 1000) % (ticksPerDay * 60 * 60 * 1000), ticksPerDay);
        }

        return String.format("%02dT%02d:%02d:%02d.%03dZ", day + 1, hour, minute, second, millis);
    }
}

