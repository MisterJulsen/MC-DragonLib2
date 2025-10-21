package de.mrjulsen.mcdragonlib.util.time.format;

import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.ITimeSystem;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;

public class TimeFormat12Hours implements ITimeFormatter {

    public static final TimeFormat12Hours INSTANCE = new TimeFormat12Hours();

    public TimeFormat12Hours() {}

    @Override
    public String format(DLTime time, TimeContext context) {
        ITimeSystem provider = time.getTimeSystem();
        long hour = 0;
        long minute = 0;
        if (context == TimeContext.REAL) {
            long ticksPerDay = provider.getTicksPerDay();            
            double midnightTicks = ticksPerDay - provider.getDaytimeOffset();
            double totalDayMillis = provider.getRealMillisFromTicks(ticksPerDay);
            double currentMillis = time.toRealMillis();
            double midnightMillis = provider.getRealMillisFromTicks(midnightTicks);
            double millisSinceMidnight = (currentMillis - midnightMillis + totalDayMillis) % totalDayMillis;
            double totalSeconds = millisSinceMidnight / 1000.0;
            double totalMinutes = totalSeconds / 60.0;            
            hour = (long)((totalMinutes / 60) % 24);
            minute = (long)(totalMinutes % 60);
        } else {
            double t = time.getTicks() + provider.getDaytimeOffset();
            double dayTicks = t % provider.getTicksPerDay();
            long ticksPerDay = provider.getTicksPerDay();
            hour = Math.floorDiv((long) (dayTicks * 24), ticksPerDay);
            minute = Math.floorDiv((long) ((dayTicks * 24 * 60) % (ticksPerDay * 60)), ticksPerDay);
        }
        String ampm = (hour < 12) ? "AM" : "PM";
        hour = (hour % 12 == 0) ? 12 : hour % 12;
        return String.format("%d:%02d %s", hour, minute, ampm);
    }    
}
